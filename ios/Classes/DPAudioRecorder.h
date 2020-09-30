
#import <Foundation/Foundation.h>

typedef void(^AudioRecorderFinishRecordingBlock)(NSData *data, NSTimeInterval audioTimeLength,NSString *path);

typedef void(^AudioStartRecordingBlock)(BOOL isRecording);

typedef void(^AudioRecordingFailBlock)(NSString *reason);

typedef void(^AudioSpeakPowerBlock)(float power);

/// 录制语音
@interface DPAudioRecorder : NSObject

/// 录制完成的回调
@property (nonatomic, copy) AudioRecorderFinishRecordingBlock audioRecorderFinishRecording;
/// 开始录制回调
@property (nonatomic, copy) AudioStartRecordingBlock audioStartRecording;
/// 录制失败回调
@property (nonatomic, copy) AudioRecordingFailBlock audioRecordingFail;
/// 音频值测量回调
@property (nonatomic, copy) AudioSpeakPowerBlock audioSpeakPower;


+ (DPAudioRecorder *)sharedInstance;

/// 传递录制文件路径
- (void)initByWavPath:(NSString*) wavPath;
- (void)initByMp3;

/// 开始录音方法
- (void)startRecording;

/// 停止录音方法
- (void)stopRecording;

@end
