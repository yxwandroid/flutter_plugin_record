package record.wilson.flutter.com.flutter_plugin_record

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar
import record.wilson.flutter.com.flutter_plugin_record.utils.AudioHandler
import record.wilson.flutter.com.flutter_plugin_record.utils.FileTool
import record.wilson.flutter.com.flutter_plugin_record.utils.LogUtils
import record.wilson.flutter.com.flutter_plugin_record.utils.RecorderUtil
import java.io.File
import java.util.*

class FlutterPluginRecordPlugin: MethodCallHandler ,PluginRegistry.RequestPermissionsResultListener{

  private val MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1
  internal  var registrar: Registrar
  internal  var channel: MethodChannel
  internal lateinit var _result: Result
  internal lateinit var _call: MethodCall
  internal lateinit var voicePlayPath: String
  private var audioHandler: AudioHandler? = null


  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      var channel = MethodChannel(registrar.messenger(), "flutter_plugin_record")
      channel.setMethodCallHandler(FlutterPluginRecordPlugin(registrar,channel))
    }
  }

  constructor(registrar: Registrar, _channel: MethodChannel) {
    this.registrar = registrar
    this.registrar.addRequestPermissionsResultListener(this)
    this.channel = _channel
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    _result = result
    _call = call
    when (call.method) {
      "init" -> init()
      "start" -> start()
      "stop" -> stop()
      "play" -> play()
      else -> result.notImplemented()
    }
  }

  private fun initRecord() {

    if (audioHandler!=null){
      audioHandler?.release()
      audioHandler =null
    }
    audioHandler = AudioHandler.createHandler(AudioHandler.Frequency.F_8000)

    Log.d("android voice  ", "init")
    val _id = _call.argument<String>("id")
    val m1 = HashMap<String, String>()
    m1["id"] = _id!!
    m1["result"] = "success"
    channel.invokeMethod("onInit", m1)

  }
  private fun play() {
    val recorderUtil = RecorderUtil(voicePlayPath)
    recorderUtil.playVoice()
    Log.d("android voice  ", "play")
    val _id = _call.argument<String>("id")
    val m1 = HashMap<String, String>()
    m1["id"] = _id!!
    channel.invokeMethod("onPlay", m1)
  }

  private fun stop() {
    if(audioHandler!=null){
      if(audioHandler?.isRecording ==true){
        audioHandler?.stopRecord()
      }
    }
    Log.d("android voice  ", "stop")
  }

  private fun start() {
    Log.d("android voice  ", "start")
    //        recorderUtil.startRecord();
    if(audioHandler?.isRecording ==true){
      audioHandler?.startRecord(null);
      audioHandler?.stopRecord()
    }
    audioHandler?.startRecord(MessageRecordListener())


    val _id = _call.argument<String>("id")
    val m1 = HashMap<String, String>()
    m1["id"] = _id!!
    m1["result"] = "success"
    channel.invokeMethod("onStart", m1)
  }

  private fun init() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      initPermission()
    }else{
      initRecord()
    }
  }

  private fun initPermission() {
    if (ContextCompat.checkSelfPermission(registrar.activity(), Manifest.permission.RECORD_AUDIO) !== PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(registrar.activity(), arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_RECORD_AUDIO)
    } else {
      initRecord()
    }
  }




  private  inner class MessageRecordListener : AudioHandler.RecordListener {


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
      val _id = _call.argument<String>("id")
      val m1 = HashMap<String, Any>()
      m1["id"] = _id!!
      m1["amplitude"] = db / 100
      m1["result"] = "success"

      registrar.activity().runOnUiThread { channel.invokeMethod("onAmplitude", m1) }


    }

    override fun onStop(recordFile: File) {
      LogUtils.LOGE("MessageRecordListener onStop $recordFile")
      voicePlayPath = recordFile.path
      val _id = _call.argument<String>("id")
      val m1 = HashMap<String, String>()
      m1["id"] = _id!!
      m1["voicePath"] = voicePlayPath
      m1["result"] = "success"

      registrar.activity().runOnUiThread { channel.invokeMethod("onStop", m1) }
    }

    override fun onError(error: Int) {
      LogUtils.LOGE("MessageRecordListener onError $error")
    }
  }



  // 权限监听回调
  override fun onRequestPermissionsResult(p0: Int, p1: Array<out String>?, p2: IntArray?): Boolean {
    if (p0 == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
      if (p2?.get(0) == PackageManager.PERMISSION_GRANTED) {
        initRecord()
        return true
      } else {
        Toast.makeText(registrar.activity(), "Permission Denied", Toast.LENGTH_SHORT).show()
      }
      return false
    }

    return false
  }


}
