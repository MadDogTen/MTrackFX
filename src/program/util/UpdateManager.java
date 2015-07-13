package program.util;

import program.information.ChangeReporter;
import program.information.ProgramSettingsController;
import program.information.ShowInfoController;
import program.information.UserInfoController;
import program.information.settings.ProgramSettings;
import program.information.settings.UserSettings;
import program.information.settings.UserShowSettings;
import program.information.show.Episode;
import program.information.show.Season;
import program.information.show.Show;
import program.io.FileManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;


// Ch
public class UpdateManager {
    private final Logger log = Logger.getLogger(UpdateManager.class.getName());

    // These both compare the version defined in Variables (Latest Version) with the version the files are currently at (Old Version), and if they don't match, converts the files to the latest version.
    public void updateProgramSettingsFile() {
        int newVersion = Variables.ProgramSettingsFileVersion, currentVersion = ProgramSettingsController.getProgramSettingsFileVersion();
        if (Variables.ProgramSettingsFileVersion == currentVersion) {
            log.info("Program settings file versions matched.");
        } else {
            log.info("Program settings file versions didn't match " + newVersion + " - " + currentVersion + ", Updating...");
            convertProgramSettingsFile(currentVersion, newVersion);
        }
    }

    public void updateUserSettingsFile() {
        int newVersion = Variables.UserSettingsFileVersion, oldVersion = UserInfoController.getUserSettingsVersion();
        if (newVersion == oldVersion) {
            log.info("User settings file versions matched.");
        } else {
            log.info("User settings file versions didn't match " + newVersion + " - " + oldVersion + ", Updating...");
            convertUserSettingsFile(oldVersion, newVersion);
        }
    }

    public void updateShowFile() {
        int newVersion = Variables.ShowFileVersion, oldVersion = ProgramSettingsController.getShowFileVersion();
        if (newVersion == oldVersion) {
            log.info("Show file versions matched.");
        } else {
            log.info("Show file versions didn't match " + newVersion + " - " + oldVersion + ", Updating...");
            convertShowFile(oldVersion, newVersion);
        }
    }

    // This checks if the user has the latest show information, and if they don't, updates it to the latest.
    public void updateMainDirectoryVersion() {
        int mainDirectoryVersion = ProgramSettingsController.getMainDirectoryVersion();
        if (mainDirectoryVersion == UserInfoController.getUserDirectoryVersion())
            log.info("User directory version matched.");
        else {
            log.info("User directory version didn't match, Updating...");
            updateUserShows(mainDirectoryVersion);
        }
    }

    // These are ran if the versions didn't match, and updates the file with the latest information. It uses the oldVersion to find where it last updated, and runs through all the cases at and below it to catch it up, than updates the file to the latest version if it ran successfully.
    @SuppressWarnings("SameParameterValue")
    private void convertProgramSettingsFile(int oldVersion, int newVersion) {
        ProgramSettings programSettings = ProgramSettingsController.getSettingsFile();
        boolean updated = false;
        if (oldVersion <= 1008) {
            @SuppressWarnings("unchecked") HashMap<String, ArrayList<String>> oldProgramSettingsFile = (HashMap<String, ArrayList<String>>) new FileManager().loadFile(Strings.EmptyString, Strings.SettingsFileName, Variables.SettingsExtension);
            switch (oldVersion) {
                case -2:
                    ArrayList<String> temp = new ArrayList<>();
                    temp.add(0, String.valueOf(Variables.ProgramSettingsFileVersion));
                    oldProgramSettingsFile.put("ProgramVersions", temp);
                    log.info("Program settings file has been updated from version -2.");
                case 1:
                    oldProgramSettingsFile.get("General").add(1, Variables.dataFolder);
                    log.info("Program has been updated from version 1.");
                case 2:
                    log.info("Program has been updated from version 2.");
                case 1001:
                    oldProgramSettingsFile.get("General").remove(1);
                    log.info("Program has been updated from version 1001.");
                case 1002:
                    log.info("Converting directories...");
                    if (oldProgramSettingsFile.containsKey("Directories")) {
                        ArrayList<String> directories = oldProgramSettingsFile.get("Directories");
                        ArrayList<String> directoriesFixed = new ArrayList<>();
                        directories.forEach(aDirectory -> {
                            int index = directories.indexOf(aDirectory);
                            directoriesFixed.add(index + ">" + aDirectory);
                            log.info("Converted " + aDirectory + " to " + index + '>' + aDirectory);
                        });
                        oldProgramSettingsFile.replace("Directories", directoriesFixed);
                    } else log.info("Program settings file contains no directories, Nothing was converted.");
                    log.info("Finished converting directories");
                    log.info("Program has been updated from version 1002.");
                case 1003:
                    oldProgramSettingsFile.get("ProgramVersions").add(1, "1");
                    log.info("Program has been updated from version 1003.");
                case 1004:
                    oldProgramSettingsFile.get("General").add(1, "false");
                    log.info("Program has been updated from version 1004.");
                case 1005:
                    oldProgramSettingsFile.get("General").add(2, Strings.EmptyString);
                    log.info("Program has been updated from version 1005.");
                case 1006:
                    oldProgramSettingsFile.get("ProgramVersions").add(2, "-2");
                    log.info("Program has been updated from version 1006.");
                case 1007:
                    ArrayList<String> temp2 = new ArrayList<>();
                    temp2.add(0, "239");
                    temp2.add(1, "29");
                    temp2.add(2, "48");
                    temp2.add(3, "50");
                    oldProgramSettingsFile.put("GuiNumberSettings", temp2);
                    //noinspection ReuseOfLocalVariable
                    temp2 = new ArrayList<>();
                    temp2.add(0, "true");
                    temp2.add(1, "true");
                    temp2.add(2, "false");
                    temp2.add(3, "false");
                    oldProgramSettingsFile.put("GuiBooleanSettings", temp2);
                    log.info("Program has been updated from version 1007.");
                case 1008:
                    programSettings = new ProgramSettings(newVersion,
                            Integer.parseInt(oldProgramSettingsFile.get("ProgramVersions").get(1)),
                            Integer.parseInt(oldProgramSettingsFile.get("ProgramVersions").get(2)),
                            Integer.parseInt(oldProgramSettingsFile.get("General").get(0)),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("General").get(1)),
                            oldProgramSettingsFile.get("General").get(2),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("DefaultUser").get(0)),
                            oldProgramSettingsFile.get("DefaultUser").get(1),
                            oldProgramSettingsFile.get("Directories"),
                            Double.parseDouble(oldProgramSettingsFile.get("GuiNumberSettings").get(0)),
                            Double.parseDouble(oldProgramSettingsFile.get("GuiNumberSettings").get(1)),
                            Double.parseDouble(oldProgramSettingsFile.get("GuiNumberSettings").get(2)),
                            Double.parseDouble(oldProgramSettingsFile.get("GuiNumberSettings").get(3)),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("GuiBooleanSettings").get(0)),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("GuiBooleanSettings").get(1)),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("GuiBooleanSettings").get(2)),
                            Boolean.parseBoolean(oldProgramSettingsFile.get("GuiBooleanSettings").get(3))
                    );
                    updated = true;

            }
        } /*else if (oldVersion < 1008) {

        }*/

        if (updated) {
            // Update Program Settings File Version
            programSettings.setProgramSettingsFileVersion(newVersion);

            ProgramSettingsController.setSettingsFile(programSettings);
            ProgramSettingsController.saveSettingsFile();
            log.info("Program settings file was successfully updated to version " + newVersion + '.');
        } else log.info("Program settings file was not updated. This is an error, please report.");
    }


    @SuppressWarnings("SameParameterValue")
    private void convertUserSettingsFile(int oldVersion, int newVersion) {
        UserSettings userSettings = UserInfoController.getUserSettings();
        boolean updated = false;
        if (oldVersion <= 1001) {
            @SuppressWarnings("unchecked") HashMap<String, HashMap<String, HashMap<String, String>>> oldUserSettingsFile = (HashMap<String, HashMap<String, HashMap<String, String>>>) new FileManager().loadFile(Variables.UsersFolder, Strings.UserName, Variables.UsersExtension);
            switch (oldVersion) {
                case -2:
                    HashMap<String, HashMap<String, String>> tempPut;
                    HashMap<String, String> temp;
                    tempPut = new HashMap<>();
                    temp = new HashMap<>();
                    temp.put("0", String.valueOf(Variables.UserSettingsFileVersion));
                    tempPut.put("UserVersions", temp);
                    oldUserSettingsFile.put("UserSettings", tempPut);
                    log.info("User settings file has been updated from version -2.");
                case 1:
                    HashMap<String, HashMap<String, String>> shows = oldUserSettingsFile.get("ShowSettings");
                    if (shows != null) {
                        shows.keySet().forEach(aShow -> shows.get(aShow).put("isHidden", "false"));
                    }
                    log.info("User settings file has been updated from version 1.");
                case 2:
                    final HashMap<String, HashMap<String, HashMap<String, String>>> finalOldUserSettingsFile = oldUserSettingsFile;
                    ShowInfoController.getShowsList().forEach(aShow -> {
                        if (!finalOldUserSettingsFile.containsKey("ShowSettings")) {
                            finalOldUserSettingsFile.put("ShowSettings", new HashMap<>());
                        }
                        if (!finalOldUserSettingsFile.get("ShowSettings").keySet().contains(aShow)) {
                            log.info("Adding " + aShow + " to user settings file.");
                            HashMap<String, String> temp2 = new HashMap<>();
                            temp2.put("isActive", "false");
                            temp2.put("isIgnored", "false");
                            temp2.put("isHidden", "false");
                            temp2.put("CurrentSeason", String.valueOf(ShowInfoController.findLowestSeason(aShow)));
                            Set<Integer> episodes = ShowInfoController.getEpisodesList(aShow, Integer.parseInt(temp2.get("CurrentSeason")));
                            temp2.put("CurrentEpisode", String.valueOf(ShowInfoController.findLowestEpisode(episodes)));
                            finalOldUserSettingsFile.get("ShowSettings").put(aShow, temp2);
                        }
                    });
                    log.info("User settings file has been updated from version 2.");
                case 1000:
                    if (!oldUserSettingsFile.containsKey("ShowSettings")) {
                        oldUserSettingsFile.put("ShowSettings", new HashMap<>());
                    }
                    oldUserSettingsFile.get("UserSettings").get("UserVersions").put("1", "0");
                    log.info("User settings file has been updated from version 1000.");
                case 1001:
                    Map<String, UserShowSettings> showsConverted = new HashMap<>();
                    oldUserSettingsFile.get("ShowSettings").forEach((showName, showSettings) -> showsConverted.put(showName, new UserShowSettings(showName, Boolean.parseBoolean(showSettings.get("isActive")), Boolean.parseBoolean(showSettings.get("isIgnored")), Boolean.parseBoolean(showSettings.get("isHidden")), Integer.parseInt(showSettings.get("CurrentSeason")), Integer.parseInt(showSettings.get("CurrentEpisode")))));
                    userSettings = new UserSettings(Strings.UserName, showsConverted);
                    UserInfoController.setUserSettings(userSettings);
                    UserInfoController.saveUserSettingsFile();
                    updated = true;
            }
        } /*else if (oldVersion > 1001) {
        }*/


        if (updated) {
            // Update User Settings File Version
            userSettings.setUserSettingsFileVersion(newVersion);
            UserInfoController.setUserSettings(userSettings);
            UserInfoController.saveUserSettingsFile();
            log.info("User settings file was successfully updated to version " + newVersion + '.');
        } else log.info("User settings file was not updated. This is an error, please report.");
    }

    private void convertShowFile(int oldVersion, int newVersion) {
        boolean updated = false;
        switch (oldVersion) {
            case -2:
                ArrayList<HashMap<String, HashMap<Integer, HashMap<String, String>>>> showsFileArray = new ArrayList<>();
                ArrayList<String> files = ProgramSettingsController.getDirectoriesNames();
                FileManager fileManager = new FileManager();
                files.forEach(aString -> {
                    int place = Integer.parseInt(aString.split("\\-|\\.")[1]);
                    //noinspection unchecked
                    showsFileArray.add((HashMap<String, HashMap<Integer, HashMap<String, String>>>) fileManager.loadFile(Variables.DirectoriesFolder, aString, Strings.EmptyString));
                });
                showsFileArray.forEach(aHashMap -> {
                    int index = showsFileArray.indexOf(aHashMap);
                    Map<String, Show> showsMap = new HashMap<>();
                    aHashMap.forEach((showName, showHashMap) -> {
                        Map<Integer, Season> seasonsMap = new HashMap<>();
                        showHashMap.forEach((season, seasonHashMap) -> {
                            Map<Integer, Episode> episodesMap = new HashMap<>();
                            seasonHashMap.forEach((episode, episodeFileName) -> {
                                int[] episodes;
                                if (episode.contains("+")) {
                                    episodes = new int[2];
                                    episodes[0] = Integer.parseInt(episode.split("[+]")[0]);
                                    episodes[1] = Integer.parseInt(episode.split("[+]")[1]);
                                } else {
                                    episodes = new int[1];
                                    episodes[0] = Integer.parseInt(episode);
                                }
                                if (episodes.length == 1) {
                                    episodesMap.put(episodes[0], new Episode(episodes[0], episodeFileName, false));
                                } else if (episodes.length == 2) {
                                    episodesMap.put(episodes[0], new Episode(episodes[0], episodeFileName, true));
                                    episodesMap.put(episodes[1], new Episode(episodes[1], episodeFileName, true));
                                }
                            });
                            seasonsMap.put(season, new Season(season, episodesMap));
                        });
                        showsMap.put(showName, new Show(showName, seasonsMap));
                    });
                    ShowInfoController.saveShowsMapFile(showsMap, index);
                });
                log.info("Show file has been updated from version -2.");
                updated = true;
        }

        if (updated) {
            // Update Program Settings File Version
            ProgramSettingsController.setShowFileVersion(newVersion);
            log.info("Show file was successfully updated to version " + newVersion + '.');
        } else log.info("Show file was not updated. This is an error, please report.");
    }

    // This finds what shows have been added / remove if another user has ran the program (and found updated information) since you last ran your profile.
    private void updateUserShows(int newVersion) {
        ArrayList<String> shows = ShowInfoController.getShowsList();
        ArrayList<String> userShows = UserInfoController.getAllNonIgnoredShows();
        ArrayList<String> ignoredShows = UserInfoController.getIgnoredShows();
        final boolean[] changed = {false};
        shows.forEach(aShow -> {
            if (!userShows.contains(aShow) && !ignoredShows.contains(aShow)) {
                log.info(aShow + " was found during user shows update and added.");
                ChangeReporter.addChange(aShow + " was added.");
                UserInfoController.addNewShow(aShow);
                changed[0] = true;
            } else if (ignoredShows.contains(aShow)) {
                log.info(aShow + " was found during user shows update and un-ignored.");
                ChangeReporter.addChange(aShow + " was added.");
                UserInfoController.setIgnoredStatus(aShow, false);
                changed[0] = true;
            }
        });
        userShows.forEach(aShow -> {
            if (!shows.contains(aShow)) {
                log.info(aShow + " wasn't found during user shows update.");
                ChangeReporter.addChange(aShow + " has been removed.");
                UserInfoController.setIgnoredStatus(aShow, true);
                changed[0] = true;
            }
        });
        if (!changed[0]) {
            log.info("No changes found.");
        }
        UserInfoController.setUserDirectoryVersion(newVersion);
    }
}