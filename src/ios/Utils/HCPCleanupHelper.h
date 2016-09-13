//
//  HCPCleanupHelper.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  清理工具类
 */
@interface HCPCleanupHelper : NSObject

/**
 *  删除老版本
 *
 *  @param ignoredReleases 忽略文件
 */
+ (void)removeUnusedReleasesExcept:(NSArray *)ignoredReleases;

@end
