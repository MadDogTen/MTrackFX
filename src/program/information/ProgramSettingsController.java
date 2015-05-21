package program.information;

import program.io.FileManager;
import program.util.Strings;
import program.util.Variables;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

public class ProgramSettingsController {
    private static final Logger log = Logger.getLogger(ProgramSettingsController.class.getName());

    private static HashMap<String, ArrayList<String>> settingsFile;

    private static void loadProgramSettingsFile() {
        if (settingsFile == null) {
            settingsFile = new FileManager().loadProgramSettings(Strings.SettingsFileName, Variables.SettingsExtension);
        }
    }

    public static int getUpdateSpeed() {
        loadProgramSettingsFile();
        return Integer.parseInt(settingsFile.get("General").get(0));
    }

    public static void setUpdateSpeed(int updateSpeed) {
        loadProgramSettingsFile();
        settingsFile.get("General").set(0, String.valueOf(updateSpeed));
        Variables.setUpdateSpeed();
    }

    public static boolean isDefaultUsername() {
        loadProgramSettingsFile();
        ArrayList<String> defaultUser = settingsFile.get("DefaultUser");
        return Boolean.parseBoolean(defaultUser.get(0));
    }

    public static String getDefaultUsername() {
        loadProgramSettingsFile();
        ArrayList<String> defaultUser = settingsFile.get("DefaultUser");
        return defaultUser.get(1);
    }

    public static void setDefaultUsername(String userName, int option) {
        log.info("ProgramSettingsController- DefaultUsername is being set...");
        ArrayList<String> defaultUsername = settingsFile.get("DefaultUser");
        if (option == 0) {
            defaultUsername.set(0, "false");
            defaultUsername.set(1, Variables.EmptyString);
        } else if (option == 1) {
            defaultUsername.set(0, "true");
            defaultUsername.set(1, userName);
        }
        settingsFile.replace("DefaultUser", defaultUsername);
    }

    public static ArrayList<String> getDirectories() {
        loadProgramSettingsFile();
        ArrayList<String> directories = new ArrayList<>();
        if (settingsFile.containsKey("Directories")) {
            for (String aDirectory : settingsFile.get("Directories")) {
                directories.add(aDirectory.split(">")[1]);
            }
        }
        return directories;
    }

    public static int getProgramSettingsVersion() { //TODO Remove -2 return when program is at Version 0.9
        loadProgramSettingsFile();
        if (settingsFile.containsKey("ProgramVersions")) {
            return Integer.parseInt(settingsFile.get("ProgramVersions").get(0));
        } else return -2;
    }

    public static boolean isDirectoryCurrentlyActive(File directory) {
        return directory.isDirectory();
    }

    public static void removeDirectory(String aDirectory) { // TODO Update other users when directory is deleted.
        loadProgramSettingsFile();
        log.info("Currently processing removal of: " + aDirectory);
        int index = getDirectoryIndex(aDirectory);
        ArrayList<HashMap<String, HashMap<Integer, HashMap<String, String>>>> showsFileArray = ShowInfoController.getDirectoriesHashMaps(index);
        Set<String> hashMapShows = ShowInfoController.getDirectoryHashMap(index).keySet();
        for (String aShow : hashMapShows) {
            log.info("Currently checking: " + aShow);
            Boolean showExistsElsewhere = ShowInfoController.doesShowExistElsewhere(aShow, showsFileArray);
            if (!showExistsElsewhere) {
                UserInfoController.setIgnoredStatus(aShow, true);
            }
        }
        new FileManager().deleteFile(Variables.DirectoriesFolder, "Directory-" + index, Variables.ShowsExtension);
        settingsFile.get("Directories").remove(index + ">" + aDirectory);
        log.info("Finished processing removal of the directory.");
    }

    public static void printAllDirectories() {
        loadProgramSettingsFile();
        log.info("Printing out all directories:");
        if (!getDirectories().isEmpty()) {
            for (String aDirectory : getDirectories()) {
                log.info(aDirectory);
            }
        } else {
            log.info("No directories.");
        }
        log.info("Finished printing out all directories:");
    }

    public static int getDirectoryIndex(String aDirectory) {
        for (String directory : settingsFile.get("Directories")) {
            if (directory.contains(aDirectory)) {
                return Integer.parseInt(directory.split(">")[0]);
            }
        }
        log.info("Error if this is reached, Please report.");
        return -3;
    }

    public static ArrayList<String> getDirectoriesNames() {
        loadProgramSettingsFile();
        ArrayList<String> directories = settingsFile.get("Directories");
        ArrayList<String> directoriesNames = new ArrayList<>();
        for (String aDirectory : directories) {
            int index = Integer.parseInt(aDirectory.split(">")[0]);
            directoriesNames.add("Directory-" + index + Variables.ShowsExtension);
        }
        return directoriesNames;
    }

    public static File getDirectory(int index) {
        loadProgramSettingsFile();
        ArrayList<String> directories = settingsFile.get("Directories");
        for (String aDirectory : directories) {
            String[] split = aDirectory.split(">");
            if (split[0].matches(String.valueOf(index))) {
                return new File(split[1]);
            }
        }
        log.info("Error if this is reached, Please report.");
        return new File(Variables.EmptyString);
    }

    public static Boolean[] addDirectory(int index, File directory) {
        loadProgramSettingsFile();
        log.info(String.valueOf(index));
        ArrayList<String> directories = settingsFile.get("Directories");
        Boolean[] answer = {false, false};
        if (!directory.toString().isEmpty() && !directories.contains(String.valueOf(directory))) {
            log.info("Added Directory");
            directories.add(index + ">" + String.valueOf(directory));
            log.info(index + ">" + String.valueOf(directory));
            settingsFile.replace("Directories", directories);
            log.info(String.valueOf(settingsFile.get("Directories")));
            answer[0] = true;
        } else if (directory.toString().isEmpty()) {
            answer[1] = true;
        }
        return answer;
    }

    public static int getLowestFreeDirectoryIndex() {
        int lowestFreeIndex = 0;
        for (String aDirectory : settingsFile.get("Directories")) {
            int currentInt = Integer.parseInt(aDirectory.split(">")[0]);
            if (lowestFreeIndex == currentInt) {
                lowestFreeIndex++;
            }
        }
        return lowestFreeIndex;
    }

    public static HashMap<String, ArrayList<String>> getSettingsFile() {
        return settingsFile;
    }

    public static void setSettingsFile(HashMap<String, ArrayList<String>> settingsFile) {
        ProgramSettingsController.settingsFile = settingsFile;
    }

    // Save the file
    public static void saveSettingsFile() {
        if (settingsFile != null) {
            new FileManager().save(settingsFile, Variables.EmptyString, Strings.SettingsFileName, Variables.SettingsExtension, true);
            log.info("ProgramSettingsController- settingsFile has been saved!");
        }
    }
}
