package com.apkspectrum.resource;

public enum _RFile implements ResFile<java.io.File>
{
	BIN_PATH					(Type.BIN, ""),

	BIN_AAPT_LIB32_LNX			(Type.BIN, "linux/libAaptNativeWrapper32.so", "nux"),
	BIN_AAPT_LIB32_WIN			(Type.BIN, "windows\\AaptNativeWrapper32.dll", "win"),
	BIN_AAPT_LIB32				(Type.BIN, new _RFile[]{ BIN_AAPT_LIB32_WIN, BIN_AAPT_LIB32_LNX }),

	BIN_AAPT_LIB64_LNX			(Type.BIN, "linux/libAaptNativeWrapper64.so", "nux"),
	BIN_AAPT_LIB64_MAC			(Type.BIN, "darwin/libAaptNativeWrapper64.dylib", "mac"),
	BIN_AAPT_LIB64_WIN			(Type.BIN, "windows\\AaptNativeWrapper64.dll", "win"),
	BIN_AAPT_LIB64				(Type.BIN, new _RFile[]{ BIN_AAPT_LIB64_WIN, BIN_AAPT_LIB64_LNX, BIN_AAPT_LIB64_MAC }),

	BIN_AAPT_LIBC32_LNX			(Type.BIN, "linux/libc++32.so", "nux"),
	BIN_AAPT_LIBC32				(Type.BIN, new _RFile[]{ BIN_AAPT_LIBC32_LNX }),

	BIN_AAPT_LIBC64_LNX			(Type.BIN, "linux/libc++64.so", "nux"),
	BIN_AAPT_LIBC64_MAC			(Type.BIN, "darwin/libc++.dylib", "mac"),
	BIN_AAPT_LIBC64				(Type.BIN, new _RFile[]{ BIN_AAPT_LIBC64_LNX, BIN_AAPT_LIBC64_MAC }),

	BIN_ADB_LNX					(Type.BIN, "linux/adb", "nux"),
	BIN_ADB_MAC					(Type.BIN, "darwin/adb", "mac"),
	BIN_ADB_WIN					(Type.BIN, "windows\\adb.exe", "win"),
	BIN_ADB						(Type.BIN, new _RFile[]{ BIN_ADB_WIN, BIN_ADB_LNX, BIN_ADB_MAC }),

	BIN_AAPT_LNX				(Type.BIN, "linux/aapt", "nux"),
	BIN_AAPT_MAC				(Type.BIN, "darwin/darwin/aapt", "mac"),
	BIN_AAPT_WIN				(Type.BIN, "windows\\aapt.exe", "win"),
	BIN_AAPT					(Type.BIN, new _RFile[]{ BIN_AAPT_WIN, BIN_AAPT_LNX, BIN_AAPT_MAC }),

	BIN_JDGUI					(Type.BIN, "jd-gui-1.6.6.jar"),

	BIN_DEX2JAR_LNX				(Type.BIN, "dex2jar/d2j-dex2jar.sh", "nux"),
	BIN_DEX2JAR_MAC				(Type.BIN, "dex2jar/d2j-dex2jar.sh", "mac"),
	BIN_DEX2JAR_WIN				(Type.BIN, "dex2jar\\d2j-dex2jar.bat", "win"),
	BIN_DEX2JAR					(Type.BIN, new _RFile[]{ BIN_DEX2JAR_WIN, BIN_DEX2JAR_LNX, BIN_DEX2JAR_MAC }),

	BIN_JADX_LNX				(Type.BIN, "jadx/bin/jadx-gui", "nux"),
	BIN_JADX_MAC				(Type.BIN, "jadx/bin/jadx-gui", "mac"),
	BIN_JADX_WIN				(Type.BIN, "jadx\\bin\\jadx-gui.bat", "win"),
	BIN_JADX_GUI				(Type.BIN, new _RFile[]{ BIN_JADX_WIN, BIN_JADX_LNX, BIN_JADX_MAC }),

	BIN_BYTECODE_VIEWER			(Type.BIN, "Bytecode-Viewer-2.9.22.jar"),

	BIN_SIGNAPK					(Type.BIN, "signapk.jar"),

	BIN_IMG_EXTRACTOR_WIN		(Type.BIN, "windows\\ImgExtractor.exe", "win"),

	PLUGIN_PATH					(Type.PLUGIN, ""),
	PLUGIN_CONF_PATH			(Type.PLUGIN, "plugins.conf"),

	SSL_TRUSTSTORE_PATH			(Type.SECURITY, "trustStore.jks"),

	DATA_PATH					(Type.DATA, ""),
	DATA_PERMISSIONS_HISTORY	(Type.DATA, "PermissionsHistory.xml"),

	RAW_ROOT_PATH				(Type.RES_ROOT, ""),
	RAW_ANDROID_MANIFEST		(Type.RES_ROOT, "AndroidManifest.xml"),

	RAW_VALUES_PATH				(Type.RES_VALUE, ""),
	RAW_PUBLIC_XML				(Type.RES_VALUE, "public.xml"),
	RAW_PERMISSIONS_HISTORY		(Type.RES_VALUE, "PermissionsHistory.xml"),

	RAW_SDK_INFO_FILE			(Type.RES_VALUE, "sdk-info.xml"),
	RAW_PROTECTION_LEVELS_HTML	(Type.RES_VALUE, "ProtectionLevels.html"),
	RAW_PERMISSION_REFERENCE_HTML(Type.RES_VALUE, "PermissionReference.html"),

	ETC_SETTINGS_FILE			(Type.ETC, "settings.txt"),
	; // ENUM END

	private DefaultResFile res;

	private _RFile(Type type, String value) {
		res = new DefaultResFile(type, value);
	}

	private _RFile(Type type, String value, String config) {
		res = new DefaultResFile(type, value, config);
	}

	private _RFile(Type type, _RFile[] cfgResources) {
		try {
			res = new DefaultResFile(type, cfgResources);
		} catch(IllegalArgumentException e) {
			//Log.v(name() + " hasn't supported config. " + res);
		}
	}

	@Override
	public String getValue() {
		return res.getValue();
	}

	@Override
	public String getConfiguration() {
		return res.getConfiguration();
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public String getPath() {
		return res.getPath();
	}

	@Override
	public java.net.URL getURL() {
		return res.getURL();
	}

	@Override
	public java.io.File get() {
		return res.get();
	}

	@Override
	public java.net.URL getResource() {
		return res.getResource();
	}

	@Override
	public java.io.InputStream getResourceAsStream() {
		return res.getResourceAsStream();
	}

	public String getString() {
		return res.getString();
	}
}
