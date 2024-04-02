package com.apkspectrum.swing;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.apkspectrum.logback.Log;
import com.apkspectrum.resource._RProp;
import com.apkspectrum.resource._RStr;

public class ApkFileChooser {

    static public JFileChooser getFileChooser(String openPath, int type, File defaultFile) {
        JFileChooser jfc = new JFileChooser(openPath) {
            private static final long serialVersionUID = 4182035928805481628L;

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result =
                            JOptionPane.showConfirmDialog(this, _RStr.QUESTION_SAVE_OVERWRITE.get(),
                                    _RStr.LABEL_SAVE_AS.get(), JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            if (!f.canWrite()) {
                                Log.e("Can't wirte file : " + f.getPath());
                                MessageBoxPane.showError(this, _RStr.MSG_CANNOT_WRITE_FILE.get());
                                return;
                            }
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setDialogType(type);
        jfc.setSelectedFile(defaultFile);

        return jfc;
    }

    static public File openApkFile(Component component) {
        JFileChooser jfc =
                getFileChooser(_RProp.S.LAST_FILE_OPEN_PATH.get(), JFileChooser.OPEN_DIALOG, null);
        jfc.setFileFilter(new FileNameExtensionFilter(_RStr.LABEL_APK_FILE_DESC.get(),
                new String[] {"apk", "apex"}));

        if (jfc.showOpenDialog(component) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File dir = jfc.getSelectedFile();
        if (dir != null) {
            com.apkspectrum.resource._RProp.LAST_FILE_OPEN_PATH
                    .setData(dir.getParentFile().getAbsolutePath());
        }
        return dir;
    }

    static public String openApkFilePath(Component component) {
        File dir = openApkFile(component);
        if (dir == null) return null;
        return dir.getPath();
    }

    static public File saveApkFile(Component component, String defaultFilePath) {
        if (!defaultFilePath.endsWith(".apk")) {
            defaultFilePath += ".apk";
        }

        JFileChooser jfc = getFileChooser(_RProp.S.LAST_FILE_SAVE_PATH.get(),
                JFileChooser.SAVE_DIALOG, new File(defaultFilePath));
        jfc.setFileFilter(new FileNameExtensionFilter(_RStr.LABEL_APK_FILE_DESC.get(),
                new String[] {"apk", "apex"}));

        if (jfc.showSaveDialog(component) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File selFile = jfc.getSelectedFile();
        if (jfc.getFileFilter() != jfc.getAcceptAllFileFilter()
                && !selFile.getPath().endsWith(".apk")) {
            selFile = new File(jfc.getSelectedFile().getPath() + ".apk");
        }

        if (selFile != null) {
            _RProp.LAST_FILE_SAVE_PATH.setData(selFile.getParentFile().getAbsolutePath());
        }
        return selFile;
    }
}
