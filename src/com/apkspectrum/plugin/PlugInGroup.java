package com.apkspectrum.plugin;

import java.util.ArrayList;

import com.apkspectrum.plugin.manifest.Component;

public class PlugInGroup extends AbstractPlugIn
{
	public PlugInGroup(PlugInPackage pluginPackage, Component component) {
		super(pluginPackage, component);
	}

	public boolean isTopGroup() {
		String gname = component.pluginGroup;
		return gname == null || gname.trim().isEmpty();
	}

	public PlugInGroup[] getChildrenGroup() {
		if(getName() == null) return null;
		ArrayList<PlugInGroup> list = new ArrayList<>();
		for(PlugInGroup g: pluginPackage.getPlugInGroups()) {
			if(this.equals(g.getParantGroup())) {
				list.add(g);
			}
		}
		return list.toArray(new PlugInGroup[list.size()]);
	}

	public PlugIn[] getPlugIn() {
		if(getName() == null) return null;
		ArrayList<PlugIn> list = new ArrayList<>();
		for(PlugIn g: pluginPackage.getPlugIn(PLUGIN_TPYE_ALL)) {
			if(this.equals(g.getParantGroup())) {
				list.add(g);
			}
		}
		return list.toArray(new PlugIn[list.size()]);
	}
}
