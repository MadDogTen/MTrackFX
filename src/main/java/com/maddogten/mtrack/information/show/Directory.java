package com.maddogten.mtrack.information.show;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

/*
      Directory stores each directory the user added, and their related information / settings.
 */

@SuppressWarnings({"ClassWithoutLogger", "DeserializableClassInSecureContext", "SerializableClassInSecureContext"})
public class Directory implements Serializable {

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 8211794325592517516L;
    private final File directory;
    private final String fileName;
    private final int index;
    @SuppressWarnings({"FieldCanBeLocal", "CanBeFinal", "unused", "FieldMayBeFinal"})
    // The idea for priority is that once this is implemented you will be able to choose which directory the program will prefer to play an episode from, if found in multiple locations. To be implemented at a later time.
    private int priority;
    private Map<String, Show> shows;
    private long lastProgramID;

    public Directory(File directory, String fileName, int index, int priority, Map<String, Show> shows, long lastProgramID) {
        this.directory = directory;
        this.fileName = fileName;
        this.index = index;
        this.priority = priority;
        this.shows = shows;
        this.lastProgramID = lastProgramID;
    }

    public File getDirectory() {
        return directory;
    }

    public String getFileName() {
        return fileName;
    }

    public int getIndex() {
        return index;
    }

    public Map<String, Show> getShows() {
        return shows;
    }

    public void setShows(Map<String, Show> shows) {
        this.shows = shows;
    }

    public long getLastProgramID() {
        return lastProgramID;
    }

    public void setLastProgramID(long lastProgramID) {
        this.lastProgramID = lastProgramID;
    }

    public String toString() {
        return directory.toString();
    }
}