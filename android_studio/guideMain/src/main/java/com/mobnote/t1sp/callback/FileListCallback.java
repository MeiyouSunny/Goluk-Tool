package com.mobnote.t1sp.callback;

import com.mobnote.t1sp.api.Callback;
import com.mobnote.t1sp.bean.FileInfo;
import com.mobnote.t1sp.util.FileXmlParser;

import java.util.List;

/**
 * 获取文件列表Callback
 * 解析XML数据
 */
public abstract class FileListCallback extends Callback<String> {

    @Override
    public void onResponse(String response) {
        super.onResponse(response);

        final List<FileInfo> files = FileXmlParser.parse(response);
        onGetFileList(files);
    }

    public abstract void onGetFileList(List<FileInfo> files);

}
