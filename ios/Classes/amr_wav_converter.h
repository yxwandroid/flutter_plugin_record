//
//  amr_wav_converter.h
//  ChatToolBarAudioButton
//
//  Created by Andrew on 2017/7/18.
//  Copyright © 2017年 Andrew. All rights reserved.
//

#ifndef AMR_WAV_CONVERTER_H
#define AMR_WAV_CONVERTER_H


/**
 *  将AMR文件转码成WAVE文件
 *
 *  @param amr_filename  要转码的AMR文件
 *  @param wave_filename 转码后的WAVE文件路径
 *
 *  @return 被转码的帧数
 */
int amr_file_to_wave_file(const char* amr_filename, const char* wave_filename);


/**
 *  将WAVE文件转码成AMR文件
 *  WAVE音频采样频率是8khz
 *  音频样本单元数 = 8000*0.02 = 160 (由采样频率决定)
 *
 *  @param wave_filename 要转码的WAVE文件
 *  @param amr_filename  转码后AMR文件
 *  @param channels      声道数: 1 : 160
 *                              2 : 160*2 = 320
 *  @param bps           采样率
 *                       bps = 8  --> 8位 unsigned char
 *                             16 --> 16位 unsigned short
 *
 *  @return 被转码的帧数
 */
int wave_file_to_amr_file(const char* wave_filename, const char* amr_filename, int channels, int bps);



#endif
