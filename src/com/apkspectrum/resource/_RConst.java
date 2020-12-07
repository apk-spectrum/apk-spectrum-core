package com.apkspectrum.resource;

import java.awt.event.InputEvent;

import com.apkspectrum.util.SystemUtil;

public interface _RConst
{
	public static final String CORE_VERSION = "1.0";

	public static final String SAMSUNG_KEY_MD5 
						= "D0:87:E7:29:12:FB:A0:64:CA:FA:78:DC:34:AE:A8:39";
	public static final String SS_TEST_KEY_MD5
						= "8D:DB:34:2F:2D:A5:40:84:02:D7:56:8A:F2:1E:29:F9";

	public static final int CTRL_MASK = SystemUtil.getMenuShortcutKeyMask();
	public static final int ALT_MASK = InputEvent.ALT_DOWN_MASK;
	public static final int SHIFT_MASK = InputEvent.SHIFT_DOWN_MASK;
	public static final int CTRL_SHIFT_MASK = CTRL_MASK | SHIFT_MASK;
	public static final int ALT_SHIFT_MASK = ALT_MASK | SHIFT_MASK;

	// @see ActionEventHandler
	public static final String OWNER_WINDOW_KEY		 = "WINDOW_KEY";
	public static final String APK_SCANNER_KEY 		 = "APK_SCANNER_KEY";
	public static final String MULTI_APK_SCANNER_KEY = "MULTI_APK_SCANNER_KEY";
	public static final String POSITION_KEY			 = "POSITION";
}
