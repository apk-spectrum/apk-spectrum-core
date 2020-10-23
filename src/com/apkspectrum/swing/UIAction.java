package com.apkspectrum.swing;

import javax.swing.Action;
import javax.swing.Icon;

public interface UIAction extends Action {
	public static final String ACTION_COMMAND_FIELD
							= "ACTION_COMMAND";
	public static final String ACTION_EVENT_HANDLER
							= "ACTION_EVENT_HANDLER";
	public static final String ACTION_REQUIRED_CONDITIONS
							= "ACTION_REQUIRED_CONDITIONS";

	public void setHandler(ActionEventHandler h);
	public ActionEventHandler getHandler();

	public void setRequiredConditions(int conditions);
	public int getRequiredConditions();

	public void setEnabled(int flags);

	public String getActionCommand();

	public String getText();
	public void setText(String text);

	public Icon getIcon();
	public void setIcon(Icon icon);

	public String getToolTipText();
	public void setToolTipText(String text);

	public int getMnemonic();
	public void setMnemonic(int mnemonic);

	public int getDisplayedMnemonicIndex();
	public void setDisplayedMnemonicIndex(int index);

	public boolean isSelected();
	public void setSelected(boolean b);
}
