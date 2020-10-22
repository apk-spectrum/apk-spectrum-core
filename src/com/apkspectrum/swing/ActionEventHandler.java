package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.apkspectrum.plugin.IPlugIn;
import com.apkspectrum.plugin.PlugInManager;
import com.apkspectrum.resource.ResAction;
import com.apkspectrum.util.ClassFinder;
import com.apkspectrum.util.Log;

public class ActionEventHandler implements ActionListener
{
	protected Map<String, ActionListener> actionMap = new HashMap<>();
	protected Map<Object, Object> dataMap;

	public ActionEventHandler() { }

	public ActionEventHandler(Package actionPackage) {
		loadActions(actionPackage);
	}

	public ActionEventHandler(Package actionPackage,
			Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
		loadActions(actionPackage, actResEnum);
	}

	public ActionEventHandler(String packageName) {
		loadActions(packageName);
	}

	public ActionEventHandler(String packageName,
			Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
		loadActions(packageName, actResEnum);
	}

	public void loadActions(Package actionPackage) {
		loadActions(actionPackage, null);
	}

	public void loadActions(Package actionPackage,
			Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
		if(actionPackage == null) return;
		loadActions(actionPackage.getName(), actResEnum);
	}

	public void loadActions(String packageName) {
		loadActions(packageName, null);
	}

	public void loadActions(String packageName,
			Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
		try {
			for(Class<?> cls : ClassFinder.getClasses(packageName)) {
				if(cls.isMemberClass() || cls.isInterface()
					|| !ActionListener.class.isAssignableFrom(cls)) continue;
				ActionListener listener = null;
				if (UIAction.class.isAssignableFrom(cls)) {
					try {
						listener = (ActionListener) cls.getDeclaredConstructor(
								ActionEventHandler.class).newInstance(this);
					} catch (Exception e) { }
				}
				if(listener == null) {
					try {
						listener = (ActionListener) cls.getDeclaredConstructor()
								.newInstance();
					} catch (Exception e1) { }
				}
				if(listener != null) {
					if (listener instanceof Action) {
						Action action = (Action) listener;
						addAction(action, actResEnum);
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
		addAction(action, null);
	}

	private void addAction(Action action,
			Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
		String actCommand = getActionCommand(action);
		if(actResEnum != null) {
			try {
				Method method = actResEnum.getMethod("valueOf", String.class);
				ResAction<?> res = (ResAction<?>)
						method.invoke(null, actCommand);
				res.set(action);
			} catch (IllegalAccessException | InvocationTargetException
					| NoSuchMethodException | SecurityException e) {
				//Log.v("No such action resource : " + actCommand);
			}
		}
		action.putValue(UIAction.ACTION_EVENT_HANDLER, this);
		addActionListener(actCommand, action);
	}

	public void addActionListener(String actionCommand, ActionListener action) {
		if(action == null) return;
		if(actionMap.containsKey(actionCommand)) {
			Log.v(String.format("addAction() %s was already existed. "
					+ "Change to new : %s", actionCommand, action));
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

	public void setActionToComponent(AbstractButton c) {
		setActionToComponent(c, c.getActionCommand());
	}

	public void setActionToComponent(AbstractButton c, String actionCommand) {
		c.setAction(getAction(actionCommand));
	}

	public void setActionToComponent(JComboBox<?> c) {
		setActionToComponent(c, c.getActionCommand());
	}

	public void setActionToComponent(JComboBox<?> c, String actionCommand) {
		c.setAction(getAction(actionCommand));
	}

	public void setActionToComponent(JTextField c, String actionCommand) {
		c.setAction(getAction(actionCommand));
	}

	public void sendEvent(String actionCommand) {
		sendEvent(getWindow(), actionCommand);
	}

	public void sendEvent(Component c, String actionCommand) {
		ActionListener action = getAction(actionCommand);
		if(action == null) return;

		action.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED,
				actionCommand));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String actCmd = e.getActionCommand();
		if(actCmd != null) {
			ActionListener act = actionMap.get(actCmd);
			if(act != null) {
				act.actionPerformed(e);
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
				return (String) actionListener.getClass().getDeclaredField(
						UIAction.ACTION_COMMAND_FIELD).get(null);
			} catch (NoSuchFieldException | SecurityException
					| IllegalArgumentException | IllegalAccessException e) {
				Log.v("No such field : " + e.getMessage()
					+ " from " + actionListener.getClass().getName());
			}
			String actCmd = null;
			if(actionListener instanceof Action) {
				actCmd = (String) ((Action) actionListener)
							.getValue(Action.ACTION_COMMAND_KEY);
			}
			if(actCmd == null) actCmd = actionListener.getClass().getName();
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
