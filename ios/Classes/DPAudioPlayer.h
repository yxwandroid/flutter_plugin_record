//
//  DPAudioPlayer.h
//  AMRMedia
//
//  Created by Andrew on 2017/7/17.
//  Copyright © 2017年 prinsun. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef void(^PlayCompleteBlock)();

typedef void(^StartPlayingBlock)(BOOL isPlaying);

@interface DPAudioPlayer : NSObject

/**
 播放完成回调
 */
@property (nonatomic, copy) PlayCompleteBlock playComplete;

/**
 开始播放回调
 */
@property (nonatomic, copy) StartPlayingBlock startPlaying;

+ (DPAudioPlayer *)sharedInstance;

/**
 播放网络amr文件

 @param urlStr amr录音文件url
 */
- (void)startPlayWithURL:(NSString *)urlStr;

/**
 播放data格式录音

 @param data 录音data
 */
- (void)startPlayWithData:(NSData *)data;

/**
 停止播放
 */
- (void)stopPlaying;

@end
