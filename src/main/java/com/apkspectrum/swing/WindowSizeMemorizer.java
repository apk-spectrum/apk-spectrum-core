package com.apkspectrum.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import com.apkspectrum.logback.Log;
import com.apkspectrum.resource.ResProp;
import com.apkspectrum.resource._RProp;

public class WindowSizeMemorizer implements ComponentListener, WindowListener {
    static public final int MEMORIZE_TYPE_WINDOWN_CLOSED = 0x01;
    static public final int MEMORIZE_TYPE_WINDOWN_RESIZED = 0x02;
    static public final int MEMORIZE_TYPE_AUTO_UNREGISTE = 0x80;

    static private List<WindowSizeMemorizer> components = new ArrayList<>();

    private static ResProp<Boolean> propEnabled;
    private static PropertyChangeListener propListener;
    private static boolean enabled;
    static {
        setEnabled(_RProp.B.SAVE_WINDOW_SIZE);
    }

    private Component component;
    private String id;
    private int flag;

    public static void setEnabled(boolean enabled) {
        if (propEnabled != null) {
            propEnabled.set(enabled);
        } else {
            WindowSizeMemorizer.enabled = enabled;
        }
    }

    public static void setEnabled(ResProp<Boolean> prop) {
        if (prop == null) return;

        if (propEnabled != null && propListener != null) {
            propEnabled.removePropertyChangeListener(propListener);
        }
        propEnabled = prop;
        enabled = propEnabled.get();

        if (propListener == null) {
            propListener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    enabled = (boolean) evt.getNewValue();
                }
            };
        }
        propEnabled.addPropertyChangeListener(propListener);
    }

    static public void registeComponent(Component component) {
        registeComponent(component, MEMORIZE_TYPE_WINDOWN_CLOSED | MEMORIZE_TYPE_AUTO_UNREGISTE);
    }

    static public void registeComponent(Component component, int flag) {
        registeComponent(component, null, flag);
    }

    static public void registeComponent(Component component, String id) {
        registeComponent(component, id,
                MEMORIZE_TYPE_WINDOWN_CLOSED | MEMORIZE_TYPE_AUTO_UNREGISTE);
    }

    static public void registeComponent(Component component, String id, int flag) {
        if (component == null || flag == 0) {
            Log.e("component is null or flag is 0");
            return;
        }
        if ((flag & MEMORIZE_TYPE_WINDOWN_CLOSED) != 0 && !(component instanceof Window)) {
            Log.e("MEMORIZE_TYPE_WINDOWN_CLOSED flag to be set possible only window component");
            return;
        }
        synchronized (components) {
            components.add(new WindowSizeMemorizer(component, id, flag));
        }
    }

    static public void unregisteComponent(Component component) {
        unregisteComponent(component, null);
    }

    static public void unregisteComponent(Component component, String id) {
        synchronized (components) {
            for (WindowSizeMemorizer c : components) {
                if (c.component.equals(component)
                        && ((id != null && id.equals(c.id)) || (id == null && c.id == null))) {
                    if ((c.flag & MEMORIZE_TYPE_WINDOWN_CLOSED) != 0) {
                        if (component instanceof Window) {
                            ((Window) component).removeWindowListener(c);
                        } else {
                            Log.e("MEMORIZE_TYPE_WINDOWN_CLOSED flag to be set possible only window component");
                        }
                    } else if ((c.flag & MEMORIZE_TYPE_WINDOWN_RESIZED) != 0) {
                        component.removeComponentListener(c);
                    } else {
                        Log.e("Unknown type : " + c.flag);
                    }
                }
            }
        }
    }

    public WindowSizeMemorizer(Component component, String id, int flag) {
        this.component = component;
        this.id = id;
        this.flag = flag;

        if ((flag & MEMORIZE_TYPE_WINDOWN_CLOSED) != 0) {
            if (component instanceof Window) {
                ((Window) component).addWindowListener(this);
            } else {
                Log.e("MEMORIZE_TYPE_WINDOWN_CLOSED flag to be set possible only window component");
            }
        } else if ((flag & MEMORIZE_TYPE_WINDOWN_RESIZED) != 0) {
            component.addComponentListener(this);
        } else {
            Log.e("Unknown type : " + flag);
        }
    }

    static public Dimension getCompoentSize(Component component) {
        return getCompoentSize(component, null, null);
    }

    static public Dimension getCompoentSize(Component component, String id) {
        return getCompoentSize(component, id, null);
    }

    static public Dimension getCompoentSize(Component component, Dimension defaultSize) {
        return getCompoentSize(component, null, defaultSize);
    }

    static public Dimension getCompoentSize(Component component, String id, Dimension defaultSize) {
        if (component == null || !enabled) return defaultSize;
        int width = defaultSize != null ? (int) defaultSize.getWidth() : component.getWidth();
        int height = defaultSize != null ? (int) defaultSize.getHeight() : component.getHeight();
        String key = "ws_" + component.getClass().getName() + (id != null ? "#" + id : "");

        Object data = _RProp.getPropData(key + "_width");
        if (data instanceof Long) {
            width = (int) (long) data;
        } else if (data instanceof Integer) {
            width = (int) data;
        }

        data = _RProp.getPropData(key + "_height");
        if (data instanceof Long) {
            height = (int) (long) data;
        } else if (data instanceof Integer) {
            height = (int) data;
        }

        return width == -1 && height == -1 ? null : new Dimension(width, height);
    }

    static public int getCompoentState(Component component) {
        return getCompoentState(component, null);
    }

    static public int getCompoentState(Component component, String id) {
        String key = "ws_" + component.getClass().getName() + (id != null ? "#" + id : "");
        int state = Frame.NORMAL;
        if (component instanceof Frame) {
            Object data = _RProp.getPropData(key + "_state");
            if (data instanceof Long) {
                state = (int) (long) data;
            } else if (data instanceof Integer) {
                state = (int) data;
            }
        }
        return state;
    }

    static public void resizeCompoent(Component component) {
        resizeCompoent(component, null, null);
    }

    static public void resizeCompoent(Component component, String id) {
        resizeCompoent(component, id, null);
    }

    static public void resizeCompoent(Component component, Dimension defaultSize) {
        resizeCompoent(component, null, defaultSize);
    }

    static public void resizeCompoent(Component component, String id, Dimension defaultSize) {
        Dimension size = getCompoentSize(component, id, defaultSize);
        if (size != null) {
            component.setSize(size);
        }
        if (component instanceof Frame) {
            int state = getCompoentState(component, id);
            ((Frame) component).setExtendedState(state);
        }
    }

    static public void apply(Component component) {
        apply(component, null, null);
    }

    static public void apply(Component component, String id) {
        apply(component, id, null);
    }

    static public void apply(Component component, Dimension defaultSize) {
        apply(component, null, defaultSize);
    }

    static public void apply(Component component, String id, Dimension defaultSize) {
        resizeCompoent(component, id, defaultSize);
        registeComponent(component, id);
    }

    private void saveComponentSize() {
        Log.v("saveComponentSize() component:" + component.getClass().getName() + ", id:" + id
                + ", flag:" + flag + " / size:" + component.getSize().toString());
        if (enabled) {
            String key = "ws_" + component.getClass().getName() + (id != null ? "#" + id : "");
            int state = Frame.NORMAL;
            if (component instanceof Frame) {
                state = ((Frame) component).getExtendedState() & Frame.MAXIMIZED_BOTH;
                _RProp.setPropData(key + "_state", state);
            }
            int width = (int) component.getSize().getWidth();
            int height = (int) component.getSize().getHeight();

            if ((state & Frame.MAXIMIZED_HORIZ) == 0) {
                _RProp.setPropData(key + "_width", width);
            }
            if ((state & Frame.MAXIMIZED_VERT) == 0) {
                _RProp.setPropData(key + "_height", height);
            }
        }

        if ((flag & MEMORIZE_TYPE_AUTO_UNREGISTE) != 0) {
            unregisteComponent(component, id);
        }
    }

    @Override
    public void componentHidden(ComponentEvent evt) {}

    @Override
    public void componentMoved(ComponentEvent evt) {}

    @Override
    public void componentResized(ComponentEvent evt) {
        saveComponentSize();
    }

    @Override
    public void componentShown(ComponentEvent evt) {}

    @Override
    public void windowActivated(WindowEvent evt) {}

    @Override
    public void windowClosed(WindowEvent evt) {
        saveComponentSize();
    }

    @Override
    public void windowClosing(WindowEvent evt) {
        saveComponentSize();
    }

    @Override
    public void windowDeactivated(WindowEvent evt) {}

    @Override
    public void windowDeiconified(WindowEvent evt) {}

    @Override
    public void windowIconified(WindowEvent evt) {}

    @Override
    public void windowOpened(WindowEvent evt) {}
}
