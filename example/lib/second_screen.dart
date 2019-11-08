import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/flutter_plugin_record.dart';

class SecondScreen extends StatefulWidget {
  @override
  _SecondScreenState createState() => _SecondScreenState();
}

class _SecondScreenState extends State<SecondScreen> {
  FlutterPluginRecord recordPlugin = new FlutterPluginRecord();

  double starty = 0.0;
  double offset = 0.0;
  bool isUp = false;
  String textShow = "按住说话";
  String toastShow = "手指上滑,取消发送";
  String voiceIco = "images/voice_volume_1.png";
  ///默认隐藏状态
  bool voiceState = true;

  @override
  void initState() {
    super.initState();
    recordPlugin.responseFromInit.listen((data) {
      if (data) {
        print("初始化成功");
      } else {
        print("初始化失败");
      }
    });
    recordPlugin.response.listen((data) {
      if (data.msg == "onStop") {
        print("onStop  " + data.path);
      } else if (data.msg == "onStart") {
        print("onStart --");
      }
    });
    recordPlugin.responseFromAmplitude.listen((data) {
      var voiceData = double.parse(data.msg);
      var tempVoice = "";
      if (voiceData > 0 && voiceData < 0.1) {
        tempVoice = "images/voice_volume_2.png";
      } else if (voiceData > 0.2 && voiceData < 0.3) {
        tempVoice = "images/voice_volume_3.png";
      } else if (voiceData > 0.3 && voiceData < 0.4) {
        tempVoice = "images/voice_volume_4.png";
      } else if (voiceData > 0.4 && voiceData < 0.5) {
        tempVoice = "images/voice_volume_5.png";
      } else if (voiceData > 0.5 && voiceData < 0.6) {
        tempVoice = "images/voice_volume_6.png";
      } else if (voiceData > 0.6 && voiceData < 0.7) {
        tempVoice = "images/voice_volume_7.png";
      } else if (voiceData > 0.7 && voiceData < 1) {
        tempVoice = "images/voice_volume_7.png";
      }
      setState(() {
        voiceIco = tempVoice;
      });

      print("振幅大小   " + voiceData.toString() + "  " + voiceIco);
    });
  }

  showVoiceView() {
    setState(() {
      textShow = "松开结束";
      voiceState = false;
    });
    start();
  }

  hideVoiceView() {
    setState(() {
      textShow = "按住说话";
      voiceState = true;
    });

    stop();

    if (isUp) {
      print("取消发送");
    } else {
      print("进行发送");
    }
  }

  moveVoiceView() {
    // print(offset - start);
    setState(() {
      isUp = starty - offset > 100 ? true : false;
      if (isUp) {
        textShow = "松开手指,取消发送";
        toastShow = textShow;
      } else {
        textShow = "松开结束";
        toastShow = "手指上滑,取消发送";
      }
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
            GestureDetector(
              onVerticalDragStart: (details) {
                starty = details.globalPosition.dy;
                showVoiceView();
              },
              onVerticalDragEnd: (details) {
                hideVoiceView();
              },
              onVerticalDragUpdate: (details) {
                offset = details.globalPosition.dy;
                moveVoiceView();
              },
              child: Container(
                height: 60,
                color: Colors.blue,
                margin: EdgeInsets.fromLTRB(50, 0, 50, 20),
                child: Center(
                  child: Text(
                    textShow,
                  ),
                ),
              ),
            ),
            Offstage(
              offstage: voiceState,
              child: Center(
                child: Opacity(
                  opacity: 0.5,
                  child: Container(
                    width: 160,
                    height: 160,
                    decoration: BoxDecoration(
                      color: Color(0xff77797A),
                      borderRadius: BorderRadius.all(Radius.circular(20.0)),
                    ),
                    child: Column(
                      children: <Widget>[
                        Container(
                          margin: EdgeInsets.only(top: 10),
                          child: new Image.asset(
                            voiceIco,
                            width: 100,
                            height: 100,
                          ),
                        ),
                        Container(
                          padding:
                          EdgeInsets.only(right: 20, left: 20, top: 0),
                          child: Text(
                            toastShow,
                            style: TextStyle(color: Colors.white),
                          ),
                        )
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _init() async {
    recordPlugin.init();
  }

  void start() async {
    recordPlugin.start();
  }

  void stop() {
    recordPlugin.stop();
  }

  void play() {
    recordPlugin.play();
  }


  @override
  void dispose() {
    recordPlugin.dispose();
    super.dispose();
  }
}
