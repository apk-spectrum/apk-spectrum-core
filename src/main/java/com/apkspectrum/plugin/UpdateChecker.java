package com.apkspectrum.plugin;

import java.util.Map;

public interface UpdateChecker extends PlugIn {
    public static final String PERIOD_PROPERTY = "period";
    public static final String LAST_UPDATE_CHECKED_PROPERTY = "lastUpdateDate";

    public static final int TYPE_LAUNCH_OPEN_LINK = 0;
    public static final int TYPE_LAUNCH_DIRECT_UPDATE = 1;
    public static final int TYPE_LAUNCH_DOWNLOAD = 2;

    public static final int STATUS_NO_UPDATED = 0;
    public static final int STATUS_UPDATE_CHEKCING = 1;
    public static final int STATUS_HAS_NEW_UPDATED = 2;
    public static final int STATUS_UPDATING = 3;
    public static final int STATUS_UPDATE_COMPLETED = 4;
    public static final int STATUS_ERROR_OCCURED = 5;

    public interface StateChangeListener {
        public void stateChanged(UpdateChecker plugin, int state);
    }

    public boolean checkNewVersion() throws NetworkException;

    public boolean hasNewVersion();

    public NetworkException getLastNetworkException();

    public long getPeriod();

    public void setPeriod(long period);

    public long getLastUpdateDate();

    public void setLastUpdateDate(long lastUpdateDate);

    public Map<?, ?> getLatestVersionInfo();

    public void setLatestVersionInfo(Map<?, ?> latestVersionInfo);

    public boolean wasPeriodPassed();

    public String getTargetPackageName();

    public int getLaunchType();

    public int getState();

    public void addStateChangeListener(StateChangeListener listener);

    public void removeStateChangeListener(StateChangeListener listener);
}
