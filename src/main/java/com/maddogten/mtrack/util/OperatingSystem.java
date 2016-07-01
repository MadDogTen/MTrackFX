package com.maddogten.mtrack.util;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public enum OperatingSystem {
    WINDOWS, MAC, NIX, NUX, AIX;

    private static final Logger log = Logger.getLogger(OperatingSystem.class.getName());

    private static final OperatingSystem operatingSystem = getOS();
    public static final File programFolder = findProgramFolder();


    public static boolean openVideo(final File file, final int startTime) {
        try {
            if (startTime != 0) log.info("Show is continuing at: \"" + startTime + "\"");
            ProcessBuilder processBuilder;
            switch (operatingSystem) {
                case WINDOWS:
                    processBuilder = new ProcessBuilder("C:\\Program Files\\VideoLAN\\VLC\\vlc.exe", "--start-time=" + startTime, /*"--stop-time=" + stopTime,*/ file.getPath());
                    return processBuilder.start().isAlive();
                case MAC:
                case NIX:
                case NUX:
                case AIX:
                    processBuilder = new ProcessBuilder("/usr/bin/vlc", "--start-time=" + startTime, file.getPath());
                    //Process process = Runtime.getRuntime().exec(new String[]{"/usr/bin/openVideo", file.getPath()});
                    return processBuilder.start().isAlive();
                default:
                    if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
                    log.warning("FileManager- Your OS is Unknown, Attempting to openVideo file anyways...");
                    return true;

            }
        } catch (IOException e) {
            GenericMethods.printStackTrace(log, e, OperatingSystem.class);
        }
        return false;
    }

    private static OperatingSystem getOS() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) return OperatingSystem.WINDOWS;
        else if (os.contains("mac")) return OperatingSystem.MAC;
        else if (os.contains("nix")) return OperatingSystem.NIX;
        else if (os.contains("nux")) return OperatingSystem.NUX;
        else if (os.contains("aix")) return OperatingSystem.AIX;
        else {
            log.warning("Your operating system is unknown, Assuming linux based, Using nix...");
            return OperatingSystem.NIX;
        }
    }

    // Using the above getOS(), Determines where the program files should be saved.
    private static File findProgramFolder() {
        String home = System.getProperty("user.home");
        switch (operatingSystem) {
            case WINDOWS:
                home = System.getenv("appdata");
                break;
            case MAC:
                home += "~/Library/Preferences";
                break;
            case NIX:
            case NUX:
            case AIX:
                home += Strings.EmptyString;
                break;
        }
        File dir = new File(home + Variables.ProgramRootFolder);
        log.info("Appdata folder: \"" + dir.getAbsolutePath() + "\".");
        return new File(dir.getAbsolutePath());
    }
}
