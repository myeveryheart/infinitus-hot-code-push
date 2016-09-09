//
//  NSError+HCPExtension.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  Error Domain
 */
extern NSString *const kHCPErrorDomain;

/**
 *  下载config文件出错
 */
extern NSInteger const kHCPFailedToDownloadApplicationConfigErrorCode;

/**
 *  本地app版本过低
 */
extern NSInteger const kHCPApplicationBuildVersionTooLowErrorCode;

/**
 *  下载manifest文件出错
 */
extern NSInteger const kHCPFailedToDownloadContentManifestErrorCode;

/**
 *  下载文件出错
 */
extern NSInteger const kHCPFailedToDownloadUpdateFilesErrorCode;

/**
 *  拷贝更新文件出错
 */
extern NSInteger const kHCPFailedToMoveLoadedFilesToInstallationFolderErrorCode;

/**
 *  更新文件不完整或者hash不对
 */
extern NSInteger const kHCPUpdateIsInvalidErrorCode;

/**
 *  拷贝老版本出错
 */
extern NSInteger const kHCPFailedToCopyFilesFromPreviousReleaseErrorCode;

/**
 *  拷贝新文件出错
 */
extern NSInteger const kHCPFailedToCopyNewContentFilesErrorCode;

/**
 *  读取当前config出错
 */
extern NSInteger const kHCPLocalVersionOfApplicationConfigNotFoundErrorCode;

/**
 *  读取当前manifest出错
 */
extern NSInteger const kHCPLocalVersionOfManifestNotFoundErrorCode;

/**
 *  读取新版config出错
 */
extern NSInteger const kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode;

/**
 *  读取新版manifest出错
 */
extern NSInteger const kHCPLoadedVersionOfManifestNotFoundErrorCode;

/**
 *  从bundle拷贝到external storage出错
 */
extern NSInteger const kHCPFailedToInstallAssetsOnExternalStorageErrorCode;

/**
 *  不需要安装更新
 */
extern NSInteger const kHCPNothingToInstallErrorCode;

/**
 *  不需要下载更新
 */
extern NSInteger const kHCPNothingToUpdateErrorCode;

/**
 *  正要安装，发现正在下载更新文件
 */
extern NSInteger const kHCPCantInstallWhileDownloadInProgressErrorCode;

/**
 *  正要下载，发现正在安装更新文件
 */
extern NSInteger const kHCPCantDownloadUpdateWhileInstallationInProgressErrorCode;

/**
 *  正要安装，发现正在安装更新文件
 */
extern NSInteger const kHCPInstallationAlreadyInProgressErorrCode;

/**
 *  正要下载，发现正在下载
 */
extern NSInteger const kHCPDownloadAlreadyInProgressErrorCode;

/**
 *  bundle还未安装到external storage
 */
extern NSInteger const kHCPAssetsNotYetInstalledErrorCode;

/**
 *  扩展NSError
 */
@interface NSError (HCPExtension)

/**
 *  创建error
 *
 *  @param errorCode   错误码
 *  @param description 描述
 *
 *  @return error实例
 */
+ (NSError *)errorWithCode:(NSInteger)errorCode description:(NSString *)description;

/**
 *  创建error
 *
 *  @param errorCode 错误码
 *  @param error     error对象
 *
 *  @return error实例
 */
+ (NSError *)errorWithCode:(NSInteger)errorCode descriptionFromError:(NSError *)error;

/**
 *  国际化
 *
 *  @return error message
 */
- (NSString *)underlyingErrorLocalizedDesription;

@end
