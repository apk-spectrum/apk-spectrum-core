package com.apkspectrum.resource;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class DefaultResProp implements ResProp<Object> {
    private static JSONObject property;
    private static PropertyChangeSupport pcs;

    static {
        loadProperty();
        if (property == null) property = new JSONObject();
        pcs = new PropertyChangeSupport(property);
    }

    private String value;
    private Object defValue;

    public DefaultResProp(String value) {
        this(value, null);
    }

    public DefaultResProp(String value, Object defValue) {
        this.value = value;
        this.defValue = defValue;
    }

    public void setDefaultValue(Object defValue) {
        this.defValue = defValue;
    }

    public Object getDefaultValue() {
        return defValue;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getString();
    }

    @Override
    public Object get() {
        return getData();
    }

    @Override
    public void set(Object data) {
        setData(data);
    }

    public Object getData() {
        return getPropData(getValue(), getDefaultValue());
    }

    public Object getData(Object ref) {
        Object result = getData();
        return result != null ? result : ref;
    }

    public String getString() {
        Object result = getData();
        return result != null ? String.valueOf(result) : "";
    }

    public int getInt() {
        return getInt(0);
    }

    public int getInt(int ref) {
        Object data = getData(ref);
        if (data == null) return ref;

        int ret = ref;
        if (data instanceof Long) {
            ret = (int) (long) data;
        } else if (data instanceof Integer) {
            ret = (int) data;
        }

        return ret;
    }

    public boolean getBoolean() {
        return getBoolean(false);
    }

    public boolean getBoolean(boolean ref) {
        Object data = getData();
        if (data == null) return ref;

        boolean ret = false;
        if (data instanceof Boolean) {
            ret = (Boolean) data;
        } else if (data instanceof String) {
            ret = "true".equalsIgnoreCase((String) data);
        } else if (data instanceof Integer) {
            ret = (Integer) data != 0;
        } else {
            ret = !String.valueOf(data).isEmpty();
        }

        return ret;
    }

    public void setData(Object data) {
        setPropData(getValue(), data);
    }

    public static Object getPropData(String key) {
        return getPropData(key, null);
    }

    public static Object getPropData(String key, Object ref) {
        Object data = property != null ? property.get(key) : ref;
        return data != null ? data : ref;
    }

    @SuppressWarnings("unchecked")
    public static void setPropData(String key, Object data) {
        Object old = null;
        if (data == null) {
            old = property.remove(key);
            if (old != null) {
                saveProperty();
                pcs.firePropertyChange(key, old, data);
            }
            return;
        }
        old = property.get(key);
        if (!data.equals(old)) {
            property.put(key, data);
            saveProperty();
            pcs.firePropertyChange(key, old, data);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        addPropertyChangeListener(this, l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        removePropertyChangeListener(this, l);
    }

    public static void addPropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(prop != null ? prop.getValue() : null, l);
    }

    public static void removePropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(prop != null ? prop.getValue() : null, l);
    }

    private static void loadProperty() {
        if (property == null) {
            File file = new File(_RFile.ETC_SETTINGS_FILE.getPath());
            if (!file.exists() || file.length() == 0) return;
            try (FileReader fileReader = new FileReader(file)) {
                JSONParser parser = new JSONParser();
                property = (JSONObject) parser.parse(fileReader);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveProperty() {
        if (property == null) return;

        String transMultiLine = property.toJSONString().replaceAll("^\\{(.*)\\}$", "{\n$1\n}")
                .replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",]*)?,)", "$1\n");
        // .replaceAll("(\"[^\"]*\":(\"[^\"]*\")?([^\",\\[]*(\\[[^\\]]\\])?)?,)",
        // "$1\n");

        try (FileWriter fw = new FileWriter(_RFile.ETC_SETTINGS_FILE.getPath());
                BufferedWriter writer = new BufferedWriter(fw)) {
            writer.write(transMultiLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
