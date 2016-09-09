//
//  HCPInstallationWorker.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPInstallationWorker.h"
#import "HCPManifestDiff.h"
#import "HCPContentManifest.h"
#import "HCPApplicationConfig.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPContentManifestStorage.h"
#import "NSError+HCPExtension.h"
#import "NSData+HCPMD5.h"

@interface HCPInstallationWorker() {
    HCPFilesStructure *_newReleaseFS;
    HCPFilesStructure *_currentReleaseFS;
    
    id<HCPConfigFileStorage> _manifestStorage;
    id<HCPConfigFileStorage> _configStorage;
    HCPApplicationConfig *_oldConfig;
    HCPApplicationConfig *_newConfig;
    HCPContentManifest *_oldManifest;
    HCPContentManifest *_newManifest;
    HCPManifestDiff *_manifestDiff;
    NSFileManager *_fileManager;
}

@property (nonatomic, strong, readwrite) NSString *workerId;

@end

@implementation HCPInstallationWorker

#pragma mark Public API

- (instancetype)initWithNewVersion:(NSString *)newVersion currentVersion:(NSString *)currentVersion {
    self = [super init];
    if (self) {
        _newReleaseFS = [[HCPFilesStructure alloc] initWithReleaseVersion:newVersion];
        _currentReleaseFS = [[HCPFilesStructure alloc] initWithReleaseVersion:currentVersion];
    }
    
    return self;
}

-(void)installWithComplitionBlock:(InstallUpdateBlock)block
{
    NSError *error = nil;
    if (![self initBeforeRun:&error] ||
        ![self isUpdateValid:&error] ||
        ![self copyFilesFromCurrentReleaseToNewRelease:&error] ||
        ![self deleteUnusedFiles:&error] ||
        ![self moveDownloadedFilesToWwwFolder:&error]) {
        NSLog(@"%@. Error code %ld", [error underlyingErrorLocalizedDesription], (long)error.code);
        [self cleanUpOnFailure];
//        [self dispatchEventWithError:error];
        block(NO, error);
    }
    else
    {
        [self cleanUpOnSucess];
        [self saveNewConfigsToWwwFolder];
        //    [self dispatchSuccessEvent];
        block(YES, nil);
    }
}


//- (void)runWithComplitionBlock:(void (^)(void))updateInstallationComplitionBlock {
//    [self dispatchBeforeInstallEvent];
//
//    NSError *error = nil;
//    if (![self initBeforeRun:&error] ||
//        ![self isUpdateValid:&error] ||
//        ![self copyFilesFromCurrentReleaseToNewRelease:&error] ||
//        ![self deleteUnusedFiles:&error] ||
//        ![self moveDownloadedFilesToWwwFolder:&error]) {
//            NSLog(@"%@. Error code %ld", [error underlyingErrorLocalizedDesription], (long)error.code);
//            [self cleanUpOnFailure];
//            [self dispatchEventWithError:error];
//        
//            return;
//    }
//    
//    [self cleanUpOnSucess];
//    [self saveNewConfigsToWwwFolder];
//    [self dispatchSuccessEvent];
//}

#pragma mark Private API

///**
// *  发送事件：即将安装
// */
//- (void)dispatchBeforeInstallEvent {
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPBeforeInstallEvent
//                                                 applicationConfig:_newConfig
//                                                            taskId:self.workerId];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}
//
///**
// *  发送事件：安装失败
// *
// *  @param error 错误
// */
//- (void)dispatchEventWithError:(NSError *)error {
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateInstallationErrorEvent
//                                                 applicationConfig:_newConfig
//                                                            taskId:self.workerId
//                                                             error:error];
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}
//
///**
// *  发送事件：安装成功
// */
//- (void)dispatchSuccessEvent {
//    NSNotification *notification = [HCPEvents notificationWithName:kHCPUpdateIsInstalledEvent
//                                                 applicationConfig:_newConfig
//                                                            taskId:self.workerId];
//    
//    
//    [[NSNotificationCenter defaultCenter] postNotification:notification];
//}

/**
 *  初始化
 *
 *  @param error 错误
 *
 *  @return <code>YES</code> 成功; <code>NO</code> 失败
 */
- (BOOL)initBeforeRun:(NSError **)error {
    *error = nil;
    
    _fileManager = [NSFileManager defaultManager];
    _manifestStorage = [[HCPContentManifestStorage alloc] initWithFileStructure:_newReleaseFS];
    _configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_newReleaseFS];
    
    // 读取当前版本的config
    _oldConfig = [_configStorage loadFromFolder:_currentReleaseFS.wwwFolder];
    if (_oldConfig == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from cache folder"];
        return NO;
    }
    
    // 读取新版本的config
    _newConfig = [_configStorage loadFromFolder:_newReleaseFS.downloadFolder];
    if (_newConfig == nil) {
        *error = [NSError errorWithCode:kHCPLoadedVersionOfApplicationConfigNotFoundErrorCode
                            description:@"Failed to load application config from download folder"];
        return NO;
    }
    
    // 读取当前版本的manifest
    _oldManifest = [_manifestStorage loadFromFolder:_currentReleaseFS.wwwFolder];
    if (_oldManifest == nil) {
        *error = [NSError errorWithCode:kHCPLocalVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load content manifest from cache folder"];
        return NO;
    }
    
    // 读取新版本的manifest
    _newManifest = [_manifestStorage loadFromFolder:_newReleaseFS.downloadFolder];
    if (_newManifest == nil) {
        *error = [NSError errorWithCode:kHCPLoadedVersionOfManifestNotFoundErrorCode
                            description:@"Failed to load content manifest from download folder"];
        return NO;
    }
    
    // 计算manifest的差别
    _manifestDiff = [_oldManifest calculateDifference:_newManifest];
    
    return YES;
}

/**
 *  验证更新，检查文件是否下载完全和HASH正确
 *
 *  @param error 错误
 *
 *  @return <code>YES</code> 成功; <code>NO</code> 失败
 */
- (BOOL)isUpdateValid:(NSError **)error {
    *error = nil;
    NSString *errorMsg = nil;
    
    NSArray *updateFileList = _manifestDiff.updateFileList;
    for (HCPManifestFile *updatedFile in updateFileList) {
        NSURL *fileLocalURL = [_newReleaseFS.downloadFolder URLByAppendingPathComponent:updatedFile.name isDirectory:NO];
        if (![_fileManager fileExistsAtPath:fileLocalURL.path]) {
            errorMsg = [NSString stringWithFormat:@"Update validation error! File not found: %@", updatedFile.name];
            break;
        }
        
        NSString *fileMD5 = [[NSData dataWithContentsOfURL:fileLocalURL] md5];
        if (![fileMD5 isEqualToString:updatedFile.md5Hash]) {
            errorMsg = [NSString stringWithFormat:@"Update validation error! File's %@ hash %@ doesnt match the hash %@ from manifest file", updatedFile.name, fileMD5, updatedFile.md5Hash];
            break;
        }
    }
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPUpdateIsInvalidErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

- (BOOL)copyFilesFromCurrentReleaseToNewRelease:(NSError **)error {
    *error = nil;
    
    // 如果有同名目录存在，先删除
    if ([_fileManager fileExistsAtPath:_newReleaseFS.wwwFolder.path]) {
        [_fileManager removeItemAtURL:_newReleaseFS.wwwFolder error:nil];
    }
    
    // 把当前目录拷贝到新目录
    if (![_fileManager copyItemAtURL:_currentReleaseFS.wwwFolder toURL:_newReleaseFS.wwwFolder error:error]) {
        NSLog(@"Installation error! Failed to copy files from %@ to %@", _currentReleaseFS.wwwFolder.path, _newReleaseFS.wwwFolder.path);
        *error = [NSError errorWithCode:kHCPFailedToCopyFilesFromPreviousReleaseErrorCode descriptionFromError:*error];
        return NO;
    }
    
    return YES;
}

/**
 *  删除无用文件
 *
 *  @param error 错误
 *
 *  @return <code>YES</code> 删除成功; <code>NO</code> 失败;
 */
- (BOOL)deleteUnusedFiles:(NSError **)error {
    *error = nil;
    NSArray *deletedFiles = _manifestDiff.deletedFiles;
    for (HCPManifestFile *deletedFile in deletedFiles) {
        NSURL *filePath = [_newReleaseFS.wwwFolder URLByAppendingPathComponent:deletedFile.name];
        if (![_fileManager removeItemAtURL:filePath error:error]) {
            NSLog(@"HCP Warinig! Failed to delete file: %@", filePath.absoluteString);
        }
    }
    
    return YES;
}

/**
 *  拷贝下载文件到www目录里
 *
 *  @param error 错误
 *
 *  @return <code>YES</code> 成功; <code>NO</code> 失败
 */
- (BOOL)moveDownloadedFilesToWwwFolder:(NSError **)error {
    *error = nil;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSArray *updatedFiles = _manifestDiff.updateFileList;
    NSString *errorMsg = nil;
    for (HCPManifestFile *manifestFile in updatedFiles) {
        // 获取更新目录和www目录
        NSURL *pathInInstallationFolder = [_newReleaseFS.downloadFolder URLByAppendingPathComponent:manifestFile.name];
        NSURL *pathInWwwFolder = [_newReleaseFS.wwwFolder URLByAppendingPathComponent:manifestFile.name];
        
        // 如果文件存在，先删掉老的
        if ([fileManager fileExistsAtPath:pathInWwwFolder.path] && ![fileManager removeItemAtURL:pathInWwwFolder error:error]) {
            errorMsg = [NSString stringWithFormat:@"Failed to delete old version of the file %@ : %@. Installation failed",
                            manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
            break;
        }
        
        // 如果需要，创建子目录
        NSURL *parentDirectoryPathInWwwFolder = [pathInWwwFolder URLByDeletingLastPathComponent];
        if (![fileManager fileExistsAtPath:parentDirectoryPathInWwwFolder.path]) {
            if (![fileManager createDirectoryAtPath:parentDirectoryPathInWwwFolder.path withIntermediateDirectories:YES attributes:nil error:error]) {
                errorMsg = [NSString stringWithFormat:@"Failed to create folder structure for file %@ : %@. Installation failed.",
                                manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
                break;
            }
        }
        
        // 拷贝新的文件到目录里
        if (![fileManager moveItemAtURL:pathInInstallationFolder toURL:pathInWwwFolder error:error]) {
            errorMsg = [NSString stringWithFormat:@"Failed to copy file %@ into www folder: %@. Installation failed.",
                            manifestFile.name, [(*error) underlyingErrorLocalizedDesription]];
            break;
        }
    }
    
    if (errorMsg) {
        *error = [NSError errorWithCode:kHCPFailedToCopyNewContentFilesErrorCode description:errorMsg];
    }
    
    return (*error == nil);
}

/**
 *  保存config文件
 */
- (void)saveNewConfigsToWwwFolder {
    [_manifestStorage store:_newManifest inFolder:_newReleaseFS.wwwFolder];
    [_configStorage store:_newConfig inFolder:_newReleaseFS.wwwFolder];
}

/**
 *  如果失败，删除目录
 */
- (void)cleanUpOnFailure {
    [_fileManager removeItemAtURL:_newReleaseFS.contentFolder error:nil];
}

/**
 *  如果成功，删除目录
 */
- (void)cleanUpOnSucess {
    [_fileManager removeItemAtURL:_newReleaseFS.downloadFolder error:nil];
}

@end
