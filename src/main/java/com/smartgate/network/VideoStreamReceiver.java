package com.smartgate.network;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

import java.nio.ByteBuffer;

public class VideoStreamReceiver {

    private MediaPlayerFactory factory;
    private EmbeddedMediaPlayer mediaPlayer;
    private WritableImage writableImage;
    private ImageView imageView;
    private int videoWidth = 640;
    private int videoHeight = 480;

    public void start(ImageView imageView) {
        this.imageView = imageView;

        try {
            factory = new MediaPlayerFactory();
            mediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer();

            mediaPlayer.videoSurface().set(new CallbackVideoSurface(
                    new BufferFormatCallback() {
                        @Override
                        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                            videoWidth = sourceWidth;
                            videoHeight = sourceHeight;
                            Platform.runLater(() -> {
                                writableImage = new WritableImage(videoWidth, videoHeight);
                                imageView.setImage(writableImage);
                                imageView.setFitWidth(320);
                                imageView.setFitHeight(240);
                            });
                            return new RV32BufferFormat(sourceWidth, sourceHeight);
                        }

                        @Override
                        public void allocatedBuffers(ByteBuffer[] buffers) {}
                    },
                    new RenderCallback() {
                        @Override
                        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                            ByteBuffer byteBuffer = nativeBuffers[0];
                            Platform.runLater(() -> {
                                if (writableImage != null) {
                                    PixelWriter pw = writableImage.getPixelWriter();
                                    pw.setPixels(0, 0, videoWidth, videoHeight,
                                            javafx.scene.image.PixelFormat.getByteBgraPreInstance(),
                                            byteBuffer, videoWidth * 4);
                                }
                            });
                        }
                    },
                    true,
                    VideoSurfaceAdapters.getVideoSurfaceAdapter()
            ));

            System.out.println("VideoStreamReceiver hazır.");
        } catch (Exception e) {
            System.err.println("VLCJ başlatma hatası: " + e.getMessage());
        }
    }

    public void playStream(String url) {
        if (mediaPlayer != null) {
            mediaPlayer.media().play(url);
            System.out.println("Video stream başlatıldı: " + url);
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.controls().stop();
            mediaPlayer.release();
        }
        if (factory != null) {
            factory.release();
        }
    }
}