//
//  HCPConfig.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  config配置
 */
@interface HCPConfig : NSObject

/**
 *  configUrl
 */
@property (nonatomic, strong) NSURL *configUrl;

/**
 *  app版本
 */
@property (nonatomic) NSUInteger nativeInterfaceVersion;

/**
 *  web端的www文件夹url
 */
@property (nonatomic, strong) NSURL *webUrl;



@end
