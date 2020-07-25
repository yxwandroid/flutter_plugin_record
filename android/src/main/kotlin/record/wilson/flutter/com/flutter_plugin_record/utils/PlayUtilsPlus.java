package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.media.MediaPlayer;


public class PlayUtilsPlus {
    PlayStateChangeListener playStateChangeListener;
    MediaPlayer player;

    public PlayUtilsPlus() {
    }

    public void setPlayStateChangeListener(PlayStateChangeListener listener) {
        this.playStateChangeListener = listener;
        //  this.playStateChangeListener.onPlayStateChange(PlayState.prepare);
    }

    public void startPlaying(String filePath) {
        try {
            isPause=false;
            this.player = new MediaPlayer();
            this.player.setDataSource(filePath);
            this.player.prepareAsync();
            this.player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mp) {
                    PlayUtilsPlus.this.player.start();
                }
            });
            if (this.playStateChangeListener != null) {
                // this.playStateChangeListener.onPlayStateChange(PlayState.start);
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
    Boolean isPause = false;

    public boolean pausePlay() {
        try {
            if (this.player.isPlaying() && !isPause) {
                this.player.pause();
                isPause = true;
            } else {
                this.player.start();
                isPause = false;
            }

        } catch (Exception var2) {
            var2.printStackTrace();
        }
        return isPause ;
    }

    public void stopPlaying() {
        try {
            if (this.player != null) {
                this.player.stop();
                this.player.reset();
                this.player=null;
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


