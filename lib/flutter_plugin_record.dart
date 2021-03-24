import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_plugin_record/const/play_state.dart';
import 'package:flutter_plugin_record/const/response.dart';
import 'package:uuid/uuid.dart';

class FlutterPluginRecord {
  final MethodChannel _channel = const MethodChannel('flutter_plugin_record');

  static final _uuid = new Uuid();
  String id = '';
  static final alis = new Map<String, FlutterPluginRecord>();

  FlutterPluginRecord() {
    id = _uuid.v4();
    alis[id] = this;
    // print("--------FlutterPluginRecord init");
  }

  ///Flutter  调用原生初始化
  Future<dynamic> _invokeMethod(String method,
      [Map<String, dynamic> arguments = const {}]) {
    Map<String, dynamic> withId = Map.of(arguments);
    withId['id'] = id;
    _channel.setMethodCallHandler(_handler);
    return _channel.invokeMethod(method, withId);
  }

  ///初始化init的回调
  StreamController<bool> _responseInitController =
      new StreamController.broadcast();

  Stream<bool> get responseFromInit => _responseInitController.stream;

  ///开始录制 停止录制的回调监听
  StreamController<RecordResponse> _responseController =
      new StreamController.broadcast();

  Stream<RecordResponse> get response => _responseController.stream;

  ///音量高低的回调
  StreamController<RecordResponse> _responseAmplitudeController =
      new StreamController.broadcast();

  Stream<RecordResponse> get responseFromAmplitude =>
      _responseAmplitudeController.stream;

  ///播放状态监听
  StreamController<PlayState> _responsePlayStateController =
      new StreamController.broadcast();

  Stream<PlayState> get responsePlayStateController =>
      _responsePlayStateController.stream;

  ///原生回调
  static Future<dynamic> _handler(MethodCall methodCall) async{
    // print("--------FlutterPluginRecord " + methodCall.method);

    String id = (methodCall.arguments as Map)['id'];
    FlutterPluginRecord recordPlugin = alis[id] ?? FlutterPluginRecord();
    switch (methodCall.method) {
      case "onInit":
        bool flag = false;
        if ("success" == methodCall.arguments["result"]) {
          flag = true;
        }
        recordPlugin._responseInitController.add(flag);
        break;
      case "onStart":
        if ("success" == methodCall.arguments["result"]) {
          RecordResponse res = new RecordResponse(
            success: true,
            path: "",
            msg: "onStart",
            key: methodCall.arguments["key"].toString(),
          );
          recordPlugin._responseController.add(res);
        }

        break;
      case "onStop":
        if ("success" == methodCall.arguments["result"]) {
          RecordResponse res = new RecordResponse(
            success: true,
            path: methodCall.arguments["voicePath"].toString(),
            audioTimeLength:
                double.parse(methodCall.arguments["audioTimeLength"]),
            msg: "onStop",
            key: methodCall.arguments["key"].toString(),
          );
          recordPlugin._responseController.add(res);
        }

        break;
      case "onPlay":
        RecordResponse res = new RecordResponse(
          success: true,
          path: "",
          msg: "开始播放",
          key: methodCall.arguments["key"].toString(),
        );
        recordPlugin._responseController.add(res);
        break;
      case "onAmplitude":
        if ("success" == methodCall.arguments["result"]) {
          RecordResponse res = new RecordResponse(
            success: true,
            path: "",
            msg: methodCall.arguments["amplitude"].toString(),
            key: methodCall.arguments["key"].toString(),
          );
          recordPlugin._responseAmplitudeController.add(res);
        }
        break;
      case "onPlayState":
        var playState = methodCall.arguments["playState"];
        var playPath = methodCall.arguments["playPath"];
        PlayState res = new PlayState(playState, playPath);
        recordPlugin._responsePlayStateController.add(res);
        break;
      case "pausePlay":
        //暂停或继续播放
        var isPlaying = methodCall.arguments["isPlaying"];
        PlayState res = new PlayState(isPlaying, "");
        recordPlugin._responsePlayStateController.add(res);
        break;
      default:
        print("default");
        break;
    }
    return null;
  }

  //初始化
  Future init() async {
    return await _invokeMethod('init', <String, String>{
      "init": "init",
    });
  }

  //初始化
  Future initRecordMp3() async {
    return await _invokeMethod('initRecordMp3', <String, String>{
      "initRecordMp3": "initRecordMp3",
    });
  }

  Future start() async {
    return await _invokeMethod('start', <String, String>{
      "start": "start",
    });
  }

  Future startByWavPath(String wavPath) async {
    return await _invokeMethod('startByWavPath', <String, String>{
      "wavPath": wavPath,
    });
  }

  Future stop() async {
    return await _invokeMethod('stop', <String, String>{
      "stop": "stop",
    });
  }

  Future play() async {
    return await _invokeMethod('play', <String, String>{
      "play": "play",
    });
  }

//  Future playByPath(String path) async {
//    return await _invokeMethod('playByPath', <String, String>{
//      "play": "play",
//      "path": path,
//    });
//  }

  ///
  /// 参数 path  播放音频的地址
  ///
  ///path 为 url类型对应的在线播放地址   https://linjuli-app-audio.oss-cn-hangzhou.aliyuncs.com/audio/50c39c768b534ce1ba25d837ed153824.wav
  ///path 对应本地文件路径对应的是本地文件播放肚子   /sdcard/flutterdemo/wiw.wav
  /// 参数  type
  /// 当path 为url   type为 url
  /// 当path 为本地地址 type为 file
  ///
  Future playByPath(String path, String type) async {
    return await _invokeMethod('playByPath', <String, String>{
      "play": "play",
      "path": path,
      "type": type,
    });
  }

  ///暂停播放
  Future pausePlay() async {
    return await _invokeMethod('pause', <String, String>{
      "pause": "pause",
    });
  }

  /// 提供停止播放的功能
  Future stopPlay() async {
    return await _invokeMethod('stopPlay', <String, String>{});
  }

  dispose() {
//     stopPlay();
    _responseInitController.close();
    _responseController.close();
    _responseAmplitudeController.close();
    _responsePlayStateController.close();
  }
}
