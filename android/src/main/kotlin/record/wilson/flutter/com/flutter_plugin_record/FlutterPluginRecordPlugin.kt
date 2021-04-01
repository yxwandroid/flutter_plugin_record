package record.wilson.flutter.com.flutter_plugin_record

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cafe.adriel.androidaudioconverter.AndroidAudioConverter
import cafe.adriel.androidaudioconverter.callback.IConvertCallback
import cafe.adriel.androidaudioconverter.callback.ILoadCallback
import cafe.adriel.androidaudioconverter.model.AudioFormat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import record.wilson.flutter.com.flutter_plugin_record.utils.*
import java.io.File
import java.util.*


class FlutterPluginRecordPlugin : FlutterPlugin, MethodCallHandler, ActivityAware ,PluginRegistry.RequestPermissionsResultListener {

    lateinit var channel: MethodChannel
    private lateinit var _result: Result
    private lateinit var call: MethodCall
    private lateinit var voicePlayPath: String
    private var recorderUtil: RecorderUtil? = null
    private var recordMp3:Boolean=false;

    @Volatile
    private var audioHandler: AudioHandler? = null

    lateinit var activity:Activity

    companion object {
        //support embedding v1
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            val plugin = initPlugin(registrar.messenger())
            plugin.activity=registrar.activity()
            registrar.addRequestPermissionsResultListener(plugin)
        }

        private fun initPlugin(binaryMessenger: BinaryMessenger):FlutterPluginRecordPlugin {
            val channel = createMethodChannel(binaryMessenger)
            val plugin = FlutterPluginRecordPlugin()
            channel.setMethodCallHandler(plugin)
            plugin.channel = channel
            return plugin
        }

        private fun createMethodChannel(binaryMessenger: BinaryMessenger):MethodChannel{
            return  MethodChannel(binaryMessenger, "flutter_plugin_record");
        }
    }
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
       val methodChannel = createMethodChannel(binding.binaryMessenger)
        methodChannel.setMethodCallHandler(this)
        channel=methodChannel
    }



    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        initActivityBinding(binding)
    }

    private fun initActivityBinding(binding: ActivityPluginBinding) {
        binding.addRequestPermissionsResultListener(this)
        activity=binding.activity
    }


    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        initActivityBinding(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }
    override fun onDetachedFromActivity() {
    }


    override  fun onMethodCall(call: MethodCall, result: Result) {
        _result = result
        this.call = call
        when (call.method) {
            "init" -> init()
            "initRecordMp3" -> initRecordMp3()
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



    //初始化wav转 MP3
    private fun initWavToMp3(){
        AndroidAudioConverter.load(activity.applicationContext, object : ILoadCallback {
            override fun onSuccess() {
                // Great!
                Log.d("android", "  AndroidAudioConverter onSuccess")
            }

            override fun onFailure(error: Exception) {
                // FFmpeg is not supported by device
                Log.d("android", "  AndroidAudioConverter onFailure")
            }
        })

    }

    private fun initRecord() {
        if (audioHandler != null) {
            audioHandler?.release()
            audioHandler = null
        }
        audioHandler = AudioHandler.createHandler(AudioHandler.Frequency.F_22050)

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
        var packageManager = activity.packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO,activity.packageName)
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
        var packageManager = activity.packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO, activity.packageName)
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
        recordMp3=false
        checkPermission()
    }
    private fun initRecordMp3(){
        recordMp3=true
        checkPermission()
        initWavToMp3()
    }

    private fun checkPermission() {
        var packageManager = activity.packageManager
        var permission = PackageManager.PERMISSION_GRANTED == packageManager.checkPermission(Manifest.permission.RECORD_AUDIO,activity.packageName)
        if (permission) {
            initRecord()
        } else {
            initPermission()
        }


    }

    private fun initPermission() {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
    }


    //自定义路径
    private inner class MessageRecordListenerByPath : AudioHandler.RecordListener {
        var wavPath = ""

        constructor(wavPath: String) {
            this.wavPath = wavPath
        }


        override fun onStop(recordFile: File?, audioTime: Double?) {
            if (recordFile != null) {
                voicePlayPath = recordFile.path
                if (recordMp3){

                    val callback: IConvertCallback = object : IConvertCallback {
                        override fun onSuccess(convertedFile: File) {

                            Log.d("android", "  ConvertCallback ${convertedFile.path}")

                            val _id = call.argument<String>("id")
                            val m1 = HashMap<String, String>()
                            m1["id"] = _id!!
                            m1["voicePath"] = convertedFile.path
                            m1["audioTimeLength"] = audioTime.toString()
                            m1["result"] = "success"
                            activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
                        }

                        override fun onFailure(error: java.lang.Exception) {
                            Log.d("android", "  ConvertCallback $error")
                        }
                    }
                    AndroidAudioConverter.with(activity.applicationContext)
                            .setFile(recordFile)
                            .setFormat(AudioFormat.MP3)
                            .setCallback(callback)
                            .convert()

                }else{
                    val _id = call.argument<String>("id")
                    val m1 = HashMap<String, String>()
                    m1["id"] = _id!!
                    m1["voicePath"] = voicePlayPath
                    m1["audioTimeLength"] = audioTime.toString()
                    m1["result"] = "success"
                    activity.runOnUiThread { channel.invokeMethod("onStop", m1) }

                }
            }

        }


        override fun getFilePath(): String {
            return wavPath;
        }

        private val fileName: String
        private val cacheDirectory: File


        init {
            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(activity)
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

            activity.runOnUiThread { channel.invokeMethod("onAmplitude", m1) }


        }

        override fun onError(error: Int) {
            LogUtils.LOGE("MessageRecordListener onError $error")
        }
    }


    private inner class MessageRecordListener : AudioHandler.RecordListener {
        override fun onStop(recordFile: File?, audioTime: Double?) {
            LogUtils.LOGE("MessageRecordListener onStop $recordFile")
            if (recordFile != null) {
                voicePlayPath = recordFile.path
                if (recordMp3){
                    val callback: IConvertCallback = object : IConvertCallback {
                        override fun onSuccess(convertedFile: File) {

                            Log.d("android", "  ConvertCallback ${convertedFile.path}")

                            val _id = call.argument<String>("id")
                            val m1 = HashMap<String, String>()
                            m1["id"] = _id!!
                            m1["voicePath"] = convertedFile.path
                            m1["audioTimeLength"] = audioTime.toString()
                            m1["result"] = "success"
                            activity.runOnUiThread { channel.invokeMethod("onStop", m1) }
                        }

                        override fun onFailure(error: java.lang.Exception) {
                            Log.d("android", "  ConvertCallback $error")
                        }
                    }
                    AndroidAudioConverter.with(activity.applicationContext)
                            .setFile(recordFile)
                            .setFormat(AudioFormat.MP3)
                            .setCallback(callback)
                            .convert()

                }else{
                    val _id = call.argument<String>("id")
                    val m1 = HashMap<String, String>()
                    m1["id"] = _id!!
                    m1["voicePath"] = voicePlayPath
                    m1["audioTimeLength"] = audioTime.toString()
                    m1["result"] = "success"
                    activity.runOnUiThread { channel.invokeMethod("onStop", m1) }

                }
            }

        }


        override fun getFilePath(): String {
            val file = File(cacheDirectory, fileName)
            return file.absolutePath
        }

        private val fileName: String
        private val cacheDirectory: File


        init {
            cacheDirectory = FileTool.getIndividualAudioCacheDirectory(activity)
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

            activity.runOnUiThread { channel.invokeMethod("onAmplitude", m1) }


        }

        override fun onError(error: Int) {
            LogUtils.LOGE("MessageRecordListener onError $error")
        }
    }




    // 权限监听回调
    override fun onRequestPermissionsResult(p0: Int, p1: Array<out String>?, p2: IntArray?): Boolean {
        if (p0 == 1) {
            if (p2?.get(0) == PackageManager.PERMISSION_GRANTED) {
//                initRecord()
                return true
            } else {

                Toast.makeText(activity, "Permission Denied", Toast.LENGTH_SHORT).show()
                DialogUtil.Dialog(activity, "申请权限")
            }
            return false
        }

        return false
    }

    

}
