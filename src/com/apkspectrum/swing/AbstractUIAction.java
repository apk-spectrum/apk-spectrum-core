package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import com.apkspectrum.util.Log;

@SuppressWarnings("serial")
public abstract class AbstractUIAction extends AbstractAction implements UIAction
{
	protected ActionEventHandler handler;

	public AbstractUIAction() { }

	public AbstractUIAction(ActionEventHandler h) {
		setHandler(h);
	}

	protected Window getWindow(ActionEvent e) {
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

	public void setHandler(ActionEventHandler h) {
		handler = h;
	}

	public ActionEventHandler getHandler() {
		return handler;
	}

	public String getActionCommand() {
		try {
			return (String) getClass().getDeclaredField(ACTION_COMMAND_FIELD).get(null);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			Log.w("No such field : " + e.getMessage() + " from " + getClass().getName());
		}
		String actCmd = (String) getValue(ACTION_COMMAND_KEY);
		return actCmd != null ? actCmd : getClass().getName();
	}

	public void setUserObject(Object obj) {
		putValue(USER_OBJECT, obj);
	}

	public Object getUserObject() {
		return getValue(USER_OBJECT);
	}
}
