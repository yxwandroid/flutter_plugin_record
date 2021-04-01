package record.wilson.flutter.com.flutter_plugin_record.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

import record.wilson.flutter.com.flutter_plugin_record.timer.ITimerChangeCallback;
import record.wilson.flutter.com.flutter_plugin_record.timer.TimerUtils;
public final class AudioHandler extends Handler {

    private static final String TAG = "AudioHandler";

    private final        WeakReference<AudioThread> mThread;
    private static final int                        MESSAGE_START_RECORD      = 0X01;
    private static final int                        MESSAGE_PAUSE_RECORD      = 0X02;
    private static final int                        MESSAGE_STOP_RECORD       = 0X03;
    private static final int                        MESSAGE_SAVE_RECORD       = 0X04;
    private static final int                        MESSAGE_ADD_LISTENER      = 0X05;
    private static final int                        MESSAGE_REMOVE_LISTENER   = 0X06;
    private static final int                        MESSAGE_RELEASE           = 0X07;
    private static final int                        MESSAGE_GET_LATEST_RECORD = 0X08;

    public static final int MAX_DB = 96;

    public static final int STATE_AUDIO_RECORD_PREPARING = 0X01;
    public static final int STATE_AUDIO_RECORD_START     = 0X02;
    public static final int STATE_AUDIO_RECORD_PAUSE     = 0X03;
    public static final int STATE_AUDIO_RECORD_STOPPED   = 0X04;

    public static AudioHandler createHandler(Frequency frequency) {
        AudioThread thread = new AudioThread(frequency);
        thread.start();
        return thread.getHandler();
    }

    private AudioHandler(AudioThread thread) {
        mThread = new WeakReference<AudioThread>(thread);
    }

    public void startRecord(RecordListener listener) {
        if (isRecording()) stopRecord();
        Message message = obtainMessage(MESSAGE_START_RECORD);
        message.obj = listener;
        sendMessage(message);
    }

    public boolean isAvailable() {
        AudioThread audioThread = mThread.get();
        return audioThread != null && audioThread.isAvailable;
    }

    public void stopRecord() {
        AudioThread audioThread = mThread.get();
        if (audioThread != null) audioThread.setPauseRecord();
    }

    public void cancelRecord() {
        AudioThread audioThread = mThread.get();
        if (audioThread != null) audioThread.setCancelRecord();
    }

    public boolean isRecording() {
        AudioThread audioThread = mThread.get();
        return audioThread != null && audioThread.isRecording();
    }

    public void release() {
        AudioThread audioThread = mThread.get();
        if (audioThread != null) audioThread.release();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        AudioThread audioThread = mThread.get();
        if (audioThread == null) return;
        switch (msg.what) {
            case MESSAGE_START_RECORD:
                Object obj = msg.obj;
                RecordListener listener = null;
                if (obj instanceof RecordListener)
                    listener = (RecordListener) obj;
                audioThread.startRecord(listener);
                break;
            default:
                break;
        }
    }

    public enum Frequency {
        F_44100(44100),
        F_22050(22050),
        F_16000(16000),
        F_11025(11025),
        F_8000(8000);
        private int f;

        private Frequency(int f) {
            this.f = f;
        }

        public int getFrequency() {
            return f;
        }

    }

    private static final class AudioThread extends Thread {

        private static final int[] FREQUENCY = {
                44100,
                22050,
                16000,
                11025,
                8000
        };
        private final        int   mPriority;

        private       AudioHandler     mHandler;
        private       boolean          isAvailable;
        private final Object           sync        = new Object();
        private       int              mFrequency;
        private       int              channel     = AudioFormat.CHANNEL_IN_MONO;
        private       int              audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private       int              bufferSize;
        private       AudioRecord      mRecord;
        private       SimpleDateFormat format;
        private       int              mTid;
        private       Looper           mLooper;

        private volatile boolean isCancel;
        private String tag = "AudioTimerTag";

        private double audioTime = 0;  //录音时长

        //--设置 tag 后可以通过 tag 操作--
        private void initTimer() {
            TimerUtils.makeBuilder().setTag(tag).setInitDelay(0).setDelay(100).setCallbacks(new ITimerChangeCallback() {
                @Override
                public void onTimeChange(long time) {
                    //Log.v("AudioTimerTag", time + "--> AudioTimer");
                    audioTime = time / 10.0;
                }
            }).build();

        }

        private AudioThread(Frequency frequency) {
            mPriority = Process.THREAD_PRIORITY_DEFAULT;
            mFrequency = frequency.getFrequency();
            isAvailable = checkSampleRatesValid(mFrequency);
            Log.e(TAG, "FREQUENCY " + mFrequency);
            format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
            if (isAvailable) {
                bufferSize = AudioRecord.getMinBufferSize(mFrequency, channel, audioFormat);
                Log.e(TAG, String.format("buffer size %d", bufferSize));
            }
            initTimer();
        }

        boolean isRecording() {
            if (mRecord == null) {
                return false;
            }
            if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
                return false;
            }
            return mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING;
        }

        private volatile boolean isRecording;

        private void startRecord(RecordListener listener) {

            audioTime =0;
            TimerUtils.startTimer(tag);
            Log.e(TAG, "call start record");
            if (!isAvailable) {
                return;
            }
            byte[] buffer = new byte[bufferSize];
            pauseRecord();
            if (mRecord == null) {
                mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC
                        , mFrequency
                        , channel
                        , audioFormat
                        , bufferSize * 10);
            }

            if (mRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                pauseRecord();
                return;
            }

            BufferedOutputStream out = null;
            File recordFile = null;
            if (listener != null) {
                String filePath = listener.getFilePath();
                if (!filePath.endsWith(".wav"))
                    filePath = filePath + ".wav";
                recordFile = new File(filePath);
            }
            try {
                if (listener != null) {
                    if (!recordFile.createNewFile()) {
                        listener.onError(-20);
                        pauseRecord();
                        return;
                    }
                    out = new BufferedOutputStream(new FileOutputStream(recordFile));
                    WaveHeaderHelper.writeHeader(out, mFrequency, 16, 1);
                }
                mRecord.startRecording();
                isCancel = false;
                if (listener != null) listener.onStart();
                Log.e(TAG, "start recording");
                isRecording = true;
//                Log.d(TAG, "BUFFER LIMIT IS " + buffer.limit() + "\n\t\t\tCAPACITY IS" + buffer.capacity());
                long length = 0;
                boolean turn = false;
                while (isRecording && mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    int read = mRecord.read(buffer, 0, buffer.length);
                    if (read < 0) {
                        Log.e(TAG, read == -3 ? "ERROR_INVALID_OPERATION"
                                : read == -2 ? "ERROR_BAD_VALUE"
                                : read == -1 ? "ERROR"
                                : String.valueOf(read));
                        if (listener != null) listener.onError(read);
                        break;
                    }
                    if (out != null) {
                        out.write(buffer, 0, read);
                    }
                    if (listener != null) {
                        if (turn) {
                            listener.onVolume(getDb(buffer));
                        }
                        turn = !turn;
                    }
                    length += read;
                }

            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                pauseRecord();
                FileTool.closeIO(out);
            }
            if (listener != null) {
                WaveHeaderHelper.writeWaveHeaderLength(recordFile);
                if (isCancel) {
                    recordFile.deleteOnExit();
                    recordFile = null;
                }
                TimerUtils.stopTimer(tag);
//                listener.onStop(recordFile);
                listener.onStop(recordFile, audioTime);
            }
        }

        public void setPauseRecord() {
            isRecording = false;
        }

        public void setCancelRecord() {
            isCancel = true;
            setPauseRecord();
        }

        private double getDb(byte[] buffer) {
            double diviation = getDiviation(buffer, 0, buffer.length);
            return 20 * Math.log10(diviation);

        }

        private static short getShort(byte argB1, byte argB2) {
            //if (ByteOrder.nativeOrder().equals(ByteOrder.BIG_ENDIAN)) {
            //    return (short) ((argB1 << 8) | argB2);
            //}
            return (short) (argB1 | (argB2 << 8));
        }

        private static double getDiviation(byte[] buffer, int start, int length) {
            if (0 != (length % 2)) {
                length--;
            }
            double[] divArray = new double[length];
//        short[] array = ByteBuffer.wrap(buffer, start, length).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().array();
            for (int i = start; i < start + length; i += 2) {
                divArray[i / 2] = getShort(buffer[i], buffer[i + 1]) * 1.0;
            }
            return StandardDiviation(divArray);
        }

        //标准差σ=sqrt(s^2)
        private static double StandardDiviation(double[] x) {
            int m = x.length;
            double sum = 0;
            for (int i = 0; i < m; i++) {//求和
                sum += x[i];
            }
            double dAve = sum / m;//求平均值
            double dVar = 0;
            for (int i = 0; i < m; i++) {//求方差
                double v = x[i] - dAve;
                dVar += v * v;
            }
            return Math.sqrt(dVar / m);
        }

        private void pauseRecord() {
            setPauseRecord();
            if (mRecord != null) {
                if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
                    mRecord.release();
                    mRecord = null;
                }
                if (mRecord != null && mRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    mRecord.stop();
                    mRecord.release();
                    mRecord = null;
                }
            }
        }

        public void release() {
            pauseRecord();
            synchronized (sync) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    if (mLooper != null) {
                        mLooper.quit();
                    }
                }
            }
        }

        @Override
        public void run() {
            mTid = Process.myTid();
            Log.e(TAG, "thread start running");
            Looper.prepare();
            synchronized (sync) {
                mLooper = Looper.myLooper();
                mHandler = new AudioHandler(this);
                sync.notifyAll();
            }
            Process.setThreadPriority(mPriority);
            Looper.loop();
            synchronized (sync) {
                mHandler = null;
                sync.notifyAll();
            }
            mTid = -1;
        }

        AudioHandler getHandler() {
            if (!isAlive()) {
                return null;
            }
            synchronized (sync) {
                while (isAlive() && mHandler == null) {
                    try {
                        sync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            return mHandler;
        }

        public boolean checkSampleRatesValid(int frequency) {
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            return bufferSize > 0;
        }

        public int getValidSampleRates() {
            for (int i = 0; i < FREQUENCY.length; i++) {
                int bufferSize = AudioRecord.getMinBufferSize(FREQUENCY[i],
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                if (bufferSize > 0) {
                    return FREQUENCY[i];
                }
            }
            return -1;
        }

    }

    public interface RecordListener {
        void onStart();

        String getFilePath();

        void onVolume(double db);

//        void onStop(File recordFile);
        void onStop(File recordFile,Double audioTime);
        void onError(int error);
    }


    private static class WaveHeaderHelper {

        private static void writeHeader(OutputStream out, int sampleRate, int encoding, int channel) throws IOException {
            writeString(out, "RIFF"); // chunk id
            writeInt(out, 0); // chunk size
            writeString(out, "WAVE"); // format
            writeString(out, "fmt "); // subchunk 1 id
            writeInt(out, 16); // subchunk 1 size
            writeShort(out, (short) 1); // audio format (1 = PCM)
            writeShort(out, (short) channel); // number of channels
            writeInt(out, sampleRate); // sample rate
            writeInt(out, sampleRate * channel * encoding >> 3); // byte rate
            writeShort(out, (short) (channel * encoding >> 3)); // block align
            writeShort(out, (short) encoding); // bits per sample
            writeString(out, "data"); // subchunk 2 id
            writeInt(out, 0); // subchunk 2 size
        }

        private static void writeWaveHeaderLength(File f) {
            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(f, "rw");
                long length = f.length();
                long chunkSize = length - 8;
                long subChunkSize = length - 44;
                raf.seek(4);
                raf.write((int) (chunkSize >> 0));
                raf.write((int) (chunkSize >> 8));
                raf.write((int) (chunkSize >> 16));
                raf.write((int) (chunkSize >> 24));
                raf.seek(40);
                raf.write((int) (subChunkSize >> 0));
                raf.write((int) (subChunkSize >> 8));
                raf.write((int) (subChunkSize >> 16));
                raf.write((int) (subChunkSize >> 24));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                FileTool.closeIO(raf);
            }
        }

        private static void writeInt(final OutputStream output, final int value) throws IOException {
            output.write(value >> 0);
            output.write(value >> 8);
            output.write(value >> 16);
            output.write(value >> 24);
        }

        private static void writeShort(final OutputStream output, final short value) throws IOException {
            output.write(value >> 0);
            output.write(value >> 8);
        }

        private static void writeString(final OutputStream output, final String value) throws IOException {
            for (int i = 0; i < value.length(); i++) {
                output.write(value.charAt(i));
            }
        }
    }

}
