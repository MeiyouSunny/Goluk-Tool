package com.mobnote.t1sp.download;

/**
 * 下载任务信息
 */
public class Task {
    public String downloadPath;
    public String savePath;

    public Task() {
    }

    public Task(String downloadPath, String savePath) {
        this.downloadPath = downloadPath;
        this.savePath = savePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (!downloadPath.equals(task.downloadPath)) return false;
        return savePath.equals(task.savePath);
    }

    @Override
    public int hashCode() {
        int result = downloadPath.hashCode();
        result = 31 * result + savePath.hashCode();
        return result;
    }
}
