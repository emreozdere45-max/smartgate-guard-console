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
    private ServerSocket activeServerSocket = null;
    private java.util.function.Consumer<byte[]> videoFrameListener = null;

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

    public void stopListening() {
        isListening = false;
        if (activeServerSocket != null) {
            try {
                activeServerSocket.close();
            } catch (Exception ignored) {}
        }
    }
    public void setVideoFrameListener(java.util.function.Consumer<byte[]> listener) {
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
                            // Video frame — boyutu oku
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
                                if (videoFrameListener != null) {
                                    videoFrameListener.accept(frameData);
                                }
                                System.out.println("Video frame alındı: " + length + " bytes");
                            }
                        } else {
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
                        if (isListening) System.err.println("Client hatası: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                if (isListening) System.err.println("Dinleme hatası: " + e.getMessage());
                isListening = false;
            }
        }).start();
    }
}