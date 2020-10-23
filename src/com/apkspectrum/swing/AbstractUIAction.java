package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public abstract class AbstractUIAction extends AbstractAction
	implements UIAction
{
	protected ActionEventHandler handler;
	protected int conditions;

	public AbstractUIAction() { }

	public AbstractUIAction(ActionEventHandler h) {
		setHandler(h);
	}

	protected Window getWindow(EventObject e) {
		Object source = null;

		if(e != null) {
			source = e.getSource();
		} else {
			AWTEvent event = EventQueue.getCurrentEvent();
			source = event.getSource();
		}

		if(source instanceof KeyStrokeAction) {
			source = ((KeyStrokeAction) source).getComponent();
		}

		if(source instanceof Component) {
			return SwingUtilities.getWindowAncestor((Component) source);
		}

		return null;
	}

	@Override
	public void putValue(String key, Object newValue) {
		if(ACTION_EVENT_HANDLER.equals(key)
				&& newValue instanceof ActionEventHandler) {
			setHandler((ActionEventHandler) newValue);
		} else if(ACTION_REQUIRED_CONDITIONS.equals(key)) {
			setRequiredConditions(newValue instanceof Integer ?
					((Integer) newValue).intValue() : 0);
		} else {
			super.putValue(key, newValue);
		}
	}

	public void setHandler(ActionEventHandler h) {
		handler = h;
		super.putValue(ACTION_EVENT_HANDLER, h);
	}

	public ActionEventHandler getHandler() {
		return handler;
	}

	@Override
	public void setRequiredConditions(int conditions) {
		this.conditions = conditions;
		super.putValue(ACTION_REQUIRED_CONDITIONS, Integer.valueOf(conditions));
	}

	@Override
	public int getRequiredConditions() {
		return conditions;
	}

	@Override
	public void setEnabled(int flags) {
		setEnabled((flags & conditions) == conditions);
	}

	@Override
	public String getActionCommand() {
		try {
			return (String) getClass()
					.getDeclaredField(ACTION_COMMAND_FIELD).get(null);
		} catch (NoSuchFieldException | SecurityException
				| IllegalArgumentException | IllegalAccessException e) {
			Log.w("No such field : " + e.getMessage()
					+ " from " + getClass().getName());
		}
		String actCmd = (String) getValue(ACTION_COMMAND_KEY);
		return actCmd != null ? actCmd : getClass().getName();
	}

	@Override
	public String getText() {
		return (String) getValue(Action.NAME);
	}

	@Override
	public void setText(String text) {
		putValue(Action.NAME, text);
	}

	@Override
	public Icon getIcon() {
		return (Icon) getValue(Action.LARGE_ICON_KEY);
	}

	@Override
	public void setIcon(Icon icon) {
		putValue(Action.LARGE_ICON_KEY, icon);
	}

	@Override
	public String getToolTipText() {
		return (String) getValue(Action.SHORT_DESCRIPTION);
	}

	@Override
	public void setToolTipText(String text) {
		putValue(Action.SHORT_DESCRIPTION, text);
	}

	@Override
	public int getMnemonic() {
		Integer mnemonic = (Integer) getValue(Action.MNEMONIC_KEY);
		return mnemonic != null ? mnemonic.intValue() : '\0';
	}

	@Override
	public void setMnemonic(int mnemonic) {
		putValue(Action.MNEMONIC_KEY, Integer.valueOf(mnemonic));
	}

	@Override
	public int getDisplayedMnemonicIndex() {
		Integer index = (Integer) getValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY);
		return index != null ? index.intValue() : -1;
	}

	@Override
	public void setDisplayedMnemonicIndex(int index) {
		putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, Integer.valueOf(index));
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(getValue(Action.SELECTED_KEY));
	}

	@Override
	public void setSelected(boolean b) {
		putValue(Action.SELECTED_KEY, Boolean.valueOf(b));
	}
}
