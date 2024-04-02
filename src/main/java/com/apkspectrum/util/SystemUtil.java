package com.apkspectrum.util;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.apkspectrum.jna.ProcessPathKernel32;
import com.apkspectrum.logback.Log;
import com.apkspectrum.resource._RProp;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Tlhelp32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.win32.W32APIOptions;

import mslinks.ShellLink;
import mslinks.ShellLinkException;

public class SystemUtil {
    public static final String OS = System.getProperty("os.name").toLowerCase();

    private static final Object lock = OS;

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isLinux() {
        return OS.contains("nux");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static boolean isMac() {
        return OS.contains("mac");
    }

    public static boolean isSolaris() {
        return OS.contains("sunos");
    }

    public static String getUserLanguage() {
        return System.getProperty("user.language");
    }

    public static String getTemporaryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    public static String getArchiveExplorer() throws Exception {
        if (isWindows()) {
            return "explorer";
        } else if (isLinux()) {
            return "file-roller";
        }
        throw new Exception("Unknown OS : " + OS);
    }

    public static String getFileExplorer() throws Exception {
        if (isWindows()) {
            return "explorer";
        } else if (isLinux()) {
            return "nautilus";
        } else if (isMac()) {
            return "open";
        }
        throw new Exception("Unknown OS : " + OS);
    }

    public static String getFileOpener() throws Exception {
        if (isWindows()) {
            return "explorer";
        } else if (isLinux()) {
            return "xdg-open";
        } else if (isMac()) {
            return "open";
        }
        throw new Exception("Unknown OS : " + OS);
    }

    public static String getDefaultEditor() throws Exception {
        if (isWindows()) {
            String editorPath = null;
            String cmdLine = SystemUtil.getOpenCommand(".txt");
            if (cmdLine != null && cmdLine.contains("%1")) {
                String cmd = cmdLine.replaceAll("\"?(.*\\.[eE][xX][eE])\"?.*", "$1");
                if (!cmd.equals(cmdLine)) {
                    editorPath = SystemUtil.getRealPath(cmd);
                    if (editorPath != null) {
                        if (!new File(editorPath).canExecute()) {
                            Log.d("editor can not execute : " + editorPath);
                            editorPath = null;
                        }
                    } else {
                        Log.d("editor is null");
                    }
                }
            }
            return editorPath != null ? editorPath : "notepad";
        } else if (isLinux()) {
            return "gedit";
        } else if (isMac()) {
            return "open";
        }
        throw new Exception("Unknown OS : " + OS);
    }

    public static void openEditor(String path) {
        openEditor(new File(path));
    }

    public static void openEditor(File file) {
        if (file == null || !file.exists()) {
            Log.d("No such file or directory");
            return;
        }

        try {
            String editor = _RProp.S.EDITOR.get();
            if (isMac() && getDefaultEditor().contentEquals(editor)) {
                exec(new String[] {editor, "-t", file.getAbsolutePath()});
            } else {
                exec(new String[] {editor, file.getAbsolutePath()});
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void openFileExplorer(String path) {
        openFileExplorer(new File(path));
    }

    public static void openFileExplorer(File file) {
        if (file == null || !file.exists()) {
            Log.d("No such file or directory");
            return;
        }

        String openPath = String.format((isWindows() && file.isFile()) ? "/select,\"%s\"" : "%s",
                file.getAbsolutePath());

        try {
            if (isMac()) {
                exec(new String[] {getFileExplorer(), "-R", openPath});
            } else {
                exec(new String[] {getFileExplorer(), openPath});
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void openArchiveExplorer(String path) {
        openArchiveExplorer(new File(path));
    }

    public static void openArchiveExplorer(File file) {
        if (file == null || !file.exists()) {
            Log.d("No such file or directory");
            return;
        }

        if (isMac()) {
            openFileExplorer(file);
            return;
        }

        try {
            exec(new String[] {getArchiveExplorer(), file.getAbsolutePath()});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void openFile(String path) {
        openFile(new File(path));
    }

    public static void openFile(File file) {
        if (file == null || !file.exists()) {
            Log.d("No such file or directory");
            return;
        }

        try {
            exec(new String[] {getFileOpener(), file.getAbsolutePath()});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static String getRealPath(String path) {
        if (path == null || path.trim().isEmpty()) return null;

        String realPath = null;

        if (path.contains(File.separator)) {
            if (path.startsWith("%")) {
                String env = path.replaceAll("^%(.*)%.*", "$1");
                if (!env.equals(path)) {
                    path = System.getenv(env) + path.replaceAll("^%.*%(.*)", "$1");
                }
            }
            File file = new File(path);
            if (!file.exists()) return null;
            realPath = file.getAbsolutePath();
        } else {
            String cmd = null;
            String regular = null;
            if (isWindows()) {
                cmd = "where";
                regular = "^[A-Z]:\\\\.*";
            } else if (isLinux() || isMac()) {
                cmd = "which";
                regular = "^/.*";
            }

            String[] result = ConsolCmd.exec(new String[] {cmd, path}, true, null);
            if (result == null || result.length <= 0 || !result[0].matches(regular)
                    || !new File(result[0]).exists()) {
                Log.d("No such file "
                        + ((result != null && result.length > 0) ? result[0] : "- result null"));
                return null;
            }
            realPath = result[0];
        }

        return realPath;
    }

    public static String[] getRealPaths(String[] paths) {
        if (paths == null || paths.length == 0) return null;

        ArrayList<String> realPathList = new ArrayList<String>(paths.length);
        ArrayList<String> shortPathList = new ArrayList<String>(paths.length);

        for (String path : paths) {
            if (path.contains(File.separator)) {
                if (path.startsWith("%")) {
                    String env = path.replaceAll("^%(.*)%.*", "$1");
                    if (!env.equals(path)) {
                        path = System.getenv(env) + path.replaceAll("^%.*%(.*)", "$1");
                    }
                }
                File file = new File(path);
                if (!file.exists()) continue;
                if (!realPathList.contains(file.getAbsolutePath())) {
                    realPathList.add(file.getAbsolutePath());
                }
            } else {
                if (path == null || path.trim().isEmpty()) continue;
                shortPathList.add(path.trim());
            }
        }

        if (!shortPathList.isEmpty()) {
            String cmd = null;
            String regular = null;
            if (isWindows()) {
                cmd = "where";
                regular = "^[A-Z]:\\\\.*";
            } else if (isLinux() || isMac()) {
                cmd = "which";
                regular = "^/.*";
            }

            shortPathList.add(0, cmd);

            String[] cmds = shortPathList.toArray(new String[shortPathList.size()]);
            String[] result = ConsolCmd.exec(cmds, true, null);
            if (result != null) {
                for (String r : result) {
                    if (r.matches(regular) && new File(r).exists() && !realPathList.contains(r)) {
                        realPathList.add(r);
                    }
                }
            }
        }

        return !realPathList.isEmpty() ? realPathList.toArray(new String[realPathList.size()])
                : null;
    }

    public static void createShortCut(String exePath, String shortCutName) {
        if (isWindows()) {
            String lnkPath = System.getProperty("user.home") + File.separator + "Desktop"
                    + File.separator + shortCutName + ".lnk";
            try {
                ShellLink.createLink(exePath, lnkPath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public static boolean hasShortCut(String exePath, String shortCutName) {
        if (isWindows()) {
            String lnkPath = System.getProperty("user.home") + File.separator + "Desktop"
                    + File.separator + shortCutName + ".lnk";

            if (!new File(lnkPath).exists()) {
                return false;
            }
            try {
                String pathToExistingFile = new ShellLink(lnkPath).resolveTarget();
                Log.v("pathToExistingFile " + pathToExistingFile);
                if (pathToExistingFile == null || !new File(pathToExistingFile).exists()
                        || !pathToExistingFile.equals(exePath)) {
                    return false;
                }
            } catch (IOException | ShellLinkException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String getOpenCommand(String suffix) throws Exception {
        synchronized (lock) {
            if (!isWindows() || !Advapi32Util.registryKeyExists(WinReg.HKEY_CLASSES_ROOT, suffix)) {
                return null;
            }

            String ftypeKey = null;
            if (suffix.startsWith(".")
                    && Advapi32Util.registryValueExists(WinReg.HKEY_CLASSES_ROOT, suffix, "")) {
                ftypeKey =
                        Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, suffix, "");
            } else {
                ftypeKey = suffix;
            }
            ftypeKey += "\\Shell\\Open\\Command";

            if (!Advapi32Util.registryKeyExists(WinReg.HKEY_CLASSES_ROOT, ftypeKey)
                    || !Advapi32Util.registryValueExists(WinReg.HKEY_CLASSES_ROOT, ftypeKey, "")) {
                return null;
            }
            return Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, ftypeKey, "");
        }
    }

    public static boolean isAssociatedWithFileType(String suffix, String exePath) {
        if (!isWindows()) {
            return true;
        }
        String cmd = null;
        try {
            cmd = getOpenCommand(suffix);
        } catch (Exception e) {
            Log.d("Failure: Can not read registry");
            e.printStackTrace();
            return isAssociatedWithFileTypeLegacy(suffix, exePath);
        }
        return cmd != null && cmd.replaceAll("\"?(.*)", "$1").startsWith(exePath);
    }

    private static boolean isAssociatedWithFileTypeLegacy(String suffix, String exePath) {
        Log.i("isAssociatedWithFileTypeLegacy()");
        String[] output = ConsolCmd.exec(new String[] {"cmd", "/c", "assoc", suffix});
        if (output == null || output.length == 0 || !output[0].startsWith(suffix + "=")) {
            return false;
        }

        String ftype = output[0].replaceAll(suffix + "=(.*)", "$1");
        if (ftype.isEmpty() || ftype.equals(output[0])) {
            return false;
        }

        output = ConsolCmd.exec(new String[] {"cmd", "/c", "ftype", ftype});
        if (output == null || output.length == 0 || !output[0].startsWith(ftype + "=")) {
            return false;
        }

        String cmd = output[0].replaceAll(ftype + "=\"?(.*)", "$1");
        if (cmd.isEmpty() || cmd.equals(output[0])) {
            return false;
        }

        return cmd.startsWith(exePath);
    }

    public static boolean setAssociateFileType(String suffix, String exePath) {
        if (isAssociatedWithFileType(suffix, exePath)) {
            return true;
        }
        String prefixKey = "ApkScanner" + suffix;
        try {
            synchronized (lock) {
                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, prefixKey + "\\CLSID");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, prefixKey + "\\CLSID",
                        "", "{E88DCCE0-B7B3-11d1-A9F0-00AA0060FA31}");
                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\DefaultIcon");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\DefaultIcon", "", exePath + ",1");
                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\OpenWithProgids");
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\OpenWithProgids", "CompressedFolder", "");
                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\Shell\\Open\\Command");
                Advapi32Util.registrySetExpandableStringValue(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\Shell\\Open\\Command", "", "\"" + exePath + "\" \"%1\"");
                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\Shell\\Install\\Command");
                Advapi32Util.registrySetExpandableStringValue(WinReg.HKEY_CLASSES_ROOT,
                        prefixKey + "\\Shell\\Install\\Command", "",
                        "\"" + exePath + "\" install \"%1\"");

                Advapi32Util.registryCreateKey(WinReg.HKEY_CLASSES_ROOT, suffix);
                Advapi32Util.registrySetStringValue(WinReg.HKEY_CLASSES_ROOT, suffix, "",
                        prefixKey);
            }
        } catch (Exception e) {
            Log.d("Failure: Can not write registry");
            e.printStackTrace();
            return setAssociateFileTypeLegacy(suffix, exePath);
        }

        if (!isAssociatedWithFileType(suffix, exePath)) {
            return false;
        }

        // refresh explorer icon
        exec(new String[] {"cmd", "/c", "assoc", suffix + "=ApkScanner" + suffix});
        return true;
    }

    private static boolean setAssociateFileTypeLegacy(String suffix, String exePath) {
        Log.i("setAssociateFileTypeLegacy()");
        if (isAssociatedWithFileType(suffix, exePath)) {
            return true;
        }
        ConsolCmd.exec(new String[][] {
            {"cmd", "/c", "reg", "add", "HKCR\\ApkScanner"+suffix+"\\CLSID", "/ve", "/t", "REG_SZ", "/d", "{E88DCCE0-B7B3-11d1-A9F0-00AA0060FA31}", "/f" },
            {"cmd", "/c", "reg", "add", "HKCR\\ApkScanner"+suffix+"\\DefaultIcon", "/ve", "/t", "REG_SZ", "/d", exePath+",1", "/f" },
            {"cmd", "/c", "reg", "add", "HKCR\\ApkScanner"+suffix+"\\OpenWithProgids", "/v", "CompressedFolder", "/t", "REG_SZ", "/f" },
            {"cmd", "/c", "reg", "add", "HKCR\\ApkScanner"+suffix+"\\Shell\\Open\\Command", "/ve", "/t", "REG_EXPAND_SZ", "/d", exePath+" \\\"%1\\\"", "/f" },
            {"cmd", "/c", "reg", "add", "HKCR\\ApkScanner"+suffix+"\\Shell\\Install\\Command", "/ve", "/t", "REG_EXPAND_SZ", "/d", exePath+" install \\\"%1\\\"", "/f" },
            {"cmd", "/c", "reg", "add", "HKCR\\"+suffix, "/ve", "/t", "REG_SZ", "/d", "ApkScanner"+suffix, "/f" },
            {"cmd", "/c", "assoc", suffix+"=ApkScanner"+suffix },
        });
        return isAssociatedWithFileType(suffix, exePath);
    }

    private static void registryDeleteKeyRecursive(HKEY root, String key) {
        synchronized (lock) {
            if (!Advapi32Util.registryKeyExists(root, key)) {
                return;
            }
            for (String subkey : Advapi32Util.registryGetKeys(root, key)) {
                registryDeleteKeyRecursive(root, key + "\\" + subkey);
            }
            Advapi32Util.registryDeleteKey(root, key);
        }
    }

    public static void unsetAssociateFileType(String suffix, String exePath) {
        if (!isWindows() || !isAssociatedWithFileType(suffix, exePath)) {
            return;
        }

        try {
            String ftypeKey = null;
            synchronized (lock) {
                if (Advapi32Util.registryValueExists(WinReg.HKEY_CLASSES_ROOT, suffix, "")) {
                    ftypeKey = Advapi32Util.registryGetStringValue(WinReg.HKEY_CLASSES_ROOT, suffix,
                            "");
                    Advapi32Util.registryDeleteValue(WinReg.HKEY_CLASSES_ROOT, suffix, "");
                } else {
                    ftypeKey = suffix + "\\Shell\\Open\\Command";
                }
            }
            registryDeleteKeyRecursive(WinReg.HKEY_CLASSES_ROOT, ftypeKey);

            // refresh explorer icon
            exec(new String[] {"cmd", "/c", "assoc", suffix + "=" + suffix});
            exec(new String[] {"cmd", "/c", "assoc", suffix + "="});
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Failure: Can not delete registry");
            unsetAssociateFileTypeLagacy(suffix, exePath);
        }
    }

    private static void unsetAssociateFileTypeLagacy(String suffix, String exePath) {
        Log.i("unsetAssociateFileTypeLagacy()");
        if (!isAssociatedWithFileType(suffix, exePath)) {
            return;
        }
        ConsolCmd.exec(new String[][] {
            {"cmd", "/c", "reg", "add", "HKCR\\"+suffix, "/ve", "/t", "REG_SZ", "/d", "", "/f" },
            {"cmd", "/c", "reg", "delete", "HKCR\\ApkScanner"+suffix, "/f" },
            {"cmd", "/c", "assoc", suffix+"=" },
        });
    }

    public static void exec(List<String> cmd) {
        exec(cmd.toArray(new String[cmd.size()]));
    }

    public static void exec(final String[] cmd) {
        Thread t = new Thread(new Runnable() {
            public void run() {
                ConsolCmd.exec(cmd);
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    public static String[] getRunningProcessFullPath(String imageName) {
        List<String> list = new ArrayList<String>();

        if (SystemUtil.isWindows()) {
            synchronized (lock) {
                Kernel32 kernel32 =
                        (Kernel32) Native.load(Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);
                Tlhelp32.PROCESSENTRY32.ByReference processEntry =
                        new Tlhelp32.PROCESSENTRY32.ByReference();
                WinNT.HANDLE processSnapshot = kernel32
                        .CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));
                try {

                    while (kernel32.Process32Next(processSnapshot, processEntry)) {
                        // looks for a specific process
                        if (imageName == null || Native.toString(processEntry.szExeFile)
                                .equalsIgnoreCase(imageName)) {
                            // System.out.print(processEntry.th32ProcessID + "\t" +
                            // Native.toString(processEntry.szExeFile) + "\t");
                            WinNT.HANDLE moduleSnapshot = kernel32.CreateToolhelp32Snapshot(
                                    Tlhelp32.TH32CS_SNAPMODULE, processEntry.th32ProcessID);
                            try {
                                ProcessPathKernel32.MODULEENTRY32.ByReference me =
                                        new ProcessPathKernel32.MODULEENTRY32.ByReference();
                                ProcessPathKernel32.INSTANCE.Module32First(moduleSnapshot, me);
                                list.add(me.szExePath());
                            } finally {
                                kernel32.CloseHandle(moduleSnapshot);
                            }
                        }
                    }
                } finally {
                    kernel32.CloseHandle(processSnapshot);
                }
            }
        } else if (SystemUtil.isLinux()) {
            String[] uid = ConsolCmd.exec(new String[] {"id", "-ur"});
            String[] cout = null;
            if (uid != null && uid.length > 0 && !uid[0].isEmpty()) {
                cout = ConsolCmd.exec(new String[] {"pgrep", "-x", "-U", uid[0], imageName});
            } else {
                cout = ConsolCmd.exec(new String[] {"pgrep", "-x", imageName});
            }
            for (String pid : cout) {
                String[] process =
                        ConsolCmd.exec(new String[] {"readlink", "/proc/" + pid + "/exe"});
                if (process != null && process.length > 0) {
                    list.add(process[0]);
                }
            }
        }

        return list.toArray(new String[list.size()]);
    }

    public static boolean checkJvmVersion(String minVersion) {
        GeneralVersionChecker jvmVer =
                GeneralVersionChecker.parseFrom(System.getProperty("java.specification.version"));
        GeneralVersionChecker minVer = GeneralVersionChecker.parseFrom(minVersion);
        return jvmVer.compareTo(minVer) >= 0;
    }

    public static int getMenuShortcutKeyMask() {
        boolean isJdkA = SystemUtil.checkJvmVersion("10");
        try {
            final Object toolkit = Toolkit.getDefaultToolkit();
            final Method getMenuShortcutKeyMask = Toolkit.class
                    .getMethod(isJdkA ? "getMenuShortcutKeyMaskEx" : "getMenuShortcutKeyMask");
            return (int) getMenuShortcutKeyMask.invoke(toolkit);
        } catch (Exception e) {
            Log.w(e.getMessage());
        }
        return SystemUtil.isMac() ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] joinArray(T[]... arrays) {
        int length = 0;
        for (T[] array : arrays) {
            length += array.length;
        }

        // T[] result = new T[length];
        final T[] result = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), length);

        int offset = 0;
        for (T[] array : arrays) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }

        return result;
    }
}
