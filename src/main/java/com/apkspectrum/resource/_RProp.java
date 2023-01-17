package com.apkspectrum.resource;

import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.UIManager;

import com.apkspectrum.util.SystemUtil;

public enum _RProp implements ResProp<Object> {
    LANGUAGE                    (SystemUtil.getUserLanguage()),
    USE_EASY_UI                 (false),
    SKIP_STARTUP_EASY_UI_DLG    (false),
    EDITOR,                     /* see getDefualtValue() */
    ADB_PATH                    (""),
    ADB_POLICY_SHARED           (true),
    ADB_DEVICE_MONITORING       (true),
    TRY_UNLOCK_AF_LAUNCH        (true),
    LAUNCH_AF_INSTALLED         (true),
    FRAMEWORK_RES               (""),
    LAST_FILE_OPEN_PATH         (""),
    LAST_FILE_SAVE_PATH         (""),
    CURRENT_THEME               (UIManager.getSystemLookAndFeelClassName()),
    SAVE_WINDOW_SIZE            (false),
    PREFERRED_LANGUAGE,         /* see getDefualtValue() */
    PRINT_MULTILINE_ATTR        (true),
    ; // ENUM END

    public enum B implements ResProp<Boolean> {
        USE_EASY_UI,
        SKIP_STARTUP_EASY_UI_DLG,
        ADB_POLICY_SHARED,
        ADB_DEVICE_MONITORING,
        TRY_UNLOCK_AF_LAUNCH,
        LAUNCH_AF_INSTALLED,
        SAVE_WINDOW_SIZE,
        PRINT_MULTILINE_ATTR,
        ; // ENUM END

        private B() {}

        private B(boolean defValue) {
            getProp(name()).setDefaultValue(Boolean.valueOf(defValue));
        }

        @Override
        public Boolean get() {
            return getProp(name()).getBoolean();
        }

        @Override
        public void set(Boolean data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    public enum S implements ResProp<String> {
        LANGUAGE,
        EDITOR,
        ADB_PATH,
        FRAMEWORK_RES,
        LAST_FILE_OPEN_PATH,
        LAST_FILE_SAVE_PATH,
        CURRENT_THEME,
        PREFERRED_LANGUAGE,
        ; // ENUM END

        private S() {}

        private S(String defValue) {
            getProp(name()).setDefaultValue(defValue);
        }

        @Override
        public String get() {
            return getProp(name()).getString();
        }

        @Override
        public void set(String data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    public enum I implements ResProp<Integer> {
        // EMPTY
        ; // ENUM END

        private I() {}

        private I(int defValue) {
            getProp(name()).setDefaultValue(Integer.valueOf(defValue));
        }

        @Override
        public Integer get() {
            return getProp(name()).getInt();
        }

        @Override
        public void set(Integer data) {
            getProp(name()).setData(data);
        }

        @Override
        public String getValue() {
            return getProp(name()).getValue();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            getProp(name()).removePropertyChangeListener(l);
        }
    }

    private DefaultResProp res;
    private static Map<String, DefaultResProp> others;

    private _RProp() {
        res = new DefaultResProp(name(), getDefaultValue());
    }

    private _RProp(Object defValue) {
        res = new DefaultResProp(name(), defValue);
    }

    public Object getDefaultValue() {
        Object obj = res != null ? res.getDefaultValue() : null;
        if (obj != null) return obj;

        switch (name()) {
            case "EDITOR":
                try {
                    obj = SystemUtil.getDefaultEditor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "PREFERRED_LANGUAGE":
                String propPreferredLanguage = SystemUtil.getUserLanguage();
                String propStrLanguage = (String) LANGUAGE.getData();
                if (!propPreferredLanguage.equals(propStrLanguage)
                        && !"en".equals(propPreferredLanguage)) {
                    propPreferredLanguage +=
                            ";" + (propStrLanguage.isEmpty() ? "en" : propStrLanguage);
                }
                obj = propPreferredLanguage + ";";
                break;
            default:
                break;
        };

        if (res != null && obj != null) res.setDefaultValue(obj);
        return obj;
    }

    @Override
    public String getValue() {
        return res.getValue();
    }

    @Override
    public String toString() {
        return res.toString();
    }

    @Override
    public Object get() {
        return res.get();
    }

    @Override
    public void set(Object data) {
        res.set(data);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        res.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        res.removePropertyChangeListener(l);
    }

    public Object getData() {
        return res.getData();
    }

    public Object getData(Object ref) {
        return res.getData(ref);
    }

    public String getString() {
        return res.getString();
    }

    public int getInt() {
        return res.getInt();
    }

    public int getInt(int ref) {
        return res.getInt(ref);
    }

    public boolean getBoolean() {
        return res.getBoolean();
    }

    public boolean getBoolean(boolean ref) {
        return res.getBoolean();
    }

    public void setData(Object data) {
        res.setData(data);
    }

    public DefaultResProp getProp() {
        return res;
    }

    public static DefaultResProp getProp(String name) {
        _RProp prop = null;
        try {
            prop = valueOf(name);
        } catch (Exception e) {
        }

        DefaultResProp res;
        if (prop != null) {
            res = prop.res;
        } else {
            if (others == null) others = new HashMap<>();
            res = others.get(name);
            if (res == null) {
                others.put(name, res = new DefaultResProp(name));
            }
        }
        return res;
    }

    public static Object getPropData(String key) {
        return DefaultResProp.getPropData(key);
    }

    public static Object getPropData(String key, Object ref) {
        return DefaultResProp.getPropData(key, ref);
    }

    public static void setPropData(String key, Object data) {
        DefaultResProp.setPropData(key, data);
    }

    public static void addPropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        DefaultResProp.addPropertyChangeListener(prop, l);
    }

    public static void removePropertyChangeListener(ResProp<?> prop, PropertyChangeListener l) {
        DefaultResProp.removePropertyChangeListener(prop, l);
    }
}
