//
//  HCPManifestFile.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  mainifest
 */
@interface HCPManifestFile : NSObject<HCPJsonConvertable>

/**
 *  名字
 */
@property (nonatomic, readonly, strong) NSString *name;

/**
 * HASH
 */
@property (nonatomic, readonly, strong) NSString *md5Hash;

/**
 *  初始化
 *
 *  @param name    名字
 *  @param md5Hash HASH
 *
 *  @return 实例
 */
- (instancetype)initWithName:(NSString *)name md5Hash:(NSString *)md5Hash;

@end
