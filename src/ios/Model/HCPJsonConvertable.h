//
//  HCPJsonConvertable.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  可转换JSON
 */
@protocol HCPJsonConvertable <NSObject>

/**
 *  转JSON
 *
 *  @return JSON object
 */
- (id)toJson;

/**
 *  JSON转object
 *
 *  @param json JSON object
 *
 *  @return 实例
 */
+ (instancetype)instanceFromJsonObject:(id)json;

@end
