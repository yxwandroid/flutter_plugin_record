import 'package:flustars/flustars.dart';
import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/flutter_plugin_record.dart';
import 'package:path_provider/path_provider.dart';

class SecondScreen extends StatefulWidget {
  @override
  _SecondScreenState createState() => _SecondScreenState();
}

class _SecondScreenState extends State<SecondScreen> {
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
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: Column(
          children: <Widget>[
            FlatButton(
              child: Text("初始化"),
              onPressed: () {
                _init();
              },
            ),
            FlatButton(
              child: Text("开始录制"),
              onPressed: () {
                start();
              },
            ),
            FlatButton(
              child: Text("根据路径录制wav文件"),
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
                playByPath(filePath);
              },
            ),

          ],
        ),
      ),
    );
  }

  void _requestAppDocumentsDirectory() {
    setState(() {
      getApplicationDocumentsDirectory().then((value) {
        String nowDataTimeStr = DateUtil.getNowDateMs().toString();
        String wavPath = value.path + "/" + nowDataTimeStr + ".wav";
        startByWavPath(wavPath);
      });
    });
  }

  ///初始化语音录制的方法
  void _init() async {
    recordPlugin.init();
  }

  ///开始语音录制的方法
  void start() async {
    recordPlugin.start();
  }

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

  ///播放指定路径录音文件
  void playByPath(String path) {
    recordPlugin.playByPath(path);
  }

  @override
  void dispose() {
    /// 当界面退出的时候是释放录音资源
    recordPlugin.dispose();
    super.dispose();
  }
}
