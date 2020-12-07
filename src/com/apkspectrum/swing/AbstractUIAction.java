package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Window;
import java.util.EventObject;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public abstract class AbstractUIAction extends AbstractAction
	implements UIAction
{
	protected ActionEventHandler handler;
	protected int conditions;

	public AbstractUIAction() { }

	public AbstractUIAction(ActionEventHandler h) {
		if(h != null) setHandler(h);
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
		switch(key) {
		case ACTION_EVENT_HANDLER:
			setHandler(newValue instanceof ActionEventHandler ?
					(ActionEventHandler) newValue : null);
			break;
		case ACTION_REQUIRED_CONDITIONS:
			setRequiredConditions(newValue instanceof Integer ?
					((Integer) newValue).intValue() : 0);
			break;
		default:
			super.putValue(key, newValue);
		}
	}

	@Override
	public Object getValue(String key) {
		switch(key) {
		case ACTION_EVENT_HANDLER:
			return getHandler();
		case ACTION_REQUIRED_CONDITIONS:
			return getRequiredConditions();
		case ACTION_COMMAND_KEY:
			return getActionCommand();
		case LARGE_ICON_KEY:
			return getLargeIcon();
		case SMALL_ICON:
			return getSmallIcon();
		default:
			return super.getValue(key);
		}
	}

	@Override
	public void setHandler(ActionEventHandler h) {
		if(h == handler) return;
		Object old = handler;
		handler = h;
		firePropertyChange(ACTION_EVENT_HANDLER, old, handler);
	}

	@Override
	public ActionEventHandler getHandler() {
		return handler;
	}

	@Override
	public void putHandlerData(String key, Object newValue) {
		if(handler == null) return;
		handler.putData(key, newValue);
	}

	@Override
	public Object getHandlerData(String key) {
		return handler != null ? handler.getData(key) : null;
	}

	@Override
	public void setRequiredConditions(int conditions) {
		Integer old = Integer.valueOf(this.conditions);
		this.conditions = conditions;
		firePropertyChange(ACTION_REQUIRED_CONDITIONS, old, conditions);
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
	public void setActionCommand(String actionCommand) {
		putValue(ACTION_COMMAND_KEY, actionCommand);
	}

	@Override
	public String getActionCommand() {
		String actCmd = (String) super.getValue(ACTION_COMMAND_KEY);
		if(actCmd == null) {
			try {
				actCmd = (String) getClass().getDeclaredField(
						ACTION_COMMAND_FIELD).get(null);
			} catch (Exception e) { }
		}
		return actCmd != null ? actCmd : getClass().getName();
	}

	@Override
	public void setDenyUpdateActionCommandKey(boolean deny) {
		putValue(DENY_UPDATE_ACTION_COMMAND_KEY, Boolean.valueOf(deny));
	}

	@Override
	public boolean isDenyUpdateActionCommandKey() {
		Object deny = getValue(DENY_UPDATE_ACTION_COMMAND_KEY);
		return deny != null && (deny instanceof Boolean && (Boolean) deny);
	}

	@Override
	public String getText() {
		return (String) getValue(NAME);
	}

	@Override
	public void setText(String text) {
		putValue(NAME, text);
	}

	@Override
	public Icon getIcon() {
		Icon icon = getLargeIcon();
		return icon != null ? icon : getSmallIcon();
	}

	@Override
	public Icon getIcon(Dimension size) {
		return getIcon(size.width, size.height);
	}

	@Override
	public Icon getIcon(int w, int h) {
        Icon icon = null;
        icon = (Icon) getValue(Action.LARGE_ICON_KEY);
        if (icon == null) {
            icon = (Icon) getValue(Action.SMALL_ICON);
        }
        if(icon instanceof ImageIcon) {
        	icon = ImageScaler.getScaledImageIcon((ImageIcon) icon, w, h);
        }
		return icon;
	}

	public Icon getLargeIcon() {
        Icon icon = null;
        icon = (Icon) super.getValue(Action.LARGE_ICON_KEY);
        if(!(icon instanceof ImageIcon)) return icon;

        Object size = getValue(LARGE_ICON_SIZE_KEY);
        if(!(size instanceof Dimension)) size = getValue(ICON_SIZE_KEY);
        if(!(size instanceof Dimension)) return icon;

        return ImageScaler.getScaledImageIcon((ImageIcon) icon,
        			((Dimension) size).width, ((Dimension) size).height);
	}

	public Icon getSmallIcon() {
        Icon icon = null;
        icon = (Icon) super.getValue(Action.SMALL_ICON);
        if(!(icon instanceof ImageIcon)) return icon;

        Object size = getValue(SMALL_ICON_SIZE_KEY);
        if(!(size instanceof Dimension)) size = getValue(ICON_SIZE_KEY);
        if(!(size instanceof Dimension)) return icon;

        return ImageScaler.getScaledImageIcon((ImageIcon) icon,
					((Dimension) size).width, ((Dimension) size).height);
	}

	@Override
	public void setIcon(Icon icon) {
		putValue(LARGE_ICON_KEY, icon);
	}

	@Override
	public String getToolTipText() {
		return (String) getValue(SHORT_DESCRIPTION);
	}

	@Override
	public void setToolTipText(String text) {
		putValue(SHORT_DESCRIPTION, text);
	}

	@Override
	public int getMnemonic() {
		Integer mnemonic = (Integer) getValue(MNEMONIC_KEY);
		return mnemonic != null ? mnemonic.intValue() : '\0';
	}

	@Override
	public void setMnemonic(int mnemonic) {
		putValue(MNEMONIC_KEY, Integer.valueOf(mnemonic));
	}

	@Override
	public int getDisplayedMnemonicIndex() {
		Integer index = (Integer) getValue(DISPLAYED_MNEMONIC_INDEX_KEY);
		return index != null ? index.intValue() : -1;
	}

	@Override
	public void setDisplayedMnemonicIndex(int index) {
		putValue(DISPLAYED_MNEMONIC_INDEX_KEY,
				Integer.valueOf(index));
	}

	@Override
	public boolean isSelected() {
		return Boolean.TRUE.equals(getValue(SELECTED_KEY));
	}

	@Override
	public void setSelected(boolean b) {
		putValue(SELECTED_KEY, Boolean.valueOf(b));
	}
}
