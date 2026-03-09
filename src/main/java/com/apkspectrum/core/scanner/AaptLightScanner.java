package com.apkspectrum.core.scanner;

import com.apkspectrum.logback.Log;

public class AaptLightScanner extends AaptScanner {

    private boolean isLightMode;

    public AaptLightScanner(StatusListener statusListener) {
        this(statusListener, true);
    }

    public AaptLightScanner(StatusListener statusListener, boolean lightMode) {
        super(statusListener);
        isLightMode = lightMode;
    }

    public void setLightMode(boolean lightMode) {
        if (isLightMode == lightMode) return;
        if (isLightMode && !lightMode && getStatus() == STATUS_ALL_COMPLETED) {
            isLightMode = lightMode;
            startResourceDumpThread();
            readWidgets();
        }
    }

    @Override
    public void openApk(String apkFilePath, String frameworkRes) {
        super.openApk(apkFilePath, frameworkRes);
        stateChanged(STATUS_ALL_COMPLETED);
    }

    @Override
    protected void startResourceDumpThread() {
        if (isLightMode) {
            Log.i("skip resourcesWithValue");
            return;
        }
        super.startResourceDumpThread();
    }

    @Override
    protected void readWidgets() {
        if (isLightMode) {
            Log.i("skip widgets");
            return;
        }
        super.readWidgets();
    }
}
