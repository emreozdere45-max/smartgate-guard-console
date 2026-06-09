package com.smartgate.backend.intercom;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Component
public class IntercomCommandClient {
    private static final int OPERATION_DOOR_UNLOCK = 12;

    private final ObjectMapper objectMapper;

    public IntercomCommandClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void unlockDoor(String host, int port, int relayNo) throws Exception {
        ComPackageModel packet = new ComPackageModel();
        packet.setOpe_type(OPERATION_DOOR_UNLOCK);
        packet.setNeedResponse(false);
        packet.setDataInt(relayNo);
        send(host, port, packet);
    }

    private void send(String host, int port, ComPackageModel packet) throws Exception {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 3000);
            try (PrintWriter out = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)),
                true
            )) {
                out.println(objectMapper.writeValueAsString(packet));
                out.flush();
            }
        }
    }
}
