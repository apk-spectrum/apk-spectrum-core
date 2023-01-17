package com.apkspectrum.data.apkinfo;

public class UsesConfigurationInfo {
    public Boolean reqFiveWayNav = null;
    public Boolean reqHardKeyboard = null;

    // ["undefined" | "nokeys" | "qwerty" | "twelvekey"]
    public String reqKeyboardType = null;

    // ["undefined" | "nonav" | "dpad" | "trackball" | "wheel"]
    public String reqNavigation = null;

    // ["undefined" | "notouch" | "stylus" | "finger"]
    public String reqTouchScreen = null;

    public String getReport() {
        StringBuilder report = new StringBuilder();

        report.append("uses configuration : \n");
        if (reqFiveWayNav != null) {
            report.append("  reqFiveWayNav : ").append(reqFiveWayNav);
        }
        report.append("\n");
        if (reqHardKeyboard != null) {
            report.append("  reqHardKeyboard : ").append(reqHardKeyboard);
        }
        report.append("\n");
        if (reqKeyboardType != null) {
            report.append("  reqKeyboardType : ").append(reqKeyboardType);
        }
        report.append("\n");
        if (reqNavigation != null) {
            report.append("  reqNavigation : ").append(reqNavigation);
        }
        report.append("\n");
        if (reqTouchScreen != null) {
            report.append("  reqTouchScreen : ").append(reqTouchScreen);
        }
        report.append("\n");

        return report.toString();
    }
}
