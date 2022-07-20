package io.gallery.db.bean;

import io.gallery.db.util.DBT;

import java.io.File;

public class FileInfo {
    private File original;
    private String name;
    private String prefix;
    private String suffix;
    private String path;
    private String parent;
    private String absolutePath;
    private String absoluteFile;
    private long lastModified;
    private long totalSpace;
    private long freeSpace;
    private long usableSpace;
    private long length;
    private boolean absolute;
    private boolean directory;
    private boolean hidden;
    private boolean file;
    private Double progressUnzip;
    private Double progressZip;

    public Double getProgressUnzip() {
        return progressUnzip;
    }

    public void setProgressUnzip(Double progressUnzip) {
        this.progressUnzip = progressUnzip;
    }

    public Double getProgressZip() {
        return progressZip;
    }

    public void setProgressZip(Double progressZip) {
        this.progressZip = progressZip;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public File getOriginal() {
        return original;
    }

    public void setOriginal(File original) {
        this.original = original;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (DBT.isNotNull(name) && this.file) {
            if (name.contains(".")) {
                this.prefix = DBT.subString(name, 0, name.lastIndexOf("."));
                this.suffix = DBT.subString(name, name.lastIndexOf(".") + 1, name.length()).toUpperCase();
            } else {
                this.prefix = name;
                this.suffix = "";
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public String getAbsoluteFile() {
        return absoluteFile;
    }

    public void setAbsoluteFile(String absoluteFile) {
        this.absoluteFile = absoluteFile;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public boolean isAbsolute() {
        return absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }
}
