//
//  HCPApplicationConfig.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"
#import "HCPContentConfig.h"

/**
 *  保存hcp.json文件的信息
 */
@interface HCPApplicationConfig : NSObject<HCPJsonConvertable>

/**
 *  内容配置信息
 *
 *  @see HCPContentConfig
 */
@property (nonatomic, strong, readonly) HCPContentConfig *contentConfig;

/**
 *  创建配置实例
 *
 *  @param configFileName 配置文件的信息名字
 *
 *  @return 配置实例
 */
+ (instancetype)configFromBundle:(NSString *)configFileName;

@end
