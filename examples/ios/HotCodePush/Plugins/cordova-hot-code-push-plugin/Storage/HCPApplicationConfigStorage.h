//
//  HCPApplicationConfigStorage.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

/**
 *  从文件夹读取和保存配置的工具类
 *
 *  @see HCPConfigFileStorage
 */
@interface HCPApplicationConfigStorage : HCPConfigStorageImpl

/**
 *  初始化
 *
 *  @param fileStructure 文件结构
 *
 *  @return 实例
 *  @see HCPFilesStructure
 */
- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure;

@end
