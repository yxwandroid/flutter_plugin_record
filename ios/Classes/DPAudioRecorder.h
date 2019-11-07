//
//  AmrRecorder.h
//  aaaa
//
//  Created by Andrew on 2017/7/17.
//  Copyright © 2017年 Andrew. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void(^AudioRecorderFinishRecordingBlock)(NSData *data, NSTimeInterval audioTimeLength,NSString *path);

typedef void(^AudioStartRecordingBlock)(BOOL isRecording);

typedef void(^AudioRecordingFailBlock)(NSString *reason);

typedef void(^AudioSpeakPowerBlock)(float power);

@interface DPAudioRecorder : NSObject

@property (nonatomic, copy) AudioRecorderFinishRecordingBlock audioRecorderFinishRecording;  //播放完成回调

@property (nonatomic, copy) AudioStartRecordingBlock audioStartRecording;                    //开始播放回调

@property (nonatomic, copy) AudioRecordingFailBlock audioRecordingFail;                      //播放失败回调

@property (nonatomic, copy) AudioSpeakPowerBlock audioSpeakPower;                            //音频值测量回调

+ (DPAudioRecorder *)sharedInstance;

/**
 开始录音
 */
- (void)startRecording;

/**
 停止录音
 */
- (void)stopRecording;

@end
