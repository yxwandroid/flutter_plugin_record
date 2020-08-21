package record.wilson.flutter.com.flutter_plugin_record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import record.wilson.flutter.com.flutter_plugin_record.utils.*
import java.io.File
import java.util.*


class FlutterPluginRecordPlugin : MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {

    private var registrar: Registrar
    private var channel: MethodChannel
    private lateinit var _result: Result
    private lateinit var call: MethodCall
    private lateinit var voicePlayPath: String
    private var recorderUtil: RecorderUtil? = null

    @Volatile
    private var audioHandler: AudioHandler? = null

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            var channel = MethodChannel(registrar.messenger(), "flutter_plugin_record")
            registrar.activeContext().applicationContext
            channel.setMethodCallHandler(FlutterPluginRecordPlugin(registrar, channel))
        }
    }

    constructor(registrar: Registrar, _channel: MethodChannel) {
        this.registrar = registrar
        this.registrar.addRequestPermissionsResultListener(this)
        this.channel = _channel
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            initPermission()
//        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        _result = result
        this.call = call
        when (call.method) {
            "init" -> init()
            "start" -> start()
            "startByWavPath" -> startByWavPath()
            "stop" -> stop()
            "play" -> play()
            "pause" -> pause()
            "playByPath" -> playByPath()
            "stopPlay" -> stopPlay()
            else -> result.notImplemented()
        }
    }



    private fun initRecord() {
        if (audioHandler != null) {
            audioHandler?.release()
            audioHandler = null
        }
        audioHandler = AudioHandler.createHandler(AudioHandler.Frequency.F_8000)

        Log.d("android voice  ", "init")
        val id = call.argument<String>("id")
        val m1 = HashMap<String, String>()
        m1["id"] = id!!
        m1["result"] = "success"
        channel.invokeMethod("onInit", m1)

    }

    private fun stopPlay() {
        recorderUtil?.stopPlay()
    }
    //暂停播放
    private fun pause() {
        val isPlaying= recorderUtil?.pausePlay()
        val _id = call.argument<String>("id")
        val m1 = HashMap<String, String>()
        m1["id"] = _id!!
        m1["result"] = "success"
        m1["isPlaying"] = isPlaying.toString()
        channel.invokeMethod("pausePlay", m1)
    }

    private fun play() {

        recorderUtil = RecorderUtil(voicePlayPath)
        recorderUtil!!.addPlayStateListener { playState ->
            print(playState)
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["playPath"] = voicePlayPath
            m1["playState"] = playState.toString()
            channel.invokeMethod("onPlayState", m1)
        }
        recorderUtil!!.playVoice()
        Log.d("android voice  ", "play")
        val _id = call.argument<String>("id")
        val m1 = HashMap<String, String>()
        m1["id"] = _id!!
        channel.invokeMethod("onPlay", m1)
    }

    private fun playByPath() {
        val path = call.argument<String>("path")
        recorderUtil = RecorderUtil(path)
        recorderUtil!!.addPlayStateListener { playState ->
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["playPath"] = path.toString();
            m1["playState"] = playState.toString()
            channel.invokeMethod("onPlayState", m1)
        }
        recorderUtil!!.playVoice()

        Log.d("android voice  ", "play")
        val _id = call.argument<String>("id")
        val m1 = HashMap<String, String>()
        m1["id"] = _id!!
        channel.invokeMethod("onPlay", m1)
    }

    @Synchronized
    private fun stop() {
        if (audioHandler != null) {
            if (audioHandler?.isRecording == true) {
                audioHandler?.stopRecord()
            }
        }
        Log.d("android voice  ", "stop")
    }

    @Synchronized
    private fun start() {
        var packageManager = registrar.activity().packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, registrar.activeContext().packageName)
        if (permission) {
            Log.d("android voice  ", "start")
            //        recorderUtil.startRecord();
            if (audioHandler?.isRecording == true) {
//            audioHandler?.startRecord(null);
                audioHandler?.stopRecord()
            }
            audioHandler?.startRecord(MessageRecordListener())


            val _id = call.argument<String>("id")
            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["result"] = "success"
            channel.invokeMethod("onStart", m1)
        } else {
            checkPermission()
        }

    }

    @Synchronized
    private fun startByWavPath() {
        var packageManager = registrar.activity().packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, registrar.activeContext().packageName)
        if (permission) {
            Log.d("android voice  ", "start")
            val _id = call.argument<String>("id")
            val wavPath = call.argument<String>("wavPath")

            if (audioHandler?.isRecording == true) {
                audioHandler?.stopRecord()
            }
            audioHandler?.startRecord(wavPath?.let { MessageRecordListenerByPath(it) })


            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["result"] = "success"
            channel.invokeMethod("onStart", m1)
        } else {
            checkPermission()
        }

    }


    private fun init() {

        checkPermission()
    }


    private fun checkPermission() {
        var packageManager = registrar.activity().packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, registrar.activeContext().packageName)
        if (permission) {
            initRecord()
        } else {
            initPermission()
        }


    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(registrar.activity(), Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(registrar.activity(), arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }


    //自定义路径
    private inner class MessageRecordListenerByPath : AudioHandler.RecordListener {
        var wavPath = ""

        constructor(wavPath: String) {
            this.wavPath = wavPath
        }


        override fun onStop(recordFile: File?, audioTime: Long?) {
            LogUtils.LOGE("MessageRecordListener onStop $recordFile")
            voicePlayPath = recordFile!!.path
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["voicePath"] = voicePlayPath
            m1["audioTimeLength"] = audioTime.toString()
            m1["result"] = "success"

            registrar.activity().runOnUiThread { channel.invokeMethod("onStop", m1) }
        }


        override fun getFilePath(): String {
            return wavPath;
        }

        private val fileName: String
        private val cacheDirectory: File


        init {
            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(registrar.activity())
            fileName = UUID.randomUUID().toString()
        }

        override fun onStart() {
            LogUtils.LOGE("MessageRecordListener onStart on start record")
        }

        override fun onVolume(db: Double) {
            LogUtils.LOGE("MessageRecordListener onVolume " + db / 100)
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, Any>()
            m1["id"] = _id!!
            m1["amplitude"] = db / 100
            m1["result"] = "success"

            registrar.activity().runOnUiThread { channel.invokeMethod("onAmplitude", m1) }


        }

        override fun onError(error: Int) {
            LogUtils.LOGE("MessageRecordListener onError $error")
        }
    }


    private inner class MessageRecordListener : AudioHandler.RecordListener {
        override fun onStop(recordFile: File?, audioTime: Long?) {
            LogUtils.LOGE("MessageRecordListener onStop $recordFile")
            voicePlayPath = recordFile!!.path
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, String>()
            m1["id"] = _id!!
            m1["voicePath"] = voicePlayPath
            m1["audioTimeLength"] = audioTime.toString()
            m1["result"] = "success"

            registrar.activity().runOnUiThread { channel.invokeMethod("onStop", m1) }
        }


        override fun getFilePath(): String {
            val file = File(cacheDirectory, fileName)
            return file.absolutePath
        }

        private val fileName: String
        private val cacheDirectory: File


        init {
            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(registrar.activity())
            fileName = UUID.randomUUID().toString()
        }

        override fun onStart() {
            LogUtils.LOGE("MessageRecordListener onStart on start record")
        }

        override fun onVolume(db: Double) {
            LogUtils.LOGE("MessageRecordListener onVolume " + db / 100)
            val _id = call.argument<String>("id")
            val m1 = HashMap<String, Any>()
            m1["id"] = _id!!
            m1["amplitude"] = db / 100
            m1["result"] = "success"

            registrar.activity().runOnUiThread { channel.invokeMethod("onAmplitude", m1) }


        }

        override fun onError(error: Int) {
            LogUtils.LOGE("MessageRecordListener onError $error")
        }
    }


    // 权限监听回调
    override fun onRequestPermissionsResult(p0: Int, p1: Array<out String>?, p2: IntArray?): Boolean {
        if (p0 == 1) {
            if (p2?.get(0) == PackageManager.PERMISSION_GRANTED) {
                return true
            } else {
                Toast.makeText(registrar.activity(), "Permission Denied", Toast.LENGTH_SHORT).show()
                DialogUtil.Dialog(registrar.activity(), "申请权限")
            }
            return false
        }

        return false
    }


}
