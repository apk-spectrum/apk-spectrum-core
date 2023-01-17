package com.apkspectrum.plugin;

public abstract class PlugInEventAdapter implements PlugInEventListener {
    @Override
    public void onPluginLoaded() {}

    @Override
    public void onPluginUpdated(UpdateChecker[] list) {}
}
