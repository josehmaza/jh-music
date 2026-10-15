package com.jh.caraudio;

public class FolderItem {
    private String name;
    private String path;
    private int trackCount;
    private String usbSource;

    public FolderItem(String name, String path, int trackCount, String usbSource) {
        this.name = name;
        this.path = path;
        this.trackCount = trackCount;
        this.usbSource = usbSource;
    }

    public String getName() { return name; }
    public String getPath() { return path; }
    public int getTrackCount() { return trackCount; }
    public void incrementCount() { this.trackCount++; }
    public String getUsbSource() { return usbSource; }
}
