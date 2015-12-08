package com.maddogten.mtrack.gui;

import com.maddogten.mtrack.information.show.Directory;
import com.maddogten.mtrack.io.FileManager;
import com.maddogten.mtrack.io.MoveWindow;
import com.maddogten.mtrack.util.ImageLoader;
import com.maddogten.mtrack.util.Strings;
import com.maddogten.mtrack.util.Variables;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TextBox {
    private static final Logger log = Logger.getLogger(TextBox.class.getName());

    @SuppressWarnings("SameParameterValue")
    public String display(String message, String messageIfNameFieldIsBlank, String defaultValue, ArrayList<String> allUsers, Window oldWindow) {
        log.finest("TextBox display has been opened.");
        Stage window = new Stage();
        window.getIcons().add(ImageLoader.getImage(Variables.Logo));
        final String[] userName = new String[1];

        window.initStyle(StageStyle.UNDECORATED);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(250);

        Label label = new Label();
        label.setText(message);

        TextField textField = new TextField();
        Button submit = new Button(Strings.Submit);
        submit.setOnAction(event -> {
            if (isValid(messageIfNameFieldIsBlank, textField.getText(), allUsers, window)) {
                userName[0] = textField.getText();
                window.close();
            } else {
                textField.clear();
            }
        });

        VBox layout = new VBox();
        layout.getChildren().addAll(label, textField, submit);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);

        window.setScene(scene);
        Platform.runLater(() -> {
            if (oldWindow != null) {
                window.setX(oldWindow.getX() + (oldWindow.getWidth() / 2) - (window.getWidth() / 2));
                window.setY(oldWindow.getY() + (oldWindow.getHeight() / 2) - (window.getHeight() / 2));
            }
            new MoveWindow().moveWindow(window, oldWindow);
        });
        window.showAndWait();

        if (userName[0].isEmpty()) {
            return defaultValue;
        } else return userName[0];
    }

    private boolean isValid(String messageIfBlank, String message, ArrayList<String> allUsers, Window oldWindow) {
        log.finest("isValid has been called.");
        if (message.isEmpty()) {
            return new ConfirmBox().display(messageIfBlank, oldWindow);
        } else if (message.contentEquals(Strings.AddNewUsername) || !message.matches("^[a-zA-Z0-9]+$")) {
            new MessageBox().display(new String[]{Strings.UsernameIsntValid}, oldWindow);
            return false;
        } else if (allUsers.contains(message)) {
            new MessageBox().display(new String[]{Strings.UsernameAlreadyTaken}, oldWindow);
            return false;
        } else if (message.length() > 20) {
            new MessageBox().display(new String[]{Strings.UsernameIsTooLong}, oldWindow);
            return false;
        } else return true;
    }

    @SuppressWarnings("SameParameterValue")
    public File addDirectoriesDisplay(ArrayList<Directory> currentDirectories, Window oldWindow) {
        log.finest("TextBox addDirectoriesDisplay has been opened.");
        Stage window = new Stage();
        ImageLoader.setIcon(window);
        final File[] directories = new File[1];

        window.initStyle(StageStyle.UNDECORATED);
        window.initModality(Modality.APPLICATION_MODAL);
        window.setMinWidth(250);

        Label label = new Label(Strings.PleaseEnterShowsDirectory);

        TextField textField = new TextField();
        textField.setPromptText(Strings.FileSeparator + Strings.PathToDirectory + Strings.FileSeparator + Strings.Shows);

        ArrayList<String> directoryPaths = new ArrayList<>();
        currentDirectories.forEach(aDirectory -> directoryPaths.add(String.valueOf(aDirectory.getDirectory())));

        Button submit = new Button(Strings.Submit);
        submit.setOnAction(e -> {
            if (isDirectoryValid(directoryPaths, textField.getText(), window)) {
                directories[0] = new File(textField.getText());
                window.close();
            } else textField.clear();
        });

        Button exit = new Button(Strings.ExitButtonText);
        exit.setOnAction(e -> {
            directories[0] = new File(Strings.EmptyString);
            window.close();
        });

        HBox hBox = new HBox();
        hBox.getChildren().addAll(submit, exit);
        hBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox();
        layout.getChildren().addAll(label, textField, hBox);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);

        window.setScene(scene);
        Platform.runLater(() -> {
            if (oldWindow != null) {
                window.setX(oldWindow.getX() + (oldWindow.getWidth() / 2) - (window.getWidth() / 2));
                window.setY(oldWindow.getY() + (oldWindow.getHeight() / 2) - (window.getHeight() / 2));
            }
            new MoveWindow().moveWindow(window, oldWindow);
            window.requestFocus();
        });
        window.showAndWait();

        return directories[0];
    }

    private boolean isDirectoryValid(ArrayList<String> currentDirectories, String directory, Window oldWindow) {
        log.finest("isDirectoryValid has been called.");
        if (currentDirectories.contains(directory)) {
            new MessageBox().display(new String[]{Strings.DirectoryIsAlreadyAdded}, oldWindow);
            return false;
        } else if (new FileManager().checkFolderExistsAndReadable(new File(directory))) {
            return true;
        } else if (directory.isEmpty()) {
            new MessageBox().display(new String[]{Strings.YouNeedToEnterADirectory}, oldWindow);
            return false;
        } else {
            new MessageBox().display(new String[]{Strings.DirectoryIsInvalid}, oldWindow);
            return false;
        }
    }
}
