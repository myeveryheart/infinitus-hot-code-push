//
//  HCPInstallationWorker.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPWorker.h"
#import "HCPFilesStructure.h"

/**
 *  安装类
 *
 *  @see HCPWorker
 */
@interface HCPInstallationWorker : NSObject<HCPWorker>

/**
 *  初始化
 *
 *  @param newVersion 需要安装的新版本
 *  @param currentVersion 现在的版本
 *
 *  @return 实例
 */
- (instancetype)initWithNewVersion:(NSString *)newVersion currentVersion:(NSString *)currentVersion;

@end
