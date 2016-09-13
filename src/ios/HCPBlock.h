//
//  HCPBlock.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

/**
 *  检查更新的结果
 *
 *  @param needUpdate 是否需要强制更新
 *  @param error      错误
 */
typedef void (^FetchUpdateBlock) (BOOL needUpdate, NSError *error);

/**
 *  文件下载完成block
 *
 *  @param success    是否成功
 *  @param totalFiles    文件总数
 *  @param fileDownloaded 已下载文件数
 *  @param error         错误
 */
typedef void (^DownloadUpdateBlock)(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error);



typedef void (^InstallUpdateBlock)(BOOL success, NSError *error);

/**
 *  把www从bundle安装到外部的block
 *
 *  @param success 是否成功
 *  @param error   错误
 */
typedef void(^InstallWwwFolderBlock)(BOOL success, NSError *error);
