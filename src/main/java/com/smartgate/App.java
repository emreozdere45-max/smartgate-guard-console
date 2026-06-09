package com.smartgate;

import com.smartgate.network.IntercomClient;
import com.smartgate.ui.MainController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static final String INTERCOM_IP = "192.168.1.100";

    @Override
    public void start(Stage stage) {
        IntercomClient intercomClient = new IntercomClient(INTERCOM_IP);
        MainController controller = new MainController(intercomClient);

        Scene scene = new Scene(controller.buildUI(), 1100, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());

        stage.setTitle("SmartGate Guard Console");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}