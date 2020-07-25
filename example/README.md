# flutter_plugin_record_example


### 使用

### 1, 初始化录制



可以在页面初始化的时候进行初始化比如: 在initState方法中进行初始化

    //实例化对象 
    FlutterPluginRecord   recordPlugin = new FlutterPluginRecord();
    //    初始化
    recordPlugin.init()

### 2, 开始录制
   
     recordPlugin.start()
### 3, 停止录制
     recordPlugin.stop()
### 4, 播放 暂停,停止播放
#### 1,播放
     
     recordPlugin.play()
     
#### 2, 暂停和继续播放
       
     recordPlugin.pausePlay();

#### 3, 停止播放
    
     recordPlugin.stopPlay();
      
### 3, 释放资源
可以在页面退出的时候进行资源释放 比如在  dispose方法中调用如下代码

     recordPlugin.dispose()
     
### 4,回调监听  
1,初始化回调监听  

  
    ///初始化方法的监听
    recordPlugin.responseFromInit.listen((data) {
      if (data) {
        print("初始化成功");
      } else {
        print("初始化失败");
      }
    });
    

2,开始录制停止录制监听

     /// 开始录制或结束录制的监听
        recordPlugin.response.listen((data) {
          if (data.msg == "onStop") {
            ///结束录制时会返回录制文件的地址方便上传服务器
            print("onStop  " + data.path);
          } else if (data.msg == "onStart") {
            print("onStart --");
          }
        });
    
3,录制声音大小回调监听


     ///录制过程监听录制的声音的大小 方便做语音动画显示图片的样式
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
            if(overlayEntry!=null){
              overlayEntry.markNeedsBuild();
            }
          });
    
          print("振幅大小   " + voiceData.toString() + "  " + voiceIco);
        });
    
     
## 2,录制组件的使用


### 1,在使用的页面进行导入package

    import 'package:flutter_plugin_record/index.dart';  
        
    
    

    
### 2,在使用的地方引入VoiceWidget组件
    
    new VoiceWidget(),
    
    
    
## TODO

* [x] 实现发送语音时间按下抬起时间很短提示
* [x] 优化代码
* [x] 实现录制完成文件路径回调功能,方面使用者可以把录音文件上传服务器


## 关注公众号获取更多内容

  ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190926100941125.jpg)

