
#import <Foundation/Foundation.h>

typedef void(^PlayCompleteBlock)(void);

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
 播放data格式录音

 @param data 录音data
 */
- (void)startPlayWithData:(NSData *)data;

/**
 停止播放
 */
- (void)stopPlaying;

/// 暂停播放
- (bool)pausePlaying;



@end
