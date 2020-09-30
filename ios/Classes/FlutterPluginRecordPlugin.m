#import "FlutterPluginRecordPlugin.h"
#import "DPAudioRecorder.h"
#import "DPAudioPlayer.h"


@implementation FlutterPluginRecordPlugin{
    FlutterMethodChannel *_channel;
    FlutterResult  _result;
    FlutterMethodCall  *_call;
    NSData  *wavData;
    NSString *audioPath;
    BOOL _isInit;//是否执行初始化的标识
}

+ (void)registerWithRegistrar:(NSObject <FlutterPluginRegistrar> *)registrar {
    FlutterMethodChannel *channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutter_plugin_record"
                                     binaryMessenger:[registrar messenger]];
    
    FlutterPluginRecordPlugin *instance =  [[FlutterPluginRecordPlugin alloc] initWithChannel:channel];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (instancetype)initWithChannel:(FlutterMethodChannel *)channel {
    self = [super init];
    if (self) {
        _channel = channel;
        _isInit = NO;
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result{
    _result  = result;
    _call = call;
    NSString *method = call.method;
    if ([@"init" isEqualToString:method]) {
        [self initRecord ];
    }else if([@"initRecordMp3" isEqualToString:method]){
        [self initMp3Record];
    }else if([@"startByWavPath" isEqualToString:method]){
        [self startByWavPath];
    }else if([@"start" isEqualToString:method]){
        [self start ];
    }else if([@"stop" isEqualToString:method]){
        [self stop ];
    }else if([@"play" isEqualToString:method]){
        [self play ];
    }else if([@"pause" isEqualToString:method]){
        [self pausePlay ];
    }else if([@"playByPath" isEqualToString:method]){
        [self playByPath];
    }else if([@"stopPlay" isEqualToString:method]){
        [self stopPlay];
    }else{
        result(FlutterMethodNotImplemented);
    }
    
}



//初始化录制mp3
- (void) initMp3Record{
    [DPAudioRecorder.sharedInstance initByMp3];
    [self initRecord];
    
}

///初始化语音录制的方法 初始化录制完成的回调,开始录制的回调,录制失败的回调,录制音量大小的回调
/// 注意未初始化的话 Flutter 不能监听到上述回调事件
- (void) initRecord{
    _isInit = YES;
    
    DPAudioRecorder.sharedInstance.audioRecorderFinishRecording = ^void (NSData *data, NSTimeInterval audioTimeLength,NSString *path){
        self->audioPath =path;
        self->wavData = data;
        NSLog(@"ios  voice   onStop");
        NSDictionary *args =   [self->_call arguments];
        NSString *mId = [args valueForKey:@"id"];
        
        NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:
                               @"success", @"result",
                               mId, @"id",
                               path, @"voicePath",
                               [NSString stringWithFormat:@"%.20lf", audioTimeLength], @"audioTimeLength",
                               nil];
        [self->_channel invokeMethod:@"onStop" arguments:dict3];
        
    };
    
    DPAudioRecorder.sharedInstance.audioStartRecording =  ^void(BOOL isRecording){
        NSLog(@"ios  voice   start  audioStartRecording");
    };
    DPAudioRecorder.sharedInstance.audioRecordingFail = ^void(NSString *reason){
        
        NSLog(@"ios  voice %@", reason);
        
    };
    DPAudioRecorder.sharedInstance.audioSpeakPower = ^void(float power){
        NSLog(@"ios  voice %f",power);
        NSString *powerStr = [NSString stringWithFormat:@"%f", power];
        NSDictionary *args =   [self->_call arguments];
        NSString *mId = [args valueForKey:@"id"];
        NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:
                               @"success",@"result",
                               mId ,@"id",
                               powerStr,@"amplitude",
                               nil];
        [self->_channel invokeMethod:@"onAmplitude" arguments:dict3];
    };
    
    NSLog(@"ios  voice   init");
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onInit" arguments:dict3];
}



/// 开始录制的方法
- (void) start{
    if (!_isInit) {
        NSLog(@"ios-------未初始化录制方法- initRecord--");
        return;
    }
    NSLog(@"ios--------start record -----function--- start----");
    [DPAudioRecorder.sharedInstance startRecording];
    
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onStart" arguments:dict3];
}

/// 根据文件路径进行录制
- (void) startByWavPath{
    if (!_isInit) {
        NSLog(@"ios-------未初始化录制方法- initRecord--");
        return;
    }
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSString *wavPath = [args valueForKey:@"wavPath"];
    
    NSLog(@"ios--------start record -----function--- startByWavPath----%@", wavPath);
    
    [DPAudioRecorder.sharedInstance initByWavPath:wavPath];
    [DPAudioRecorder.sharedInstance startRecording];
    
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onStart" arguments:dict3];
}



/// 停止录制的方法
- (void) stop{
    if (!_isInit) {
        NSLog(@"ios-------未初始化录制方法- initRecord--");
        return;
    }
    NSLog(@"ios--------stop record -----function--- stop----");
    [DPAudioRecorder.sharedInstance stopRecording];
}



///  播放录制完成的音频
- (void) play{
    
    NSLog(@"ios------play voice by warData----function---play--");
    [DPAudioPlayer.sharedInstance startPlayWithData:self->wavData];
    DPAudioPlayer.sharedInstance.playComplete = ^void(){
        NSLog(@"ios-----播放完成----by play");
        NSDictionary *args =   [self->_call arguments];
        NSString *mId = [args valueForKey:@"id"];
        NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:self->audioPath,@"playPath",@"complete",@"playState",mId,@"id", nil];
        [self->_channel invokeMethod:@"onPlayState" arguments:dict3];
    };
    
    
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onPlay" arguments:dict3];
}
- (void)stopPlay{
    [DPAudioPlayer.sharedInstance stopPlaying];
    
}
- (void) pausePlay{
    
    NSLog(@"ios------pausePlay----function---pausePlay--");
    bool isPlaying =  [DPAudioPlayer.sharedInstance pausePlaying];
    
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSString *isPlayingStr = nil;
    if (isPlaying) {
        isPlayingStr = @"true";
    }else{
        isPlayingStr = @"false";
    }
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:
                           @"success",@"result",
                           isPlayingStr,@"isPlaying",
                           mId,@"id",
                           nil];
    [_channel invokeMethod:@"pausePlay" arguments:dict3];
}

/// 根据指定路径播放音频
- (void) playByPath{
    NSLog(@"ios------play voice by path-----function---playByPath---");
    NSDictionary *args =   [_call arguments];
    NSString *filePath = [args valueForKey:@"path"];
    
    NSString *typeStr = [args valueForKey:@"type"];
    NSData *data;
    if ([typeStr isEqualToString:@"url"]) {
        data =[[NSData alloc]initWithContentsOfURL:[NSURL URLWithString:filePath]];
    }else if([typeStr isEqualToString:@"file"]){
        data= [NSData dataWithContentsOfFile:filePath];
        
    }
    
    [DPAudioPlayer.sharedInstance startPlayWithData:data];
    DPAudioPlayer.sharedInstance.playComplete = ^void(){
        NSLog(@"ios-----播放完成----by playbyPath---");
        NSDictionary *args =   [self->_call arguments];
        NSString *mId = [args valueForKey:@"id"];
        NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:filePath,@"playPath",@"complete",@"playState",mId,@"id", nil];
        [self->_channel invokeMethod:@"onPlayState" arguments:dict3];
    };
    
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onPlay" arguments:dict3];
}


@end

