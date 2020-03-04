#import "FlutterPluginRecordPlugin.h"
#import "DPAudioRecorder.h"
#import "DPAudioPlayer.h"



//#import <flutter_plugin_record/flutter_plugin_record-Swift.h>

//@implementation FlutterPluginRecordPlugin
//+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
//  [SwiftFlutterPluginRecordPlugin registerWithRegistrar:registrar];
//}
//@end

@implementation FlutterPluginRecordPlugin{
    FlutterMethodChannel *_channel;
    FlutterResult  _result;
    FlutterMethodCall  *_call;
    NSData  *wavData;
    NSString *audioPath;
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
    }
    return self;
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result{
    _result  = result;
    _call = call;
    NSString *method = call.method;
    if ([@"init" isEqualToString:method]) {
        [self initRecord ];
    }else if([@"start" isEqualToString:method]){
        [self start ];
    }else if([@"stop" isEqualToString:method]){
        [self stop ];
    }else if([@"play" isEqualToString:method]){
        [self play ];
    }else if([@"playByPath" isEqualToString:method]){
         [self playByPath];
    }else{
      result(FlutterMethodNotImplemented);
    }

}

- (void) initRecord{
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

- (void) start{
    
    [DPAudioRecorder.sharedInstance startRecording];
    
    NSLog(@"ios  voice   start");
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onStart" arguments:dict3];
}
- (void) stop{
    [DPAudioRecorder.sharedInstance stopRecording];
}

//public enum PlayState {
//    prepare, start, pause, complete
//}

- (void) play{
    
    [DPAudioPlayer.sharedInstance startPlayWithData:self->wavData];
    DPAudioPlayer.sharedInstance.playComplete = ^void(){
        NSLog(@"播放完成");
        NSDictionary *args =   [self->_call arguments];
        NSString *mId = [args valueForKey:@"id"];
        NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:audioPath,@"playPath",@"complete",@"playState",mId,@"id", nil];
        [self->_channel invokeMethod:@"onPlayState" arguments:dict3];
    };
    
    NSLog(@"ios  voice   play");
    NSDictionary *args =   [_call arguments];
    NSString *mId = [args valueForKey:@"id"];
    NSDictionary *dict3 = [NSDictionary dictionaryWithObjectsAndKeys:@"success",@"result",mId,@"id", nil];
    [_channel invokeMethod:@"onPlay" arguments:dict3];
}

- (void) playByPath{
    
    NSLog(@"ios  voice   play");
    NSDictionary *args =   [_call arguments];
    NSString *filePath = [args valueForKey:@"path"];
    NSData* data= [NSData dataWithContentsOfFile:filePath];
    
    [DPAudioPlayer.sharedInstance startPlayWithData:data];
     DPAudioPlayer.sharedInstance.playComplete = ^void(){
        NSLog(@"播放完成");
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

