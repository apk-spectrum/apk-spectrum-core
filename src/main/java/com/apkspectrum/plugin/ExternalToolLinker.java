package com.apkspectrum.plugin;

import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.plugin.manifest.Component;
import com.apkspectrum.util.ConsolCmd;

public class ExternalToolLinker extends AbstractExternalTool {

    public ExternalToolLinker(PlugInPackage pluginPackage, Component component) {
        super(pluginPackage, component);
    }

    @Override
    public void launch() {
        ApkInfo info = PlugInManager.getApkInfoByEventSource();
        if (info == null) return;
        launch(info.filePath);
    }

    @Override
    public void launch(final String src) {
        if (src == null) return;
        Thread t = new Thread(new Runnable() {
            public void run() {
                String tmp = component.param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%",
                        src.replaceAll("\\\\", "\\\\\\\\"));
                ConsolCmd.exec(new String[] {component.path, tmp}, true);
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }

    @Override
    public void launch(final String src1, final String src2) {
        if (src1 == null) return;
        Thread t = new Thread(new Runnable() {
            public void run() {
                String tmp1 = component.param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%",
                        src1.replaceAll("\\\\", "\\\\\\\\"));
                String tmp2 = component.param.replaceAll("%[aA][pP][kK]_[pP][aA][tT][hH]%",
                        src2.replaceAll("\\\\", "\\\\\\\\"));
                ConsolCmd.exec(new String[] {component.path, tmp1, tmp2}, true);
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
}
