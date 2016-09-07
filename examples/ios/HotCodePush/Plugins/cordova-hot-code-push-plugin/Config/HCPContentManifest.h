//
//  HCPContentManifest.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

#import "HCPJsonConvertable.h"
#import "HCPManifestDiff.h"

/**
 *  manifest文件
 */
@interface HCPContentManifest : NSObject<HCPJsonConvertable>

/**
 *  web文件列表，数组里是HCPManifestFile
 *
 *  @see HCPManifestFile
 */
@property (nonatomic, readonly, strong) NSArray *files;

/**
 *  比较manifest
 *
 *  @param comparedManifest 新的manifest
 *
 *  @return manifests差别
 *  @see HCPManifestFile
 *  @see HCPManifestDiff
 */
- (HCPManifestDiff *)calculateDifference:(HCPContentManifest *)comparedManifest;

@end
