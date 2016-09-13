//
//  HCPContentManifestStorage.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPConfigStorageImpl.h"
#import "HCPFilesStructure.h"

/**
 *  保存和读取manifest文件的工具类
 * 
 *  @see HCPContentManifest
 */
@interface HCPContentManifestStorage : HCPConfigStorageImpl

/**
 *  初始化
 *
 *  @param fileStructure fileStructure
 *
 *  @return 实例
 */
- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure;

@end
