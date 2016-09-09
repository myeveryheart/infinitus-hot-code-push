//
//  HCPAssetsFolderHelper.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPAssetsFolderHelper.h"
#import "NSError+HCPExtension.h"
#import "NSBundle+HCPExtension.h"

@interface HCPAssetsFolderHelper()

/**
 *  正在作业的标志，避免多次调用起冲突
 */
@property (nonatomic) BOOL isWorking;

@end

@implementation HCPAssetsFolderHelper

#pragma mark Public API

+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL completionBlock:(InstallWwwFolderBlock)block
{
    HCPAssetsFolderHelper *helper = [HCPAssetsFolderHelper sharedInstance];
    if (helper.isWorking) {
        return;
    }
    helper.isWorking = YES;
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [helper __installWwwFolderToExternalStorageFolder:externalFolderURL completionBlock:block];
    });
}

#pragma mark Private API

+ (HCPAssetsFolderHelper *)sharedInstance {
    static HCPAssetsFolderHelper *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[HCPAssetsFolderHelper alloc] init];
    });
    
    return sharedInstance;
}

- (void)__installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL completionBlock:(InstallWwwFolderBlock)block
{
    NSError *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:externalFolderURL.path];
    
    // 删除原来的www文件
    if (isWWwFolderExists) {
        [fileManager removeItemAtURL:[externalFolderURL URLByDeletingLastPathComponent] error:&error];
    }
    
    // 创建新的的www文件
    if (![fileManager createDirectoryAtURL:[externalFolderURL URLByDeletingLastPathComponent] withIntermediateDirectories:YES attributes:nil error:&error]) {
        block(NO, error);
//        [self dispatchErrorEvent:error];
        return;
    }
    
    // 拷贝本地www到外部www
    NSURL *localWww = [NSURL fileURLWithPath:[NSBundle pathToWwwFolder] isDirectory:YES];
    [fileManager copyItemAtURL:localWww toURL:externalFolderURL error:&error];
    
    if (error)
    {
        block(NO, error);
    } else {
        block(YES, nil);
    }
    
    self.isWorking = NO;
}

///**
// *  发送错误事件
// *
// *  @param error 错误
// */
//- (void)dispatchErrorEvent:(NSError *)error {
//    NSString *errorMsg = [error.userInfo[NSUnderlyingErrorKey] localizedDescription];
//    NSError *hcpError = [NSError errorWithCode:kHCPFailedToInstallAssetsOnExternalStorageErrorCode description:errorMsg];
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPBundleAssetsInstallationErrorEvent
//                                                 applicationConfig:nil
//                                                            taskId:nil
//                                                             error:hcpError];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}
//
///**
// *  发送成功事件
// */
//- (void)dispatchSuccessEvent {
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPBundleAssetsInstalledOnExternalStorageEvent
//                                                 applicationConfig:nil
//                                                            taskId:nil];
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}
//
///**
// *  发送安装之前事件
// */
//- (void)dispatchBeforeInstallEvent {
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent
//                                                 applicationConfig:nil
//                                                            taskId:nil];
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}

@end
