package com.apkspectrum.data.apkinfo;

public class SupportsScreensInfo {
    public Boolean resizeable = null;
    public Boolean smallScreens = null;
    public Boolean normalScreens = null;
    public Boolean largeScreens = null;
    public Boolean xlargeScreens = null;
    public Boolean anyDensity = null;
    public Integer requiresSmallestWidthDp = null;
    public Integer compatibleWidthLimitDp = null;
    public Integer largestWidthLimitDp = null;

    public String getReport() {
        StringBuilder report = new StringBuilder();

        report.append("supports screens :\n");
        if (resizeable != null) {
            report.append("  resizeable : ").append(resizeable).append("\n");
        }
        if (smallScreens != null) {
            report.append("  smallScreens : ").append(smallScreens).append("\n");
        }
        if (normalScreens != null) {
            report.append("  normalScreens : ").append(normalScreens).append("\n");
        }
        if (largeScreens != null) {
            report.append("  largeScreens : ").append(largeScreens).append("\n");
        }
        if (xlargeScreens != null) {
            report.append("  xlargeScreens : ").append(xlargeScreens).append("\n");
        }
        if (anyDensity != null) {
            report.append("  anyDensity : ").append(anyDensity).append("\n");
        }
        if (requiresSmallestWidthDp != null) {
            report.append("  requiresSmallestWidthDp : ").append(requiresSmallestWidthDp)
                    .append("\n");
        }
        if (compatibleWidthLimitDp != null) {
            report.append("  compatibleWidthLimitDp : ").append(compatibleWidthLimitDp)
                    .append("\n");
        }
        if (largestWidthLimitDp != null) {
            report.append("  largestWidthLimitDp : ").append(largestWidthLimitDp).append("\n");
        }

        return report.toString();
    }
}
