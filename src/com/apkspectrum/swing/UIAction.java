package com.apkspectrum.swing;

import javax.swing.Action;

public interface UIAction extends Action {
	public static final String ACTION_COMMAND_FIELD = "ACTION_COMMAND";
	public static final String USER_OBJECT = "USER_OBJECT";

	public void setHandler(ActionEventHandler h);
	public ActionEventHandler getHandler();
	public String getActionCommand();
	public void setUserObject(Object obj);
	public Object getUserObject();
}
