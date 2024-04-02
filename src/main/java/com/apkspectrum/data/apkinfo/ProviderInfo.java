package com.apkspectrum.data.apkinfo;

public class ProviderInfo extends ComponentInfo {
    public String[] authorities = null; // "list"
    public Boolean grantUriPermissions = null;
    public Integer initOrder = null;
    public Boolean multiprocess = null;
    public String process = null; // "string"
    public String readPermission = null; // "string"
    public Boolean syncable = null;
    public String writePermission = null; // "string"

    public GrantUriPermissionInfo[] grantUriPermission = null;
    public PathPermissionInfo[] pathPermission = null;

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
        if (readPermission != null) {
            report.append("readPermission : ").append(readPermission).append("\n");
        }
        if (writePermission != null) {
            report.append("writePermission : ").append(writePermission).append("\n");
        }

        return report.toString();
    }
}
