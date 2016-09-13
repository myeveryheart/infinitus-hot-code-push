//
//  HCPContentConfig.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPContentConfig.h"

@interface HCPContentConfig()

@property (nonatomic, strong, readwrite) NSString *releaseVersion;
@property (nonatomic, readwrite) NSInteger minimumNativeVersion;
@property (nonatomic, strong, readwrite) NSURL *contentURL;
@property (nonatomic, readwrite) HCPUpdateTime updateTime;

@end

#pragma mark Json keys declaration

static NSString *const RELEASE_VERSION_JSON_KEY = @"release";
static NSString *const MINIMUM_NATIVE_VERSION_JSON_KEY = @"min_native_interface";
static NSString *const UPDATE_TIME_JSON_KEY = @"update";
static NSString *const CONTENT_URL_JSON_KEY = @"content_url";

#pragma mark HCPUpdateTime enum strings declaration

static NSString *const UPDATE_TIME_FORCED = @"forced";
static NSString *const UPDATE_TIME_SILENT = @"silent";

@implementation HCPContentConfig

#pragma mark Private API

/**
 *  enum转string.
 *
 *  @param updateTime
 *
 *  @return string
 */
- (NSString *)updateTimeEnumToString:(HCPUpdateTime)updateTime {
    NSString *value = @"";
    switch (updateTime) {
        case HCPUpdateTimeForced: {
            value = UPDATE_TIME_FORCED;
            break;
        }
        case HCPUpdateTimeSilent: {
            value = UPDATE_TIME_SILENT;
            break;
        }
        case HCPUpdateTimeUndefined:
        default: {
            break;
        }
    }
    
    return value;
}

/**
 *  string转enum
 *
 *  @param updateTime string
 *
 *  @return enum
 */
- (HCPUpdateTime)updateTimeStringToEnum:(NSString *)updateTime {
    HCPUpdateTime value = HCPUpdateTimeUndefined;
    if ([updateTime isEqualToString:UPDATE_TIME_FORCED]) {
        value = HCPUpdateTimeForced;
    } else if ([updateTime isEqualToString:UPDATE_TIME_SILENT]) {
        value = HCPUpdateTimeSilent;
    }     
    return value;
}

#pragma mark HCPJsonConvertable implementation

- (id)toJson {
    NSMutableDictionary *jsonObject = [[NSMutableDictionary alloc] init];
    if (_releaseVersion) {
        jsonObject[RELEASE_VERSION_JSON_KEY] = _releaseVersion;
    }
    
    if (_minimumNativeVersion > 0) {
        jsonObject[MINIMUM_NATIVE_VERSION_JSON_KEY] = [NSNumber numberWithInteger:_minimumNativeVersion];
    }
    
    NSString *updateTimeStr = [self updateTimeEnumToString:_updateTime];
    if (updateTimeStr) {
        jsonObject[UPDATE_TIME_JSON_KEY] = updateTimeStr;
    }
    
    if (_contentURL) {
        jsonObject[CONTENT_URL_JSON_KEY] = _contentURL.absoluteString;
    }
    
    return jsonObject;
}

+ (instancetype)instanceFromJsonObject:(id)json {
    if (![json isKindOfClass:[NSDictionary class]]) {
        return nil;
    }
    NSDictionary *jsonObject = json;
    
    HCPContentConfig *contentConfig = [[HCPContentConfig alloc] init];
    contentConfig.releaseVersion = jsonObject[RELEASE_VERSION_JSON_KEY];
    contentConfig.minimumNativeVersion = [(NSNumber *)jsonObject[MINIMUM_NATIVE_VERSION_JSON_KEY] integerValue];
    contentConfig.contentURL = [NSURL URLWithString:jsonObject[CONTENT_URL_JSON_KEY]];
    
    NSString *updateTime = jsonObject[UPDATE_TIME_JSON_KEY];
    contentConfig.updateTime = [contentConfig updateTimeStringToEnum:updateTime];
    
    return contentConfig;
}

@end
