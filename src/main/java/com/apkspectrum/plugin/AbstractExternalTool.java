package com.apkspectrum.plugin;

import com.apkspectrum.plugin.manifest.Component;
import com.apkspectrum.util.SystemUtil;

public abstract class AbstractExternalTool extends AbstractPlugIn implements ExternalTool {
    public AbstractExternalTool(PlugInPackage pluginPackage, Component component) {
        super(pluginPackage, component);
    }

    @Override
    public String getToolType() {
        if (isNormalTool()) return TYPE_NORMAL_TOOL;
        return component.like;
    }

    @Override
    public boolean isDecorderTool() {
        return TYPE_DECORDER_TOOL.equals(component.like);
    }

    @Override
    public boolean isDiffTool() {
        return TYPE_DIFF_TOOL.equals(component.like);
    }

    @Override
    public boolean isNormalTool() {
        return component.like == null || (!isDecorderTool() && !isDiffTool());
    }

    @Override
    public boolean isSupoortedOS() {
        String supportedOS = component.supportedOS;
        return supportedOS == null || supportedOS.isEmpty()
                || (SystemUtil.isWindows() && "windows".equals(supportedOS))
                || (SystemUtil.isLinux() && "linux".equals(supportedOS))
                || (SystemUtil.isMac() && "darwin".equals(supportedOS));
    }

    @Override
    public boolean isEnabled() {
        return isSupoortedOS() && super.isEnabled();
    }
}
