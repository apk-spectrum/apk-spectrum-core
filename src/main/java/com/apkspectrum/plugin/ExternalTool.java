package com.apkspectrum.plugin;

public interface ExternalTool extends PlugIn {
    public static final String TYPE_NORMAL_TOOL = "normal";
    public static final String TYPE_DECORDER_TOOL = "decorder";
    public static final String TYPE_DIFF_TOOL = "difftool";
    public static final String TYPE_UNKNOWN = "unknown";

    public String getToolType();

    public boolean isDecorderTool();

    public boolean isDiffTool();

    public boolean isNormalTool();

    public boolean isSupoortedOS();

    public void launch(String src);

    public void launch(String src1, String src2);
}
