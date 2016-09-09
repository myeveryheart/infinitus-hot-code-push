//
//  HCPWorker.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPBlock.h"
#import "HCPUpdateLoader.h"

/**
 *  下载和安装
 */
@protocol HCPWorker <NSObject>

///**
// *  每个work分配一个id
// */
//@property (nonatomic, strong, readonly) NSString *workerId;
//
///**
// *  启动下载和安装
// */
//- (void)runWithComplitionBlock:(void (^)(void))updateLoaderComplitionBlock;

/**
 *  是否要升级
 *
 *  @param block FetchUpdateBlock
 */
- (void)fetchWithComplitionBlock:(HCPFileFetchCompletionBlock)block;

/**
 *  下载更新
 *
 *  @param block DownloadUpdateBlock
 */
- (void)downloadWithNewAppConfig:(HCPApplicationConfig *)newAppConfig complitionBlock:(HCPFileDownloadCompletionBlock)block;

/**
 *  安装更新
 *
 *  @param block FetchUpdateBlock
 */
- (void)installWithComplitionBlock:(InstallUpdateBlock)block;

@end
