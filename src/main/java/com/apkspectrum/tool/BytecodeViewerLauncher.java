package com.apkspectrum.tool;

import java.io.File;

import com.apkspectrum.logback.Log;
import com.apkspectrum.resource._RFile;
import com.apkspectrum.resource._RStr;
import com.apkspectrum.swing.MessageBoxPane;
import com.apkspectrum.util.ConsolCmd;
import com.apkspectrum.util.ConsolCmd.ConsoleOutputObserver;
import com.apkspectrum.util.SystemUtil;

public class BytecodeViewerLauncher {
    static public void run(String jarFilePath) {
        run(jarFilePath, null);
    }

    static public void run(final String jarFilePath, final ConsoleOutputObserver observer) {
        if (jarFilePath == null || !(new File(jarFilePath).isFile())) {
            Log.e("No such file : " + jarFilePath);
            return;
        }

        if (!SystemUtil.checkJvmVersion("1.8")) {
            MessageBoxPane.showWarring(null, _RStr.MSG_WARN_UNSUPPORTED_JVM.get());
        }

        Thread t = new Thread(new Runnable() {
            public void run() {
                ConsolCmd.exec(new String[] {"java", "-version"}, true, observer);
                ConsolCmd.exec(new String[] {"java", "-jar", _RFile.BIN_BYTECODE_VIEWER.getPath(),
                        jarFilePath}, true, observer);
                if (observer != null) {
                    observer.ConsolOutput("BytecodeViewerLauncher Completed");
                }
            }
        });
        t.setPriority(Thread.NORM_PRIORITY);
        t.start();
    }
}
