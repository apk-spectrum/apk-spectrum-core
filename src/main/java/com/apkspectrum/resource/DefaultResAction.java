package com.apkspectrum.resource;

import java.awt.Dimension;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.Action;
import javax.swing.Icon;

public class DefaultResAction implements ResAction<ResAction<?>>
{
	private ResString<?> text, toolTipText;
	private ResImage<?> image;
	private Icon icon;
	private Dimension iconSize;

	private Collection<Action> actions;

	public DefaultResAction(ResString<?> text) {
		this(text, (Icon) null, null);
	}

	public DefaultResAction(ResString<?> text, ResString<?> toolTipText) {
		this(text, (Icon) null, toolTipText);
	}

	public DefaultResAction(ResString<?> text, ResImage<?> image) {
		this(text, image, null);
	}

	public DefaultResAction(ResString<?> text, Icon icon) {
		this(text, icon, null);
	}

	public DefaultResAction(ResString<?> text, ResImage<?> image,
			ResString<?> toolTipText) {
		this(text, (Icon) null, toolTipText);
		this.image = image;
	}

	public DefaultResAction(ResString<?> text, Icon icon,
			ResString<?> toolTipText) {
		this.text = text;
		this.icon = icon;
		this.toolTipText = toolTipText;

		if(text != null || toolTipText != null) {
			_RStr.addLanguageChangeListener(new LanguageChangeListener() {
				@Override
				public void languageChange(String oldLang, String newLang) {
					if(actions == null || actions.isEmpty()) return;
					for(Action a: actions) applyText(a);
				}
			});
		}
	}

	@Override
	public ResAction<?> get() {
		return this;
	}

	@Override
	public String getText() {
		return text != null ? text.getString() : null;
	}

	@Override
	public Icon getIcon() {
		if(image == null) return icon;
		if(iconSize == null) return image.getImageIcon();
		return image.getImageIcon(iconSize.width, iconSize.height);
	}

	@Override
	public String getToolTipText() {
		return toolTipText != null ? toolTipText.getString() : null;
	}

	@Override
	public void setIconSize(Dimension iconSize) {
		this.iconSize = iconSize;
	}

	@Override
	public void set(Action a) {
		if(a == null) return;

		Object data = getIcon();
		if(data != null) a.putValue(Action.LARGE_ICON_KEY, data);

		if(text != null || toolTipText != null) {
			applyText(a);
			if(actions == null) actions = new HashSet<>();
			if(!actions.contains(a)) actions.add(a);
		}
	}

	private void applyText(Action a) {
		Object data = getText();
		if(data != null) a.putValue(Action.NAME, data);

		data = getToolTipText();
		if(data != null) a.putValue(Action.SHORT_DESCRIPTION, data);
	}
}
