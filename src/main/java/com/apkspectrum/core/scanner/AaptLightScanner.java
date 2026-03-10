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
    protected void startResourceDumpThread() {
        if (isLightMode) {
            Log.i("skip resourcesWithValue");
            stateChanged(STATUS_RES_DUMP_COMPLETED);
            return;
        }
        super.startResourceDumpThread();
    }

    @Override
    protected void readWidgets() {
        if (isLightMode) {
            Log.i("skip widgets");
            stateChanged(STATUS_WIDGET_COMPLETED);
            return;
        }
        super.readWidgets();
    }

    @Override
    protected void stateChanged(int status) {
        int scanningStatus = 0;
        StatusListener statusListener = null;
        synchronized (this) {
            super.stateChanged(status);
            scanningStatus = this.scanningStatus;
            statusListener = this.statusListener;
        }

        if (statusListener != null) {
            if (status != STATUS_CERT_COMPLETED
                    && (scanningStatus | STATUS_CERT_COMPLETED) == STATUS_ALL_COMPLETED) {
                Log.i("I: light completed... ");
                statusListener.onSuccess();
                statusListener.onCompleted();
            }
        }
    }

    @Override
    public boolean isCompleted(int status) {
        if (status == STATUS_ALL_COMPLETED) {
            status &= ~STATUS_CERT_COMPLETED;
            Log.i("remove STATUS_CERT_COMPLETED");
        }
        return super.isCompleted(status);
    }
}
