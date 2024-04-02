package com.apkspectrum.core.permissionmanager;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.NamedNodeMap;

import com.apkspectrum.logback.Log;
import com.apkspectrum.util.XmlPath;

public class UnitRecord<T> {
    public final Class<T> clazz;
    public final String name;

    public final int latestSdk;
    public final int addedSdk;
    public final int removedSdk;
    public final int deprecatedSdk;

    final T[] histories;

    public UnitRecord(Class<T> clazz, XmlPath node) throws IllegalArgumentException {
        if (clazz == null || node == null) {
            throw new IllegalArgumentException(
                    "clazz(" + clazz + ") or node(" + node + ") is null");
        }
        if (!clazz.equals(PermissionInfoExt.class) && !clazz.equals(PermissionGroupInfoExt.class)) {
            throw new IllegalArgumentException("Unsupported record type : " + clazz.toString());
        }
        this.clazz = clazz;

        name = node.getAttribute("name");
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Node was not have attribute as 'name'.");
        }
        int addedSdk = 1, removedSdk = -1, deprecatedSdk = -1;

        XmlPath patchs = node.getNodeList("patch");
        String tmp = patchs.getCount() > 0 ? patchs.getAttribute(0, "sdk") : null;
        try {
            latestSdk = tmp != null ? Integer.parseInt(tmp) + 1 : 1;
        } catch (NumberFormatException e) {
            Log.e("NumberFormatException for SDK version. " + e.getMessage());
            throw e;
        }

        @SuppressWarnings("unchecked")
        T[] temp = (T[]) Array.newInstance(clazz, patchs.getCount() + 1);
        histories = temp;

        boolean isDeprecated = false, isRemoved = false;
        Object info = null;
        for (int i = 0, cnt = patchs.getCount(); i <= cnt; i++) {
            try {
                info = histories[i] = (i == 0) ? clazz.getConstructor().newInstance()
                        : clazz.getConstructor(clazz).newInstance(info);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e1) {
                e1.printStackTrace();
            }
            XmlPath prePatch = (i == 0) ? node : patchs.getNode(i - 1);
            NamedNodeMap attrs = prePatch.getAttributes();

            int sdk = latestSdk;
            if (i == 0) {
                try {
                    clazz.getField("sdk").setInt(info, latestSdk);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
                        | IllegalAccessException e) {
                    // ignore
                }
            } else if (attrs.getNamedItem("sdk") == null) {
                Log.e("No have SDK specification");
                continue;
            } else {
                try {
                    String strSdk = attrs.getNamedItem("sdk").getNodeValue();
                    sdk = Integer.parseInt(strSdk);
                } catch (NumberFormatException e) {
                    Log.e("NumberFormatException for SDK version. " + e.getMessage());
                    continue;
                }
            }

            for (int j = attrs.getLength() - 1; j >= 0; --j) {
                String name = attrs.item(j).getNodeName().trim();
                String value = attrs.item(j).getNodeValue().trim();
                try {
                    Field field = clazz.getField(name);
                    Class<?> fieldClazz = field.getType();
                    if (fieldClazz.equals(String.class)) {
                        field.set(info, value);
                    } else if (fieldClazz.equals(Integer.class)) {
                        if (value != null && !value.isEmpty()) {
                            field.set(info, Integer.parseInt(value));
                        }
                    } else if (fieldClazz.getName().equals("int")) {
                        field.setInt(info, Integer.parseInt(value));
                    } else {
                        Log.e(name + " : Unknown type " + fieldClazz.toString());
                    }
                } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
                    // ignore
                }
                switch (name) {
                    case "name":
                        if (this.name != value) {
                            Log.w("Name is no matched : " + value);
                        }
                        break;
                    case "label":
                    case "description":
                    case "icon":
                        // It's very slower
                        // try {
                        //     Field field = clazz.getField(name+"s");
                        //     if (field.getType().equals(ResourceInfo[].class)) {
                        //         ResourceInfo[] resInfo;
                        //         resInfo = PermissionManager.getResource(value, -1);
                        //         field.set(info, resInfo);
                        //     }
                        // } catch (NoSuchFieldException | SecurityException
                        //         | IllegalAccessException e) {
                        //     // ignore
                        // }
                        break;
                    case "protectionLevel":
                        try {
                            Field field = clazz.getField("protectionFlags");
                            if (field.getType().getName().equals("int")) {
                                field.setInt(info, PermissionInfoExt.parseProtectionFlags(value));
                            }
                        } catch (NoSuchFieldException | SecurityException
                                | IllegalAccessException e) {
                            // ignroe
                        }
                        break;
                    case "action":
                        if ("added".equals(value)) {
                            addedSdk = sdk;
                        } else if ("removed".equals(value)) {
                            removedSdk = sdk;
                        }
                }
            }
            try {
                String preComment = prePatch.getComment();
                String comment = (String) clazz.getField("comment").get(info);
                if (preComment != null && !preComment.equals(comment)) {
                    clazz.getField("comment").set(info, preComment);
                    Object val = null;
                    try {
                        val = clazz.getMethod("isDeprecated").invoke(info);
                        if (isDeprecated && !(Boolean) val) {
                            deprecatedSdk = sdk + 1;
                        }
                        isDeprecated = (Boolean) val;
                    } catch (NoSuchMethodException | IllegalAccessException | SecurityException
                            | InvocationTargetException e) {
                        // ignore
                    }
                    try {
                        val = clazz.getMethod("isRemoved").invoke(info);
                        boolean removed = (Boolean) val;
                        if (isRemoved && !removed) removedSdk = sdk + 1;
                        isRemoved = removed;
                    } catch (NoSuchMethodException | InvocationTargetException
                            | IllegalAccessException | SecurityException e) {
                        // ignore
                    }
                }
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException e) {
                // ignore
            }
        }
        this.addedSdk = addedSdk;
        this.removedSdk = removedSdk;
        this.deprecatedSdk = deprecatedSdk;
    }

    public T[] getHistories() {
        return histories;
    }

    public T getInfomation(int sdk) {
        if (histories == null || histories.length == 0) {
            Log.w("No have history");
            return null;
        }
        if (sdk < addedSdk) {
            Log.v("This SDK(" + sdk + ") version was not have permission. " + name
                    + " added in API level " + addedSdk);
            return null;
        }
        if (removedSdk > -1 && sdk >= removedSdk) {
            Log.v("This SDK(" + sdk + ") version was not have permission. " + name
                    + " removed at API level " + removedSdk);
            return null;
        }
        T info = histories[0];
        for (int i = 0; i < histories.length; i++) {
            try {
                T pre = histories[i];
                if (sdk > pre.getClass().getField("sdk").getInt(pre)) break;
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
                    | SecurityException e) {
                e.printStackTrace();
            }
            info = histories[i];
        }
        /* Move into each InfoExt, because it's very slower
        if (info != null) {
            for (String name: new String[] {"label", "description", "icon"}) {
                try {
                    if (clazz.getField(name+"s").get(info) == null) {
                        String value = (String) clazz.getField(name).get(info);
                        clazz.getField(name+"s").set(info
                                , PermissionManager.getResource(value, -1));
                    }
                } catch (NoSuchFieldException | SecurityException
                        | IllegalAccessException e) {
                    // ignore
                }
            }
        }
        //*/
        return (T) info;
    }

    public boolean isDeprecated() {
        return deprecatedSdk > -1;
    }

    public int getDeprecatedSdk() {
        return deprecatedSdk;
    }

    public int getAddedSdk() {
        return addedSdk;
    }

    public int getRemovedSdk() {
        return removedSdk;
    }

    public boolean isPermissionRecord() {
        return clazz.equals(PermissionInfoExt.class);
    }

    public boolean isPermissionGroupRecord() {
        return clazz.equals(PermissionGroupInfoExt.class);
    }
}
