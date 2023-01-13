package com.apkspectrum.plugin;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.io.File;

import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.data.apkinfo.ApkInfoHelper;
import com.apkspectrum.data.apkinfo.ResourceInfo;
import com.apkspectrum.plugin.manifest.Component;
import com.apkspectrum.util.FileUtil;

public class PackageSearcherLinker extends AbstractPackageSearcher {

    public PackageSearcherLinker(PlugInPackage pluginPackage, Component component) {
        super(pluginPackage, component);
    }

    @Override
    public boolean trySearch() {
        return true;
    }

    @Override
    public void launch() {
        ApkInfo apkInfo = PlugInManager.getApkInfoByEventSource();
        if (apkInfo == null) return;

        String name = null;
        switch (getSupportType()) {
            case SEARCHER_TYPE_PACKAGE_NAME:
                name = apkInfo.manifest.packageName;
                break;
            case SEARCHER_TYPE_APP_NAME:
                ResourceInfo[] labels = apkInfo.manifest.application.labels;
                name = ApkInfoHelper.getResourceValue(labels, getPreferLangForAppName());
                break;
            case SEARCHER_TYPE_PACKAGE_HASH:
                File apkFile = new File(apkInfo.filePath);
                name = FileUtil.getMessageDigest(apkFile, "SHA-256").replaceAll(":", "");
                break;
        }

        String filter = null;
        try {
            filter = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            filter = name;
        }
        String url = component.url.replaceAll("%[tT][aA][rR][gG][eE][tT]%", filter);
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(new URI(url));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

}
