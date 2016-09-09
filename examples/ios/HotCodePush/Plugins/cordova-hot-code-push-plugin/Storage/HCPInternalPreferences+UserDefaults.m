//
//  HCPInternalPreferences+UserDefaults.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPInternalPreferences+UserDefaults.h"

static NSString *const CONFIG_USER_DEFAULTS_KEY = @"hot_code_push_config";

@implementation HCPInternalPreferences (UserDefaults)

- (void)saveToUserDefaults {
    id json = [self toJson];
    
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    [userDefaults setObject:json forKey:CONFIG_USER_DEFAULTS_KEY];
    [userDefaults synchronize];
}

+ (HCPInternalPreferences *)loadFromUserDefaults {
    NSUserDefaults *userDefaults = [NSUserDefaults standardUserDefaults];
    id json = [userDefaults objectForKey:CONFIG_USER_DEFAULTS_KEY];
    if (json) {
        return [HCPInternalPreferences instanceFromJsonObject:json];
    }
    
    return nil;
}


@end
