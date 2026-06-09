package com.smartgate.network;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class IntercomClient {
    private static final int INTERCOM_COMMAND_PORT = 5432;
    private static final int OPERATION_DOOR_UNLOCK = 12;
    private static final int OPERATION_HANDSHAKE_GUVENLIK = 42;

    private final String intercomIp;
    private final Gson gson = new Gson();

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
}

