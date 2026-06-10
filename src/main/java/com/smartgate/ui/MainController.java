package com.smartgate.ui;

import com.smartgate.database.AlarmDAO;
import com.smartgate.database.GateLogDAO;
import com.smartgate.llm.TextToSqlService;
import com.smartgate.model.Alarm;
import com.smartgate.model.GateLog;
import com.smartgate.network.IntercomClient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.smartgate.BackendApiClient;
import java.time.LocalDateTime;
import java.util.List;

public class MainController {
    private final BackendApiClient backendApiClient = new BackendApiClient();
    private final IntercomClient intercomClient;
    private final GateLogDAO gateLogDAO = new GateLogDAO();
    private final AlarmDAO alarmDAO = new AlarmDAO();
    private final TextToSqlService textToSqlService = new TextToSqlService();

    private TableView<GateLog> gateLogTable;
    private TableView<Alarm> alarmTable;
    private TextArea llmOutput;
    private TextField llmInput;

    public MainController(IntercomClient intercomClient) {
        this.intercomClient = intercomClient;
    }

    public BorderPane buildUI() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Üst başlık
        Label title = new Label("SmartGate Guard Console");
        title.getStyleClass().add("title");
        HBox header = new HBox(title);
        header.setPadding(new Insets(16, 24, 16, 24));
        header.getStyleClass().add("header");

        // Sol panel - Kontroller
        VBox leftPanel = buildLeftPanel();
        leftPanel.setPrefWidth(220);

        // Orta panel - Tablolar
        VBox centerPanel = buildCenterPanel();

        // Alt panel - LLM
        VBox bottomPanel = buildBottomPanel();

        root.setTop(header);
        root.setLeft(leftPanel);
        root.setCenter(centerPanel);
        root.setBottom(bottomPanel);
        // Otomatik yenileme - her 10 saniyede bir
        javafx.animation.Timeline autoRefresh = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(10),
                        e -> refreshTables()
                )
        );
        autoRefresh.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefresh.play();

        return root;
    }

    private VBox buildLeftPanel() {
        Button unlockBtn = new Button("🔓 Kapıyı Aç");
        unlockBtn.getStyleClass().add("btn-primary");
        unlockBtn.setMaxWidth(Double.MAX_VALUE);
        unlockBtn.setOnAction(e -> {
            new Thread(() -> {
                boolean success = backendApiClient.unlockDoor("1");
                if (success) {
                    System.out.println("Backend üzerinden kapı açıldı.");
                } else {
                    System.out.println("Backend bağlanamadı, direkt TCP deneniyor...");
                    intercomClient.unlockDoor();
                }
                logGateOpen();
            }).start();
        });

        Button handshakeBtn = new Button("🤝 Handshake");
        handshakeBtn.getStyleClass().add("btn-secondary");
        handshakeBtn.setMaxWidth(Double.MAX_VALUE);
        handshakeBtn.setOnAction(e -> intercomClient.sendSecurityHandshake(1, "10.194.166.78"));

        Button refreshBtn = new Button("🔄 Yenile");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshTables());

        Label connLabel = new Label("Interkom Bağlantısı");
        connLabel.getStyleClass().add("section-label");

        VBox panel = new VBox(12, connLabel, unlockBtn, handshakeBtn, new Separator(), refreshBtn);
        panel.setPadding(new Insets(16));
        panel.getStyleClass().add("left-panel");
        return panel;
    }

    private VBox buildCenterPanel() {
        // Gate log tablosu
        gateLogTable = new TableView<>();
        TableColumn<GateLog, String> timeCol = new TableColumn<>("Zaman");
        timeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getUnlockTime() != null ?
                                data.getValue().getUnlockTime().toLocalDate().toString() + " " +
                                        data.getValue().getUnlockTime().toLocalTime().toString().substring(0, 8) : ""
                ));
        timeCol.setPrefWidth(160);

        TableColumn<GateLog, String> methodCol = new TableColumn<>("Yöntem");
        methodCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getUnlockMethod()));
        methodCol.setPrefWidth(120);

        gateLogTable.getColumns().addAll(timeCol, methodCol);
        gateLogTable.setPrefHeight(200);
        gateLogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Alarm tablosu
        alarmTable = new TableView<>();
        TableColumn<Alarm, String> alarmTimeCol = new TableColumn<>("Zaman");
        alarmTimeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getAlarmTime() != null ?
                                data.getValue().getAlarmTime().toLocalDate().toString() + " " +
                                        data.getValue().getAlarmTime().toLocalTime().toString().substring(0, 8) : ""
                ));
        alarmTimeCol.setPrefWidth(160);

        TableColumn<Alarm, String> alarmTypeCol = new TableColumn<>("Tip");
        alarmTypeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getAlarmType()));
        alarmTypeCol.setPrefWidth(100);

        TableColumn<Alarm, String> alarmAptCol = new TableColumn<>("Daire");
        alarmAptCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getApartmentNo()));
        alarmAptCol.setPrefWidth(120);

        alarmTable.getColumns().addAll(alarmTimeCol, alarmTypeCol, alarmAptCol);
        alarmTable.setPrefHeight(200);
        alarmTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Label logLabel = new Label("Kapı Giriş Kayıtları");
        logLabel.getStyleClass().add("section-label");
        Label alarmLabel = new Label("Aktif Alarmlar");
        alarmLabel.getStyleClass().add("section-label");

        VBox panel = new VBox(8, logLabel, gateLogTable, alarmLabel, alarmTable);
        panel.setPadding(new Insets(16));
        return panel;
    }

    private VBox buildBottomPanel() {
        llmInput = new TextField();
        llmInput.setPromptText("Örn: Bugün açılan kapıları listele...");
        llmInput.getStyleClass().add("llm-input");

        Button askBtn = new Button("🤖 Sorgula");
        askBtn.getStyleClass().add("btn-primary");
        askBtn.setOnAction(e -> handleLlmQuery());

        llmOutput = new TextArea();
        llmOutput.setEditable(false);
        llmOutput.setPrefHeight(100);
        llmOutput.getStyleClass().add("llm-output");

        HBox inputRow = new HBox(8, llmInput, askBtn);
        HBox.setHgrow(llmInput, Priority.ALWAYS);

        Label llmLabel = new Label("🧠 Yapay Zeka Sorgu");
        llmLabel.getStyleClass().add("section-label");

        VBox panel = new VBox(8, llmLabel, inputRow, llmOutput);
        panel.setPadding(new Insets(16));
        panel.getStyleClass().add("bottom-panel");
        return panel;
    }

    private void handleLlmQuery() {
        String question = llmInput.getText().trim();
        if (question.isEmpty()) return;

        llmOutput.setText("Sorgu üretiliyor...");
        new Thread(() -> {
            String result = textToSqlService.generateAndExecute(question);
            Platform.runLater(() -> llmOutput.setText(result));
        }).start();
    }

    private void logGateOpen() {
        new Thread(() -> {
            GateLog log = new GateLog();
            log.setUnlockTime(LocalDateTime.now());
            log.setUnlockMethod("CONSOLE");
            log.setGateId(1);
            log.setResidentId(0);
            gateLogDAO.insert(log);
            Platform.runLater(() -> refreshTables());
        }).start();
    }

    private void refreshTables() {
        List<GateLog> logs = gateLogDAO.getAll();
        List<Alarm> alarms = alarmDAO.getUnresolved();
        Platform.runLater(() -> {
            gateLogTable.setItems(FXCollections.observableArrayList(logs));
            alarmTable.setItems(FXCollections.observableArrayList(alarms));
        });
    }
}