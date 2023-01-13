package com.apkspectrum.resource;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

public interface ResComp<T> extends Res<T> {
    public static final String RCOMP_SET_TEXT_KEY = "RcompApplyText";
    public static final String RCOMP_SET_ICON_KEY = "RcompApplyIcon";

    public String getText();

    public Icon getIcon();

    public String getToolTipText();

    public void setIconSize(Dimension iconSize);

    public void set(Component c);
}
