package com.apkspectrum.resource;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import com.apkspectrum.swing.ImageScaler;

public class DefaultResImage implements ResImage<Image> {
    private String value;

    public DefaultResImage(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getConfiguration() {
        return null;
    }

    @Override
    public String toString() {
        return getPath();
    }

    @Override
    public String getPath() {
        return getURL().toExternalForm();
    }

    @Override
    public URL getURL() {
        return getClass().getResource("/icons/" + value);
    }

    @Override
    public Image get() {
        return getImage();
    }

    @Override
    public Image getImage() {
        return getImageIcon().getImage();
    }

    @Override
    public Image getImage(int w, int h) {
        return ImageScaler.getScaledImage(getImage(), w, h);
    }

    @Override
    public ImageIcon getImageIcon() {
        return new ImageIcon(getURL());
    }

    @Override
    public ImageIcon getImageIcon(int w, int h) {
        return ImageScaler.getScaledImageIcon(getImage(), w, h);
    }

    @Override
    public URL getResource() {
        return getURL();
    }

    @Override
    public InputStream getResourceAsStream() {
        return getClass().getResourceAsStream("/icons/" + value);
    }

    public static Image getImage(String path) {
        return getImage(_RImg.class.getResource(path));
    }

    public static Image getImage(URL url) {
        return getImageIcon(url).getImage();
    }

    public static Image getImage(String path, int w, int h) {
        return getImage(_RImg.class.getResource(path), w, h);
    }

    public static Image getImage(URL url, int w, int h) {
        return ImageScaler.getScaledImage(getImage(url), w, h);
    }

    public static ImageIcon getImageIcon(String path) {
        return new ImageIcon(_RImg.class.getResource(path));
    }

    public static ImageIcon getImageIcon(URL url) {
        return new ImageIcon(url);
    }

    public static ImageIcon getImageIcon(String path, int w, int h) {
        return getImageIcon(_RImg.class.getResource(path), w, h);
    }

    public static ImageIcon getImageIcon(URL url, int w, int h) {
        return ImageScaler.getScaledImageIcon(getImageIcon(url), w, h);
    }
}
