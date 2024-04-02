package com.apkspectrum.data.apkinfo;

public class ActivityAliasInfo extends ComponentInfo {
    public String targetActivity = null; // "string"

    public String getReport() {
        StringBuilder report = new StringBuilder();
        report.append("name : ").append(name).append("\n");
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
            report.append("\nintent-filter count : ").append(intentFilter.length).append("\n");
            for (IntentFilterInfo info : intentFilter) {
                report.append("intent-filter : \n");
                if (info.ation != null) {
                    for (ActionInfo a : info.ation) {
                        report.append("  ").append(a.name).append("\n");
                    }
                }
                if (info.category != null) {
                    for (CategoryInfo c : info.category) {
                        report.append("  ").append(c.name).append("\n");
                    }
                }
            }
        }

        return report.toString();
    }
}
