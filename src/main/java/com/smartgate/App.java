package com.smartgate;

import com.smartgate.network.IntercomClient;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
    private static final String DEFAULT_INTERCOM_IP = "192.168.1.100";

    @Override
    public void start(Stage stage) {
        IntercomClient intercomClient = new IntercomClient(DEFAULT_INTERCOM_IP);

        Label title = new Label("SmartGate Guard Console");
        title.getStyleClass().add("title");

        Label status = new Label("Interkom IP: " + DEFAULT_INTERCOM_IP);
        status.getStyleClass().add("status");

        Button unlockButton = new Button("Kapiyi Ac");
        unlockButton.setOnAction(event -> intercomClient.unlockDoor());

        Button handshakeButton = new Button("Handshake Gonder");
        handshakeButton.setOnAction(event -> intercomClient.sendSecurityHandshake(1, "192.168.1.150"));

        HBox actions = new HBox(12, unlockButton, handshakeButton);
        VBox content = new VBox(16, title, status, actions);
        content.setPadding(new Insets(24));

        BorderPane root = new BorderPane(content);
        Scene scene = new Scene(root, 900, 560);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setTitle("SmartGate Guard Console");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

