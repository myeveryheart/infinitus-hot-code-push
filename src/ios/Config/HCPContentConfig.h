//
//  HCPContentConfig.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  枚举更新类型
 */
typedef NS_ENUM(NSUInteger, HCPUpdateTime){
    /**
     *  未定义
     */
    HCPUpdateTimeUndefined = 0,
    /**
     *  强制更新
     */
    HCPUpdateTimeForced = 1,
    /**
     *  静默更新
     */
    HCPUpdateTimeSilent = 2
};

/**
 *  hcp.json文件对应的配置
 */
@interface HCPContentConfig : NSObject<HCPJsonConvertable>

/**
 *  www版本
 */
@property (nonatomic, strong, readonly) NSString *releaseVersion;

/**
 *  要求的最低的app版本
 */
@property (nonatomic, readonly) NSInteger minimumNativeVersion;

/**
 *  资源文件在web端的url
 */
@property (nonatomic, strong, readonly) NSURL *contentURL;

/**
 *  更新类型
 * 
 *  @see HCPUpdateTime
 */
@property (nonatomic, readonly) HCPUpdateTime updateTime;

@end
