package com.apkspectrum.plugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.apkspectrum.plugin.manifest.Component;
import com.apkspectrum.resource.LanguageChangeListener;
import com.apkspectrum.resource._RConst;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.swing.AbstractUIAction;
import com.apkspectrum.swing.ImageScaler;

public abstract class AbstractPlugIn implements PlugIn
{
	protected PlugInPackage pluginPackage;
	protected Component component;
	protected boolean enabled;

	private PropertyChangeSupport pcs;

	public AbstractPlugIn(PlugInPackage pluginPackage, Component component) {
		this.pluginPackage = pluginPackage;
		this.component = component;
		this.enabled = component != null ? component.enabled : false;
	}

	@Override
	public PlugInPackage getPlugInPackage() {
		return pluginPackage;
	}

	@Override
	public PlugInConfig getPlugInConfig() {
		return new PlugInConfig(pluginPackage);
	}

	@Override
	public String getPackageName() {
		return pluginPackage.getPackageName();
	}

	protected String trimAndEmptyIsNull(String str) {
		if(str == null || (str = str.trim()).isEmpty()) return null;
		return str;
	}

	@Override
	public String getName() {
		String name = trimAndEmptyIsNull(component.name);
		if(name != null && name.startsWith(".")) {
			name = getPackageName() + name;
		}
		return name;
	}

	@Override
	public String getGroupName() {
		String name = trimAndEmptyIsNull(component.pluginGroup);
		if(name != null && name.startsWith(".")) {
			name = getPackageName() + name;
		}
		return name;
	}

	@Override
	public URL getIconURL() {
		if(component.icon != null) {
			try {
				URI uri = pluginPackage.getResourceUri(component.icon);
				return uri != null ? uri.toURL() : pluginPackage.getIconURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Icon getIcon() {
		URL url = getIconURL();
		if(url == null) return null;
		return new ImageIcon(url);
	}

	@Override
	public Icon getIcon(Dimension size) {
		return getIcon(size.width, size.height);
	}

	@Override
	public Icon getIcon(int w, int h) {
		URL url = getIconURL();
		if(url == null) return null;

		return ImageScaler.getScaledImageIcon(new ImageIcon(url), w, h);
	}

	@Override
	public String getLabel() {
		String label = pluginPackage.getResourceString(component.label);
		return label != null ? label : pluginPackage.getLabel();
	}

	@Override
	public String getDescription() {
		String desc = pluginPackage.getResourceString(component.description);
		return desc != null ? desc : pluginPackage.getDescription();
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(this.enabled == enabled) return;
		this.enabled = enabled;
		firePropertyChange(ENABLED, !enabled, enabled);
	}

	@Override
	public boolean isEnabled() {
		return isEnabled(true);
	}

	@Override
	public boolean isEnabled(boolean inheritance) {
		boolean enabled = this.enabled;
		if(enabled && inheritance) {
			enabled = pluginPackage.isEnabled();
			if(enabled) {
				PlugIn parent = getParantGroup();
				enabled = (parent == null) || parent.isEnabled();
			}
		}
		return enabled;
	}

	@Override
	public int getType() {
		if(this instanceof PackageSearcher) {
			return PLUGIN_TPYE_PACKAGE_SEARCHER;
		}
		if(this instanceof UpdateChecker) {
			return PLUGIN_TPYE_UPDATE_CHECKER;
		}
		if(this instanceof ExternalTool) {
			return PLUGIN_TPYE_EXTERNAL_TOOL;
		}
		if(this instanceof ExtraComponent) {
			return PLUGIN_TPYE_EXTRA_COMPONENT;
		}
		return PLUGIN_TPYE_UNKNOWN;
	}

	@Override
	public PlugInGroup getParantGroup() {
		if(pluginPackage == null) return null;
		return pluginPackage.getPlugInGroup(component.pluginGroup);
	}

	@Override
	public String getActionCommand() {
		String typeName = null;
		switch(getType()) {
		case PLUGIN_TPYE_PACKAGE_SEARCHER:
			typeName = PackageSearcher.class.getSimpleName()
						+ "#" + ((PackageSearcher)this).getSupportType();
			break;
		case PLUGIN_TPYE_UPDATE_CHECKER:
			typeName = UpdateChecker.class.getSimpleName();
			break;
		case PLUGIN_TPYE_EXTERNAL_TOOL:
			typeName = ExternalTool.class.getSimpleName()
						+ "#" + ((ExternalTool)this).getToolType();
			break;
		default:
			typeName = PlugIn.class.getName();
			break;
		}
		return getPackageName() + "!" + typeName + "@" + getName()
					+ "[0x" + Integer.toHexString(component.hashCode()) + "]";
	}

	@Override
	public Action makeAction() {
		return makeAction(this instanceof ActionListener ? (ActionListener) this
														: null);
	}

	@SuppressWarnings("serial")
	protected Action makeAction(final ActionListener listener) {
		final Action action;
		if(listener instanceof Action) {
			action = (Action) listener;
		} else {
			action = new AbstractUIAction() {
				static final String POSITION = _RConst.POSITION_KEY;

				@Override
				public void actionPerformed(ActionEvent e) {
					Object posision = getValue(POSITION);
					if(posision != null
							&& e.getSource() instanceof JComponent) {
						JComponent comp = (JComponent) e.getSource();
						if(comp.getClientProperty(POSITION) == null) {
							comp.putClientProperty(POSITION, posision);
						}
					}
					if(listener != null) listener.actionPerformed(e);
					else launch();
				}
			};
		}
		action.putValue(Action.ACTION_COMMAND_KEY, getActionCommand());
		action.putValue(Action.LARGE_ICON_KEY, getIcon());
		action.setEnabled(isEnabled());
		boolean hasTextRes = applyText(action);

		addPropertyChangeListener(ENABLED, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				action.setEnabled(enabled);
			}
		});

		if(hasTextRes) {
			_RStr.addLanguageChangeListener(new LanguageChangeListener() {
				@Override
				public void languageChange(String oldLang, String newLang) {
					applyText(action);
				}
			});
		}

		return action;
	}

	private boolean applyText(Action action) {
		String label = getLabel();
		String desc = getDescription();
		if(label != null) action.putValue(Action.NAME, label);
		if(desc != null) action.putValue(Action.SHORT_DESCRIPTION, desc);
		return label != null || desc != null;
	}

	@Override
	public Map<String, Object> getChangedProperties() {
		HashMap<String, Object> data = new HashMap<>();
		if(component.enabled != isEnabled(false)) {
			if(!(this instanceof ExternalTool)
					|| ((ExternalTool)this).isSupoortedOS()) {
				data.put(ENABLED, isEnabled(false));
			}
		}
		return data;
	}

	@Override
	public void restoreProperties(Map<?, ?> data) {
		if(data == null) return;
		if(data.containsKey(ENABLED)) {
			setEnabled((boolean)data.get(ENABLED));
		}
	}

	@Override
	public int hashCode() {
		return component.hashCode();
	}

	protected void firePropertyChange(String propertyName,
			Object oldValue, Object newValue) {
		if(pcs == null) return;
		pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

	@Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    	if(listener == null) return;
    	if(pcs == null) pcs = new PropertyChangeSupport(this);
    	else pcs.removePropertyChangeListener(listener);
    	pcs.addPropertyChangeListener(listener);
    }

	@Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    	if(pcs == null) return;
    	pcs.removePropertyChangeListener(listener);
    }

	@Override
    public void addPropertyChangeListener(String prop,
    		PropertyChangeListener listener) {
    	if(prop == null || listener == null) return;
    	if(pcs == null) pcs = new PropertyChangeSupport(this);
    	pcs.addPropertyChangeListener(prop, listener);
    }

	@Override
    public void removePropertyChangeListener(String prop,
    		PropertyChangeListener listener) {
    	if(pcs == null) return;
    	pcs.removePropertyChangeListener(prop, listener);
    	if(pcs.getPropertyChangeListeners().length == 0) pcs = null;
    }
}
