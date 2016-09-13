//
//  HCPConfigStorageImpl.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPConfigFileStorage.h"

/**
 *  实现HCPConfigFileStorage protocol
 *  
 *  @see HCPConfigFileStorage
 */
@interface HCPConfigStorageImpl : NSObject<HCPConfigFileStorage>

/**
 *  读取文件夹里的配置文件的url
 *
 *  @param folder 文件夹
 *
 *  @return 配置文件的url
 */
- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder;

/**
 *  把JSON对象转成自定义对象
 *
 *  @param jsonObject JSON对象
 *
 *  @return 自定义对象
 *  @see HCPJsonConvertable
 */
- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject;

@end
