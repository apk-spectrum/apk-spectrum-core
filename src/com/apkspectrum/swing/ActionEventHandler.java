package com.apkspectrum.swing;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class ActionEventHandler
	implements ActionListener, PropertyChangeListener
{
	public static final String CONDITIONS_CHANGED = "conditions_changed";

	protected Map<String, ActionListener> actionMap = new HashMap<>();
	protected Map<Object, Object> dataMap;

	protected int flags;

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

		action.addPropertyChangeListener(this);
		updateAction(action);
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

	public Action[] getActions() {
		List<Action> list = new ArrayList<>(actionMap.size());
		for(ActionListener listener: actionMap.values()) {
			if(listener instanceof Action) {
				list.add((Action) listener);
			}
		}
		return list.toArray(new Action[list.size()]);
	}

	public ActionListener getActionListener(String actionCommand) {
		if(actionCommand == null) return null;
		return actionMap.get(actionCommand);
	}

	public ActionListener[] getActionListeners() {
		return actionMap.values().toArray(new ActionListener[actionMap.size()]);
	}

	public void setFlag(int flag) {
		int oldValue = this.flags;
		if((oldValue & flag) != flag) {
			this.flags |= flag;
			updateActions();
			firePropertyChange(CONDITIONS_CHANGED,
					Integer.valueOf(oldValue), Integer.valueOf(this.flags));
		}
	}

	public void unsetFlag(int flag) {
		int oldValue = this.flags;
		if((oldValue & flag) != 0) {
			this.flags &= ~flag;
			updateActions();
			firePropertyChange(CONDITIONS_CHANGED,
					Integer.valueOf(oldValue), Integer.valueOf(this.flags));
		}
	}

	protected void updateActions() {
		for(ActionListener listener: actionMap.values()) {
			if(listener instanceof Action) {
				updateAction((Action) listener);
			}
		}
	}

	protected void updateAction(Action action) {
		if(action instanceof UIAction) {
			((UIAction) action).setEnabled(flags);
		} else {
			Object data = action.getValue(UIAction.ACTION_REQUIRED_CONDITIONS);
			if(data != null) {
				if(data instanceof Integer) {
					int condition = ((Integer) data).intValue();
					action.setEnabled((flags & condition) == condition);
				} else {
					action.setEnabled(true);
				}
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if(UIAction.ACTION_REQUIRED_CONDITIONS.equals(evt.getPropertyName())
				&& evt.getSource() instanceof Action) {
			Action action = (Action) evt.getSource();
			updateAction(action);
			if(evt.getOldValue() != null && evt.getNewValue() == null) {
				action.setEnabled(true);
			}
		}
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

	public Object getData(String key) {
		return dataMap != null ? dataMap.get(key) : null;
	}

	public void putData(String key, Object newValue) {
		Object oldValue = null;
		if(dataMap == null) {
			dataMap = new HashMap<>();
		} else if(dataMap.containsKey(key)) {
			oldValue = dataMap.get(key);
		}
		if(newValue == null) {
			dataMap.remove(key);
		} else {
			dataMap.put(key, newValue);
		}
		firePropertyChange(key, oldValue, newValue);
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

	private static PropertyChangeSupport pcs;

    protected void firePropertyChange(String propertyName, Object oldValue,
    		Object newValue) {
        if (pcs == null ||
            (oldValue != null && newValue != null
            	&& oldValue.equals(newValue))) {
            return;
        }
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public synchronized void addPropertyChangeListener(
    		PropertyChangeListener listener) {
        if (pcs == null) {
        	pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(
    		PropertyChangeListener listener) {
        if (pcs == null) {
            return;
        }
        pcs.removePropertyChangeListener(listener);
    }

    public synchronized PropertyChangeListener[] getPropertyChangeListeners() {
        if (pcs == null) {
            return new PropertyChangeListener[0];
        }
        return pcs.getPropertyChangeListeners();
    }
}
