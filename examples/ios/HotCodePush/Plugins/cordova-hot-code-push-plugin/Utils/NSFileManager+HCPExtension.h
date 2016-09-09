//
//  NSFileManager+HCPExtension.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  扩展NSFileManager
 */
@interface NSFileManager (HCPExtension)

/**
 *  获取Application Support路径，更新都是在这里面完成的
 *
 *  @return URL
 */
- (NSURL *)applicationSupportDirectory;

/**
 *  获取Cache路径，下载的更新包放这里
 *
 *  @return URL
 */
- (NSURL *)applicationCacheDirectory;

@end
