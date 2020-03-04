package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.media.MediaPlayer;


public class PlayUtilsPlus {
    PlayStateChangeListener playStateChangeListener;
    MediaPlayer player;

    public PlayUtilsPlus() {
    }

    public void setPlayStateChangeListener(PlayStateChangeListener listener) {
        this.playStateChangeListener = listener;
        this.playStateChangeListener.onPlayStateChange(PlayState.prepare);
    }

    public void startPlaying(String filePath) {
        try {
            this.player = new MediaPlayer();
            this.player.setDataSource(filePath);
            this.player.prepareAsync();
            this.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    PlayUtilsPlus.this.player.start();
                }
            });
            if (this.playStateChangeListener != null) {
                this.playStateChangeListener.onPlayStateChange(PlayState.start);
            }

            this.player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    PlayUtilsPlus.this.stopPlaying();
                }
            });
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public void pausePlay() {
        try {
            if (this.player != null) {
                this.player.pause();
                if (this.playStateChangeListener != null) {
                    this.playStateChangeListener.onPlayStateChange(PlayState.pause);
                }
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void stopPlaying() {
        try {
            if (this.player != null) {
                this.player.stop();
                this.player.reset();
                if (this.playStateChangeListener != null) {
                    this.playStateChangeListener.onPlayStateChange(PlayState.complete);
                }
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public boolean isPlaying() {
        try {
            return this.player != null && this.player.isPlaying();
        } catch (Exception var2) {
            return false;
        }
    }

    public interface PlayStateChangeListener {
        void onPlayStateChange(PlayState playState);
    }
}


