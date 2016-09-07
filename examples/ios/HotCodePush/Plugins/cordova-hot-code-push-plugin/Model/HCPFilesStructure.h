//
//  HCPFilesStructure.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  文件结构
 *  
 *  @see HCPFileStructure
 */
@interface HCPFilesStructure : NSObject

/**
 *  初始化
 *
 *  @param releaseVersion 文件版本
 *
 *  @return object 实例
 */
- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion;

/**
 *  更新目录的绝对路径
 */
@property (nonatomic, strong, readonly) NSURL *contentFolder;

/**
 *  web文件在外部存储的绝对路径
 */
@property (nonatomic, strong, readonly) NSURL *wwwFolder;

/**
 *  存储下载文件的文件夹绝对路径
 */
@property (nonatomic, strong, readonly) NSURL *downloadFolder;

/**
 *  config文件的名字
 */
@property (nonatomic, strong, readonly) NSString *configFileName;

/**
 *  manifest文件的名字
 */
@property (nonatomic, strong, readonly) NSString *manifestFileName;

/**
 *  更新的根目录
 *
 *  @return url 更新的根目录
 */
+ (NSURL *)hcpRootFolder;

/**
 *  config文件默认的名字
 *
 *  @return config文件默认的名字
 */
+ (NSString *)defaultConfigFileName;

/**
 *  manifest文件默认的名字
 *
 *  @return config文件默认的名字
 */
+ (NSString *)defaultManifestFileName;

@end
