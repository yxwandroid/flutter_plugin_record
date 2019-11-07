//
//  RecordManager.swift
//  record_demo_ios
//
//  Created by wilson on 2019/6/19.
//  Copyright © 2019 wilson. All rights reserved.
//

import Foundation
import AVFoundation


//  1. 定协议
protocol MyDelegate {
    func onPlayAmplitude(amplitude:Double)->();
    func onVoicePathSuccess( voicePath:String)->();
}
class RecordManager {
    var delegate:MyDelegate?
    var recorder:AVAudioRecorder? //录音器
    var player:AVAudioPlayer? //播放器
    var recorderSeetingsDic:[String : Any]? //录音器设置参数数组
    var volumeTimer:Timer! //定时器线程，循环监测录音的音量大小
    var aacPath:String? //录音存储路径
    
    init() {
        //初始化录音器
        let session:AVAudioSession = AVAudioSession.sharedInstance()
        
        //设置录音类型
//        try! session.setCategory(AVAudioSession.Category.pl)
        try! session.setCategory(AVAudioSession.Category.playAndRecord)
        //设置支持后台
        try! session.setActive(true)
        //获取Document目录
        let docDir = NSSearchPathForDirectoriesInDomains(.documentDirectory,
                                                         .userDomainMask, true)[0]
        
        
        // 创建一个日期格式器
        let dformatter = DateFormatter()
        dformatter.dateFormat = "yyyyMMddHHmmss"
        let  wavName = dformatter.string(from: Date())
        
        print("当前日期时间：\(dformatter.string(from: Date()))")
        //组合录音文件路径
        aacPath = docDir + "/"+wavName+"_record.wav"
        
        //        //初始化字典并添加设置参数
        //        recorderSeetingsDic =
        //            [
        //                AVFormatIDKey: NSNumber(value: kAudioFormatMPEG4AAC),
        //                AVNumberOfChannelsKey: 2, //录音的声道数，立体声为双声道
        //                AVEncoderAudioQualityKey : AVAudioQuality.max.rawValue,
        //                AVEncoderBitRateKey : 320000,
        //                AVSampleRateKey : 44100.0 //录音器每秒采集的录音样本数
        //        ]
        
        
      //  print(aacPath)
        recorderSeetingsDic = [AVSampleRateKey: NSNumber(value: 8000.0),//采样率
            AVFormatIDKey: NSNumber(value: kAudioFormatLinearPCM),//音频格式
            AVLinearPCMBitDepthKey: NSNumber(value: 16),//采样位数
            AVNumberOfChannelsKey: NSNumber(value: 1),//通道数
//            AVEncoderAudioQualityKey: NSNumber(value: AVAudioQuality.min.rawValue)//录音质量
        ];
    }
    
    
    func start() {
        // recoder_manager.beginRecord()//开始录音
        
         
   
        let url = URL(fileURLWithPath: aacPath!)
        //初始化录音器
        recorder = try! AVAudioRecorder(url:url,
                                        settings: recorderSeetingsDic!)
        if recorder != nil {
            //开启仪表计数功能
            recorder!.isMeteringEnabled = true
            //准备录音
            recorder!.prepareToRecord()
            //开始录音
            recorder!.record()
            //启动定时器，定时更新录音音量
            volumeTimer = Timer.scheduledTimer(timeInterval: 0.1, target: self,
                                               selector: #selector(levelTimer),
                                               userInfo: nil, repeats: true)
        }
        
    }
    
  func stop() {
        // recoder_manager.stopRecord()//结束录音
        
        //停止录音
        recorder?.stop()
        //录音器释放
        recorder = nil
        //暂停定时器
        volumeTimer.invalidate()
        volumeTimer = nil
    
        delegate?.onVoicePathSuccess(voicePath: aacPath!)
        try!AVAudioSession.sharedInstance().overrideOutputAudioPort(AVAudioSession.PortOverride.speaker)
        
    }
    
 func  play() {
        // recoder_manager.play()//播放录制的音频
    
    
 
        //播放
        player = try! AVAudioPlayer(contentsOf: URL(string: aacPath!)!)
        if player == nil {
            print("播放失败")
        }else{
            player?.play()
        }
    }
    
    
    //定时检测录音音量
    @objc func levelTimer(){
        recorder!.updateMeters() // 刷新音量数据
       // let averageV:Float = recorder!.averagePower(forChannel: 0) //获取音量的平均值
        let maxV:Float = recorder!.peakPower(forChannel: 0) //获取音量最大值
        let lowPassResult:Double = pow(Double(10), Double(0.05*maxV))
      
        delegate?.onPlayAmplitude(amplitude: lowPassResult)
    }
    
}

