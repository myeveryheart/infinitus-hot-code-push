//
//  HCPConfigStorage.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPJsonConvertable.h"

/**
 *  Protocol 配置文件存储
 */
@protocol HCPConfigFileStorage <NSObject>

/**
 *  保存配置到文件夹
 *
 *  @param config    配置
 *  @param folderURL 文件夹
 *  @return <code>YES</code> 保存成功; <code>NO</code> 保存失败
 *  @see HCPJsonConvertable
 */
- (BOOL)store:(id<HCPJsonConvertable>)config inFolder:(NSURL *)folderURL;

/**
 *  从文件夹读取配置
 *
 *  @param folderURL 文件夹
 *
 *  @return 配置
 */
- (id<HCPJsonConvertable>)loadFromFolder:(NSURL *)folderURL;

@end
