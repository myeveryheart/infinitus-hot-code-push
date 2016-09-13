//
//  HCPUpdateLoader.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPBlock.h"
#import "HCPFileDownloader.h"

/**
 *  下载工具类
 *
 *  @see HCPUpdateLoaderWorker
 */
@interface HCPUpdateLoader : NSObject

/**
 *  单例
 *
 *  @return 实例
 */
+ (HCPUpdateLoader *)sharedInstance;

/**
 *  检查更新
 *
 *  @param configUrl            服务器上的configUrl
 *  @param currentWebVersion    当前www版本
 *  @param currentNativeVersion 当前app版本
 *  @param block                block
 */
- (void)fetchUpdateWithConfigUrl:(NSURL *)configUrl currentWebVersion:(NSString *)currentWebVersion currentNativeVersion:(NSUInteger)currentNativeVersion fetchUpdateBlock:(FetchUpdateBlock)block;

/**
 *  新增下载任务
 *
 *  @param block                block
 */
- (void)downloadUpdateWithDownloadUpdateBlock:(HCPFileDownloadCompletionBlock)block;

/**
 *  是否正在下载
 *
 *  @return <code>YES</code> 是, <code>NO</code> 否
 */
@property (nonatomic, readonly, getter=isDownloadInProgress) BOOL isDownloadInProgress;




/**
 *  是否正在安装
 *
 *  @return <code>YES</code> 是; <code>NO</code> 否
 */
@property (nonatomic, readonly, getter=isInstallationInProgress) BOOL isInstallationInProgress;

/**
 *  启动安装
 *
 *  @param installVersion 需要安装的版本
 *  @param currentVersion 当前版本
 *  @param error 错误
 *
 *  @return <code>YES</code> 启动成功; <code>NO</code> 启动失败
 */
- (void)installVersion:(NSString *)newVersion
        currentVersion:(NSString *)currentVersion
       completionBlock:(HCPFileInstallCompletionBlock)block;



@end
