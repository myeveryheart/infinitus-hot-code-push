//
//  HCPUpdateLoaderWorker.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPUpdateLoaderWorker.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "HCPManifestDiff.h"
#import "HCPManifestFile.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPContentManifestStorage.h"
#import "HCPFileDownloader.h"
#import "NSError+HCPExtension.h"
#import "HCPContentManifest.h"

@interface HCPUpdateLoaderWorker() {
    NSURL *_configURL;
    HCPFilesStructure *_hcpFiles;
    NSUInteger _nativeInterfaceVersion;
    
    id<HCPConfigFileStorage> _appConfigStorage;
    id<HCPConfigFileStorage> _manifestStorage;
    
    HCPApplicationConfig *_oldAppConfig;
    HCPContentManifest *_oldManifest;
    
    void (^_complitionBlock)(void);
}

@property (nonatomic, strong, readwrite) NSString *workerId;
//@property (nonatomic, strong) HCPApplicationConfig *appConfigNew;

@end

@implementation HCPUpdateLoaderWorker

#pragma mark Public API

- (instancetype)initWithConfigUrl:(NSURL *)configURL currentWebVersion:(NSString *)currentWebVersion nativeInterfaceVersion:(NSUInteger)currentNativeVersion {
    self = [super init];
    if (self) {
        _configURL = configURL;
        _nativeInterfaceVersion = currentNativeVersion;
        _workerId = [self generateWorkerId];
        _hcpFiles = [[HCPFilesStructure alloc] initWithReleaseVersion:currentWebVersion];
        _appConfigStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_hcpFiles];
        _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:_hcpFiles];
    }
    
    return self;
}

- (void)fetchWithComplitionBlock:(HCPFileFetchCompletionBlock)block
{
    // 初始化
    NSError *error = nil;
    if (![self loadLocalConfigs:&error]) {
//        [self notifyWithError:error applicationConfig:nil];
        block(NO, error, nil);
        return;
    }
    
    HCPFileDownloader *configDownloader = [[HCPFileDownloader alloc] init];
    
    // 下载新的config
    [configDownloader downloadDataFromUrl:_configURL completionBlock:^(NSData *data, NSError *error) {
        HCPApplicationConfig *newAppConfig = [self getApplicationConfigFromData:data error:&error];
        if (newAppConfig == nil) {
            error = [NSError errorWithCode:kHCPFailedToDownloadApplicationConfigErrorCode descriptionFromError:error];
            block(NO, error, nil);
            return;
        }
        
        //新版本号比旧版大才更新
        if ([newAppConfig.contentConfig.releaseVersion compare:_oldAppConfig.contentConfig.releaseVersion] != NSOrderedDescending)
        {
            error = [NSError errorWithCode:kHCPNothingToUpdateErrorCode description:@"Nothing to update"];
            block(NO, error, newAppConfig);
            return;
        }
        
        // 本地app版本是否支持新版本
        if (newAppConfig.contentConfig.minimumNativeVersion > _nativeInterfaceVersion) {
            error = [NSError errorWithCode:kHCPApplicationBuildVersionTooLowErrorCode description:@"Application build version is too low for this update"];
            block(NO, error, newAppConfig);
            return;
        }
        
        if (newAppConfig.contentConfig.updateTime == HCPUpdateTimeForced)
        {
            //强制更新
            block(YES, error, newAppConfig);
            return;
        }
//        else
//        {
//            // 静默更新
//            // 下载新的manifest
//            NSURL *manifestFileURL = [self.appConfigNew.contentConfig.contentURL URLByAppendingPathComponent:_hcpFiles.manifestFileName];
//            [configDownloader downloadDataFromUrl:manifestFileURL completionBlock:^(NSData *data, NSError *error) {
//                HCPContentManifest *newManifest = [self getManifestConfigFromData:data error:&error];
//                if (newManifest == nil) {
//                    error = [NSError errorWithCode:kHCPFailedToDownloadContentManifestErrorCode descriptionFromError:error];
//                    return;
//                }
//                
//                // 比较manifest
//                HCPManifestDiff *manifestDiff = [_oldManifest calculateDifference:newManifest];
//                if (manifestDiff.isEmpty) {
//                    [_manifestStorage store:newManifest inFolder:_hcpFiles.wwwFolder];
//                    [_appConfigStorage store:self.appConfigNew inFolder:_hcpFiles.wwwFolder];
//                    error = [NSError errorWithCode:kHCPNothingToUpdateErrorCode description:@"Nothing to update"];
//                    return;
//                }
//                
//                // 新版文件
//                _hcpFiles = [[HCPFilesStructure alloc] initWithReleaseVersion:self.appConfigNew.contentConfig.releaseVersion];
//                
//                // 创建文件夹
//                [self createNewReleaseDownloadFolder:_hcpFiles.downloadFolder];
//                
//                // 下载更新文件
//                NSArray *updatedFiles = manifestDiff.updateFileList;
//                if (updatedFiles.count > 0) {
//                    [self downloadUpdatedFiles:updatedFiles appConfig:self.appConfigNew manifest:newManifest completionBlock:nil];
//                    return;
//                }
//                
//                // 保存manifest和config
//                [_manifestStorage store:newManifest inFolder:_hcpFiles.downloadFolder];
//                [_appConfigStorage store:self.appConfigNew inFolder:_hcpFiles.downloadFolder];
//                
////                [self notifyUpdateDownloadSuccess:self.appConfigNew];
//            }];
//        }
    }];
}

- (void)downloadWithNewAppConfig:(HCPApplicationConfig *)newAppConfig complitionBlock:(HCPFileDownloadCompletionBlock)block
{
    // 下载新的manifest
    NSURL *manifestFileURL = [newAppConfig.contentConfig.contentURL URLByAppendingPathComponent:_hcpFiles.manifestFileName];
    HCPFileDownloader *configDownloader = [[HCPFileDownloader alloc] init];
    [configDownloader downloadDataFromUrl:manifestFileURL completionBlock:^(NSData *data, NSError *error) {
        HCPContentManifest *newManifest = [self getManifestConfigFromData:data error:&error];
        if (newManifest == nil) {
            error = [NSError errorWithCode:kHCPFailedToDownloadContentManifestErrorCode descriptionFromError:error];
            block(NO, 0, 0, error, nil);
            return;
        }
        
        // 比较manifest
        HCPManifestDiff *manifestDiff = [_oldManifest calculateDifference:newManifest];
        if (manifestDiff.isEmpty) {
            [_manifestStorage store:newManifest inFolder:_hcpFiles.wwwFolder];
            [_appConfigStorage store:newAppConfig inFolder:_hcpFiles.wwwFolder];
            error = [NSError errorWithCode:kHCPNothingToUpdateErrorCode description:@"Nothing to update"];
//            [self notifyNothingToUpdate:self.appConfigNew];
            block(NO, 0, 0, error, newAppConfig);
            return;
        }
        
        // 新版文件
        _hcpFiles = [[HCPFilesStructure alloc] initWithReleaseVersion:newAppConfig.contentConfig.releaseVersion];
        
        // 创建文件夹
        [self createNewReleaseDownloadFolder:_hcpFiles.downloadFolder];
        
        // 下载更新文件
        NSArray *updatedFiles = manifestDiff.updateFileList;
        if (updatedFiles.count > 0) {
            [self downloadUpdatedFiles:updatedFiles appConfig:newAppConfig manifest:newManifest completionBlock:block];
            return;
        }
        
        // 保存manifest和config
        [_manifestStorage store:newManifest inFolder:_hcpFiles.downloadFolder];
        [_appConfigStorage store:newAppConfig inFolder:_hcpFiles.downloadFolder];
        
//        [self notifyUpdateDownloadSuccess:self.appConfigNew];
        block(YES, 0, 0, nil, newAppConfig);
    }];
}

-(void)installWithComplitionBlock:(InstallUpdateBlock)block
{
    
}

//- (void)run {
//    [self runWithComplitionBlock:nil];
//}
//
//- (void)runWithComplitionBlock:(void (^)(void))updateLoaderComplitionBlock {
//    _complitionBlock = updateLoaderComplitionBlock;
//    
//    // 初始化
//    NSError *error = nil;
//    if (![self loadLocalConfigs:&error]) {
//        [self notifyWithError:error applicationConfig:nil];
//        return;
//    }
//    
//    HCPFileDownloader *configDownloader = [[HCPFileDownloader alloc] init];
//    
//    // 下载新的config
//    [configDownloader downloadDataFromUrl:_configURL completionBlock:^(NSData *data, NSError *error) {
//        HCPApplicationConfig *_newAppConfig = [self getApplicationConfigFromData:data error:&error];
//        if (_newAppConfig == nil) {
//            [self notifyWithError:[NSError errorWithCode:kHCPFailedToDownloadApplicationConfigErrorCode descriptionFromError:error]
//                applicationConfig:nil];
//            return;
//        }
//        
//        // 检查新旧版版本号是否一样
//        if ([_newAppConfig.contentConfig.releaseVersion isEqualToString:_oldAppConfig.contentConfig.releaseVersion]) {
//            [self notifyNothingToUpdate:_newAppConfig];
//            return;
//        }
//        
//        // 本地app版本是否支持新版本
//        if (_newAppConfig.contentConfig.minimumNativeVersion > _nativeInterfaceVersion) {
//            [self notifyWithError:[NSError errorWithCode:kHCPApplicationBuildVersionTooLowErrorCode
//                                             description:@"Application build version is too low for this update"]
//                applicationConfig:_newAppConfig];
//            return;
//        }
//        
//        // 下载新的manifest
//        NSURL *manifestFileURL = [_newAppConfig.contentConfig.contentURL URLByAppendingPathComponent:_hcpFiles.manifestFileName];
//        [configDownloader downloadDataFromUrl:manifestFileURL completionBlock:^(NSData *data, NSError *error) {
//            HCPContentManifest *newManifest = [self getManifestConfigFromData:data error:&error];
//            if (newManifest == nil) {
//                [self notifyWithError:[NSError errorWithCode:kHCPFailedToDownloadContentManifestErrorCode
//                                        descriptionFromError:error]
//                    applicationConfig:_newAppConfig];
//                return;
//            }
//            
//            // 比较manifest
//            HCPManifestDiff *manifestDiff = [_oldManifest calculateDifference:newManifest];
//            if (manifestDiff.isEmpty) {
//                [_manifestStorage store:newManifest inFolder:_hcpFiles.wwwFolder];
//                [_appConfigStorage store:_newAppConfig inFolder:_hcpFiles.wwwFolder];
//                [self notifyNothingToUpdate:_newAppConfig];
//                return;
//            }
//            
//            // 新版文件
//            _hcpFiles = [[HCPFilesStructure alloc] initWithReleaseVersion:_newAppConfig.contentConfig.releaseVersion];
//            
//            // 创建文件夹
//            [self createNewReleaseDownloadFolder:_hcpFiles.downloadFolder];
//            
//            // 下载更新文件
//            NSArray *updatedFiles = manifestDiff.updateFileList;
//            if (updatedFiles.count > 0) {
//                [self downloadUpdatedFiles:updatedFiles appConfig:_newAppConfig manifest:newManifest];
//                return;
//            }
//            
//            // 保存manifest和config，，
//            [_manifestStorage store:newManifest inFolder:_hcpFiles.downloadFolder];
//            [_appConfigStorage store:_newAppConfig inFolder:_hcpFiles.downloadFolder];
//            
//            [self notifyUpdateDownloadSuccess:_newAppConfig];
//        }];
//    }];
//}

#pragma mark Private API

- (void)downloadUpdatedFiles:(NSArray *)updatedFiles
                   appConfig:(HCPApplicationConfig *)_newAppConfig
                    manifest:(HCPContentManifest *)newManifest
             completionBlock:(HCPFileDownloadCompletionBlock)block
{
    
    // 下载文件
    HCPFileDownloader *downloader = [[HCPFileDownloader alloc] init];
    
    [downloader downloadFiles:updatedFiles
                      fromURL:_newAppConfig.contentConfig.contentURL
                     toFolder:_hcpFiles.downloadFolder
              completionBlock:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error) {
                  if (error)
                  {
                      // 删除文件夹
                      [[NSFileManager defaultManager] removeItemAtURL:_hcpFiles.contentFolder error:nil];
                      // 发送错误
                      error = [NSError errorWithCode:kHCPFailedToDownloadUpdateFilesErrorCode descriptionFromError:error];
                  }
                  else
                  {
                      // 保存config和manifest
                      [_manifestStorage store:newManifest inFolder:_hcpFiles.downloadFolder];
                      [_appConfigStorage store:_newAppConfig inFolder:_hcpFiles.downloadFolder];
                  }
                  // 通知完成
                  block(success, totalFiles, fileDownloaded, error, _newAppConfig);
              }];
}

- (HCPApplicationConfig *)getApplicationConfigFromData:(NSData *)data error:(NSError **)error {
    if (*error) {
        return nil;
    }
    
    NSDictionary* json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:error];
    if (*error) {
        return nil;
    }
    
    return [HCPApplicationConfig instanceFromJsonObject:json];
}

- (HCPContentManifest *)getManifestConfigFromData:(NSData *)data error:(NSError **)error {
    if (*error) {
        return nil;
    }
    
    NSDictionary* json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:error];
    if (*error) {
        return nil;
    }
    
    return [HCPContentManifest instanceFromJsonObject:json];
}

/**
 *  读取当前版本config
 *
 *  @param error 错误
 *
 *  @return <code>YES</code> 读取成功; <code>NO</code> 失败
 */
- (BOOL)loadLocalConfigs:(NSError **)error {
    *error = nil;
    _oldAppConfig = [_appConfigStorage loadFromFolder:_hcpFiles.wwwFolder];
    if (_oldAppConfig == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load current application config"];
        return NO;
    }
    
    _oldManifest = [_manifestStorage loadFromFolder:_hcpFiles.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load current manifest file"];
        return NO;
    }
    
    return YES;
}

/**
 *  发送错误
 *
 *  @param error  错误
 *  @param config config
 */
//- (void)notifyWithError:(NSError *)error applicationConfig:(HCPApplicationConfig *)config {
//    if (_complitionBlock) {
//        _complitionBlock();
//    }
//    
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateDownloadErrorEvent
//                                                 applicationConfig:config
//                                                            taskId:self.workerId
//                                                             error:error];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}

/**
 *  已经是最新版本不需要更新
 *
 *  @param config config
 */
//- (void)notifyNothingToUpdate:(HCPApplicationConfig *)config {
//    if (_complitionBlock) {
//        _complitionBlock();
//    }
//    
//    NSError *error = [NSError errorWithCode:kHCPNothingToUpdateErrorCode description:@"Nothing to update"];
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPNothingToUpdateEvent
//                                                 applicationConfig:config
//                                                            taskId:self.workerId
//                                                             error:error];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}

/**
 *  下载更新包完成
 *
 *  @param config config
 */
//- (void)notifyUpdateDownloadSuccess:(HCPApplicationConfig *)config {
//    if (_complitionBlock) {
//        _complitionBlock();
//    }
//    
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsReadyForInstallationEvent
//                                                 applicationConfig:config
//                                                            taskId:self.workerId];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}

/**
 *  创建新下载目录
 *
 *  @param downloadFolder 下载目录
 */
- (void)createNewReleaseDownloadFolder:(NSURL *)downloadFolder {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    NSError *error = nil;
    if ([fileManager fileExistsAtPath:downloadFolder.path]) {
        [fileManager removeItemAtURL:downloadFolder error:&error];
    }
    
    [fileManager createDirectoryAtURL:downloadFolder withIntermediateDirectories:YES attributes:nil error:&error];
}

/**
 *  创建id
 *
 *  @return worker id
 */
- (NSString *)generateWorkerId {
    NSTimeInterval millis = [[NSDate date] timeIntervalSince1970];
    
    return [NSString stringWithFormat:@"%f",millis];
}

@end
