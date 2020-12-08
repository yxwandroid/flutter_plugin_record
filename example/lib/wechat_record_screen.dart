import 'package:flutter/material.dart';
import 'package:flutter_plugin_record/index.dart';

class WeChatRecordScreen extends StatefulWidget {
  @override
  _WeChatRecordScreenState createState() => _WeChatRecordScreenState();
}

class _WeChatRecordScreenState extends State<WeChatRecordScreen> {
  String toastShow = "悬浮框";
  OverlayEntry overlayEntry;

  showView(BuildContext context) {
    if (overlayEntry == null) {
      overlayEntry = new OverlayEntry(builder: (content) {
        return Positioned(
          top: MediaQuery.of(context).size.height * 0.5 - 80,
          left: MediaQuery.of(context).size.width * 0.5 - 80,
          child: Material(
            child: Center(
              child: Opacity(
                opacity: 0.8,
                child: Container(
                  width: 100,
                  height: 100,
                  decoration: BoxDecoration(
                    color: Color(0xff77797A),
                    borderRadius: BorderRadius.all(Radius.circular(20.0)),
                  ),
                  child: Column(
                    children: <Widget>[
                      Container(
//                      padding: EdgeInsets.only(right: 20, left: 20, top: 0),
                        child: Text(
                          toastShow,
                          style: TextStyle(
                            fontStyle: FontStyle.normal,
                            color: Colors.white,
                            fontSize: 14,
                          ),
                        ),
                      )
                    ],
                  ),
                ),
              ),
            ),
          ),
        );
      });
      Overlay.of(context).insert(overlayEntry);
    }
  }

  startRecord() {
    print("开始录制");
  }

  stopRecord(String path, double audioTimeLength) {
    print("结束束录制");
    print("音频文件位置" + path);
    print("音频录制时长" + audioTimeLength.toString());
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("仿微信发送语音"),
      ),
      body: Container(
        child: Column(
          children: <Widget>[
            new FlatButton(
                onPressed: () {
                  showView(context);
                },
                child: new Text("悬浮组件")),
            new FlatButton(
                onPressed: () {
                  if (overlayEntry != null) {
                    overlayEntry.remove();
                    overlayEntry = null;
                  }
                },
                child: new Text("隐藏悬浮组件")),
            new FlatButton(
                onPressed: () {
                  setState(() {
                    toastShow = "111";
                    if (overlayEntry != null) {
                      overlayEntry.markNeedsBuild();
                    }
                  });
                },
                child: new Text("悬浮窗状态更新")),
            new VoiceWidget(
              startRecord: startRecord,
              stopRecord: stopRecord,
              // 加入定制化Container的相关属性
              height: 40.0,
            ),
          ],
        ),
      ),
    );
  }
}
