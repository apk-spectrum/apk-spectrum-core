package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import com.apkspectrum.plugin.IPlugIn;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.util.ClassFinder;
import com.apkspectrum.util.Log;

abstract public class ActionEventHandler implements ActionListener
{
	protected Map<String, ActionListener> actionMap = new HashMap<>();
	protected Map<Object, Object> dataMap;

	public ActionEventHandler() { }

	public ActionEventHandler(Package actionPackage) {
		loadActions(actionPackage);
	}

	public ActionEventHandler(String packageName) {
		loadActions(packageName);
	}

	public void loadActions(Package actionPackage) {
		if(actionPackage == null) return;
		loadActions(actionPackage.getName());
	}

	public void loadActions(String packageName) {
		try {
			for(Class<?> cls : ClassFinder.getClasses(packageName)) {
				if(cls.isMemberClass() || cls.isInterface()
					|| !ActionListener.class.isAssignableFrom(cls)) continue;
				ActionListener listener = null;
				if (UIAction.class.isAssignableFrom(cls)) {
					try {
						listener = (ActionListener) cls.getDeclaredConstructor(ActionEventHandler.class).newInstance(this);
					} catch (Exception e) { }
				}
				if(listener == null) {
					try {
						listener = (ActionListener) cls.getDeclaredConstructor().newInstance();
					} catch (Exception e1) { }
				}
				if(listener != null) {
					if (listener instanceof Action) {
						addAction((Action)listener);
					} else {
						addActionListener(getActionCommand(listener), listener);
					}
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	public void addAction(Action action) {
		addActionListener(getActionCommand(action), action);
	}

	public void addActionListener(String actionCommand, ActionListener action) {
		if(action == null) return;
		if(actionMap.containsKey(actionCommand)) {
			Log.v(String.format("addAction() %s was already existed. So change to new : %s", actionCommand, action));
		}
		actionMap.put(actionCommand, action);
	}

	public void removeAction(Action action) {
		if(action == null) return;
		removeActionListener(getActionCommand(action));
	}

	public void removeActionListener(String actionCommand) {
		if(actionCommand == null) return;
		if(actionMap.containsKey(actionCommand)) {
			actionMap.remove(actionCommand);
		}
	}

	public Action getAction(String actionCommand) {
		if(actionCommand == null) return null;
		ActionListener listener = actionMap.get(actionCommand);
		return listener instanceof Action ? (Action)listener : null;
	}

	public ActionListener getActionListener(String actionCommand) {
		if(actionCommand == null) return null;
		return actionMap.get(actionCommand);
	}

	public void sendEvent(String actionCommand) {
		sendEvent(getWindow(), actionCommand);
	}

	public void sendEvent(Component c, String actionCommand) {
		ActionListener action = getAction(actionCommand);
		if(action == null) return;

		action.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, actionCommand));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		actionPerformed(e, null);
	}

	public void actionPerformed(ActionEvent e, Object userObject) {
		String actCmd = e.getActionCommand();
		if(actCmd != null) {
			ActionListener act = actionMap.get(actCmd);
			if(act != null) {
				if(userObject != null && act instanceof Action) {
					((Action)act).putValue(UIAction.USER_OBJECT, userObject);
				}
				act.actionPerformed(e);
				if(userObject != null && act instanceof Action) {
					((Action)act).putValue(UIAction.USER_OBJECT, null);
				}
				return;
			}

			IPlugIn plugin = PlugInManager.getPlugInByActionCommand(actCmd);
			if(plugin != null) {
				plugin.launch();
				return;
			}
		}
		Log.e("Unknown action command : " + actCmd);
	}

	public Object getData(Object key) {
		return dataMap != null ? dataMap.get(key) : null;
	}

	public void putData(Object key, Object value) {
		if(dataMap == null) {
			dataMap = new HashMap<>();
		}
		if(value == null) {
			dataMap.remove(key);
		} else {
			dataMap.put(key, value);
		}
	}

	private String getActionCommand(ActionListener actionListener) {
		if(actionListener instanceof UIAction) {
			return ((UIAction) actionListener).getActionCommand();
		} else {
			try {
				return (String) actionListener.getClass().getDeclaredField(UIAction.ACTION_COMMAND_FIELD).get(null);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				Log.v("No such field : " + e.getMessage() + " from " + getClass().getName());
			}
			String actCmd = null;
			if(actionListener instanceof Action) {
				actCmd = (String) ((Action) actionListener).getValue(Action.ACTION_COMMAND_KEY);
			}
			if(actCmd == null) actCmd = getClass().getName();
			return actCmd;
		}
	}

	protected static Window getWindow() {
		Object source = null;

		AWTEvent e = EventQueue.getCurrentEvent();
		if(e != null) {
			source = e.getSource();
		}

		if(source instanceof Component) {
			return SwingUtilities.getWindowAncestor((Component) source);
		}

		return null;
	}
}
