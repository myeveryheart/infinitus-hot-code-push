//
//  HCPFileDownloader.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPBlock.h"
#import "HCPApplicationConfig.h"

/**
 *  文件下载完成block
 *
 *  @param success    是否成功
 *  @param totalFiles    文件总数
 *  @param fileDownloaded 已下载文件数
 *  @param error         错误
 *  @param newAppConfig         新配置
 */
typedef void (^HCPFileDownloadCompletionBlock)(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error, HCPApplicationConfig *newAppConfig);

/**
 *  检查更新完成block
 *
 *  @param needUpdate 是否需要强制更新
 *  @param error      错误
 *  @param newAppConfig         新配置
 */
typedef void (^HCPFileFetchCompletionBlock)(BOOL needUpdate, NSError *error, HCPApplicationConfig *newAppConfig);




typedef void (^HCPFileInstallCompletionBlock)(BOOL success, NSError *error, HCPApplicationConfig *newAppConfig);

/**
 *  数据下载完成block
 *
 *  @param data  下载数据
 *  @param error 错误
 */
typedef void (^HCPDataDownloadCompletionBlock)(NSData *data, NSError *error);

/**
 *  下载文件的工具类
 */
@interface HCPFileDownloader : NSObject

/**
 *  异步下载
 *
 *  @param url      下载的url
 *  @param block    下载的数据
 */
- (void) downloadDataFromUrl:(NSURL*) url completionBlock:(HCPDataDownloadCompletionBlock) block;

/**
 *  异步下载文件
 *
 *  @param filesList  文件列表
 *  @param contentURL 文件url
 *  @param folderURL  存储url
 *  @param block      下载完成block
 *
 *  @see HCPManifestFile
 */
- (void) downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL completionBlock:(DownloadUpdateBlock)block;

@end
