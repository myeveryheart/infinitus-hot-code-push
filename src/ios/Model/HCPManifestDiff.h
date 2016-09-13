//
//  HCPManifestDiff.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPManifestFile.h"

/**
 *  两个manifest文件的差异
 */
@interface HCPManifestDiff : NSObject

/**
 *  获取新增的和修改的文件
 *
 *  @return HCPManifestFile数组
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *updateFileList;

/**
 *  两个manifest是否一样
 */
@property (nonatomic, readonly, getter=isEmpty) BOOL isEmpty;

/**
 *  新增文件
 *
 *  @return HCPManifestFile数组
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *addedFiles;

/**
 *  修改文件
 *
 *  @return HCPManifestFile数组
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *changedFiles;

/**
 *  删除文件
 * 
 *  @return HCPManifestFile数组
 *  @see HCPManifestFile
 */
@property (nonatomic, strong, readonly) NSArray *deletedFiles;

/**
 *  初始化
 *
 *  @param addedFiles   新增文件
 *  @param changedFiles 修改文件
 *  @param deletedFiles 删除文件
 *
 *  @return 实例
 *  @see HCPManifestFile
 */
- (instancetype)initWithAddedFiles:(NSArray *)addedFiles changedFiles:(NSArray *)changedFiles deletedFiles:(NSArray *)deletedFiles;

@end
