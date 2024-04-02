package com.apkspectrum.data.apkinfo;

public class ActivityInfo extends ComponentInfo {
    public Boolean allowEmbedded = null;
    public Boolean allowTaskReparenting = null;
    public Boolean alwaysRetainTaskState = null;
    public Boolean autoRemoveFromRecents = null;
    public ResourceInfo[] banners = null; // "drawable resource"
    public Boolean clearTaskOnLaunch = null;
    public String configChanges = null; /*["mcc", "mnc", "locale",
                                    "touchscreen", "keyboard", "keyboardHidden",
                                    "navigation", "screenLayout", "fontScale",
                                    "uiMode", "orientation", "screenSize",
                                    "smallestScreenSize"] */
    public String documentLaunchMode = null; /*["intoExisting" | "always" |
                                      "none" | "never"] */
    public Boolean excludeFromRecents = null;
    public Boolean finishOnTaskLaunch = null;
    public Boolean hardwareAccelerated = null;
    public String launchMode = null; /* ["multiple" | "singleTop" |
                                  "singleTask" | "singleInstance"]*/
    public Integer maxRecents = null;
    public Boolean multiprocess = null;
    public Boolean noHistory = null;  
    public String parentActivityName = null; //"string" 
    public String process = null; //"string"
    public Boolean relinquishTaskIdentity = null;
    public String screenOrientation = null; /*["unspecified" | "behind" |
                                        "landscape" | "portrait" |
                                        "reverseLandscape" | "reversePortrait" |
                                        "sensorLandscape" | "sensorPortrait" |
                                        "userLandscape" | "userPortrait" |
                                        "sensor" | "fullSensor" | "nosensor" |
                                        "user" | "fullUser" | "locked"] */
    public Boolean stateNotNeeded = null;
    public String taskAffinity = null; //"string"
    public ResourceInfo[] themes = null; // "resource or theme"
    public String uiOptions = null; // ["none" | "splitActionBarWhenNarrow"]
    public String windowSoftInputMode = null; /*["stateUnspecified",
                                    "stateUnchanged", "stateHidden",
                                    "stateAlwaysHidden", "stateVisible",
                                    "stateAlwaysVisible", "adjustUnspecified",
                                    "adjustResize", "adjustPan"] */

    public String getReport() {
        StringBuilder report = new StringBuilder();
        report.append("name : " + name + "\n");
        if (enabled != null) {
            report.append("enabled : ").append(enabled).append("\n");
        }
        if (exported != null) {
            report.append("exported : ").append(exported).append("\n");
        }
        if (permission != null) {
            report.append("permission : ").append(permission).append("\n");
        }

        if (intentFilter != null) {
            report.append("\nintent-filter count : ")
                  .append(intentFilter.length).append("\n");
            for (IntentFilterInfo info: intentFilter) {
                report.append("intent-filter : \n");
                if (info.ation != null) {
                    for (ActionInfo a: info.ation) {
                        report.append("  ").append(a.name).append("\n");
                    }
                }
                if (info.category != null) {
                    for (CategoryInfo c: info.category) {
                        report.append("  ").append(c.name).append("\n");
                    }
                }
            }
        }

        return report.toString();
    }
}
