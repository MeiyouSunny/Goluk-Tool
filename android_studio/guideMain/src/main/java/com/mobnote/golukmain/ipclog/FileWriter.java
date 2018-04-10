package com.mobnote.golukmain.ipclog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * 文件写入
 */
public class FileWriter {

    /**
     * 写入数据到对应的文件
     *
     * @param data 待写入的数据
     * @param file 对应的文件
     */
    public void writeDataToFile(String data, File file) {

        FileOutputStream out;
        BufferedWriter writer = null;
        try {
            // 追加
            out = new FileOutputStream(file, true);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
