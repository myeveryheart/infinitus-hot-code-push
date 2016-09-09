//
//  HCPInternalPreferences.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  更新功能使用的参数
 */
@interface HCPInternalPreferences : NSObject<HCPJsonConvertable>

/**
 *  上次app启动时的版本，可以用来检测app是否已更新
 */
@property (nonatomic) NSInteger appBuildVersion;

/**
 *  www文件夹是否安装到外部存储
 */
@property (nonatomic, getter=isWwwFolderInstalled) BOOL wwwFolderInstalled;

/**
 *  上一个版本号，可以用来回滚
 */
@property (nonatomic, strong) NSString *previousReleaseVersionName;

/**
 *  当前版本号
 */
@property (nonatomic, strong) NSString *currentReleaseVersionName;

/**
 *  已准备好安装的版本号
 */
@property (nonatomic, strong) NSString *readyForInstallationReleaseVersionName;

/**
 *  初始化
 *
 *  @return 实例
 */
+ (HCPInternalPreferences *)defaultConfig;


@end
