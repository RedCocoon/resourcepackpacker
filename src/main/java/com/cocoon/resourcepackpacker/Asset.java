package com.cocoon.resourcepackpacker;

import javafx.scene.control.TreeItem;

public class Asset {

    private String path = null;
    private String file = null;
    private Boolean status = true;

    public Asset() {
    }

    public Asset(String file, Boolean status, String path) {
        setPath(path);
        setFile(file);
        setStatus(status);
    }

    public Asset(String file, Boolean status) {
        setFile(file);
        setStatus(status);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
    
    public static TreeItem<Asset> newAsset(String file, Boolean status) {
        return new TreeItem<>(new Asset(file, status));
    }

    public static TreeItem<Asset> newAsset(String file, Boolean status, String path) {
        return new TreeItem<Asset>(new Asset(file, status, path));
    }
}
