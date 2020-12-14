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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.apkspectrum.resource.ResAction;
import com.apkspectrum.util.ClassFinder;
import com.apkspectrum.util.Log;

public class ActionEventHandler
	implements ActionListener, PropertyChangeListener
{
	public static final String CONDITIONS_KEY = "conditions_key";

	protected Map<String, ActionListener> actionMap = new HashMap<>();
	protected Map<Object, Object> dataMap;

	protected boolean allowUpdateActionCommandKey = true;
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
			Class<? extends Enum<? extends ResAction<?>>> actRes) {
		try {
			for(Class<?> cls : ClassFinder.getClasses(packageName)) {
				if(cls.isMemberClass() || cls.isInterface()
					|| Modifier.isAbstract(cls.getModifiers())
					|| !ActionListener.class.isAssignableFrom(cls)) continue;
				ActionListener listener = createActionInstance(cls);
				String actCmd = getActionCommand(listener);
				if (listener instanceof Action) {
					ResAction<?> res = getResAction(actRes, actCmd);
					if(res != null) res.set((Action) listener);
				}
				if(listener != null && !actionMap.containsValue(listener)) {
					addActionListener(actCmd, listener);
				}
			}
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	private ActionListener createActionInstance(Class<?> cls) {
		ActionListener listener = null;
		try {
			Constructor<?> def = null;
			for(Constructor<?> c :cls.getConstructors()) {
				Class<?>[] t = c.getParameterTypes();
				if(t.length == 0) def = c;
				if(t.length != 1) continue;
				if(t[0].isAssignableFrom(getClass())) {
					listener = (ActionListener) c.newInstance(this);
					break;
				};
			}
			if(listener == null && def != null) {
				listener = (ActionListener) def.newInstance();
			}
		} catch (Exception e) { }
		return listener;
	}

	private ResAction<?> getResAction(
			Class<? extends Enum<? extends ResAction<?>>> actRes,
			String actionCommand) {
		ResAction<?> res = null;
		if(actRes != null) {
			Method method;
			try {
				method = actRes.getMethod("valueOf", String.class);
				res = (ResAction<?>) method.invoke(null, actionCommand);
			} catch (Exception e) {
				//Log.v("No such action resource : " + actCommand);
			}
		}
		return res;
	}

	public void addAction(Action action) {
		addActionListener(getActionCommand(action), action);
	}

	public void addAction(String actionCommand, Action action) {
		addActionListener(actionCommand, action);
	}

	public void addActionListener(String actionCommand, ActionListener action) {
		if(action == null) return;
		Action oldAction = getAction(actionCommand);

		if(actionMap.containsKey(actionCommand)) {
			Log.v(String.format("addAction() %s was already existed. "
					+ "Change to new : %s", actionCommand, action));
		}

		if(action instanceof Action) {
			((Action) action).putValue(UIAction.ACTION_EVENT_HANDLER, this);
			((Action) action).addPropertyChangeListener(this);
		}

		actionMap.put(actionCommand, action);

		if(oldAction != null && !actionMap.containsValue(oldAction)) {
			oldAction.removePropertyChangeListener(this);
			oldAction.putValue(UIAction.ACTION_EVENT_HANDLER, null);
		}

		if(action instanceof Action) {
			updateActionStatus((Action) action);
		}
	}

	public void removeAction(Action action) {
		if(action == null || !actionMap.containsValue(action)) return;

		ArrayList<String> list = new ArrayList<>();
		for(Entry<String, ActionListener> e : actionMap.entrySet()) {
			if(action.equals(e.getValue())) {
				list.add(e.getKey());
			}
		}

		for(String actCmd : list) {
			removeActionListener(actCmd);
		}
	}

	public void removeActionListener(String actionCommand) {
		if(actionCommand == null || !actionMap.containsKey(actionCommand)) {
			return;
		}

		Action action = getAction(actionCommand);
		actionMap.remove(actionCommand);
		if(action != null && !actionMap.containsValue(action)) {
			action.removePropertyChangeListener(this);
			action.putValue(UIAction.ACTION_EVENT_HANDLER, null);
		}
	}

	public void changeActionCommand(String oldCmd, String newCmd) {
		Action action;
		if(newCmd == null || (action = getAction(oldCmd)) == null) return;
		actionMap.remove(oldCmd);
		actionMap.put(newCmd, action);
		firePropertyChange(Action.ACTION_COMMAND_KEY, oldCmd, newCmd);
	}

	public Action getAction(String actionCommand) {
		ActionListener listener = getActionListener(actionCommand);
		return listener instanceof Action ? (Action) listener : null;
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

	public int getFlag() {
		return this.flags;
	}

	public void setFlag(int flag) {
		int oldValue = this.flags;
		if((oldValue & flag) != flag) {
			this.flags |= flag;
			updateActionStatus();
			firePropertyChange(CONDITIONS_KEY,
					Integer.valueOf(oldValue), Integer.valueOf(this.flags));
		}
	}

	public void unsetFlag(int flag) {
		int oldValue = this.flags;
		if((oldValue & flag) != 0) {
			this.flags &= ~flag;
			updateActionStatus();
			firePropertyChange(CONDITIONS_KEY,
					Integer.valueOf(oldValue), Integer.valueOf(this.flags));
		}
	}

	public void setAllowUpdateActionCommandKey(boolean allowed) {
		allowUpdateActionCommandKey = allowed;
	}

	public boolean isAllowUpdateActionCommandKey() {
		return allowUpdateActionCommandKey;
	}

	protected void updateActionStatus() {
		for(ActionListener listener: actionMap.values()) {
			if(listener instanceof Action) {
				updateActionStatus((Action) listener);
			}
		}
	}

	protected void updateActionStatus(Action action) {
		if(action instanceof UIAction) {
			((UIAction) action).setEnabled(flags);
			return;
		}

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

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Action action = (Action) evt.getSource();
		switch(evt.getPropertyName()) {
		case UIAction.ACTION_REQUIRED_CONDITIONS:
			updateActionStatus(action);
			if(evt.getOldValue() != null && evt.getNewValue() == null) {
				action.setEnabled(true);
			}
			break;
		case Action.ACTION_COMMAND_KEY:
			if(!isAllowUpdateActionCommandKey() ||
					Objects.equals(evt.getOldValue(), evt.getNewValue())) {
				break;
			}

			Object deny;
			deny = action.getValue(UIAction.DENY_UPDATE_ACTION_COMMAND_KEY);
			if(deny != null && (deny instanceof Boolean && (Boolean) deny)) {
				break;
			}

			String oldActCmd = (String) evt.getOldValue();
			if(action.equals(getAction(oldActCmd))) {
				String newActCmd = (String) evt.getNewValue();
				if(newActCmd != null) {
					changeActionCommand(oldActCmd, (String) evt.getNewValue());
				} else {
					removeActionListener(oldActCmd);
				}
			}
			break;
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
			ActionListener act = getActionListener(actCmd);
			if(act != null) {
				act.actionPerformed(e);
				return;
			}
		}
		Log.e("Unknown action command : " + actCmd);
	}

	public Object getData(String key) {
		if(CONDITIONS_KEY.equals(key)) {
			return Integer.valueOf(getFlag());
		}
		return dataMap != null ? dataMap.get(key) : null;
	}

	public void putData(String key, Object newValue) {
		if(CONDITIONS_KEY.equals(key)) {
			setFlag(!(newValue instanceof Integer) ? 0
					: ((Integer) newValue).intValue());
			return;
		}
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

	private String getActionCommand(ActionListener action) {
		if(action == null) return null;

		String actCmd = null;
		if(action instanceof Action) {
			actCmd = (String) ((Action) action)
						.getValue(Action.ACTION_COMMAND_KEY);
		}
		if(actCmd == null) {
			try {
				actCmd = (String) action.getClass().getDeclaredField(
						UIAction.ACTION_COMMAND_FIELD).get(null);
			} catch (Exception e) { }
		}
		if(actCmd == null) actCmd = action.getClass().getName();
		return actCmd;
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
