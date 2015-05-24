package program.io;

import program.Main;
import program.information.ChangeReporter;
import program.information.ProgramSettingsController;
import program.information.ShowInfoController;
import program.information.UserInfoController;
import program.util.Clock;
import program.util.FindLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CheckShowFiles {
    private static final Logger log = Logger.getLogger(CheckShowFiles.class.getName());

    private static boolean recheckShowFileRunning = false, keepRunning = false;
    private static ArrayList<String> emptyShows = new ArrayList<>();
    private static int runNumber = 0;

    public static void recheckShowFile(Boolean forceRun) {
        final Boolean[] hasChanged = {false};
        int timer = Clock.getTimeSeconds();
        if (!recheckShowFileRunning || (forceRun && keepRunning)) {
            log.info("Started rechecking shows...");
            recheckShowFileRunning = true;
            keepRunning = !forceRun;
            ArrayList<String> activeShows = UserInfoController.getActiveShows();
            FileManager fileManager = new FileManager();
            ProgramSettingsController.getDirectoriesIndexes().forEach(aIndex -> {
                HashMap<String, HashMap<Integer, HashMap<String, String>>> hashMap = ShowInfoController.getDirectoryHashMap(aIndex);
                File folderLocation = ProgramSettingsController.getDirectory(aIndex);
                if (ProgramSettingsController.isDirectoryCurrentlyActive(folderLocation)) {
                    for (String aShow : activeShows) {
                        log.info("Currently rechecking " + aShow);
                        int currentSeason = UserInfoController.getCurrentSeason(aShow);
                        if (hashMap.containsKey(aShow)) {
                            Set<Integer> seasons = hashMap.get(aShow).keySet();
                            Iterator<Integer> seasonsIterator = seasons.iterator();
                            while (seasonsIterator.hasNext()) {
                                int aSeason = seasonsIterator.next();
                                if (aSeason < currentSeason) {
                                    seasonsIterator.remove();
                                    log.finest("Season " + aSeason + " was skipped in rechecking as the current user is past it.");
                                }
                            }
                            seasons.forEach(aSeason -> {
                                if (fileManager.checkFolderExists(String.valueOf(folderLocation) + '\\' + aShow + "\\Season " + aSeason + '\\')) {
                                    log.info("Checking for new episodes for " + aShow + " - Season: " + aSeason);
                                    ArrayList<String> changedEpisodes = hasEpisodesChanged(aShow, aSeason, folderLocation, hashMap);
                                    if (!changedEpisodes.isEmpty()) {
                                        ChangeReporter.addChange(aShow + "- Season: " + aSeason + " Episode(s): " + changedEpisodes + " has changed");
                                        hasChanged[0] = true;
                                        UpdateShowFiles.checkForNewOrRemovedEpisodes(folderLocation, aShow, aSeason, hashMap, aIndex);
                                    }
                                }
                            });
                            ArrayList<Integer> changedSeasons = hasSeasonsChanged(aShow, folderLocation, hashMap);
                            Iterator<Integer> changedSeasonIterator = changedSeasons.iterator();
                            while (changedSeasonIterator.hasNext()) {
                                int aSeason = changedSeasonIterator.next();
                                if (aSeason < currentSeason) {
                                    changedSeasonIterator.remove();
                                }
                            }
                            if (!changedSeasons.isEmpty()) {
                                ChangeReporter.addChange(aShow + "- Season(s): " + changedSeasons + " has changed");
                                hasChanged[0] = true;
                                UpdateShowFiles.checkForNewOrRemovedSeasons(folderLocation, aShow, changedSeasons, hashMap, aIndex);
                            }
                            if (!Main.running || (!forceRun && !keepRunning)) {
                                break;
                            }
                        }
                    }
                    HashMap<String, HashMap<Integer, HashMap<String, String>>> changedShows = hasShowsChanged(folderLocation, hashMap, forceRun);
                    if (!changedShows.isEmpty()) {
                        log.info("Current Shows have changed.");
                        hasChanged[0] = true;
                        ArrayList<String> ignoredShows = UserInfoController.getIgnoredShows();
                        changedShows.keySet().forEach(aNewShow -> {
                            ChangeReporter.addChange(aNewShow + " has been added!");
                            hashMap.put(aNewShow, changedShows.get(aNewShow));
                            if (ignoredShows.contains(aNewShow)) {
                                UserInfoController.setIgnoredStatus(aNewShow, false);
                            }
                        });
                        ShowInfoController.saveShowsHashMapFile(hashMap, aIndex);
                        changedShows.keySet().forEach(UserInfoController::addNewShow);
                        ProgramSettingsController.setMainDirectoryVersion(ProgramSettingsController.getMainDirectoryVersion() + 1, true);
                    }
                }
            });
        }
        if (hasChanged[0] && Main.running) {
            log.info("Some shows have been updated.");
            log.info("Finished Rechecking Shows! - It took " + Clock.timeTakenSeconds(timer) + " seconds.");
        } else if (Main.running) {
            log.info("All shows were the same.");
            log.info("Finished Rechecking Shows! - It took " + Clock.timeTakenSeconds(timer) + " seconds.");
        }
        recheckShowFileRunning = false;
    }

    @SuppressWarnings("unchecked")
    private static ArrayList<String> hasEpisodesChanged(String aShow, Integer aSeason, File folderLocation, HashMap<String, HashMap<Integer, HashMap<String, String>>> showsFile) {
        Set<String> oldEpisodeList = showsFile.get(aShow).get(aSeason).keySet();
        ArrayList<String> newEpisodesList = FindLocation.findEpisodes(folderLocation, aShow, aSeason);
        ArrayList<String> newEpisodesListFixed = new ArrayList<>(0);
        ArrayList<String> changedEpisodes = new ArrayList<>();
        if ((oldEpisodeList.isEmpty()) && newEpisodesList.isEmpty()) {
            return changedEpisodes;
        }
        if (newEpisodesList != null) {
            newEpisodesList.forEach(aNewEpisode -> {
                ArrayList<Integer> EpisodeInfo = ShowInfoController.getEpisodeSeasonInfo(aNewEpisode);
                if (!EpisodeInfo.isEmpty()) {
                    if (EpisodeInfo.size() == 2) {
                        String episodeNumber = String.valueOf(EpisodeInfo.get(1));
                        newEpisodesListFixed.add(episodeNumber);
                    } else if (EpisodeInfo.size() == 3) {
                        String episodeNumber = String.valueOf(EpisodeInfo.get(1) + "+" + EpisodeInfo.get(2));
                        newEpisodesListFixed.add(episodeNumber);
                    }
                }
            });
            changedEpisodes.addAll(oldEpisodeList.stream().filter(aOldEpisode -> !newEpisodesListFixed.contains(aOldEpisode)).collect(Collectors.toList()));
            changedEpisodes.addAll(newEpisodesListFixed.stream().filter(newEpisode -> !oldEpisodeList.contains(newEpisode)).collect(Collectors.toList()));
        } else return (ArrayList<String>) oldEpisodeList;
        return changedEpisodes;
    }

    private static ArrayList<Integer> hasSeasonsChanged(String aShow, File folderLocation, HashMap<String, HashMap<Integer, HashMap<String, String>>> showsFile) {
        Set<Integer> oldSeasons = showsFile.get(aShow).keySet();
        ArrayList<Integer> newSeasons = FindLocation.findSeasons(folderLocation, aShow);
        Iterator<Integer> newSeasonsIterator = newSeasons.iterator();
        while (newSeasonsIterator.hasNext()) {
            if (isSeasonEmpty(aShow, newSeasonsIterator.next(), folderLocation)) {
                newSeasonsIterator.remove();
            }
        }
        ArrayList<Integer> ChangedSeasons = new ArrayList<>();
        ChangedSeasons.addAll(oldSeasons.stream().filter(aOldSeason -> !newSeasons.contains(aOldSeason)).collect(Collectors.toList()));
        ChangedSeasons.addAll(newSeasons.stream().filter(aNewSeason -> !oldSeasons.contains(aNewSeason)).collect(Collectors.toList()));
        return ChangedSeasons;
    }

    private static HashMap<String, HashMap<Integer, HashMap<String, String>>> hasShowsChanged(File folderLocation, HashMap<String, HashMap<Integer, HashMap<String, String>>> showsFile, Boolean forceRun) {
        HashMap<String, HashMap<Integer, HashMap<String, String>>> newShows = new HashMap<>(0);
        Set<String> oldShows = showsFile.keySet();
        FindLocation.findShows(folderLocation).forEach(aShow -> {
            if (forceRun || runNumber == 5) {
                emptyShows = new ArrayList<>();
                runNumber = 0;
            }
            if (Main.running && !oldShows.contains(aShow) && !emptyShows.contains(aShow)) {
                log.info("Currently checking if new & valid: " + aShow);
                // Integer = Season Number -- HashMap = Episodes in that season from episodeNumEpisode
                HashMap<Integer, HashMap<String, String>> seasonEpisode = new HashMap<>(0);
                FindLocation.findSeasons(folderLocation, aShow).forEach(aSeason -> {
                    // First String = Episode Number -- Second String = Episode Location
                    HashMap<String, String> episodeNumEpisode = new HashMap<>(0);
                    ArrayList<String> episodesFull = FindLocation.findEpisodes(folderLocation, aShow, aSeason);
                    if (!episodesFull.isEmpty()) {
                        episodesFull.forEach(aEpisode -> {
                            ArrayList<Integer> episode = ShowInfoController.getEpisodeSeasonInfo(aEpisode);
                            if (!episode.isEmpty()) {
                                if (episode.size() == 2) {
                                    String episodeNumber = String.valueOf(episode.get(1));
                                    episodeNumEpisode.put(episodeNumber, (folderLocation + "\\" + aShow + '\\' + "Season " + aSeason + '\\' + aEpisode));
                                } else if (episode.size() == 3) {
                                    String episodeNumber = String.valueOf(episode.get(1) + "+" + episode.get(2));
                                    episodeNumEpisode.put(episodeNumber, (folderLocation + "\\" + aShow + '\\' + "Season " + aSeason + '\\' + aEpisode));
                                } else {
                                    log.warning("Error 1 if at this point!" + " + " + episode);
                                }
                            }
                        });
                        if (!episodeNumEpisode.isEmpty()) {
                            seasonEpisode.put(aSeason, episodeNumEpisode);
                        }
                    }
                });
                if (!seasonEpisode.keySet().isEmpty()) {
                    newShows.put(aShow, seasonEpisode);
                } else emptyShows.add(aShow);
            }
        });
        runNumber++;
        return newShows;
    }

    public static ArrayList<String> getEmptyShows() {
        return emptyShows;
    }

    private static Boolean isSeasonEmpty(String aShow, Integer aSeason, File folderLocation) {
        ArrayList<String> episodesFull = FindLocation.findEpisodes(folderLocation, aShow, aSeason);
        final Boolean[] answer = {true};
        if (!episodesFull.isEmpty()) {
            episodesFull.forEach(aEpisode -> {
                if (!ShowInfoController.getEpisodeSeasonInfo(aEpisode).isEmpty()) {
                    answer[0] = false;
                }
            });
        }
        return answer[0];
    }
}
