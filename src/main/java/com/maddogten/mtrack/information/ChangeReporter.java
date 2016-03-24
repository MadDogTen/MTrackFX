package com.maddogten.mtrack.information;

import com.maddogten.mtrack.util.Strings;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/*
      ChangeReporter holds all the strings that the ChangesBox displays.
      Any changes added are put on top of the list.
 */

public class ChangeReporter {
    private static final Logger log = Logger.getLogger(ChangeReporter.class.getName());
    private static final Set<String> changedShows = new HashSet<>();
    // Stores all the changes that are added with addChange().
    private static String[] changes = new String[0];
    private static boolean isChanges = false;

    // This first saves the current list, Reinitialize changes as the the old length + 1, Adds the newInfo to changes[0], then iterates thorough the rest adding them started at changes[1].
    public static void addChange(String newInfo) {
        int toRemove = -1;
        for (int i = 0; i < changes.length; i++) {
            if (changes[i].replace("+", "a").replace("-", "m").matches(newInfo.replace("+", "a").replace("-", "m"))) {
                toRemove = i;
                break;
            }
        }
        if (toRemove != -1) {
            String[] correctedList = new String[changes.length - 1];
            for (int i = 0; i < changes.length; i++)
                if (toRemove != i) correctedList[i > toRemove ? i - 1 : i] = changes[i];
            changes = correctedList;
        }

        log.info("Adding new change: \"" + newInfo + "\".");
        String[] currentList = changes;
        changes = new String[currentList.length + 1];
        changes[0] = newInfo;
        int iterator = 1;
        for (String aString : currentList) {
            changes[iterator] = aString;
            iterator++;
        }
        changedShows.add(newInfo.contains(Strings.DashSeason.getValue()) ? newInfo.replaceFirst("[+~\\-]\\s", "").split(Strings.DashSeason.getValue())[0] : newInfo.replaceFirst("[+~\\-]\\s", ""));
        if (!isChanges) isChanges = true;
    }

    // This completely clears the changes String[] so it can start new.
    public static void resetChanges() {
        if (changes.length > 0) {
            changes = new String[0];
            isChanges = false;
        }
        if (!changedShows.isEmpty()) changedShows.clear();
        log.info("Change list has been cleared.");
    }

    public static String[] getChanges() {
        return changes;
    }

    public static void setChanges(String[] newChangeList) {
        if (newChangeList.length > 0) for (String change : newChangeList)
            changedShows.add(change.contains(Strings.DashSeason.getValue()) ? change.replaceFirst("[+~\\-]\\s", "").split(Strings.DashSeason.getValue())[0] : change.replaceFirst("[+~\\-]\\s", ""));
        if (changes.length == 0) changes = newChangeList;
        else {
            String[] tempSave = changes;
            changes = newChangeList;
            for (int i = tempSave.length - 1; i >= 0; i--) addChange(tempSave[i]);
        }
        if (changes.length > 0 && !isChanges) isChanges = true;
    }

    public static boolean getIsChanges() {
        return isChanges;
    }

    public static void setIsChanges(final boolean isChanges) {
        ChangeReporter.isChanges = isChanges;
    }

    public static boolean wasShowChanged(String aShow) {
        return changedShows.contains(aShow);
    }
}
