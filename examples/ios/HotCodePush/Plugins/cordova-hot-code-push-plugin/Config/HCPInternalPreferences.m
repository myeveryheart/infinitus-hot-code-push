//
//  HCPInternalPreferences.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPInternalPreferences.h"
#import "NSBundle+HCPExtension.h"
#import "HCPApplicationConfig.h"
#import "HCPFilesStructure.h"

#pragma mark JSON keys for hcp options

static NSString *const APPLICATION_BUILD_VERSION = @"app_build_version";
static NSString *const WWW_FOLDER_INSTALLED_FLAG = @"www_folder_installed";
static NSString *const PREVIOUS_RELEASE_VERSION_NAME = @"previous_release_version_name";
static NSString *const CURRENT_RELEASE_VERSION_NAME = @"current_release_version_name";
static NSString *const READY_FOR_INSTALLATION_RELEASE_VERSION_NAME = @"ready_for_installation_release_version_name";

@implementation HCPInternalPreferences

#pragma mark Public API

+ (HCPInternalPreferences *)defaultConfig {
    HCPInternalPreferences *hcpConfig = [[HCPInternalPreferences alloc] init];
    hcpConfig.appBuildVersion = [NSBundle applicationBuildVersion];
    hcpConfig.wwwFolderInstalled = NO;
    hcpConfig.previousReleaseVersionName = @"";
    hcpConfig.readyForInstallationReleaseVersionName = @"";
    
    HCPApplicationConfig *config = [HCPApplicationConfig configFromBundle:[HCPFilesStructure defaultConfigFileName]];
    hcpConfig.currentReleaseVersionName = config.contentConfig.releaseVersion;
    
    return hcpConfig;
}

#pragma mark HCPJsonConvertable implementation

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPInternalPreferences *hcpConfig = [[HCPInternalPreferences alloc] init];
    hcpConfig.appBuildVersion = [(NSNumber *)jsonObject[APPLICATION_BUILD_VERSION] integerValue];
    hcpConfig.wwwFolderInstalled = [(NSNumber *)jsonObject[WWW_FOLDER_INSTALLED_FLAG] boolValue];
    hcpConfig.currentReleaseVersionName = (NSString *)jsonObject[CURRENT_RELEASE_VERSION_NAME];
    hcpConfig.previousReleaseVersionName = (NSString *)jsonObject[PREVIOUS_RELEASE_VERSION_NAME];
    hcpConfig.readyForInstallationReleaseVersionName = (NSString *)jsonObject[READY_FOR_INSTALLATION_RELEASE_VERSION_NAME];
    
    return hcpConfig;
}

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    jsonObject[APPLICATION_BUILD_VERSION] = [NSNumber numberWithInteger:self.appBuildVersion];
    jsonObject[WWW_FOLDER_INSTALLED_FLAG] = [NSNumber numberWithBool:self.isWwwFolderInstalled];
    jsonObject[PREVIOUS_RELEASE_VERSION_NAME] = self.previousReleaseVersionName;
    jsonObject[CURRENT_RELEASE_VERSION_NAME] = self.currentReleaseVersionName;
    jsonObject[READY_FOR_INSTALLATION_RELEASE_VERSION_NAME] = self.readyForInstallationReleaseVersionName;
    
    return jsonObject;
}

@end
