import 'dart:io';

import 'package:flustars/flustars.dart';
import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/flutter_plugin_record.dart';
import 'package:path_provider/path_provider.dart';

class RecordMp3Screen extends StatefulWidget {
  @override
  _RecordMp3ScreenState createState() => _RecordMp3ScreenState();
}

class _RecordMp3ScreenState extends State<RecordMp3Screen> {
  FlutterPluginRecord recordPlugin = new FlutterPluginRecord();

  String filePath = "";

  @override
  void initState() {
    super.initState();

    ///初始化方法的监听
    recordPlugin.responseFromInit.listen((data) {
      if (data) {
        print("初始化成功");
      } else {
        print("初始化失败");
      }
    });

    /// 开始录制或结束录制的监听
    recordPlugin.response.listen((data) {
      if (data.msg == "onStop") {
        ///结束录制时会返回录制文件的地址方便上传服务器
        print("onStop  文件路径" + data.path);
        filePath = data.path;
        print("onStop  时长 " + data.audioTimeLength.toString());
      } else if (data.msg == "onStart") {
        print("onStart --");
      } else {
        print("--" + data.msg);
      }
    });

    ///录制过程监听录制的声音的大小 方便做语音动画显示图片的样式
    recordPlugin.responseFromAmplitude.listen((data) {
      var voiceData = double.parse(data.msg);
      print("振幅大小   " + voiceData.toString());
    });

    recordPlugin.responsePlayStateController.listen((data) {
      print("播放路径   " + data.playPath);
      print("播放状态   " + data.playState);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('录制mp3'),
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            FlatButton(
              child: Text("初始化录制mp3"),
              onPressed: () {
                _initRecordMp3();
              },
            ),
            FlatButton(
              child: Text("开始录制"),
              onPressed: () {
                start();
              },
            ),
            FlatButton(
              child: Text("根据路径录制mp3文件"),
              onPressed: () {
                _requestAppDocumentsDirectory();
              },
            ),
            FlatButton(
              child: Text("停止录制"),
              onPressed: () {
                stop();
              },
            ),
            FlatButton(
              child: Text("播放"),
              onPressed: () {
                play();
              },
            ),
            FlatButton(
              child: Text("播放本地指定路径录音文件"),
              onPressed: () {
                playByPath(filePath,"file");
              },
            ),
            FlatButton(
              child: Text("播放网络mp3文件"),
              onPressed: () {
                playByPath("https://test-1259809289.cos.ap-nanjing.myqcloud.com/temp.mp3","url");
              },
            ),
            FlatButton(
              child: Text("暂停|继续播放"),
              onPressed: () {
                pause();
              },
            ),
            FlatButton(
              child: Text("停止播放"),
              onPressed: () {
                stopPlay();
              },
            ),
          ],
        ),
      ),
    );
  }

  void _requestAppDocumentsDirectory() {
//    if(Platform.isIOS){
//      //ios相关代码
//      setState(() {
//        getApplicationDocumentsDirectory().then((value) {
//          String nowDataTimeStr = DateUtil.getNowDateMs().toString();
//          String wavPath = value.path + "/" + nowDataTimeStr + ".wav";
//          startByWavPath(wavPath);
//        });
//      });
//    }else if(Platform.isAndroid){
//      //android相关代码
//    }

    setState(() {
      getApplicationDocumentsDirectory().then((value) {
        String nowDataTimeStr = DateUtil.getNowDateMs().toString();
        // TODO  注意IOS 传递的Mp3路径一定是以 .MP3 结尾
        String wavPath ="";
        if (Platform.isIOS) {
           wavPath = value.path + "/" + nowDataTimeStr+".MP3";
        }else{
           wavPath = value.path + "/" + nowDataTimeStr;
        }
        startByWavPath(wavPath);
      });
    });
  }

  ///初始化语音录制的方法
  void _initRecordMp3() async {
    recordPlugin.initRecordMp3();
  }

  ///开始语音录制的方法
  void start() async {
    recordPlugin.start();
  }

  ///根据传递的路径进行语音录制
  void startByWavPath(String wavPath) async {
    recordPlugin.startByWavPath(wavPath);
  }

  ///停止语音录制的方法
  void stop() {
    recordPlugin.stop();
  }

  ///播放语音的方法
  void play() {
    recordPlugin.play();
  }

  ///播放指定路径录音文件  url为iOS播放网络语音，file为播放本地语音文件
  void playByPath(String path,String type) {
    recordPlugin.playByPath(path,type);
  }

  ///暂停|继续播放
  void pause() {
    recordPlugin.pausePlay();
  }

  @override
  void dispose() {
    /// 当界面退出的时候是释放录音资源
    recordPlugin.dispose();
    super.dispose();
  }

  void stopPlay() {
    recordPlugin.stopPlay();
  }
}
