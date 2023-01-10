package com.apkspectrum.tool.adb;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IDevice.DeviceState;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;
import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.tool.adb.AdbDeviceHelper.CommandRejectedException;
import com.apkspectrum.util.Log;


public class PackageManager {
	private static final ArrayList<IPackageStateListener> sPackageListeners = new ArrayList<>();
	private static final Object sLock = sPackageListeners;

	private static Map<IDevice, Map<String, PackageInfo>> packagesMap = new HashMap<>();
	private static Map<String, PackageInfo[]> packageListCache = new HashMap<>();
	private static Map<IDevice, String> focusPackageCache = new HashMap<>();

	public static void addPackageStateListener(IPackageStateListener listener) {
		synchronized (sLock) {
			if (!sPackageListeners.contains(listener)) {
				sPackageListeners.add(listener);
			}
		}
	}

	public static void removePackageStateListener(IPackageStateListener listener) {
		synchronized (sLock) {
			sPackageListeners.remove(listener);
		}
	}

	private static void packageInstalled(PackageInfo packageInfo) {
		IPackageStateListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sPackageListeners.toArray(
					new IPackageStateListener[sPackageListeners.size()]);
			Map<String, PackageInfo> devicePackagList = packagesMap.get(packageInfo.device);
			if (devicePackagList == null) {
				devicePackagList = new HashMap<String, PackageInfo>();
				packagesMap.put(packageInfo.device, devicePackagList);
			}
			devicePackagList.put(packageInfo.packageName, packageInfo);
		}

		for (IPackageStateListener listener : listenersCopy) {
			try {
				listener.packageInstalled(packageInfo);
			} catch (Exception e) {
				Log.e(e.toString());
			}
		}
	}

	private static void packageUninstalled(PackageInfo packageInfo) {
		IPackageStateListener[] listenersCopy = null;
		synchronized (sLock) {
			listenersCopy = sPackageListeners.toArray(
					new IPackageStateListener[sPackageListeners.size()]);
			Map<String, PackageInfo> devicePackagList = packagesMap.get(packageInfo.device);
			if (devicePackagList != null
					&& devicePackagList.containsKey(packageInfo.packageName)) {
				devicePackagList.remove(packageInfo.packageName);
			}
		}

		for (IPackageStateListener listener : listenersCopy) {
			try {
				listener.packageUninstalled(packageInfo);
			} catch (Exception e) {
				Log.e(e.toString());
			}
		}
	}

	public static PackageInfo getPackageInfo(IDevice device, String packageName) {
		return getPackageInfo(device, packageName, true);
	}

	public static PackageInfo getPackageInfo(IDevice device, String packageName
			, boolean useCache) {
		if (device == null || !device.isOnline()) {
			Log.e("device is null or no online");
			return null;
		}
		if (packageName == null || packageName.isEmpty()) {
			Log.e("package name is null");
			return null;
		}

		PackageInfo info = null;
		synchronized (sLock) {
			Map<String, PackageInfo> devicePackagList = packagesMap.get(device);
			if (devicePackagList == null) {
				devicePackagList = new HashMap<String, PackageInfo>();
				packagesMap.put(device, devicePackagList);
			}
			if (useCache) {
				info = devicePackagList.get(packageName);
			}
			if (info == null) {
				info = new PackageInfo(device, packageName);
				if (info.getApkPath() == null) {
					info = null;
				} else {
					devicePackagList.put(packageName, info);
				}
			}
		}
		return info;
	}

	public static PackageInfo[] getPackageList(IDevice device) {
		return getPackageList(device, true);
	}

	public static PackageInfo[] getPackageList(IDevice device, boolean useCache) {
		if (useCache) {
			synchronized (sLock) {
				PackageInfo[] cache = packageListCache.get(device.getSerialNumber());
				if (cache != null) {
					return cache;
				}
			}
		}

		ArrayList<PackageInfo> list = new ArrayList<PackageInfo>();

		SimpleOutputReceiver output = new SimpleOutputReceiver();
		output.setTrimLine(false);

		try {
			device.executeShellCommand("pm list packages -f -i -u", output);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException e1) {
			e1.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
		}
		String[] pmList = output.getOutput();

		output.clear();
		try {
			device.executeShellCommand("dumpsys package", output);
		} catch (TimeoutException | AdbCommandRejectedException
				| ShellCommandUnresponsiveException | IOException e1) {
			e1.printStackTrace();
		}

		boolean start = false;
		PackageInfo pack = null;
		String verName = null;
		String verCode = null;
		for (String line: output.getOutput()) {
			if (!start) {
				if (line.startsWith("Packages:")) {
					start = true;
				}
				continue;
			}
			if (line.matches("^\\s*Package\\s*\\[.*")) {
				if (pack != null) {
					if (pack.apkPath == null) {
						pack.apkPath = pack.codePath;
					}
					String path = pack.apkPath;
					if (!path.endsWith(".apk") && path.contains("/")) {
						String name = path.substring(path.lastIndexOf("/"));
						pack.apkPath += name + ".apk";
						Log.v("Change a path to " + pack.apkPath);
					}
					pack.label = pack.apkPath.replaceAll(".*/", "")
							+ " - [" + pack.packageName + "] - "
							+ verName + "/" + verCode;
					list.add(pack);
				}
				String packagName = line.replaceAll("^\\s*Package\\s*\\[(.*)\\].*:\\s*$", "$1");
				pack = new PackageInfo(device, packagName);
				verName = null;
				verCode = null;
				for (String l: pmList) {
					if (l.matches("^package:.*=" + packagName + "\\s*installer=.*")) {
						pack.apkPath = l.replaceAll("^package:(.*)="
								+ packagName + "\\s*installer=(.*)", "$1");
						pack.installer = l.replaceAll("^package:(.*)="
								+ packagName + "\\s*installer=(.*)", "$2");
					}
				}
			} else if (pack != null && pack.codePath == null
					&& line.matches("^\\s*codePath=.*$")) {
				pack.codePath = line.replaceAll("^\\s*codePath=\\s*(\\S*).*$", "$1");
				if (pack.apkPath != null && !pack.apkPath.startsWith(pack.codePath)) {
					pack.apkPath = pack.codePath;
				}
			} else if (verName == null && line.matches("^\\s*versionName=.*$")) {
				verName = line.replaceAll("^\\s*versionName=\\s*(\\S*).*$", "$1");
			} else if (verCode == null && line.matches("^\\s*versionCode=.*$")) {
				verCode = line.replaceAll("^\\s*versionCode=\\s*(\\S*).*$", "$1");
			}
		}
		output.clear();

		if (pack != null) {
			if (pack.apkPath == null) {
				pack.apkPath = pack.codePath;
			}
			String path = pack.apkPath;
			if (!path.endsWith(".apk") && path.contains("/")) {
				String name = path.substring(path.lastIndexOf("/"));
				pack.apkPath += name + ".apk";
				Log.v("Change a path to " + pack.apkPath);
			}
			pack.label = pack.apkPath.replaceAll(".*/", "")
					+ " - [" + pack.packageName + "] - "
					+ verName + "/" + verCode;
			list.add(pack);
		}

		StringBuilder cmd = new StringBuilder();
		cmd.append("ls /system/framework/*.apk ");
		cmd.append("2>/dev/null");
		try {
			device.executeShellCommand(cmd.toString(), output);
		} catch (TimeoutException | AdbCommandRejectedException
				| ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		}
		for (String line: output.getOutput()) {
			if (line.equals("/system/framework/framework-res.apk")
					|| !line.endsWith(".apk")) continue;
			String packagName = line.replaceAll(".*/(.*)\\.apk", "$1");
			pack = new PackageInfo(device, packagName);
			pack.apkPath = line;
			pack.codePath = "/system/framework";
			pack.label = pack.apkPath.replaceAll(".*/", "");
			list.add(pack);
		}

		PackageInfo[] packageList = list.toArray(new PackageInfo[list.size()]);
		synchronized (sLock) {
			packageListCache.put(device.getSerialNumber(), packageList);
		}
		return packageList;
	}

	public static void removeListCache(IDevice device) {
		synchronized (sLock) {
			packageListCache.remove(device.getSerialNumber());
		}
	}

	public static void removeCache(IDevice device) {
		synchronized (sLock) {
			if (packagesMap.containsKey(device)) {
				packagesMap.remove(device);
			}
		}
	}

	public static IDevice[] getInstalledDevices(String packageName) {
		ArrayList<IDevice> list = new ArrayList<IDevice>();
		synchronized (sLock) {
			for (Entry<IDevice, Map<String, PackageInfo>> entry: 
					packagesMap.entrySet()) {
				if (entry.getKey().isOnline()
						&& entry.getValue().containsKey(packageName)) {
					list.add(entry.getKey());
				}
			}
		}

		return list.toArray(new IDevice[list.size()]);
	}

	public static String installPackage(IDevice device, String localApkPath
			, boolean reinstall, String... extraArgs) {
		String errMsg = null;

		if (device == null) {
			errMsg = "Device is null";
		} else if (device.getState() != DeviceState.ONLINE) {
			errMsg = "Device is no online : " + device.getState();
		} else if (localApkPath == null || localApkPath.isEmpty()
				|| !new File(localApkPath).isFile()) {
			errMsg = "No Such local apk file : " + localApkPath;
		}

		String packageName = ApkScanner.getPackageName(localApkPath);
		if (packageName == null || packageName.isEmpty()) {
			errMsg = "Invalid APK file. Cannot read package name";
		}

		if (device == null || errMsg != null) {
			return errMsg;
		}

		try {
			device.installPackage(localApkPath, reinstall, extraArgs);
			packageInstalled(new PackageInfo(device, packageName));
		} catch (InstallException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
		}

		return errMsg;
	}

	public static String uninstallPackage(IDevice device, String packageName) {
		PackageInfo packageInfo = getPackageInfo(device, packageName);
		if (packageInfo == null) {
			return "Unknown package";
		}
		return uninstallPackage(packageInfo);
	}

	public static String uninstallPackage(PackageInfo packageInfo) {
		String errMsg = null;

		if (packageInfo == null || packageInfo.packageName == null) {
			errMsg = "PackageInfo is null";
		} else if (packageInfo.device == null) {
			errMsg = "Device is null";
		} else if (packageInfo.device.getState() != DeviceState.ONLINE) {
			errMsg = "Device is no online : " + packageInfo.device.getState();
		} else if (packageInfo.isSystemApp()) {
			errMsg = "System applications can not be uninstalled.";
		} else {
			String apkPath = packageInfo.getApkPath();
			if (apkPath == null || apkPath.isEmpty()) {
				errMsg = "No such apk file";
			} else {
				packageInfo.clear();
				String newApkPath = packageInfo.getApkPath();
				if (newApkPath == null || newApkPath.isEmpty()) {
					errMsg = "Already uninstalled";
				} else if (!apkPath.equals(newApkPath)) {
					Log.w("Changed apk path '" + apkPath
							+ "' to '" + newApkPath + "'");
				}
			}
		}

		if (packageInfo == null || errMsg != null) {
			return errMsg;
		}

		try {
			errMsg = packageInfo.device.uninstallPackage(packageInfo.packageName);
			if (errMsg == null || errMsg.isEmpty()) {
				packageInfo.clear();
				packageUninstalled(packageInfo);
			}
		} catch (InstallException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
		}

		return errMsg;
	}

	public static String removePackage(IDevice device, String packageName) {
		PackageInfo packageInfo = getPackageInfo(device, packageName);
		if (packageInfo == null) {
			return "Unknown package";
		}
		return removePackage(packageInfo);
	}

	public static String removePackage(PackageInfo packageInfo) {
		String errMsg = null;

		if (packageInfo == null || packageInfo.packageName == null) {
			errMsg = "PackageInfo is null";
			return errMsg;
		} else if (packageInfo.device == null) {
			errMsg = "Device is null";
		} else if (packageInfo.device.getState() != DeviceState.ONLINE) {
			errMsg = "Device is no online : " + packageInfo.device.getState();
		} else {
			if (packageInfo.isSystemApp()) {
				if (!AdbDeviceHelper.hasSu(packageInfo.device)) {
					errMsg = "This device was not rooting!\n"
							+ "Can not remove for the system package!";
				} else if (!AdbDeviceHelper.isRoot(packageInfo.device)) {
					errMsg = "No root, retry after change to root mode";
				}
			}

			String apkPath = packageInfo.getApkPath();
			if (apkPath == null || apkPath.isEmpty()) {
				errMsg = "No such apk file";
			} else {
				String newApkPath = packageInfo.getRealApkPath();
				if (newApkPath == null || newApkPath.isEmpty()) {
					errMsg = "Already uninstalled";
				} else if (!apkPath.equals(newApkPath)) {
					Log.w("Changed apk path '" + apkPath
							+ "' to '" + newApkPath + "'");
				}
			}
		}

		if (errMsg == null && packageInfo.isSystemApp()) {
			try (SocketChannel adbChannel = AndroidDebugBridge.openConnection()) {
				AdbDeviceHelper.remount((InetSocketAddress) adbChannel.getRemoteAddress()
						, packageInfo.device);
				packageInfo.device.executeShellCommand("su root setenforce 0"
						, new NullOutputReceiver());
			} catch (TimeoutException | CommandRejectedException | IOException
					| ShellCommandUnresponsiveException e1) {
				errMsg = e1.getMessage();
				e1.printStackTrace();
			} catch (AdbCommandRejectedException e1) {
				Log.w(e1.getMessage());
			}
		}

		String removePath = null;
		if (errMsg == null) {
			removePath = packageInfo.getApkPath().replaceAll(
							"^(/system/(priv-)?app/[^/]*/)[^/]*\\.apk", "$1");
			Log.v("remove target path: " + removePath);
			if (removePath.matches("^/system/(priv-)?app/$")) {
				errMsg = "Can't remove system floder: " + removePath;
			}
		}

		if (errMsg != null) {
			return errMsg;
		}

		try {
			SimpleOutputReceiver out = new SimpleOutputReceiver();
			packageInfo.device.executeShellCommand("rm -r " + removePath, out);
			//packageInfo.device.removeRemotePackage(packageInfo.getApkPath());
			for (String line: out.getOutput()) {
				if (!line.isEmpty()) {
					errMsg = line;
					break;
				}
			}

			if (errMsg == null || errMsg.isEmpty()) {
				packageInfo.clear();
				packageUninstalled(packageInfo);
			}
		} catch (TimeoutException | AdbCommandRejectedException
				| ShellCommandUnresponsiveException | IOException e) {
			errMsg = e.getMessage();
			e.printStackTrace();
		}

		return errMsg;
	}

	public static String pullApk(final IDevice device, final String srcApkPath
			, final String destApkPath) {
		String errMsg = null;
		try {
			try {
				device.pullFile(srcApkPath, destApkPath);
			} catch (SyncException e) {
				Log.w("Unable to pull from " + srcApkPath);

				final String tmpPath = "/sdcard/tmp";
				StringBuilder cmd = new StringBuilder();
				cmd.append("ls ").append(srcApkPath).append(" > /dev/null && ")
				   .append("mkdir -p ").append(tmpPath)
				   .append(srcApkPath.substring(0, srcApkPath.lastIndexOf("/")))
				   .append(" && ")
				   .append("cp ").append(srcApkPath).append(" ").append(tmpPath)
				   .append(srcApkPath);
				Log.v(cmd.toString());

				final StringBuilder output = new StringBuilder();
				device.executeShellCommand(cmd.toString()
						, new IShellOutputReceiver() {
					@Override
					public void addOutput(byte[] data, int offset, int length) {
						output.append(new String(data));
					}

					@Override
					public void flush() {
						Log.v(output.toString());
					}

					@Override
					public boolean isCancelled() {
						return false;
					}
				});
				if (output.length() > 0) {
					throw new IOException(output.toString());
				}
				device.pullFile(tmpPath + srcApkPath, destApkPath);
			}
		} catch (AdbCommandRejectedException | IOException | TimeoutException
				| SyncException | ShellCommandUnresponsiveException e1) {
			errMsg = e1.getMessage();
			Log.w(e1.getMessage());
		}

		if (errMsg == null && !new File(destApkPath).isFile()) {
			errMsg = "Unknown Error";
		}
		return errMsg;
	}

	public static String clearData(PackageInfo packageInfo) {
		String errMsg = null;

		if (packageInfo == null || packageInfo.packageName == null) {
			errMsg = "PackageInfo is null";
			return errMsg;
		} else if (packageInfo.device == null) {
			errMsg = "Device is null";
		} else if (packageInfo.device.getState() != DeviceState.ONLINE) {
			errMsg = "Device is no online : " + packageInfo.device.getState();
		}

		if (errMsg == null) {
			errMsg = "";
			try {
				SimpleOutputReceiver output = new SimpleOutputReceiver();
				output.setTrimLine(false);
				String cmd = "pm clear " + packageInfo.packageName;
				packageInfo.device.executeShellCommand(cmd, output);
				String[] result = output.getOutput();
				if (result != null) {
					for (String s: result) {
						if (s.equalsIgnoreCase("Success")) {
							return null;
						}
						errMsg += s + "\n";
					}
				}
			} catch (TimeoutException | ShellCommandUnresponsiveException
					| IOException e) {
				errMsg = e.getMessage();
				e.printStackTrace();
			} catch (AdbCommandRejectedException e1) {
				Log.w(e1.getMessage());
			}
		}

		return errMsg;
	}

	public static List<WindowStateInfo> getCurrentlyDisplayedPackages(IDevice device) {
		SimpleOutputReceiver output = new SimpleOutputReceiver();
		output.setTrimLine(false);
		try {
			device.executeShellCommand("dumpsys window windows", output);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException | AdbCommandRejectedException e) {
			Log.w(e.getMessage());
			e.printStackTrace();
		}
		String[] result = output.getOutput();

		ArrayList<WindowStateInfo> windows = new ArrayList<WindowStateInfo>();
		ArrayList<String> windowDump = new ArrayList<String>();
		boolean isWindowInfoBlock = false;
		WindowStateInfo win = new WindowStateInfo();
		String blockEndRegex = "";

		String currentFocus = null;
		String focusedApp = null;

		for (String line: result) {
			if (line.matches("^(\\s+)Window\\s+#.*")) {
				isWindowInfoBlock = true;
				blockEndRegex = "^" + line.replaceAll("^(\\s+)Window\\s+#.*", "$1") + "\\S.*";
				if (win != null) {
					if (win.name == null || win.packageName == null
							|| win.packageName.equalsIgnoreCase("null")
							|| win.hasSurface == null || !win.hasSurface) {
						Log.v("Unkown window or no surface : " + win.name
								+ ", package: " + win.packageName
								+ " hasSurface: " + win.hasSurface);
					} else {
						win.dump = windowDump.toArray(new String[windowDump.size()]);
						windows.add(win);
					}
				}
				windowDump.clear();
				win = new WindowStateInfo();
				win.name = line.replaceAll("^\\s+Window\\s+#[0-9]+\\s+(Window\\{.*\\}):.*$", "$1");
				//Log.v("stateName:" + winStateInfo.name);
				windowDump.add(line);
				continue;
			} else if (isWindowInfoBlock && line.matches(blockEndRegex)) {
				isWindowInfoBlock = false;
			}
			if (isWindowInfoBlock) {
				windowDump.add(line);

				if (win.mToken == null && line.matches("^\\s+mToken=.*$")) {
					win.mToken = line.replaceAll("^\\s+mToken=(.*)$", "$1");
					//Log.v("mToken:" + winStateInfo.mToken);
				} else if (win.packageName == null
						&& line.matches(".*\\s+package=(\\S+)\\s*.*")) {
					win.packageName = line.replaceAll(".*\\s+package=(\\S+)\\s*.*", "$1");
					//Log.v("packageName:" + winStateInfo.packageName);
				} else if (win.hasSurface == null
						&& line.matches(".*\\s+mHasSurface=(\\S+)\\s*.*")) {
					win.hasSurface = "true".equalsIgnoreCase(
								line.replaceAll(".*\\s+mHasSurface=(\\S+)\\s*.*", "$1"));
					//Log.v("hasSurface:" + winStateInfo.hasSurface);
				}
			} else {
				if (currentFocus == null && line.matches("\\s+mCurrentFocus=(.*)$")) {
					currentFocus = line.replaceAll("\\s+mCurrentFocus=(.*)$", "$1");
				} else if (focusedApp == null && line.matches("\\s+mFocusedApp=(.*)$")) {
					focusedApp = line.replaceAll("\\s+mFocusedApp=(.*)$", "$1");
				}
			}
		}
		if (win != null) {
			if (win.name == null || win.packageName == null
					|| win.packageName.equalsIgnoreCase("null")
					|| win.hasSurface == null || !win.hasSurface) {
				Log.v("Unkown window or no surface : " + win.name
						+ ", package: " + win.packageName
						+ " hasSurface: " + win.hasSurface);
			} else {
				win.dump = windowDump.toArray(new String[windowDump.size()]);
				windows.add(win);
			}
		}

		Log.v("currentFocus " + currentFocus + ", focusedApp " + focusedApp);
		for (WindowStateInfo info: windows) {
			info.isCurrentFocus = info.name.equals(currentFocus);
			info.isFocusedApp = info.name.equals(focusedApp);

			if (info.isCurrentFocus) {
				focusPackageCache.put(device, info.packageName);
			}
			// Log.v("info.pakc : " + info.packageName + ", curFocus : "
			//		+ info.isCurrentFocus + ", focused : " + info.isFocusedApp);
		}

		return windows;
	}

	public static String getCurrentFocusPackage(IDevice device, boolean force) {
		// Q OS not supported.
		int apiLevel = Integer.parseInt(device.getProperty(IDevice.PROP_BUILD_API_LEVEL));
		if (apiLevel >= 29) {
			return null;
		}
		if (force || !focusPackageCache.containsKey(device)) {
			getCurrentlyDisplayedPackages(device);
		}
		return focusPackageCache.get(device);
	}

	public static List<String> getRecentlyActivityPackages(IDevice device) {
		SimpleOutputReceiver output = new SimpleOutputReceiver();
		output.setTrimLine(false);
		try {
			device.executeShellCommand("am stack list", output);
		} catch (TimeoutException | ShellCommandUnresponsiveException | IOException e) {
			e.printStackTrace();
		} catch (AdbCommandRejectedException e1) {
			Log.w(e1.getMessage());
		}
		String[] result = output.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		boolean isLegacy = false;
		for (String line: result) {
			if (line.startsWith("  taskId=")) {
				String pkg = line.replaceAll("  taskId=[0-9]*:\\s([^/]*)/.*", "$1").trim();
				if (pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if (!pkg.contains(" ") && !pkgList.contains(pkg)) {
						if (line.indexOf("visible=true") >= 0)
							pkgList.add(0, pkg);
						else
							pkgList.add(pkg);
					} else {
						Log.w("Unknown pkg - " + pkg);
					}
				}
			}
			if (line.startsWith("Error: unknown command 'list'")) {
				isLegacy = true;
				break;
			} else if (line.startsWith("Error:")) {
				Log.e(line);
			}
		}

		if (isLegacy) {
			return getRecentlyActivityPackagesLegacy(device);
		}

		return pkgList;
	}

	private static List<String> getRecentlyActivityPackagesLegacy(IDevice device) {
		SimpleOutputReceiver output = new SimpleOutputReceiver();
		output.setTrimLine(false);
		try {
			device.executeShellCommand("am stack boxes", output);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException | AdbCommandRejectedException e) {
			Log.w(e.getMessage());
			e.printStackTrace();
		}
		String[] result = output.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		for (String line: result) {
			if (line.startsWith("    taskId=")) {
				String pkg = line.replaceAll("    taskId=[0-9]*:\\s([^/]*)/.*", "$1").trim();
				if (pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if (!pkg.contains(" ") && !pkgList.contains(pkg)) {
						pkgList.add(0, pkg);
					} else {
						Log.w("Unknown pkg - " + pkg);
					}
				}
			}
		}
		return pkgList;
	}

	public static List<String> getCurrentlyRunningPackages(IDevice device) {
		SimpleOutputReceiver output = new SimpleOutputReceiver();
		output.setTrimLine(false);
		try {
			device.executeShellCommand("ps;ps -e", output);
		} catch (TimeoutException | ShellCommandUnresponsiveException
				| IOException | AdbCommandRejectedException e) {
			Log.w(e.getMessage());
			e.printStackTrace();
		}
		String[] result = output.getOutput();
		ArrayList<String> pkgList = new ArrayList<String>();
		for (String line: result) {
			if (!line.startsWith("root")) {
				String pkg = line.replaceAll(".* ([^\\s:]*)(:.*)?$", "$1");
				if (pkg != null && !pkg.isEmpty() && !pkg.equals(line)) {
					if (!pkg.startsWith("/") && !pkgList.contains(pkg)) {
						pkgList.add(pkg);
					}
				}
			}
		}
		if (pkgList.size() > 0 && pkgList.get(0).equals("NAME")) {
			pkgList.remove(0);
		}
		return pkgList;
	}
}
