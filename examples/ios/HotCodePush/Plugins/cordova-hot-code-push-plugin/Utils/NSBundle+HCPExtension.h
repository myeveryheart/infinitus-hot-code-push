//
//  NSBundle+Extension.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  扩展NSBundle
 */
@interface NSBundle (HCPExtension)

/**
 *  app build 版本号
 *
 *  @return build app build 版本号
 */
+ (NSInteger)applicationBuildVersion;

/**
 *  www文件夹在app bundle里的路径
 *
 *  @return 路径
 */
+ (NSString *)pathToWwwFolder;

@end
