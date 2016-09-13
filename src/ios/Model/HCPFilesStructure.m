//
//  HCPFilesStructureImpl.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPFilesStructure.h"
#import "NSFileManager+HCPExtension.h"

#pragma mark Predefined folders and file names

static NSString *const HCP_FOLDER = @"hot-code-push";
static NSString *const DOWNLOAD_FOLDER = @"update";
static NSString *const WWWW_FOLDER = @"www";
static NSString *const HCP_JSON_FILE_PATH = @"hcp.json";
static NSString *const HCP_MANIFEST_FILE_PATH = @"hcp.manifest";

@interface HCPFilesStructure()

@property (nonatomic, strong, readwrite) NSURL *contentFolder;
@property (nonatomic, strong, readwrite) NSURL *downloadFolder;
@property (nonatomic, strong, readwrite) NSURL *wwwFolder;

@end

@implementation HCPFilesStructure

#pragma mark Public API

- (instancetype)initWithReleaseVersion:(NSString *)releaseVersion {
    self = [super init];
    if (self) {
        [self localInitWithReleaseVersion:releaseVersion];
    }
    
    return self;
}

+ (NSURL *)hcpRootFolder {
    static NSURL *_hcpRootFolder = nil;
    if (_hcpRootFolder != nil) {
        return _hcpRootFolder;
    }

    // 创建根目录
    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSURL *supportDir = [fileManager applicationSupportDirectory];
    _hcpRootFolder = [supportDir URLByAppendingPathComponent:HCP_FOLDER isDirectory:YES];
    if (![fileManager fileExistsAtPath:_hcpRootFolder.path]) {
        [fileManager createDirectoryAtURL:_hcpRootFolder withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    // 把这个目录设置为不用iCloud备份
    // https://developer.apple.com/library/ios/qa/qa1719/_index.html
    NSError *error = nil;
    BOOL success = [_hcpRootFolder setResourceValue:[NSNumber numberWithBool:YES] forKey:NSURLIsExcludedFromBackupKey error:&error];
    if (!success) {
        NSLog(@"Error excluding %@ from backup %@", [_hcpRootFolder lastPathComponent], error);
    }
    
    return _hcpRootFolder;
}

- (void)localInitWithReleaseVersion:(NSString *)releaseVersion {
    _contentFolder = [[HCPFilesStructure hcpRootFolder]
                      URLByAppendingPathComponent:releaseVersion isDirectory:YES];
}

- (NSURL *)downloadFolder {
    if (_downloadFolder == nil) {
        _downloadFolder = [self.contentFolder URLByAppendingPathComponent:DOWNLOAD_FOLDER isDirectory:YES];
    }
    
    return _downloadFolder;
}

- (NSURL *)wwwFolder {
    if (_wwwFolder == nil) {
        _wwwFolder = [self.contentFolder URLByAppendingPathComponent:WWWW_FOLDER isDirectory:YES];
    }
    
    return _wwwFolder;
}

- (NSString *)configFileName {
    return HCP_JSON_FILE_PATH;
}

- (NSString *)manifestFileName {
    return HCP_MANIFEST_FILE_PATH;
}

+ (NSString *)defaultConfigFileName {
    return HCP_JSON_FILE_PATH;
}

+ (NSString *)defaultManifestFileName {
    return HCP_MANIFEST_FILE_PATH;
}

@end