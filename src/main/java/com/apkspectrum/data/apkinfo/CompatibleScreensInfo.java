package com.apkspectrum.data.apkinfo;

public class CompatibleScreensInfo {
    public static class Screen {
        // ["small" | "normal" | "large" | "xlarge"]
        public String screenSize = null;

        // ["ldpi" | "mdpi" | "hdpi" | "xhdpi" | "280" | "360" | "420" | "480" | "560" ]
        public String screenDensity = null;
    }

    public Screen[] screen = null;

    public String getReport() {
        StringBuilder report = new StringBuilder();

        report.append("compatible screens :\n");
        for (Screen s : screen) {
            if (s.screenSize != null) {
                report.append("  screenSize : " + s.screenSize);
                if (s.screenDensity != null) {
                    report.append(", screenDensity : " + s.screenDensity + "\n");
                }
            } else if (s.screenDensity != null) {
                report.append("   screenDensity : " + s.screenDensity + "\n");
            }
        }

        return report.toString();
    }
}
