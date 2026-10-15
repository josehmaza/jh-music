package com.jh.caraudio;

public class Track {
    private String id;
    private String title;
    private String artist;
    private String album;
    private String folder;
    private String path;
    private String filePath;
    private String format;
    private int durationSec;
    private String usbSource; // "USB 1", "USB 2", "SD", etc.
    private boolean isFavorite;

    public Track(String filePath, String title, String artist, String album, String folder, String path, String format, int durationSec, String usbSource) {
        this.filePath = filePath;
        this.id = String.valueOf(filePath.hashCode());
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.folder = folder;
        this.path = path;
        this.format = format;
        this.durationSec = durationSec;
        this.usbSource = usbSource;
        this.isFavorite = false;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getAlbum() { return album; }
    public String getFolder() { return folder; }
    public String getPath() { return path; }
    public String getFilePath() { return filePath; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public int getDurationSec() { return durationSec; }
    public void setDurationSec(int durationSec) { this.durationSec = durationSec; }
    public String getUsbSource() { return usbSource; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getFormattedDuration() {
        int m = durationSec / 60;
        int s = durationSec % 60;
        return String.format("%02d:%02d", m, s);
    }
}
