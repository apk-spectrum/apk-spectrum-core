package com.apkspectrum.swing;

import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class UIActionEvent extends ActionEvent
{
	private Object userData;

	public UIActionEvent(Object source, int id, String command) {
		super(source, id, command);
	}

	public UIActionEvent(Object source, int id, String command,
			Object userData) {
		super(source, id, command);
		setUserObject(userData);
	}

	public UIActionEvent(Object source, int id, String command, int modifiers,
			Object userData) {
		super(source, id, command, modifiers);
	}

	public UIActionEvent(Object source, int id, String command, int modifiers) {
		super(source, id, command, modifiers);
		setUserObject(userData);
	}

	public UIActionEvent(Object source, int id, String command, long when,
			int modifiers) {
		super(source, id, command, when, modifiers);
	}

	public UIActionEvent(Object source, int id, String command, long when,
			int modifiers, Object userData) {
		super(source, id, command, when, modifiers);
		setUserObject(userData);
	}

	public void setUserObject(Object userData) {
		this.userData = userData;
	}

	public Object getUserObject() {
		return userData;
	}
}
