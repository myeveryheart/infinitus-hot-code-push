//
//  HCPCleanupHelper.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPCleanupHelper.h"
#import "HCPFilesStructure.h"

@interface HCPCleanupHelper() {
    __block BOOL _isExecuting;
}

@end

@implementation HCPCleanupHelper

#pragma mark Public API

+ (void)removeUnusedReleasesExcept:(NSArray *)ignoredReleases {
    HCPCleanupHelper *helper = [HCPCleanupHelper sharedInstance];
    [helper removeReleaseFoldersExcluding:ignoredReleases];
}

#pragma Private API

/**
 *  单例
 *
 *  @return class instance
 */
+ (HCPCleanupHelper *)sharedInstance {
    static HCPCleanupHelper *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    
    return sharedInstance;
}

- (void)removeReleaseFoldersExcluding:(NSArray *)ignoredReleases {
    if (_isExecuting) {
        return;
    }
    
    _isExecuting = YES;
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        [self removeFoldersWithIgnoreList:ignoredReleases];
        _isExecuting = NO;
    });
}

- (void)removeFoldersWithIgnoreList:(NSArray *)ignoreList {
    NSURL *rootFolder = [HCPFilesStructure hcpRootFolder];
    NSFileManager *fm = [NSFileManager defaultManager];
    
    NSArray *releases = [fm contentsOfDirectoryAtURL:rootFolder includingPropertiesForKeys:@[] options:kNilOptions error:nil];
    if (releases.count == 0) {
        return;
    }
    
    for (NSURL *releaseFolderURL in releases) {
        BOOL shouldIgnore = NO;
        NSString *releaseFolder = [releaseFolderURL lastPathComponent];
        for (NSString *ignorePath in ignoreList) {
            if (ignorePath.length == 0) {
                continue;
            }
            
            if ([releaseFolder isEqualToString:ignorePath]) {
                shouldIgnore = YES;
                break;
            }
        }
        
        if (!shouldIgnore) {
            NSLog(@"Removing old release content: %@", releaseFolderURL.path.lastPathComponent);
            [fm removeItemAtURL:releaseFolderURL error:nil];
        }
    }
}

@end
