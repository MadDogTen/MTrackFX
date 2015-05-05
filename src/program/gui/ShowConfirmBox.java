package program.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import program.input.MoveWindow;

public class ShowConfirmBox {

    int answer;
    private MoveWindow moveWindow = new MoveWindow();

    public int display(String title, String message, TabPane oldTabPane) {
        Stage window = new Stage();
        window.initStyle(StageStyle.UNDECORATED);

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);

        Label label = new Label();
        label.setText(message);

        Button yesButton = new Button("Yes"), noButton = new Button("No");
        yesButton.setMinHeight(20);
        yesButton.setMinWidth(30);
        noButton.setMinHeight(20);
        noButton.setMinWidth(30);


        yesButton.setOnAction(e -> {
            answer = 1;
            window.close();
        });

        noButton.setOnAction(e -> {
            answer = 0;
            window.close();
        });

        HBox layout2 = new HBox();
        layout2.getChildren().addAll(yesButton, noButton);
        layout2.setAlignment(Pos.CENTER);

        layout2.setPadding(new Insets(4, 6, 6, 6));

        Button nextEpisode = new Button("Next Episode");
        nextEpisode.setMinHeight(20);
        nextEpisode.setMinWidth(30);

        nextEpisode.setOnAction(e -> {
            answer = 2;
            window.close();
        });

        VBox layout = new VBox();
        layout.getChildren().addAll(label, layout2, nextEpisode);
        layout.setAlignment(Pos.CENTER);

        layout.setPadding(new Insets(6, 0, 0, 0));

        Scene scene = new Scene(layout);

        moveWindow.moveWindow(window, scene);

        window.setScene(scene);
        window.setX((oldTabPane.getScene().getWindow().getX() + oldTabPane.getWidth() / 2));
        window.setY((oldTabPane.getScene().getWindow().getY() + oldTabPane.getHeight() / 2));
        window.showAndWait();

        return answer;
    }
}
