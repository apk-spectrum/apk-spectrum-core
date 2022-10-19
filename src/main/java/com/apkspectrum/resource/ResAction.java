package com.apkspectrum.resource;

import java.awt.Dimension;

import javax.swing.Action;
import javax.swing.Icon;

public interface ResAction<T> extends Res<T>
{
	public String getText();
	public Icon getIcon();
	public String getToolTipText();
	public void setIconSize(Dimension iconSize);

	public void set(Action c);
}
