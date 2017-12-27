package dvr.oneed.com.ait_wifi_lib.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/6/26 0026.
 */
public class LocalMediaFile implements Serializable {
    public String path;
    public String fileName;
    public String thumbPath;
    public boolean selector;
    public boolean showSelector = false;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
