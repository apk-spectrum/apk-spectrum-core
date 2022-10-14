package com.apkspectrum.plugin;

public interface PlugInEventListener
{
	public void onPluginLoaded();
	public void onPluginUpdated(UpdateChecker[] list);
}
