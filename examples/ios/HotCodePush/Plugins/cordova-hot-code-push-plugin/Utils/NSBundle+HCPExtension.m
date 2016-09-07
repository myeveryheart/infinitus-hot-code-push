//
//  NSBundle+Extension.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "NSBundle+HCPExtension.h"
#import "NSError+HCPExtension.h"

static NSString *const WWW_FOLDER_IN_BUNDLE = @"www";

@implementation NSBundle (HCPExtension)

#pragma mark Public API

+ (NSInteger)applicationBuildVersion {
    NSBundle *mainBundle = [NSBundle mainBundle];
    id appBuildVersion = [mainBundle objectForInfoDictionaryKey:(NSString *)kCFBundleVersionKey];
    if (appBuildVersion == nil) {
        return 0;
    }
    
    return [appBuildVersion integerValue];
}

+ (NSString *)pathToWwwFolder {
    return [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:WWW_FOLDER_IN_BUNDLE];
}

@end
