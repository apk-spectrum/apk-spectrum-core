package com.apkspectrum.core.signer;

import com.apkspectrum.resource._RFile;
import com.apkspectrum.util.ConsolCmd;

public class ApkSigner {
    public static String signApk(String pemFilePath, String pk8FilePath, String apkFilePath,
            String newSignedApkPath) {
        String[] result = ConsolCmd.exec(
                new String[] {"java", "-Dfile.encoding=utf-8", "-jar", _RFile.BIN_SIGNAPK.getPath(),
                        pemFilePath, pk8FilePath, apkFilePath, newSignedApkPath},
                true, null);
        StringBuilder errMessage = new StringBuilder();
        for (String line : result) {
            errMessage.append(line);
            errMessage.append("\n");
        }
        return errMessage.toString();
    }
}
