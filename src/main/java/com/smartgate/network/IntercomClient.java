package com.smartgate.network;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class IntercomClient {
    // Operation kodları (Constants.java'dan alındı)
    private static final int INTERCOM_COMMAND_PORT = 5432;
    private static final int INTERCOM_LISTEN_PORT = 5432;

    public static final int OPERATION_DOOR_UNLOCK = 12;
    public static final int OPERATION_HANDSHAKE_GUVENLIK = 42;
    public static final int OPERATION_HANDSHAKE_GUVENLIK_RESPONSE = 43;
    public static final int OPERATION_ALARM_TRIGGERED = 47;
    public static final int OPERATION_ALARM_TRIGGERED_FOR_SECONDARY = 53;
    public static final int OPERATION_ARAMA_REQUEST = 9;
    public static final int OPERATION_ARAMA_REQUEST_RESPONSE = 10;
    public static final int OPERATION_ARAMA_REQUEST_CANCEL = 11;
    public static final int OPERATION_CALL_DAIRE_REQ = 91;
    public static final int OPERATION_CALL_DAIRE_RESP = 92;
    public static final int OPERATION_CALL_DAIRE_ANSWERED = 93;
    public static final int OPERATION_CALL_HANGUP_BY_REMOTE = 94;
    public static final int OPERATION_CLOSE_TRIGGERED_ALARM = 52;
    public static final int OPERATION_CHECK_CONNECTION = 65;
    public static final int OPERATION_CHECK_CONNECTION_RESPONSE = 66;
    public static final int OPERATION_MESSAGE = 13;
    public static final int OPERATION_MESSAGE_READ_BY_RECEIVER = 36;
    public static final int OPERATION_KAPI_ZILI_STATE = 50;
    public static final int OPERATION_EV_GUVENLIK_STATE = 30;
    public static final int OPERATION_HANDSHAKE_DAIRE = 1;
    public static final int OPERATION_HANDSHAKE_ZIL_PANEL = 3;
    public static final int OPERATION_GET_DATE_TIME = 7;
    public static final int OPERATION_ADD_NEW_DAIRE = 5;

    private final String intercomIp;
    private final Gson gson = new Gson();
    private boolean isListening = false;
    private ServerSocket activeServerSocket = null;
    private Consumer<byte[]> videoFrameListener = null;

    public IntercomClient(String intercomIp) {
        this.intercomIp = intercomIp;
    }

    public void sendCommand(ComPackageModel model) {
        Thread commandThread = new Thread(() -> {
            try (Socket socket = new Socket(intercomIp, INTERCOM_COMMAND_PORT);
                 PrintWriter out = new PrintWriter(
                         new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                         true)) {
                String jsonMessage = gson.toJson(model);
                out.println(jsonMessage);
                out.flush();
                System.out.println("Sent intercom command: " + jsonMessage);
            } catch (Exception e) {
                System.err.println("Intercom command failed: " + e.getMessage());
            }
        });
        commandThread.setDaemon(true);
        commandThread.start();
    }

    public void sendCommandToIp(String ip, ComPackageModel model) {
        new Thread(() -> {
            try (Socket socket = new Socket(ip, INTERCOM_COMMAND_PORT);
                 PrintWriter out = new PrintWriter(
                         new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                         true)) {
                String jsonMessage = gson.toJson(model);
                out.println(jsonMessage);
                out.flush();
                System.out.println("Komut gönderildi: " + ip + " → " + jsonMessage);
            } catch (Exception e) {
                System.err.println("Komut gönderilemedi: " + ip + " - " + e.getMessage());
            }
        }).start();
    }

    public void unlockDoor() {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_DOOR_UNLOCK);
        packet.setNeedResponse(false);
        packet.setDataInt(1);
        sendCommand(packet);
    }

    public void sendSecurityHandshake(int securityNo, String localIp) {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_HANDSHAKE_GUVENLIK);
        packet.setNeedResponse(true);
        packet.setGuvenlik(new ComPackageModel.SecurityInfo(securityNo, localIp));
        sendCommand(packet);
    }

    public void answerCall() {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_ARAMA_REQUEST_RESPONSE);
        packet.setNeedResponse(false);
        sendCommand(packet);
    }

    public void cancelCall() {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_ARAMA_REQUEST_CANCEL);
        packet.setNeedResponse(false);
        sendCommand(packet);
    }

    public void closeAlarm(int alarmId) {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_CLOSE_TRIGGERED_ALARM);
        packet.setNeedResponse(false);
        packet.setDataInt(alarmId);
        sendCommand(packet);
    }

    public void sendMessageToApartment(String apartmentIp, String message) {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_MESSAGE);
        packet.setNeedResponse(false);
        packet.setDataString(message);

        ComPackageModel.DaireRef daireRef = new ComPackageModel.DaireRef();
        daireRef.ip = apartmentIp;
        packet.setDaire(daireRef);

        ComPackageModel.UserMessageInfo userMsg = new ComPackageModel.UserMessageInfo();
        userMsg.text = message;
        userMsg.datetime = java.time.LocalDateTime.now().toString();
        userMsg.senderId = 1;
        userMsg.senderType = 1;
        userMsg.uniqueID = java.util.UUID.randomUUID().toString();
        packet.setUserMessage(userMsg);

        sendCommandToIp(apartmentIp, packet);
        System.out.println("Mesaj gönderildi: " + apartmentIp + " → " + message);
    }

    public void stopListening() {
        isListening = false;
        if (activeServerSocket != null) {
            try { activeServerSocket.close(); } catch (Exception ignored) {}
        }
    }

    public void setVideoFrameListener(Consumer<byte[]> listener) {
        this.videoFrameListener = listener;
    }

    public void startListening(Consumer<ComPackageModel> onPacketReceived) {
        if (isListening) return;
        isListening = true;

        new Thread(() -> {
            try {
                activeServerSocket = new ServerSocket();
                activeServerSocket.setReuseAddress(true);
                activeServerSocket.bind(new InetSocketAddress(INTERCOM_LISTEN_PORT));
                System.out.println("İnterkom dinleniyor: port " + INTERCOM_LISTEN_PORT);

                while (isListening) {
                    try (Socket client = activeServerSocket.accept()) {
                        InputStream is = client.getInputStream();
                        byte[] header = new byte[4];
                        int read = is.read(header, 0, 4);
                        if (read < 4) continue;

                        if (header[0] == (byte)0xDE && header[1] == (byte)0xAD
                                && header[2] == (byte)0xBE && header[3] == (byte)0xEF) {
                            byte[] sizeBuf = new byte[3];
                            is.read(sizeBuf, 0, 3);
                            int length = ((sizeBuf[0] & 0xFF) << 16)
                                    | ((sizeBuf[1] & 0xFF) << 8)
                                    | (sizeBuf[2] & 0xFF);
                            if (length > 0 && length < 10_000_000) {
                                byte[] frameData = new byte[length];
                                int total = 0;
                                while (total < length) {
                                    int r = is.read(frameData, total, length - total);
                                    if (r < 0) break;
                                    total += r;
                                }
                                if (videoFrameListener != null) videoFrameListener.accept(frameData);
                                System.out.println("Video frame alındı: " + length + " bytes");
                            }
                        } else {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                            String rest = reader.readLine();
                            if (rest == null) continue;
                            String json = new String(header) + rest;
                            ComPackageModel packet = gson.fromJson(json, ComPackageModel.class);
                            System.out.println("Komut paketi alındı: ope_type=" + packet.getOpe_type());
                            onPacketReceived.accept(packet);
                        }
                    } catch (Exception e) {
                        if (isListening) System.err.println("Client hatası: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                if (isListening) System.err.println("Dinleme hatası: " + e.getMessage());
                isListening = false;
            }
        }).start();
    }

    public void sendSecurityHandshakeToIp(String targetIp, int securityNo, String localIp) {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_HANDSHAKE_GUVENLIK);
        packet.setNeedResponse(true);
        packet.setGuvenlik(new ComPackageModel.SecurityInfo(securityNo, localIp));
        sendCommandToIp(targetIp, packet);
    }

    public void scanNetworkForDevices(java.util.function.Consumer<String> onDeviceFound) {
        new Thread(() -> {
            System.out.println("Ağ taraması başlıyor...");
            for (int i = 1; i <= 10; i++) {
                String ip = "172." + i + ".255.1";
                try {
                    java.net.InetAddress addr = java.net.InetAddress.getByName(ip);
                    if (addr.isReachable(1000)) {
                        System.out.println("Cihaz bulundu (ping): " + ip);
                        onDeviceFound.accept(ip);
                    } else {
                        try (java.net.Socket s = new java.net.Socket()) {
                            s.connect(new java.net.InetSocketAddress(ip, INTERCOM_COMMAND_PORT), 1000);
                            System.out.println("Cihaz bulundu (TCP): " + ip);
                            onDeviceFound.accept(ip);
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
            }
            System.out.println("Ağ taraması tamamlandı.");
        }).start();
    }
    public void unlockDoorToIp(String targetIp) {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_DOOR_UNLOCK);
        packet.setNeedResponse(false);
        packet.setDataInt(1);
        sendCommandToIp(targetIp, packet);
        System.out.println("Kapı açma komutu gönderildi: " + targetIp);
    }
}