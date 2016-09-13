//
//  HCPUpdateLoaderWorker.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPFilesStructure.h"
#import "HCPWorker.h"

/**
 *  实现下载逻辑
 *
 *  @see HCPWorker
 */
@interface HCPUpdateLoaderWorker : NSObject<HCPWorker>

/**
 *  初始化
 *
 *  @param configURL     web的config
 *  @param currentVersion 当前版本
 *
 *  @return 实例
 *  @see HCPFilesStructure
 */
- (instancetype)initWithConfigUrl:(NSURL *)configURL currentWebVersion:(NSString *)currentWebVersion nativeInterfaceVersion:(NSUInteger)currentNativeVersion;

@end
