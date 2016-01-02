package com.maddogten.mtrack.gui;

import com.maddogten.mtrack.information.show.Directory;
import com.maddogten.mtrack.io.FileManager;
import com.maddogten.mtrack.io.MoveStage;
import com.maddogten.mtrack.util.GenericMethods;
import com.maddogten.mtrack.util.Strings;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
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

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
      TextBox handles multiple stages which allow the user to type their input. They check that the input is valid for the given request, and if it is, returns it.
      addUser- Allows the user to give a username, then checks it against isUserValid(), and if it is, returns the username. If the username is empty, The user name pick
      to use the default username, or enter one. If the username is invalid, The reason why is displayed, and allows the user to correct the issue.
      addDirectory- Allows the user to add a directory, then checks that the directory isn't already added, exists, and is otherwise valid.
 */

public class TextBox {
    private static final Logger log = Logger.getLogger(TextBox.class.getName());

    @SuppressWarnings("SameParameterValue")
    public String addUser(StringProperty message, StringProperty messageIfNameFieldIsBlank, String defaultValue, ArrayList<String> allUsers, Stage oldStage) {
        log.fine("addUser display has been opened.");

        Stage addUserStage = new Stage();
        if (oldStage != null) addUserStage.initOwner(oldStage);
        addUserStage.initStyle(StageStyle.UNDECORATED);
        addUserStage.initModality(Modality.APPLICATION_MODAL);
        addUserStage.setMinWidth(250);
        GenericMethods.setIcon(addUserStage);

        Label label = new Label();
        label.textProperty().bind(message);

        TextField textField = new TextField();
        final String[] userName = new String[]{Strings.EmptyString};
        Button submit = new Button();
        submit.textProperty().bind(Strings.Submit);
        submit.setOnAction(event -> {
            if (textField.getText().isEmpty() && new ConfirmBox().confirm(messageIfNameFieldIsBlank, oldStage)) {
                userName[0] = defaultValue;
                addUserStage.close();
            } else if (isUserValid(textField.getText(), allUsers, addUserStage)) {
                userName[0] = textField.getText();
                addUserStage.close();
            }
        });
        Button exitButton = new Button();
        exitButton.setText(Strings.ExitButtonText);
        exitButton.setOnAction(e -> {
            userName[0] = Strings.EmptyString;
            addUserStage.close();
        });

        HBox buttonLayout = new HBox();
        buttonLayout.getChildren().addAll(submit, exitButton);
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.setPadding(new Insets(5, 0, 0, 0));
        buttonLayout.setSpacing(3);

        VBox layout = new VBox();
        layout.getChildren().addAll(label, textField, buttonLayout);
        layout.setAlignment(Pos.CENTER);

        Platform.runLater(() -> new MoveStage().moveStage(layout, oldStage));

        addUserStage.setScene(new Scene(layout));
        addUserStage.show();
        addUserStage.hide();
        if (addUserStage.getOwner() != null) {
            addUserStage.setX(addUserStage.getOwner().getX() + (addUserStage.getOwner().getWidth() / 2) - (addUserStage.getWidth() / 2));
            addUserStage.setY(addUserStage.getOwner().getY() + (addUserStage.getOwner().getHeight() / 2) - (addUserStage.getHeight() / 2));
        }
        addUserStage.showAndWait();

        log.fine("addUser display has been closed.");
        return userName[0];
    }

    private boolean isUserValid(String user, ArrayList<String> allUsers, Stage oldStage) {
        log.fine("isUserValid has been called.");
        if (user.contentEquals(Strings.AddNewUsername.getValue()) || !user.matches("^[a-zA-Z0-9]+$"))
            new MessageBox().message(new StringProperty[]{Strings.UsernameIsntValid}, oldStage);
        else if (allUsers.contains(user))
            new MessageBox().message(new StringProperty[]{Strings.UsernameAlreadyTaken}, oldStage);
        else if (user.length() > 20)
            new MessageBox().message(new StringProperty[]{Strings.UsernameIsTooLong}, oldStage);
        else return true;
        return false;
    }

    public File addDirectory(ArrayList<Directory> currentDirectories, Stage oldStage) {
        log.fine("addDirectory has been opened.");

        Stage addDirectoryStage = new Stage();
        if (oldStage != null) addDirectoryStage.initOwner(oldStage);
        addDirectoryStage.initStyle(StageStyle.UNDECORATED);
        addDirectoryStage.initModality(Modality.APPLICATION_MODAL);
        addDirectoryStage.setMinWidth(250);
        GenericMethods.setIcon(addDirectoryStage);

        Label label = new Label();
        label.textProperty().bind(Strings.PleaseEnterShowsDirectory);

        TextField textField = new TextField();
        textField.setPromptText(Strings.FileSeparator + Strings.PathToDirectory.getValue() + Strings.FileSeparator + Strings.Shows.getValue());

        ArrayList<String> directoryPaths = new ArrayList<>();
        currentDirectories.forEach(aDirectory -> directoryPaths.add(String.valueOf(aDirectory.getDirectory())));
        final File[] directories = new File[1];
        Button submit = new Button();
        submit.textProperty().bind(Strings.Submit);
        submit.setOnAction(e -> {
            if (isDirectoryValid(directoryPaths, textField.getText(), addDirectoryStage)) {
                directories[0] = new File(textField.getText());
                addDirectoryStage.close();
            }
        });
        Button exit = new Button(Strings.ExitButtonText);
        exit.setOnAction(e -> {
            directories[0] = new File(Strings.EmptyString);
            addDirectoryStage.close();
        });

        HBox hBox = new HBox();
        hBox.getChildren().addAll(submit, exit);
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(3, 0, 6, 0));
        hBox.setSpacing(3);

        VBox layout = new VBox();
        layout.getChildren().addAll(label, textField, hBox);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(6, 6, 6, 6));

        Platform.runLater(() -> {
            new MoveStage().moveStage(layout, oldStage);
            addDirectoryStage.requestFocus();
        });

        addDirectoryStage.setScene(new Scene(layout));
        addDirectoryStage.show();
        addDirectoryStage.hide();
        if (addDirectoryStage.getOwner() != null) {
            addDirectoryStage.setX(addDirectoryStage.getOwner().getX() + (addDirectoryStage.getOwner().getWidth() / 2) - (addDirectoryStage.getWidth() / 2));
            addDirectoryStage.setY(addDirectoryStage.getOwner().getY() + (addDirectoryStage.getOwner().getHeight() / 2) - (addDirectoryStage.getHeight() / 2));
        }
        addDirectoryStage.showAndWait();

        log.fine("addDirectory has been closed.");
        return directories[0];
    }

    private boolean isDirectoryValid(ArrayList<String> currentDirectories, String directory, Stage oldStage) {
        log.fine("isDirectoryValid has been called.");
        if (currentDirectories.contains(directory))
            new MessageBox().message(new StringProperty[]{Strings.DirectoryIsAlreadyAdded}, oldStage);
        else if (new FileManager().checkFolderExistsAndReadable(new File(directory))) return true;
        else if (directory.isEmpty())
            new MessageBox().message(new StringProperty[]{Strings.YouNeedToEnterADirectory}, oldStage);
        else new MessageBox().message(new StringProperty[]{Strings.DirectoryIsInvalid}, oldStage);
        return false;
    }
}
