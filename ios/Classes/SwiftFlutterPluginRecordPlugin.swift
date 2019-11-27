import Flutter
import UIKit

public class SwiftFlutterPluginRecordPlugin: NSObject, FlutterPlugin {
  
     let mChannel:FlutterMethodChannel!
     var  _result:FlutterResult!
     var _call:FlutterMethodCall!
     //    var recoderManager:RecordManager!
     var  wavData:Data! = nil
     public static func register(with registrar: FlutterPluginRegistrar) {
         let channel = FlutterMethodChannel(name: "flutter_plugin_record", binaryMessenger: registrar.messenger())
         let instance = SwiftFlutterPluginRecordPlugin(channel: channel)
         registrar.addMethodCallDelegate(instance, channel: channel)
     }
     
     init(channel:FlutterMethodChannel) {
         mChannel = channel
         super.init()
     }
     
     public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
         _result = result;
         _call = call;
         switch (call.method) {
         case "init":
             initRecord();
             break;
         case "start":
             start();
             break;
         case "stop":
             stop();
             break;
         case "play":
             play();
             break;
         default:
             result(FlutterMethodNotImplemented)
             break;
    
         }
     }
     
     
     func initRecord(){
         //        recoderManager = RecordManager()//初始化
         //        recoderManager.delegate = self
         
         //录制完成
         DPAudioRecorder.sharedInstance()?.audioRecorderFinishRecording = {
             (data,audioTimeLength,path) in
             
          //   print(path)
             print("录制完成")
             
             self.wavData = data
     
             ///将Data数据保存起来  解决上传格式不统一的问题
             let documentPath2 = NSHomeDirectory() + "/Documents/aochuang_yun_ios.wav"
             FileManager.default.createFile(atPath: documentPath2, contents: data, attributes: nil)
    
             guard let args = self._call.arguments else {
                 return
             }
             if let myArgs = args as? [String: Any]{
                 let _id = myArgs["id"] as? String
                 let mi = [
                     "id": _id,
                     "voicePath": documentPath2,
                     "result":"success"]
                 self.mChannel.invokeMethod("onStop", arguments:mi)
             }
         }
         
         DPAudioRecorder.sharedInstance()?.audioStartRecording = {(isRecording) in
             print(isRecording)
             print("录制开始")
         }
         DPAudioRecorder.sharedInstance()?.audioRecordingFail = { (reason) in
             
             print("reason")
             
         }
         DPAudioRecorder.sharedInstance()?.audioSpeakPower = {(power)in
             
             print(power)
             guard let args = self._call.arguments else {
                 return
             }
             if let myArgs = args as? [String: Any]{
                 let _id = myArgs["id"] as? String
                 let mi = [
                     "id": _id,
                     "amplitude": String(power),
                     "result":"success"]
                 self.mChannel.invokeMethod("onAmplitude", arguments:mi)
             }
         }
         
         print("ios  voice  ", "init");
         guard let args = _call.arguments else {
             return
         }
         if let myArgs = args as? [String: Any]{
             let _id = myArgs["id"] as? String
             let mi = ["result": "success","id": _id,]
             self.mChannel.invokeMethod("onInit", arguments:mi)
         }
     }
     
     func start(){
         //  recoderManager.start()//开始录音
         DPAudioRecorder.sharedInstance().startRecording()
         guard let args = _call.arguments else {
             return
         }
         if let myArgs = args as? [String: Any]{
             let _id = myArgs["id"] as? String
             let mi = ["result": "success","id": _id,]
             self.mChannel.invokeMethod("onStart", arguments:mi)
         }
     }
     
     func stop(){
         DPAudioRecorder.sharedInstance()?.stopRecording()
         // recoderManager.stop()//结束录音
     }
     
     func play(){
         //  recoderManager.play()//播放录制的音频
         
         DPAudioPlayer.sharedInstance()?.startPlay(with: wavData)
         
         DPAudioPlayer.sharedInstance()?.playComplete = { ()
             in
             print("播放完成")
         }
         guard let args = _call.arguments else {
             return
         }
         if let myArgs = args as? [String: Any]{
             let _id = myArgs["id"] as? String
             let mi = ["id": _id,]
             self.mChannel.invokeMethod("onPlay", arguments:mi)
         }
         
     }
     
     
     //    func onPlayAmplitude(amplitude: Double) {
     //
     //        guard let args = _call.arguments else {
     //            return
     //        }
     //        if let myArgs = args as? [String: Any]{
     //            let _id = myArgs["id"] as? String
     //            let mi = [
     //                "id": _id,
     //                "amplitude": String(amplitude),
     //                "result":"success"]
     //            self.mChannel.invokeMethod("onAmplitude", arguments:mi)
     //        }
     //
     //    }
     //
     //    func onVoicePathSuccess(voicePath: String) {
     //        guard let args = _call.arguments else {
     //            return
     //        }
     //        if let myArgs = args as? [String: Any]{
     //            let _id = myArgs["id"] as? String
     //            let mi = [
     //                "id": _id,
     //                "voicePath": voicePath,
     //                "result":"success"]
     //            self.mChannel.invokeMethod("onStop", arguments:mi)
     //        }
     //
     //    }
     //
}
