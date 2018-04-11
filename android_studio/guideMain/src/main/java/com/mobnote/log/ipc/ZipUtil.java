package com.mobnote.log.ipc;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;

/**
 * Zip4J Util
 */
public class ZipUtil {

    /**
     * 压缩文件夹
     *
     * @param zipFileOrFolder 待压缩的文件或文件夹
     * @param desZipFile      压缩后的文件
     */
    public boolean zipFolder(File zipFileOrFolder, File desZipFile) {
        if (!zipFileOrFolder.exists())
            return false;

        try {
            ZipFile zipFile = new ZipFile(desZipFile);

            ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

            if (zipFileOrFolder.isDirectory())
                zipFile.addFolder(zipFileOrFolder, parameters);
            else
                zipFile.addFile(zipFileOrFolder, parameters);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

}
