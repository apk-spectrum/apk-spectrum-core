package com.apkspectrum.resource;

public enum _RAct implements ResAction<ResAction<?>> {
    // Empty
    ; // ENUM END

    private DefaultResAction res;

    private _RAct(ResString<?> text) {
        res = new DefaultResAction(text);
    }

    private _RAct(ResString<?> text, ResString<?> toolTipText) {
        res = new DefaultResAction(text, toolTipText);
    }

    private _RAct(ResString<?> text, ResImage<?> image) {
        res = new DefaultResAction(text, image);
    }

    private _RAct(ResString<?> text, javax.swing.Icon icon) {
        res = new DefaultResAction(text, icon);
    }

    private _RAct(ResString<?> text, ResImage<?> image, ResString<?> toolTipText) {
        res = new DefaultResAction(text, image, toolTipText);
    }

    private _RAct(ResString<?> text, javax.swing.Icon icon, ResString<?> toolTipText) {
        res = new DefaultResAction(text, icon, toolTipText);
    }

    @Override
    public ResAction<?> get() {
        return res.get();
    }

    @Override
    public String getText() {
        return res.getText();
    }

    @Override
    public javax.swing.Icon getIcon() {
        return res.getIcon();
    }

    @Override
    public String getToolTipText() {
        return res.getToolTipText();
    }

    @Override
    public void setIconSize(java.awt.Dimension iconSize) {
        res.setIconSize(iconSize);
    }

    @Override
    public void set(javax.swing.Action a) {
        res.set(a);
    }

    public static ResAction<?> getResAction(String name) {
        ResAction<?> res = null;
        try {
            res = valueOf(name);
        } catch (Exception e) {
        }
        return res;
    }
}
