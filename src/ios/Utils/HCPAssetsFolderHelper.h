//
//  HCPAssetsFolderHelper.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>
#import "HCPBlock.h"

/**
 *  把www文件夹安装到外部存储的工具类
 */
@interface HCPAssetsFolderHelper : NSObject

/**
 *  把bunlde的www文件夹安装到外部存储
 *
 *  @param externalFolderURL 外部存储的url
 */
+ (void)installWwwFolderToExternalStorageFolder:(NSURL *)externalFolderURL completionBlock:(InstallWwwFolderBlock)block;

@end
