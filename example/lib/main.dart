import 'package:flutter/material.dart';
import 'package:flutter_plugin_record_example/overlay_screen.dart';
import 'package:flutter_plugin_record_example/path_provider_screen.dart';
import 'package:flutter_plugin_record_example/second_screen.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      title: 'Flutter Demo',
      theme: new ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: new MyHomePage(title: 'Flutter Demo Home Page'),
      routes: {
        "SecondScreen": (BuildContext context) => new SecondScreen(),
        "OverLayScreen": (BuildContext context) => new OverLayScreen(),
        "PathProviderScreen": (BuildContext context) => new PathProviderScreen(),
      },
    );
  }
}

class MyHomePage extends StatefulWidget {
  MyHomePage({Key key, this.title}) : super(key: key);
  final String title;

  @override
  _MyHomePageState createState() => new _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {


  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return new Scaffold(
      appBar: new AppBar(
        title: new Text("flutter版微信语音录制实现"),
      ),
      body: new Center(
        child: new Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            new FlatButton(
                onPressed: () {
                  Navigator.pushNamed<dynamic>(context, "SecondScreen");
                },
                child: new Text("进入语音录制界面")),
            new FlatButton(
                onPressed: () {
                  Navigator.pushNamed<dynamic>(context, "OverLayScreen");
                },
                child: new Text("进入仿微信录制界面")),
            new FlatButton(
                onPressed: () {
                  Navigator.pushNamed<dynamic>(context, "PathProviderScreen");
                },
                child: new Text("进入文件路径获取界面")),

          ],
        ),
      ),
    );
  }
}
