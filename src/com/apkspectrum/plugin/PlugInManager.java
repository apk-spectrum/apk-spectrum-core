package com.apkspectrum.plugin;

import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.SwingWorker;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.plugin.gui.NetworkErrorDialog;
import com.apkspectrum.plugin.gui.UpdateNotificationWindow;
import com.apkspectrum.plugin.manifest.InvalidManifestException;
import com.apkspectrum.resource.Res;
import com.apkspectrum.resource._RConst;
import com.apkspectrum.resource._RFile;
import com.apkspectrum.swing.ApkActionEventHandler;
import com.apkspectrum.util.GeneralVersionChecker;
import com.apkspectrum.util.Log;

public final class PlugInManager
{
	private static List<PlugInPackage> pluginPackages = new ArrayList<>();
	private static String lang = "";

	private static List<PlugInEventListener> eventListeners = new ArrayList<>();
	private static final Object sLock = eventListeners;

	private static String appPackageName;
	private static String appVersion;
	private static Res<String> appName;
	private static Res<Image> appIcon;

	private static UpdateChecker[] latestUpdatedList;

	private static ApkActionEventHandler actionHandler;

	private PlugInManager() { }

	public static void setAppPackage(String appPackageName,
			String appVerseion, Res<String> appName, Res<Image> appIcon) {
		PlugInManager.appPackageName = appPackageName;
		PlugInManager.appVersion = appVerseion;
		PlugInManager.appName = appName;
		PlugInManager.appIcon = appIcon;
	}

	public static String getAppPackage() {
		return appPackageName;
	}

	public static String getAppVersion() {
		return appVersion;
	}

	public static String getAppTitle() {
		return appName != null ? appName.get() : null;
	}

	public static Image getAppIcon() {
		return appIcon.get();
	}

	public static void addPlugInEventListener(PlugInEventListener listener) {
		synchronized (sLock) {
			if (!eventListeners.contains(listener)) {
				eventListeners.add(listener);
			}
			if(!pluginPackages.isEmpty()) {
				listener.onPluginLoaded();
			}
		}
	}

	public static void removePlugInEventListener(PlugInEventListener listener) {
		synchronized (sLock) {
			eventListeners.remove(listener);
		}
	}

	public static PlugInPackage getPlugInPackage(String packageName) {
		if(packageName == null || packageName.trim().isEmpty()) return null;
		for(PlugInPackage pack: pluginPackages) {
			if(packageName.equals(pack.getPackageName())) return pack;
		}
		return null;
	}

	public static PlugInPackage[] getPlugInPackages() {
		return pluginPackages.toArray(new PlugInPackage[pluginPackages.size()]);
	}

	public static UpdateChecker[] getUpdateChecker() {
		ArrayList<UpdateChecker> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_UPDATE_CHECKER)) {
				if(p instanceof UpdateChecker && p.isEnabled()) {
					list.add((UpdateChecker)p);
				}
			}
		}
		return list.toArray(new UpdateChecker[list.size()]);
	}

	public static ExternalTool[] getExternalTool() {
		ArrayList<ExternalTool> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_EXTERNAL_TOOL)) {
				if( p instanceof ExternalTool
						&& p.isEnabled()
						&& ((ExternalTool) p).isSupoortedOS()
						&& !((ExternalTool) p).isDecorderTool()) {
					list.add((ExternalTool)p);
				}
			}
		}
		return list.toArray(new ExternalTool[list.size()]);
	}

	public static ExternalTool[] getDecorderTool() {
		ArrayList<ExternalTool> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_EXTERNAL_TOOL)) {
				if( p instanceof ExternalTool
						&& p.isEnabled()
						&& ((ExternalTool) p).isSupoortedOS()
						&& ((ExternalTool) p).isDecorderTool() ) {
					list.add((ExternalTool)p);
				}
			}
		}
		return list.toArray(new ExternalTool[list.size()]);
	}

	public static PackageSearcher[] getPackageSearchers() {
		ArrayList<PackageSearcher> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_PACKAGE_SEARCHER)) {
				if(p instanceof PackageSearcher && p.isEnabled()) {
					list.add((PackageSearcher)p);
				}
			}
		}
		return list.toArray(new PackageSearcher[list.size()]);
	}

	public static PackageSearcher[] getPackageSearchers(int type) {
		ArrayList<PackageSearcher> list = new ArrayList<>();
		for(PackageSearcher searcher: getPackageSearchers()) {
			if((searcher.getSupportType() & type) == type) {
				list.add(searcher);
			}
		}
		return list.toArray(new PackageSearcher[list.size()]);
	}

	public static ExtraComponent<?>[] getExtraComponenet() {
		ArrayList<ExtraComponent<?>> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_EXTRA_COMPONENT)) {
				if(p instanceof ExtraComponent && p.isEnabled()) {
					list.add((ExtraComponent<?>)p);
				}
			}
		}
		return list.toArray(new ExtraComponent[list.size()]);
	}

	public static PlugIn[] getPlugInAll() {
		ArrayList<PlugIn> list = new ArrayList<>();
		for(PlugInPackage pack: pluginPackages) {
			for(PlugIn p: pack.getPlugIn(PlugIn.PLUGIN_TPYE_ALL)) {
				list.add(p);
			}
		}
		return list.toArray(new PlugIn[list.size()]);
	}

	public static PlugIn getPlugInByActionCommand(String actionCommand) {
		if(actionCommand == null) return null;
		String packageName = actionCommand.replaceAll("!.*", "");
		PlugInPackage pack = getPlugInPackage(packageName);
		return pack == null ? null
				: pack.getPlugInByActionCommand(actionCommand);
	}

	public static Action getPlugInAction(PlugIn plugin) {
		return getPlugInAction(plugin.getActionCommand());
	}

	public static Action getPlugInAction(String actionCommand) {
		if(actionCommand == null) return null;

		Action action = getActionEventHandler().getAction(actionCommand);
		if(action != null) return action;

		String packageName = actionCommand.replaceAll("!.*", "");

		PlugInPackage pack = getPlugInPackage(packageName);
		if(pack == null) return null;

		action = pack.makePlugInAction(actionCommand);
		if(action == null) return null;

		getActionEventHandler().addAction(actionCommand, action);
		return action;
	}

	public static ApkActionEventHandler getActionEventHandler() {
		if(actionHandler == null) {
			actionHandler = new ApkActionEventHandler();
		}
		return actionHandler;
	}

	public static void setActionEventHandler(ApkActionEventHandler handler) {
		actionHandler = handler;
	}

	public static ApkInfo getApkInfo() {
		return actionHandler != null ? actionHandler.getApkInfo() : null;
	}

	public static ApkInfo getApkInfo(int pos) {
		return actionHandler != null ? actionHandler.getApkInfo(pos) : null;
	}

	public static ApkInfo getApkInfoByEventSource() {
		return actionHandler != null ? actionHandler.getApkInfoByEventSource()
				: null;
	}

	public static ApkInfo getApkInfoByEventSource(EventObject e) {
		return actionHandler != null ? actionHandler.getApkInfoByEventSource(e)
				: null;
	}

	public static void setLang(String newLang) {
		lang = newLang != null ? newLang.trim() : "";
	}

	public static String getLang() {
		return lang != null ? lang : "";
	}

	public static void loadPlugIn() {
		synchronized (sLock) {
			pluginPackages.clear();

			File pluginFolder = _RFile.PLUGIN_PATH.get();
			if(!pluginFolder.isDirectory()) {
				Log.v("No such plugins: " + _RFile.PLUGIN_PATH.getPath());
				return;
			}

			File[] pluginFiles = pluginFolder.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".xml") || name.endsWith(".jar");
				}
			});

			GeneralVersionChecker coreVer, minVer;
			coreVer = GeneralVersionChecker.parseFrom(_RConst.CORE_VERSION);
			for(File pluginFile: pluginFiles) {
				PlugInPackage pack = null;
				try {
					pack = new PlugInPackage(pluginFile);
				} catch (InvalidManifestException e) {
					e.printStackTrace();
				}
				if(pack != null) {
					String packageName = pack.getPackageName();
					minVer = GeneralVersionChecker.parseFrom(
							pack.getMinCoreVersion());
					if(coreVer.compareTo(minVer) < 0) {
						Log.w(packageName + " requires core version " + minVer
								+ " or higher.");
						continue;
					}
					PlugInPackage oldPack = getPlugInPackage(packageName);
					if(oldPack != null) {
						Log.i(packageName + " was already existed "
								+ "a same package : " + oldPack.getPlugInUri());
						if(oldPack.getVersionCode() < pack.getVersionCode()) {
							Log.i("This is new version, so remove old version "
									+ oldPack.getPlugInUri());
							pluginPackages.remove(oldPack);
							pluginPackages.add(pack);
						} else {
							Log.i("This is old version or same, so do not add "
									+ "a package : " + pack.getPlugInUri());
						}
					} else {
						pluginPackages.add(pack);
					}
				}
			}

			loadProperty();

			PlugInEventListener[] listenersCopy = null;
			synchronized (sLock) {
				listenersCopy = eventListeners.toArray(
						new PlugInEventListener[eventListeners.size()]);
			}

			for (PlugInEventListener listener : listenersCopy) {
				listener.onPluginLoaded();
				if(latestUpdatedList != null) {
					listener.onPluginUpdated(latestUpdatedList);
				}
			}
		}
	}

	public static void actionPerformed(ActionEvent e) {
		String actCmd = e.getActionCommand();
		if(actCmd == null) return;

		Action action = getPlugInAction(actCmd);
		if(action == null) return;

		action.actionPerformed(e);
	}

	public static void checkForUpdates() {
		checkForUpdatesWithUI(null);
	}

	public static void checkForUpdates(final long delayMs) {
		checkForUpdatesWithUI(null, delayMs);
	}

	public static void checkForUpdatesWithUI(final Component parent) {
		checkForUpdates(parent, PlugInManager.getUpdateChecker());
	}

	public static void checkForUpdatesWithUI(final Component parent,
			final long delayMs) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
            	checkForUpdatesWithUI(parent);
            }
        }, delayMs);
	}

	private static void checkForUpdates(final Component parent,
			final UpdateChecker[] updater) {
        new SwingWorker<UpdateChecker[], UpdateChecker>() {
			@Override
			protected UpdateChecker[] doInBackground() throws Exception {
				ArrayList<UpdateChecker> newUpdates = new ArrayList<>();
				for(UpdateChecker uc: updater) {
					if(!uc.wasPeriodPassed()) {
						if(uc.hasNewVersion()) {
							newUpdates.add(uc);
						}
						continue;
					}
					try {
						if(uc.checkNewVersion()) {
							newUpdates.add(uc);
						};
					} catch (NetworkException e) {
						publish(uc);
						if(e.isNetworkNotFoundException()) {
							Log.d("isNetworkNotFoundException");
							break;
						}
					}
				}
				return newUpdates.toArray(new UpdateChecker[newUpdates.size()]);
			}

			@Override
			protected void process(List<UpdateChecker> updater) {
				if(parent == null) return;

				ArrayList<UpdateChecker> retryUpdates = new ArrayList<>();
				for(UpdateChecker uc: updater) {
					int ret = NetworkErrorDialog.show(parent, uc);
					switch(ret) {
					case NetworkErrorDialog.RESULT_RETRY:
						retryUpdates.add(uc);
					}
				}
				if(!retryUpdates.isEmpty()) {
					checkForUpdates(parent, retryUpdates
							.toArray(new UpdateChecker[retryUpdates.size()]));
				}
			}

			@Override
			protected void done() {
				UpdateChecker[] updaters = null;
				try {
					updaters = get();
					if(updaters != null && updaters.length == 0) {
						updaters = null;
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				if(updaters != null && parent != null) {
					if(!"true".equals(PlugInConfig.getGlobalConfiguration(
							PlugInConfig.CONFIG_NO_LOOK_UPDATE_POPUP))) {
						UpdateNotificationWindow.show(parent, updaters);
					}
				}

				PlugInManager.saveProperty();

				if(updaters != null) {
					latestUpdatedList = updaters;
					PlugInEventListener[] listenersCopy = null;
					synchronized (sLock) {
						listenersCopy = eventListeners.toArray(
								new PlugInEventListener[eventListeners.size()]);
					}

					for (PlugInEventListener listener : listenersCopy) {
						listener.onPluginUpdated(updaters);
					}
				}
			}
		}.execute();
	}

	public static UpdateChecker[] getLatestUpdated() {
		return latestUpdatedList;
	}

	public static Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		for(PlugInPackage pack: pluginPackages) {
			Map<String, Object> prop = pack.getChangedProperties();
			if(!prop.isEmpty()) {
				data.put(pack.getPackageName(), prop);
			}
		}
		if(!PlugInConfig.configurations.isEmpty()) {
			data.put("globalConfiguration", PlugInConfig.configurations);
		}
		return data;
	}

	public static void restoreProperties(Map<?, ?> data) {
		if(data == null) return;

		if(data.containsKey("globalConfiguration")) {
			@SuppressWarnings("unchecked")
			Map<String, String> map
					= (Map<String, String>) data.get("globalConfiguration");
			PlugInConfig.configurations.putAll(map);
			data.remove("globalConfiguration");
		}

		for(Entry<?, ?> entry: data.entrySet()) {
			PlugInPackage pack = getPlugInPackage((String) entry.getKey());
			if(pack != null) {
				pack.restoreProperties((Map<?, ?>) entry.getValue());
			} else {
				Log.w("unknown package : " + entry.getKey());
			}
		}
	}

	public static void loadProperty()
	{
		File file = _RFile.PLUGIN_CONF_PATH.get();
		if(!file.exists() || file.length() == 0) return;
		try(FileReader fileReader = new FileReader(file)) {
			JSONParser parser = new JSONParser();
			restoreProperties((JSONObject)parser.parse(fileReader));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveProperty()
	{
		File file = _RFile.PLUGIN_CONF_PATH.get();
		try {
			if(!file.exists() && !file.createNewFile()) {
				Log.w("Cann't create file : "
						+ _RFile.PLUGIN_CONF_PATH.getPath());
			}
		} catch (IOException e1) { }

		if(!file.canWrite()) {
			Log.v("Cann't write file : " + _RFile.PLUGIN_CONF_PATH.getPath());
			return;
		}

		String transMultiLine = JSONValue.toJSONString(getChangedProperties())
				.replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
				.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
		//.replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)", "$1\n");

		try( FileWriter fw = new FileWriter(file);
			 BufferedWriter writer = new BufferedWriter(fw) ) {
			writer.write(transMultiLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
