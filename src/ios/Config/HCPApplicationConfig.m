//
//  HCPApplicationConfig.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPApplicationConfig.h"
#import "NSBundle+HCPExtension.h"
#import "NSError+HCPExtension.h"

@interface HCPApplicationConfig()

@property (nonatomic, strong, readwrite) HCPContentConfig *contentConfig;

@end

@implementation HCPApplicationConfig

#pragma mark Public API

+ (instancetype)configFromBundle:(NSString *)configFileName {
    NSURL *wwwFolderURL = [NSURL fileURLWithPath:[NSBundle pathToWwwFolder] isDirectory:YES];
    NSURL *hcpJsonFileURLFromBundle = [wwwFolderURL URLByAppendingPathComponent:configFileName];
    
    NSData *jsonData = [NSData dataWithContentsOfURL:hcpJsonFileURLFromBundle];
    if (jsonData == nil) {
        return nil;
    }
    
    NSError *error = nil;
    id json = [NSJSONSerialization JSONObjectWithData:jsonData options:kNilOptions error:&error];
    if (error) {
        NSLog(@"Can't read application config from bundle. %@", [error underlyingErrorLocalizedDesription]);
        return nil;
    }
    
    return [HCPApplicationConfig instanceFromJsonObject:json];
}

#pragma mark HCPJsonConvertable implementation

- (id)toJson {
    NSMutableDictionary *jsonObject;
    NSDictionary *contentConfigJsonObject = [self.contentConfig toJson];
    
    if (contentConfigJsonObject) {
        jsonObject = [[NSMutableDictionary alloc] initWithDictionary:contentConfigJsonObject];
    } else {
        jsonObject = [[NSMutableDictionary alloc] init];
    }
    
    return jsonObject;
}

+ (instancetype)instanceFromJsonObject:(id)json {
    if (!json || ![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPApplicationConfig *appConfig = [[HCPApplicationConfig alloc] init];
    appConfig.contentConfig = [HCPContentConfig instanceFromJsonObject:jsonObject];
    
    return appConfig;
}

@end
