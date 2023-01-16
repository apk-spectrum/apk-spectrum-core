package com.apkspectrum.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import com.apkspectrum.core.scanner.ApkScanner;
import com.apkspectrum.data.apkinfo.ApkInfo;
import com.apkspectrum.logback.Log;
import com.apkspectrum.resource.ResAction;
import com.apkspectrum.resource._RConst;

public class ApkActionEventHandler extends ActionEventHandler {
    public ApkActionEventHandler() {
        super();
    }

    public ApkActionEventHandler(Package actionPackage) {
        super(actionPackage);
    }

    public ApkActionEventHandler(Package actionPackage,
            Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
        super(actionPackage, actResEnum);
    }

    public ApkActionEventHandler(String packageName) {
        super(packageName);
    }

    public ApkActionEventHandler(String packageName,
            Class<? extends Enum<? extends ResAction<?>>> actResEnum) {
        super(packageName, actResEnum);
    }

    public ApkScanner getApkScanner() {
        Object source = getData(_RConst.APK_SCANNER_KEY);
        return source instanceof ApkScanner ? (ApkScanner) source : null;
    }

    public ApkScanner getApkScanner(int position) {
        if (position == -1) return getApkScanner();

        Object source = getData(_RConst.MULTI_APK_SCANNER_KEY);
        if (!(source instanceof ApkScanner[])) return null;

        ApkScanner[] multiSacnner = (ApkScanner[]) source;
        if (multiSacnner.length == 0 || multiSacnner.length <= position) {
            return null;
        }

        return multiSacnner[position];
    }

    public ApkInfo getApkInfo() {
        ApkScanner scanner = getApkScanner();
        return scanner != null ? scanner.getApkInfo() : null;
    }

    public ApkInfo getApkInfo(int position) {
        ApkScanner scanner = getApkScanner(position);
        return scanner != null ? scanner.getApkInfo() : null;
    }

    public ApkScanner getApkScannerByEventSource() {
        return getApkScannerByEventSource(null);
    }

    public ApkScanner getApkScannerByEventSource(EventObject e) {
        Object source = getData(_RConst.APK_SCANNER_KEY);
        if (source instanceof ApkScanner) {
            return (ApkScanner) source;
        }

        source = getData(_RConst.MULTI_APK_SCANNER_KEY);
        if (!(source instanceof ApkScanner[])) return null;

        ApkScanner[] multiSacnner = (ApkScanner[]) source;
        if (multiSacnner.length == 0) return null;

        source = null;
        if (e == null) e = EventQueue.getCurrentEvent();
        if (e != null) source = e.getSource();

        if (source instanceof KeyStrokeAction) {
            source = ((KeyStrokeAction) source).getComponent();
        }
        if (!(source instanceof Component)) return null;

        int position = -1;
        Component parent = (Component) source;
        while (parent != null) {
            if (parent instanceof JComponent) {
                JComponent comp = (JComponent) parent;
                Integer pos;
                pos = (Integer) comp.getClientProperty(_RConst.POSITION_KEY);
                if (pos != null) {
                    position = pos.intValue();
                    break;
                }
            }
            if (position == -1 && parent instanceof JSplitPane) {
                Component c = ((JSplitPane) parent).getLeftComponent();
                if (source.equals(c) || (c instanceof Container
                        && ((Container) c).isAncestorOf((Component) source))) {
                    position = 0;
                    break;
                }
                c = ((JSplitPane) parent).getRightComponent();
                if (source.equals(c) || (c instanceof Container
                        && ((Container) c).isAncestorOf((Component) source))) {
                    position = 1;
                    break;
                }
            }
            source = parent;
            parent = parent.getParent();
        }

        if (position == -1 || multiSacnner.length <= position || multiSacnner[position] == null) {
            Log.e("Unknown position or null : " + position);
            return null;
        }

        return multiSacnner[position];
    }

    public ApkInfo getApkInfoByEventSource() {
        return getApkInfoByEventSource(null);
    }

    public ApkInfo getApkInfoByEventSource(EventObject e) {
        ApkScanner scanner = getApkScannerByEventSource(e);
        return scanner != null ? scanner.getApkInfo() : null;
    }
}
