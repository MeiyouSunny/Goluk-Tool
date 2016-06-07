package com.mobnote.eventbus;

/**
 * Created by leege100 on 16/5/20.
 */
public class EventAddTailer{

    /** 开始导出 */
    public static final int EXPORT_STATUS_START = 1;
    /** 导出中 */
    public static final int EXPORT_STATUS_EXPORTING = 2;
    /** 导出成功（完成） */
    public static final int EXPORT_STATUS_FINISH = 3;
    /** 导出失败 */
    public static final int EXPORT_STATUS_FAILED = 4;
    private int exportStatus;
    private float exportProcess;
    private String exportPath;
    public EventAddTailer(int status,float process,String path){
        this.exportStatus = status;
        this.exportProcess = process;
        this.exportPath = path;
    }

    public int getExportStatus() {
        return exportStatus;
    }

    public void setExportStatus(int exportStatus) {
        this.exportStatus = exportStatus;
    }

    public float getExportProcess() {
        return exportProcess;
    }

    public void setExportProcess(float exportProcess) {
        this.exportProcess = exportProcess;
    }

    public String getExportPath() {
        return exportPath;
    }

    public void setExportPath(String exprotPath) {
        this.exportPath = exprotPath;
    }
}
