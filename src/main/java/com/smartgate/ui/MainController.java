package com.smartgate.ui;

import com.smartgate.BackendApiClient;
import com.smartgate.database.AlarmDAO;
import com.smartgate.database.GateLogDAO;
import com.smartgate.llm.TextToSqlService;
import com.smartgate.model.Alarm;
import com.smartgate.model.GateLog;
import com.smartgate.model.Visitor;
import com.smartgate.network.IntercomClient;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class MainController {
    private final BackendApiClient backendApiClient = new BackendApiClient();
    private final IntercomClient intercomClient;
    private final GateLogDAO gateLogDAO = new GateLogDAO();
    private final AlarmDAO alarmDAO = new AlarmDAO();
    private final TextToSqlService textToSqlService = new TextToSqlService();

    private TableView<GateLog> gateLogTable;
    private TableView<Alarm> alarmTable;
    private TableView<Visitor> visitorTable;
    private TextField visitorNameInput;
    private ComboBox<String> visitorTypeInput;
    private TextField blockInput;
    private TextField apartmentInput;
    private TextField visitReasonInput;

    // AI popup
    private VBox aiPopup;
    private TextField aiInput;
    private TextArea aiOutput;
    private boolean aiOpen = false;

    public MainController(IntercomClient intercomClient) {
        this.intercomClient = intercomClient;
    }

    public BorderPane buildUI() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        root.setTop(buildHeader());
        root.setLeft(buildLeftPanelWithAi());
        root.setCenter(buildCenterPanel());

        startAutoRefresh();
        startIntercomListener();

        return root;
    }

    private HBox buildHeader() {
        Label title = new Label("🛡 SmartGate Guard Console");
        title.getStyleClass().add("title");

        Label clock = new Label();
        clock.getStyleClass().add("clock-label");
        Timeline clockTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            clock.setText(ZonedDateTime.now(ZoneId.of("Europe/Istanbul"))
                    .toLocalTime().toString().substring(0, 8));
        }));
        clockTimer.setCycleCount(Animation.INDEFINITE);
        clockTimer.play();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12, title, spacer, clock);
        header.setPadding(new Insets(14, 24, 14, 24));
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("header");
        return header;
    }

    private VBox buildLeftPanelWithAi() {
        Label connLabel = new Label("KONTROL");
        connLabel.getStyleClass().add("section-label");

        Button unlockBtn = new Button("🔓  Kapıyı Aç");
        unlockBtn.getStyleClass().add("btn-primary");
        unlockBtn.setMaxWidth(Double.MAX_VALUE);
        unlockBtn.setOnAction(e -> new Thread(() -> {
            boolean success = backendApiClient.unlockDoor("1");
            if (!success) intercomClient.unlockDoor();
            Platform.runLater(this::refreshTables);
        }).start());

        Button handshakeBtn = new Button("🤝  Handshake");
        handshakeBtn.getStyleClass().add("btn-secondary");
        handshakeBtn.setMaxWidth(Double.MAX_VALUE);
        handshakeBtn.setOnAction(e -> intercomClient.sendSecurityHandshake(1, "10.194.166.78"));

        Button refreshBtn = new Button("🔄  Yenile");
        refreshBtn.getStyleClass().add("btn-secondary");
        refreshBtn.setMaxWidth(Double.MAX_VALUE);
        refreshBtn.setOnAction(e -> refreshTables());

        Button testAlarmBtn = new Button("🚨  Test Alarm");
        testAlarmBtn.getStyleClass().add("btn-warning");
        testAlarmBtn.setMaxWidth(Double.MAX_VALUE);
        testAlarmBtn.setOnAction(e -> triggerTestAlarm());

        Label statsLabel = new Label("DURUM");
        statsLabel.getStyleClass().add("section-label");

        Label dbStatus = new Label("● PostgreSQL");
        dbStatus.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 12px;");

        Label intercomStatus = new Label("● İnterkom Dinleniyor");
        intercomStatus.setStyle("-fx-text-fill: #3fb950; -fx-font-size: 12px;");

        // AI bölümü
        aiInput = new TextField();
        aiInput.setPromptText("Soru sor...");
        aiInput.getStyleClass().add("llm-input");
        aiInput.setVisible(false);
        aiInput.setManaged(false);

        aiOutput = new TextArea();
        aiOutput.setEditable(false);
        aiOutput.setPrefHeight(120);
        aiOutput.setWrapText(true);
        aiOutput.getStyleClass().add("llm-output");
        aiOutput.setVisible(false);
        aiOutput.setManaged(false);

        Button sendBtn = new Button("🤖 Sor");
        sendBtn.getStyleClass().add("btn-primary");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setVisible(false);
        sendBtn.setManaged(false);
        sendBtn.setOnAction(e -> handleAiQuery());
        aiInput.setOnAction(e -> handleAiQuery());

        // N butonu
        Circle circle = new Circle(24);
        circle.setFill(Color.web("#1f6feb"));
        circle.setStroke(Color.web("#388bfd"));
        circle.setStrokeWidth(2);

        Text nText = new Text("N");
        nText.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: white;");

        Circle ring = new Circle(30);
        ring.setFill(Color.TRANSPARENT);
        ring.setStroke(Color.web("#388bfd"));
        ring.setStrokeWidth(2);
        ring.getStrokeDashArray().addAll(15.0, 30.0);

        RotateTransition rotate = new RotateTransition(Duration.seconds(3), ring);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        StackPane aiBtn = new StackPane(ring, circle, nText);
        aiBtn.setCursor(javafx.scene.Cursor.HAND);
        aiBtn.setMaxWidth(Double.MAX_VALUE);
        aiBtn.setOnMouseClicked(e -> {
            aiOpen = !aiOpen;
            aiInput.setVisible(aiOpen);
            aiInput.setManaged(aiOpen);
            sendBtn.setVisible(aiOpen);
            sendBtn.setManaged(aiOpen);
            aiOutput.setVisible(aiOpen);
            aiOutput.setManaged(aiOpen);
            if (aiOpen) aiInput.requestFocus();
        });

        Label aiLabel = new Label("YAPAY ZEKA");
        aiLabel.getStyleClass().add("section-label");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox panel = new VBox(10,
                connLabel,
                unlockBtn, handshakeBtn,
                new Separator(),
                refreshBtn, testAlarmBtn,
                new Separator(),
                statsLabel,
                dbStatus, intercomStatus,
                spacer,
                new Separator(),
                aiLabel,
                aiBtn,
                aiInput,
                sendBtn,
                aiOutput
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(220);
        panel.getStyleClass().add("left-panel");
        return panel;
    }

    private ScrollPane buildCenterPanel() {
        // ── Üst satır: Kapı Logları | Aktif Alarmlar ──
        VBox logsBox = buildTableBox("📋  Kapı Giriş Kayıtları", buildGateLogTable());
        VBox alarmsBox = buildTableBox("🚨  Aktif Alarmlar", buildAlarmTable());

        HBox topRow = new HBox(12, logsBox, alarmsBox);
        HBox.setHgrow(logsBox, Priority.ALWAYS);
        HBox.setHgrow(alarmsBox, Priority.ALWAYS);

        // ── Alt satır: Ziyaretçi Yönetimi ──
        VBox visitorsBox = buildTableBox("👥  Ziyaretçi Kayıtları", buildVisitorSection());

        VBox center = new VBox(12, topRow, visitorsBox);
        center.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(center);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #0d1117; -fx-background: #0d1117;");
        return scroll;
    }

    private VBox buildTableBox(String title, javafx.scene.Node content) {
        Label label = new Label(title);
        label.getStyleClass().add("section-label");

        VBox box = new VBox(6, label, content);
        box.getStyleClass().add("card");
        box.setPadding(new Insets(12));
        return box;
    }

    private TableView<GateLog> buildGateLogTable() {
        gateLogTable = new TableView<>();
        gateLogTable.setPlaceholder(new Label("Kayıt yok"));

        TableColumn<GateLog, String> timeCol = new TableColumn<>("Zaman");
        timeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getUnlockTime() != null ?
                        d.getValue().getUnlockTime().toString().substring(0, 16).replace('T', ' ') : ""
        ));

        TableColumn<GateLog, String> methodCol = new TableColumn<>("Yöntem");
        methodCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getUnlockMethod()
        ));

        gateLogTable.getColumns().addAll(timeCol, methodCol);
        gateLogTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        gateLogTable.setPrefHeight(220);
        return gateLogTable;
    }

    private TableView<Alarm> buildAlarmTable() {
        alarmTable = new TableView<>();
        alarmTable.setPlaceholder(new Label("Aktif alarm yok ✓"));

        TableColumn<Alarm, String> timeCol = new TableColumn<>("Zaman");
        timeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getAlarmTime() != null ?
                        d.getValue().getAlarmTime().toString().substring(0, 16).replace('T', ' ') : ""
        ));

        TableColumn<Alarm, String> typeCol = new TableColumn<>("Tip");
        typeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getAlarmType()
        ));

        TableColumn<Alarm, String> severityCol = new TableColumn<>("Öncelik");
        severityCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getSeverity()
        ));

        TableColumn<Alarm, String> sourceCol = new TableColumn<>("Kaynak");
        sourceCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getSourceLabel()
        ));

        TableColumn<Alarm, Void> resolveCol = new TableColumn<>("");
        resolveCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✓ Çözüldü");
            { btn.getStyleClass().add("btn-success");
                btn.setOnAction(e -> {
                    Alarm a = getTableView().getItems().get(getIndex());
                    new Thread(() -> { alarmDAO.markResolved(a.getId()); Platform.runLater(() -> refreshTables()); }).start();
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });
        resolveCol.setPrefWidth(90);
        TableColumn<Alarm, String> warningCol = new TableColumn<>("");
        warningCol.setCellFactory(col -> new TableCell<>() {
            private final Label warningLabel = new Label("⚠");
            private Timeline blink;

            {
                warningLabel.setStyle("-fx-text-fill: #f85149; -fx-font-size: 16px;");
                blink = new Timeline(
                        new KeyFrame(Duration.millis(500), e -> warningLabel.setVisible(true)),
                        new KeyFrame(Duration.millis(1000), e -> warningLabel.setVisible(false))
                );
                blink.setCycleCount(Animation.INDEFINITE);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    blink.stop();
                    return;
                }
                Alarm alarm = getTableView().getItems().get(getIndex());
                if ("CRITICAL".equals(alarm.getSeverity())) {
                    warningLabel.setVisible(true);
                    blink.play();
                    setGraphic(warningLabel);
                } else {
                    blink.stop();
                    setGraphic(null);
                }
            }
        });
        warningCol.setPrefWidth(35);
        warningCol.setMaxWidth(35);
        warningCol.setMinWidth(35);
        alarmTable.getColumns().addAll(warningCol, timeCol, typeCol, severityCol, sourceCol, resolveCol);
        alarmTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        alarmTable.setPrefHeight(220);
        return alarmTable;
    }

    private VBox buildVisitorSection() {
        // Form
        visitorNameInput = new TextField();
        visitorNameInput.setPromptText("Ad Soyad");

        visitorTypeInput = new ComboBox<>(FXCollections.observableArrayList(
                "MISAFIR", "KURYE", "BAKICI", "TEMIZLIK", "TEKNIK_SERVIS", "GUVENLIK_PERSONELI"
        ));
        visitorTypeInput.setValue("MISAFIR");
        visitorTypeInput.setPrefWidth(140);

        blockInput = new TextField();
        blockInput.setPromptText("Blok");
        blockInput.setPrefWidth(70);

        apartmentInput = new TextField();
        apartmentInput.setPromptText("Daire");
        apartmentInput.setPrefWidth(70);
        apartmentInput.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                apartmentInput.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        visitReasonInput = new TextField();
        visitReasonInput.setPromptText("Ziyaret sebebi");

        Button saveBtn = new Button("➕ Kaydet");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> saveVisitor());

        HBox form = new HBox(8,
                visitorNameInput, visitorTypeInput,
                blockInput, apartmentInput,
                visitReasonInput, saveBtn
        );
        HBox.setHgrow(visitorNameInput, Priority.ALWAYS);
        HBox.setHgrow(visitReasonInput, Priority.ALWAYS);
        form.setAlignment(Pos.CENTER_LEFT);

        // Tablo
        visitorTable = new TableView<>();
        visitorTable.setPlaceholder(new Label("Ziyaretçi kaydı yok"));
        visitorTable.setPrefHeight(220);

        TableColumn<Visitor, String> timeCol = new TableColumn<>("Saat");
        timeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                formatApiTime(d.getValue().getEntryTime())
        ));
        timeCol.setPrefWidth(130);

        TableColumn<Visitor, String> nameCol = new TableColumn<>("Ad Soyad");
        nameCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getVisitorName()
        ));

        TableColumn<Visitor, String> typeCol = new TableColumn<>("Tür");
        typeCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getVisitorType()
        ));

        TableColumn<Visitor, String> aptCol = new TableColumn<>("Daire");
        aptCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getApartmentLabel()
        ));
        aptCol.setPrefWidth(80);

        TableColumn<Visitor, String> statusCol = new TableColumn<>("Durum");
        statusCol.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getStatus()
        ));
        statusCol.setPrefWidth(100);

        TableColumn<Visitor, Void> actionCol = new TableColumn<>("İşlem");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✓");
            private final Button rejectBtn = new Button("✗");
            private final Button exitBtn = new Button("Çıkış");
            private final HBox actions = new HBox(4, approveBtn, rejectBtn, exitBtn);
            {
                approveBtn.getStyleClass().add("btn-success");
                rejectBtn.getStyleClass().add("btn-danger");
                exitBtn.getStyleClass().add("btn-secondary-small");
                approveBtn.setOnAction(e -> updateVisitorStatus(getVisitor(), "approve"));
                rejectBtn.setOnAction(e -> updateVisitorStatus(getVisitor(), "reject"));
                exitBtn.setOnAction(e -> updateVisitorStatus(getVisitor(), "exit"));
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || getVisitor() == null) { setGraphic(null); return; }
                String s = getVisitor().getStatus() == null ? "" : getVisitor().getStatus();
                approveBtn.setDisable("APPROVED".equals(s) || "EXITED".equals(s));
                rejectBtn.setDisable("REJECTED".equals(s) || "EXITED".equals(s));
                exitBtn.setDisable("EXITED".equals(s));
                setGraphic(actions);
            }
            private Visitor getVisitor() {
                int i = getIndex();
                return (i < 0 || i >= getTableView().getItems().size()) ? null : getTableView().getItems().get(i);
            }
        });
        actionCol.setPrefWidth(160);

        visitorTable.getColumns().addAll(timeCol, nameCol, typeCol, aptCol, statusCol, actionCol);
        visitorTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return new VBox(8, form, visitorTable);
    }

    private void toggleAi(boolean open) {
        aiOpen = open;
        aiPopup.setVisible(open);
        aiPopup.setManaged(open);
        if (open) aiInput.requestFocus();
    }

    private void handleAiQuery() {
        String question = aiInput.getText().trim();
        if (question.isEmpty()) return;
        aiOutput.setText("Düşünüyor...");
        new Thread(() -> {
            String result = textToSqlService.generateAndExecute(question);
            Platform.runLater(() -> aiOutput.setText(result));
        }).start();
    }

    private void startAutoRefresh() {
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> refreshTables()));
        autoRefresh.setCycleCount(Animation.INDEFINITE);
        autoRefresh.play();
    }

    private void startIntercomListener() {
        intercomClient.startListening(packet -> {
            if (packet.getOpe_type() == 47) {
                Alarm alarm = createAlarmFromPacket(packet);
                new Thread(() -> {
                    alarmDAO.insert(alarm);
                    Platform.runLater(() -> {
                        refreshTables();
                        showAlarmPopup(alarm);
                    });
                }).start();
            }
        });
    }

    private String getAlarmTypeName(int dataInt) {
        return switch (dataInt) {
            case 1 -> "PIR";
            case 2 -> "MANYETİK";
            case 3 -> "SU BASMA";
            case 4 -> "YANGIN";
            case 5 -> "GAZ";
            default -> "ALARM";
        };
    }

    private void showAlarmPopup(Alarm alarm) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("⚠ ACİL ALARM");
        alert.setHeaderText(alarm.getAlarmType() + " ALARMI — " + alarm.getSeverity());
        alert.setContentText(
                "Daire: " + alarm.getApartmentNo() + "\n" +
                        "Kaynak: " + alarm.getSourceLabel() + "\n" +
                        "Saat: " + (alarm.getAlarmTime() != null ? alarm.getAlarmTime().toString().substring(11, 19) : "")
        );
        alert.show();
    }

    private void triggerTestAlarm() {
        Alarm alarm = new Alarm();
        alarm.setAlarmTime(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")).toLocalDateTime());
        alarm.setAlarmType("FIRE");
        alarm.setSeverity("CRITICAL");
        alarm.setApartmentNo("15B");
        alarm.setSourceLabel("Test yangın sensörü");
        alarm.setResolved(false);
        new Thread(() -> {
            alarmDAO.insert(alarm);
            Platform.runLater(() -> { refreshTables(); showAlarmPopup(alarm); });
        }).start();
    }

    private void saveVisitor() {
        String name = visitorNameInput.getText().trim();
        if (name.isEmpty()) return;
        new Thread(() -> {
            Visitor v = backendApiClient.createVisitor(
                    name, visitorTypeInput.getValue(),
                    blockInput.getText().trim(),
                    apartmentInput.getText().trim(),
                    visitReasonInput.getText().trim()
            );
            Platform.runLater(() -> {
                if (v != null) {
                    visitorNameInput.clear(); blockInput.clear();
                    apartmentInput.clear(); visitReasonInput.clear();
                    refreshTables();
                }
            });
        }).start();
    }

    private void updateVisitorStatus(Visitor visitor, String action) {
        if (visitor == null || visitor.getId() == null) return;
        new Thread(() -> {
            Visitor updated = switch (action) {
                case "approve" -> backendApiClient.approveVisitor(visitor.getId());
                case "reject" -> backendApiClient.rejectVisitor(visitor.getId());
                case "exit" -> backendApiClient.exitVisitor(visitor.getId());
                default -> null;
            };
            if (updated != null) Platform.runLater(this::refreshTables);
        }).start();
    }

    private void refreshTables() {
        new Thread(() -> {
            List<GateLog> logs = gateLogDAO.getAll();
            List<Alarm> alarms = alarmDAO.getUnresolved();
            List<Visitor> visitors = backendApiClient.getVisitors();
            Platform.runLater(() -> {
                gateLogTable.setItems(FXCollections.observableArrayList(logs));
                alarmTable.setItems(FXCollections.observableArrayList(alarms));
                visitorTable.setItems(FXCollections.observableArrayList(visitors));
            });
        }).start();
    }

    private String formatApiTime(String value) {
        if (value == null || value.length() < 16) return "";
        return value.substring(0, 16).replace('T', ' ');
    }
    private Alarm createAlarmFromPacket(com.smartgate.network.ComPackageModel packet) {
        Alarm alarm = new Alarm();
        alarm.setAlarmTime(ZonedDateTime.now(ZoneId.of("Europe/Istanbul")).toLocalDateTime());

        String rawData = packet.getDataString() != null ? packet.getDataString().trim() : "";
        String[] parts = rawData.split("\\|");

        String alarmType = parts.length > 0 && !parts[0].isBlank()
                ? normalizeAlarmType(parts[0]) : "UNKNOWN";
        String apartmentNo = parts.length > 1 && !parts[1].isBlank()
                ? parts[1].trim() : "Bilinmiyor";
        String sourceLabel = parts.length > 2 && !parts[2].isBlank()
                ? parts[2].trim() : getDefaultSourceLabel(alarmType);

        alarm.setAlarmType(alarmType);
        alarm.setApartmentNo(apartmentNo);
        alarm.setSourceLabel(sourceLabel);
        alarm.setSeverity(getSeverityForAlarmType(alarmType));
        alarm.setResolved(false);
        return alarm;
    }

    private String normalizeAlarmType(String rawType) {
        String type = rawType.trim().toUpperCase();
        return switch (type) {
            case "YANGIN", "FIRE" -> "FIRE";
            case "GAZ", "GAS" -> "GAS";
            case "SU", "SU_BASKINI", "FLOOD" -> "FLOOD";
            case "HAREKET", "PIR", "MOTION" -> "MOTION";
            case "KAPI", "PENCERE", "DOOR", "WINDOW", "DOOR_WINDOW" -> "DOOR_WINDOW";
            default -> "UNKNOWN";
        };
    }

    private String getSeverityForAlarmType(String alarmType) {
        return switch (alarmType) {
            case "FIRE", "GAS" -> "CRITICAL";
            case "FLOOD" -> "HIGH";
            case "MOTION", "DOOR_WINDOW" -> "MEDIUM";
            default -> "HIGH";
        };
    }

    private String getDefaultSourceLabel(String alarmType) {
        return switch (alarmType) {
            case "FIRE" -> "Yangın sensörü";
            case "GAS" -> "Gaz kaçak sensörü";
            case "FLOOD" -> "Su baskını sensörü";
            case "MOTION" -> "Hareket sensörü";
            case "DOOR_WINDOW" -> "Kapı/Pencere sensörü";
            default -> "Bilinmeyen alarm kaynağı";
        };
    }
}