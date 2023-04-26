package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.os.Environment;
import android.util.Log;

import com.maple.recorder.recording.AudioChunk;
import com.maple.recorder.recording.AudioRecordConfig;
import com.maple.recorder.recording.MsRecorder;
import com.maple.recorder.recording.PullTransport;
import com.maple.recorder.recording.Recorder;

import java.io.File;
import java.util.Date;


public class RecorderUtil {

    Recorder recorder;
    public static String rootPath = "/yun_ke_fu/flutter/wav_file/";
    String voicePath;
    PlayUtilsPlus playUtils;

    RecordListener recordListener;
    PlayStateListener playStateListener;

    public RecorderUtil() {
        initVoice();
    }



    public RecorderUtil(String path) {
        voicePath =path;
    }





    public void addPlayAmplitudeListener(RecordListener recordListener) {
        this.recordListener = recordListener;
    }
    public void addPlayStateListener(PlayStateListener playStateListener) {
        this.playStateListener = playStateListener;
    }

    private void initVoice() {
        initPath();
        initVoicePath();
        initRecorder();
    }



    //初始化存储路径
    private void initPath() {
        String ROOT = "";// /storage/emulated/0
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            ROOT = Environment.getExternalStorageDirectory().getPath();
            Log.e("voice", "系统方法：" + ROOT);
        }
        rootPath = ROOT + rootPath;

        File lrcFile = new File(rootPath);
        if (!lrcFile.exists()) {
            lrcFile.mkdirs();
        }

        Log.e("voice", "初始存储路径" + rootPath);
    }


    private void initVoicePath() {
        String forDate = DateUtils.dateToString(new Date());
        String name = "wav-" + forDate;
        voicePath = rootPath + name + ".wav";
        Log.e("voice", "初始化语音路径" + voicePath);


    }


    private void initRecorder() {
        recorder = MsRecorder.wav(
                new File(voicePath),
                new AudioRecordConfig(),
                new PullTransport.Default()
                        .setOnAudioChunkPulledListener(new PullTransport.OnAudioChunkPulledListener() {
                            @Override
                            public void onAudioChunkPulled(AudioChunk audioChunk) {
                                if (recordListener != null) {
                                    recordListener.onPlayAmplitude(audioChunk.maxAmplitude());
                                }
                            }
                        })

        );
    }

    public void startRecord() {
        if (recordListener != null) {
            recordListener.onVoicePathSuccess(voicePath);
        }

        recorder.stopRecording();
        recorder.startRecording();
    }

    public void stopRecord() {
        recorder.stopRecording();
    }

    public void playVoice() {
        if (playUtils == null) {
            playUtils = new PlayUtilsPlus();
            playUtils.setPlayStateChangeListener(new PlayUtilsPlus.PlayStateChangeListener() {
                @Override
                public void onPlayStateChange(PlayState playState) {
                    playStateListener.playState(playState);
                }
            });
        }
        if(playUtils.isPlaying()) {
            playUtils.stopPlaying();
        }
        playUtils.startPlaying(voicePath);
    }

    public  boolean pausePlay(){
        LogUtils.LOGD("wilson","pausePlay");
        boolean isPlaying = playUtils.pausePlay();
        return isPlaying;
    }

    public  void stopPlay(){
        LogUtils.LOGD("wilson","stopPlay");
        playUtils.stopPlaying();
    }

    public interface RecordListener {
        void onPlayAmplitude(Double amplitude);

        void onVoicePathSuccess(String voicePath);

    }

    public interface PlayStateListener {

        void playState(PlayState playState);

    }
}
