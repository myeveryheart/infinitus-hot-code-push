//
//  HCPUpdateLoader.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPUpdateLoader.h"
#import "NSError+HCPExtension.h"
#import "HCPUpdateLoaderWorker.h"
#import "HCPInstallationWorker.h"

@interface HCPUpdateLoader() {
    __block BOOL _isExecuting;
    id<HCPWorker> _task;
    HCPApplicationConfig *_newAPPConfig;
    HCPFilesStructure *_filesStructure;
}

@property (nonatomic, readwrite, getter=isInstallationInProgress) BOOL isInstallationInProgress;

@end

@implementation HCPUpdateLoader

#pragma mark Public API

+ (HCPUpdateLoader *)sharedInstance {
    static HCPUpdateLoader *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    
    return sharedInstance;
}

- (BOOL)isDownloadInProgress {
    return _isExecuting;
}

- (void)fetchUpdateWithConfigUrl:(NSURL *)configUrl currentWebVersion:(NSString *)currentWebVersion currentNativeVersion:(NSUInteger)currentNativeVersion fetchUpdateBlock:(FetchUpdateBlock)block
{
    NSError *error = nil;
    if (_isExecuting) {
        error = [NSError errorWithCode:kHCPDownloadAlreadyInProgressErrorCode description:@"Download already in progress. Please, wait for it to finish."];
        block(NO, error);
        return;
    }
    
    if ([HCPUpdateLoader sharedInstance].isInstallationInProgress) {
        error = [NSError errorWithCode:kHCPCantDownloadUpdateWhileInstallationInProgressErrorCode description:@"Installation is in progress, can't launch the download task. Please, wait for it to finish."];
        block(NO, error);
        return;
    }
    
    _task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl
                                                        currentWebVersion:currentWebVersion
                                                   nativeInterfaceVersion:currentNativeVersion];
    _isExecuting = YES;
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [_task fetchWithComplitionBlock:^(BOOL needUpdate, NSError *error, HCPApplicationConfig *newAppConfig) {
            dispatch_async(dispatch_get_main_queue(), ^{
                _isExecuting = NO;
                _newAPPConfig = newAppConfig;
                block(needUpdate, error);
            });
        }];
    });
}

- (void)downloadUpdateWithDownloadUpdateBlock:(HCPFileDownloadCompletionBlock)block
{
    NSError *error = nil;
    if (_isExecuting) {
        error = [NSError errorWithCode:kHCPDownloadAlreadyInProgressErrorCode description:@"Download already in progress. Please, wait for it to finish."];
        block(NO, 0, 0, error, nil);
        return;
    }
    
    if ([HCPUpdateLoader sharedInstance].isInstallationInProgress) {
        error = [NSError errorWithCode:kHCPCantDownloadUpdateWhileInstallationInProgressErrorCode description:@"Installation is in progress, can't launch the download task. Please, wait for it to finish."];
        block(NO, 0, 0, error, nil);
        return;
    }
    
//    id<HCPWorker> task = [[HCPUpdateLoaderWorker alloc] initWithConfigUrl:configUrl
//                                                        currentWebVersion:currentWebVersion
//                                                   nativeInterfaceVersion:currentNativeVersion];
    _isExecuting = YES;
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [_task downloadWithNewAppConfig:_newAPPConfig complitionBlock:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error, HCPApplicationConfig *newAppConfig) {
            dispatch_async(dispatch_get_main_queue(), ^{
                _isExecuting = NO;
                block(success, totalFiles, fileDownloaded, error, newAppConfig);
            });
        }];
    });
}

- (void)installVersion:(NSString *)newVersion
        currentVersion:(NSString *)currentVersion
       completionBlock:(HCPFileInstallCompletionBlock)block {
    NSError *error = nil;
    
    // 如果正在安装 - 退出
    if (_isInstallationInProgress) {
        error = [NSError errorWithCode:kHCPInstallationAlreadyInProgressErorrCode
                           description:@"Installation is already in progress"];
        block(NO, error, nil);
    }
    
    // 如果正在下载 - 退出
    if ([HCPUpdateLoader sharedInstance].isDownloadInProgress) {
        error = [NSError errorWithCode:kHCPCantInstallWhileDownloadInProgressErrorCode
                           description:@"Can't perform the installation, while update download in progress"];
        block(NO, error, nil);
    }
    
    HCPFilesStructure *newVersionFS = [[HCPFilesStructure alloc] initWithReleaseVersion:newVersion];
    
    // 检查是否有需要安装的文件
    if (![[NSFileManager defaultManager] fileExistsAtPath:newVersionFS.downloadFolder.path]) {
        error = [NSError errorWithCode:kHCPNothingToInstallErrorCode description:@"Nothing to install"];
        block(NO, error, nil);
    }
    
    // 开始安装
    id<HCPWorker> installationTask = [[HCPInstallationWorker alloc] initWithNewVersion:newVersion currentVersion:currentVersion];
    
    _isInstallationInProgress = YES;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [installationTask installWithComplitionBlock:^(BOOL success, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                _isInstallationInProgress = NO;
                block(success, error, _newAPPConfig);
            });
        }];
    });
}

#pragma mark Private API

//- (void)executeTask:(id<HCPWorker>)task {
//    _isExecuting = YES;
//    
//    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//        [task runWithComplitionBlock:^{
//            _isExecuting = NO;
//        }];
//    });
//}

@end
