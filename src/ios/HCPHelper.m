//
//  HCPHelper.m
//  HotCodePush
//
//  Created by M on 16/8/26.
//
//

#import "HCPHelper.h"

#import "HCPFileDownloader.h"
#import "HCPFilesStructure.h"
#import "HCPUpdateLoader.h"
#import "HCPInternalPreferences+UserDefaults.h"
#import "NSJSONSerialization+HCPExtension.h"
#import "HCPConfig.h"
#import "NSBundle+HCPExtension.h"
#import "HCPApplicationConfigStorage.h"
#import "HCPAssetsFolderHelper.h"
#import "NSError+HCPExtension.h"
#import "HCPCleanupHelper.h"



@interface HCPHelper()

@property(strong,nonatomic) NSURL *webUrl;
@property(strong,nonatomic) HCPFilesStructure *filesStructure;
@property(strong,nonatomic) HCPApplicationConfig *appConfig;
@property(strong,nonatomic) HCPInternalPreferences *hcpInternalPrefs;
@property(strong,nonatomic) HCPConfig *hcpConfig;


@end

@implementation HCPHelper

-(NSURL *)pathToWww
{
    if ([self isWWwFolderNeedsToBeInstalled])
    {
        return [NSURL URLWithString:[NSBundle pathToWwwFolder]];
    }
    else
    {
        return _filesStructure.wwwFolder;
    }
}

-(HCPHelper *)initWithWebUrl:(NSURL *)webUrl
{
    self = [super init];
    if (self)
    {
        self.webUrl = webUrl;
        [self doLocalInit];
        
        // 清理空间
        if (_hcpInternalPrefs.currentReleaseVersionName.length > 0) {
            [HCPCleanupHelper removeUnusedReleasesExcept:@[_hcpInternalPrefs.currentReleaseVersionName,
                                                           _hcpInternalPrefs.previousReleaseVersionName,
                                                           _hcpInternalPrefs.readyForInstallationReleaseVersionName]];
        }
        
        [self loadApplicationConfig];
    }
    
    return self;
}

-(BOOL)loadFromExternalStorageFolder
{
    if ([self isWWwFolderNeedsToBeInstalled])
    {
        [self installWwwFolder];
        return NO;
    }
    return YES;
}

-(void)fetchUpdate:(FetchUpdateBlock)block
{
    [[HCPUpdateLoader sharedInstance] fetchUpdateWithConfigUrl:_hcpConfig.configUrl
                                                currentWebVersion:_hcpInternalPrefs.currentReleaseVersionName
                                             currentNativeVersion:_hcpConfig.nativeInterfaceVersion
                                                            fetchUpdateBlock:^(BOOL needUpdate, NSError *error) {
                                                                block(needUpdate, error);
                                                                if (error)
                                                                {
                                                                    [self onUpdateDownloadErrorEvent:error];
                                                                }
                                                            }];
}

-(void)downloadUpdate:(DownloadUpdateBlock)block
{
    [[HCPUpdateLoader sharedInstance] downloadUpdateWithDownloadUpdateBlock:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error, HCPApplicationConfig *newAppConfig) {
        if (success && !error)
        {
            [self onUpdateIsReadyForInstallation:newAppConfig];
        }
        block(success, totalFiles, fileDownloaded, error);
    }];
}

/**
 *  Install update.
 *
 *  @param callbackID callbackId id of the caller on JavaScript side; it will be used to send back the result of the installation process
 *
 *  @return <code>YES</code> if installation has started; <code>NO</code> otherwise
 */
-(void)installUpdate:(InstallUpdateBlock)block
{
    NSString *newVersion = _hcpInternalPrefs.readyForInstallationReleaseVersionName;
    NSString *currentVersion = _hcpInternalPrefs.currentReleaseVersionName;
    
    [[HCPUpdateLoader sharedInstance] installVersion:newVersion currentVersion:currentVersion completionBlock:^(BOOL success, NSError *error, HCPApplicationConfig *newAppConfig) {
        if (error)
        {
            //            if (error.code == kHCPNothingToInstallErrorCode)
            //            {
            //                NSNotification *notification = [HCPEvents notificationWithName:kHCPNothingToInstallEvent
            //                                                             applicationConfig:nil
            //                                                                        taskId:nil
            //                                                                         error:error];
            //                [self onNothingToInstallEvent:notification];
            //            }
        }
        block(success, error);
        [self onUpdateInstalledEvent:newAppConfig];
    }];
}


#pragma mark Lifecycle

//- (void)onAppTerminate {
//    [self unsubscribeFromEvents];
//}

- (void)onResume:(NSNotification *)notification {
//    if (!_pluginXmlConfig.isUpdatesAutoInstallationAllowed ||
//        _hcpInternalPrefs.readyForInstallationReleaseVersionName.length == 0) {
//        return;
//    }
//    
//    // load app config from update folder and check, if we are allowed to install it
//    HCPFilesStructure *fs = [[HCPFilesStructure alloc] initWithReleaseVersion:_hcpInternalPrefs.readyForInstallationReleaseVersionName];
//    id<HCPConfigFileStorage> configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:fs];
//    HCPApplicationConfig *configFromNewRelease = [configStorage loadFromFolder:fs.downloadFolder];
//    
//    if (configFromNewRelease.contentConfig.updateTime == HCPUpdateOnResume ||
//        configFromNewRelease.contentConfig.updateTime == HCPUpdateNow) {
//        [self _installUpdate:nil];
//    }
}

#pragma mark Private API

- (void)installWwwFolder
{
    if (_hcpInternalPrefs.isWwwFolderInstalled)
    {
        _hcpInternalPrefs.wwwFolderInstalled = NO;
        [_hcpInternalPrefs saveToUserDefaults];
    }
    
    if (_filesStructure.wwwFolder)
    {
        [HCPAssetsFolderHelper installWwwFolderToExternalStorageFolder:_filesStructure.wwwFolder completionBlock:^(BOOL success, NSError *error)
        {
            [self onAssetsInstalledOnExternalStorageEvent];
        }];
    }    
}

/**
 *  Load application config from file system
 */
- (void)loadApplicationConfig {
    id<HCPConfigFileStorage> configStorage = [[HCPApplicationConfigStorage alloc] initWithFileStructure:_filesStructure];
    _appConfig = [configStorage loadFromFolder:_filesStructure.wwwFolder];
}

/**
 *  Check if www folder already exists on the external storage.
 *
 *  @return <code>YES</code> - www folder doesn't exist, we need to install it; <code>NO</code> - folder already installed
 */
- (BOOL)isWWwFolderNeedsToBeInstalled {
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isApplicationUpdated = [NSBundle applicationBuildVersion] != _hcpInternalPrefs.appBuildVersion;
    BOOL isWWwFolderExists = [fileManager fileExistsAtPath:_filesStructure.wwwFolder.path];
    BOOL isWWwFolderInstalled = _hcpInternalPrefs.isWwwFolderInstalled;
    
    return isApplicationUpdated || !isWWwFolderExists || !isWWwFolderInstalled;
}

/**
 *  Perform initialization of the plugin variables.
 */
- (void)doLocalInit
{
    // 初始化config
    _hcpConfig = [[HCPConfig alloc] init];
    _hcpConfig.webUrl = self.webUrl;
    
    // 加载internal preferences
    _hcpInternalPrefs = [HCPInternalPreferences loadFromUserDefaults];
    if (_hcpInternalPrefs == nil || _hcpInternalPrefs.currentReleaseVersionName.length == 0)
    {
        _hcpInternalPrefs = [HCPInternalPreferences defaultConfig];
        if (_hcpInternalPrefs.currentReleaseVersionName.length >0)
        {
            [_hcpInternalPrefs saveToUserDefaults];
        }
        else
        {
            NSLog(@"无法读取配置文件");
            return;
        }
    }
    NSLog(@"Currently running release version %@", _hcpInternalPrefs.currentReleaseVersionName);
    // init file structure for www files
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_hcpInternalPrefs.currentReleaseVersionName];
}


/**
 *  If needed - add path to www folder on the external storage to the provided path.
 *
 *  @param pagePath path to which we want add www folder
 *
 *  @return resulting path
 */
- (NSURL *)appendWwwFolderPathToPath:(NSString *)pagePath {
    if ([pagePath hasPrefix:_filesStructure.wwwFolder.absoluteString]) {
        return [NSURL URLWithString:pagePath];
    }
    
    return [_filesStructure.wwwFolder URLByAppendingPathComponent:pagePath];
}

#pragma mark Events

///**
// *  监听事件
// */
//- (void)subscribeToEvents
//{
//    [self subscribeToLifecycleEvents];
//    [self subscribeToInternalEvents];
//}
//
///**
// *  监听生命周期事件
// */
//- (void)subscribeToLifecycleEvents
//{
//    [[NSNotificationCenter defaultCenter] addObserver:self
//                                             selector:@selector(onResume:)
//                                                 name:UIApplicationWillEnterForegroundNotification
//                                               object:nil];
//    
//}

/**
 *  监听内部事件
 */
//- (void)subscribeToInternalEvents
//{
//    NSNotificationCenter *notificationCenter = [NSNotificationCenter defaultCenter];
//    
//    // 拷贝bundle事件
//    [notificationCenter addObserver:self
//                           selector:@selector(onBeforeAssetsInstalledOnExternalStorageEvent:)
//                               name:kHCPBeforeBundleAssetsInstalledOnExternalStorageEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onAssetsInstalledOnExternalStorageEvent:)
//                               name:kHCPBundleAssetsInstalledOnExternalStorageEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onAssetsInstallationErrorEvent:)
//                               name:kHCPBundleAssetsInstallationErrorEvent
//                             object:nil];
//    
//    // 下载事件
//    [notificationCenter addObserver:self
//                           selector:@selector(onUpdateDownloadErrorEvent:)
//                               name:kHCPUpdateDownloadErrorEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onNothingToUpdateEvent:)
//                               name:kHCPNothingToUpdateEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onUpdateIsReadyForInstallation:)
//                               name:kHCPUpdateIsReadyForInstallationEvent
//                             object:nil];
//    
//    // 安装更新事件
//    [notificationCenter addObserver:self
//                           selector:@selector(onUpdateInstallationErrorEvent:)
//                               name:kHCPUpdateInstallationErrorEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onBeforeInstallEvent:)
//                               name:kHCPBeforeInstallEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onUpdateInstalledEvent:)
//                               name:kHCPUpdateIsInstalledEvent
//                             object:nil];
//    [notificationCenter addObserver:self
//                           selector:@selector(onNothingToInstallEvent:)
//                               name:kHCPNothingToInstallEvent
//                             object:nil];
//}

/**
 *  移除监听
 */
//- (void)unsubscribeFromEvents {
//    [[NSNotificationCenter defaultCenter] removeObserver:self];
//}

#pragma mark Bundle installation events

- (void)onBeforeAssetsInstalledOnExternalStorageEvent:(NSNotification *)notification
{
    
}

- (void)onAssetsInstalledOnExternalStorageEvent
{
    _hcpInternalPrefs.appBuildVersion = [NSBundle applicationBuildVersion];
    _hcpInternalPrefs.wwwFolderInstalled = YES;
    [_hcpInternalPrefs saveToUserDefaults];
    
//    // 检查更新
//    [self loadApplicationConfig];
//    
//    if (![HCPUpdateLoader sharedInstance].isDownloadInProgress &&
//        ![HCPUpdateInstaller sharedInstance].isInstallationInProgress)
//    {
//        [self fetchUpdate:^(BOOL needUpdate, NSError *error) {
//            if (error == nil)
//            {
//                [self downloadUpdate:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error) {
//                    //安装
//                }];
//            }
//        }];
//    }
}

- (void)onAssetsInstallationErrorEvent:(NSNotification *)notification
{
    
}

#pragma mark Update download events

- (void)onUpdateDownloadErrorEvent:(NSError *)error
{
    NSLog(@"Error during update: %@", [error underlyingErrorLocalizedDesription]);
    
    // probably never happens, but just for safety
    [self rollbackIfCorrupted:error];
}

- (void)onNothingToUpdateEvent:(NSNotification *)notification
{
    NSLog(@"Nothing to update");
}

- (void)onUpdateIsReadyForInstallation:(HCPApplicationConfig *)newAppConfig
{
    // new application config from server
//    HCPApplicationConfig *newConfig = notification.userInfo[kHCPEventUserInfoApplicationConfigKey];
    
    NSLog(@"Update is ready for installation: %@", newAppConfig.contentConfig.releaseVersion);
    
    // store, that we are ready for installation
    _hcpInternalPrefs.readyForInstallationReleaseVersionName = newAppConfig.contentConfig.releaseVersion;
    [_hcpInternalPrefs saveToUserDefaults];
    
//    // if it is allowed - launch the installation
//    if (![HCPUpdateLoader sharedInstance].isDownloadInProgress && ![HCPUpdateInstaller sharedInstance].isInstallationInProgress)
//    {
//        [self installUpdate:^(BOOL success, NSError *error) {
//            
//        }];
//    }
}

#pragma mark Update installation events

- (void)onNothingToInstallEvent:(NSNotification *)notification
{
    
}

- (void)onBeforeInstallEvent:(NSNotification *)notification
{

}

- (void)onUpdateInstallationErrorEvent:(NSNotification *)notification
{
    _hcpInternalPrefs.readyForInstallationReleaseVersionName = @"";
    [_hcpInternalPrefs saveToUserDefaults];
    
    // probably never happens, but just for safety
//    NSError *error = notification.userInfo[kHCPEventUserInfoErrorKey];
//    [self rollbackIfCorrupted:error];
}

- (void)onUpdateInstalledEvent:(HCPApplicationConfig *)newAppConfig
{
    _appConfig = newAppConfig;
    
    _hcpInternalPrefs.readyForInstallationReleaseVersionName = @"";
    _hcpInternalPrefs.previousReleaseVersionName = _hcpInternalPrefs.currentReleaseVersionName;
    _hcpInternalPrefs.currentReleaseVersionName = _appConfig.contentConfig.releaseVersion;
    [_hcpInternalPrefs saveToUserDefaults];
    
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_hcpInternalPrefs.currentReleaseVersionName];
}

#pragma mark Rollback process

- (void)rollbackToPreviousRelease {
    _hcpInternalPrefs.readyForInstallationReleaseVersionName = @"";
    _hcpInternalPrefs.currentReleaseVersionName = _hcpInternalPrefs.previousReleaseVersionName;
    _hcpInternalPrefs.previousReleaseVersionName = @"";
    [_hcpInternalPrefs saveToUserDefaults];
    
    _filesStructure = [[HCPFilesStructure alloc] initWithReleaseVersion:_hcpInternalPrefs.currentReleaseVersionName];
    
    if (_appConfig) {
        [self loadApplicationConfig];
    }
}

- (void)rollbackIfCorrupted:(NSError *)error {
    if (error.code != kHCPLocalVersionOfApplicationConfigNotFoundErrorCode && error.code != kHCPLocalVersionOfManifestNotFoundErrorCode) {
        return;
    }
    
    if (_hcpInternalPrefs.previousReleaseVersionName.length > 0) {
        NSLog(@"WWW folder is corrupted, rolling back to previous version.");
        [self rollbackToPreviousRelease];
    } else {
        NSLog(@"WWW folder is corrupted, reinstalling it from bundle.");
        [self installWwwFolder];
    }
}

@end
