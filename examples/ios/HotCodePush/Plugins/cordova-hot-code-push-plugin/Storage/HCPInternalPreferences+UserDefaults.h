//
//  HCPInternalPreferences+UserDefaults.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPInternalPreferences.h"

/**
 *  扩展HCPInternalPreferences，增加读取和存储UserDefaults
 *
 *  @see HCPConfig
 */
@interface HCPInternalPreferences (UserDefaults)

/**
 *  保存到UserDefaults
 */
- (void)saveToUserDefaults;

/**
 *  读取UserDefaults
 *
 *  @return 实例
 */
+ (HCPInternalPreferences *)loadFromUserDefaults;

@end
