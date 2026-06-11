package com.smartgate.network;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.util.function.Consumer;

public class IntercomClient {
    private static final int INTERCOM_COMMAND_PORT = 5432;
    private static final int OPERATION_DOOR_UNLOCK = 12;
    private static final int OPERATION_HANDSHAKE_GUVENLIK = 42;
    private static final int INTERCOM_LISTEN_PORT = 50556;

    private final String intercomIp;
    private final Gson gson = new Gson();
    private boolean isListening = false;

    public IntercomClient(String intercomIp) {
        this.intercomIp = intercomIp;
    }

    public void sendCommand(ComPackageModel model) {
        Thread commandThread = new Thread(() -> {
            try (Socket socket = new Socket(intercomIp, INTERCOM_COMMAND_PORT);
                 PrintWriter out = new PrintWriter(
                         new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),
                         true
                 )) {
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

    public void startListening(Consumer<ComPackageModel> onPacketReceived) {
        if (isListening) return;
        isListening = true;

        new Thread(() -> {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(INTERCOM_LISTEN_PORT));
                System.out.println("İnterkom dinleniyor: port " + INTERCOM_LISTEN_PORT);

                while (true) {
                    try (Socket client = serverSocket.accept()) {
                        InputStream is = client.getInputStream();
                        byte[] header = new byte[4];
                        int read = is.read(header, 0, 4);
                        if (read < 4) continue;

                        // Video frame mi kontrol et
                        if (header[0] == (byte)0xDE && header[1] == (byte)0xAD
                                && header[2] == (byte)0xBE && header[3] == (byte)0xEF) {
                            System.out.println("Video frame alındı.");
                            // Video işleme ileride eklenecek
                        } else {
                            // JSON paket — alarm veya komut
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(is));
                            String rest = reader.readLine();
                            if (rest == null) continue;
                            String json = new String(header) + rest;
                            ComPackageModel packet = gson.fromJson(json, ComPackageModel.class);
                            System.out.println("Komut paketi alındı: ope_type=" + packet.getOpe_type());
                            onPacketReceived.accept(packet);
                        }
                    } catch (Exception e) {
                        System.err.println("Client hatası: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Dinleme hatası: " + e.getMessage());
                isListening = false;
            } finally {
                if (serverSocket != null) {
                    try { serverSocket.close(); } catch (Exception ignored) {}
                }
            }
        }).start();
    }
}