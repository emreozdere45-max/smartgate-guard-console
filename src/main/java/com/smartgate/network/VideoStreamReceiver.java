package com.smartgate.network;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.component.CallbackMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class VideoStreamReceiver {

    private static final int VIDEO_PORT = 50556;
    private EmbeddedMediaPlayer mediaPlayer;
    private boolean running = false;

    public void start(javafx.scene.image.ImageView imageView) {
        if (running) return;
        running = true;

        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(VIDEO_PORT)) {
                serverSocket.setReuseAddress(true);
                System.out.println("Video stream bekleniyor: port " + VIDEO_PORT);

                while (running) {
                    try (Socket client = serverSocket.accept()) {
                        System.out.println("Video bağlantısı geldi: " + client.getInetAddress());
                        InputStream is = client.getInputStream();
                        handleVideoStream(is, imageView);
                    } catch (Exception e) {
                        if (running) System.err.println("Video client hatası: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.err.println("Video stream hatası: " + e.getMessage());
            }
        }).start();
    }

    private void handleVideoStream(InputStream is, javafx.scene.image.ImageView imageView) throws IOException {
        byte[] syncWord = new byte[]{(byte)0xDE, (byte)0xAD, (byte)0xBE, (byte)0xEF};
        byte[] headerBuf = new byte[7];
        ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

        while (running) {
            // 7 byte header oku
            int read = is.readNBytes(headerBuf, 0, 7);
            if (read < 7) break;

            // Sync word kontrol
            if (headerBuf[0] != syncWord[0] || headerBuf[1] != syncWord[1]
                    || headerBuf[2] != syncWord[2] || headerBuf[3] != syncWord[3]) {
                System.err.println("Sync word eşleşmedi, atlanıyor...");
                continue;
            }

            // Frame uzunluğu hesapla
            int length = ((headerBuf[4] & 0xFF) << 16)
                    | ((headerBuf[5] & 0xFF) << 8)
                    | (headerBuf[6] & 0xFF);

            if (length <= 0 || length > 10_000_000) {
                System.err.println("Geçersiz frame uzunluğu: " + length);
                continue;
            }

            // Frame verisini oku
            byte[] frameData = new byte[length];
            int totalRead = 0;
            while (totalRead < length) {
                int r = is.read(frameData, totalRead, length - totalRead);
                if (r < 0) break;
                totalRead += r;
            }

            System.out.println("Video frame alındı, boyut: " + length + " bytes");
            // Frame verisi burada işlenecek (VLCJ entegrasyonu ilerleyen adımda)
        }
    }

    public void stop() {
        running = false;
    }
}