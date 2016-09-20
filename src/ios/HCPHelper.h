//
//  HCPHelper.h
//  HotCodePush
//
//  Created by M on 16/8/26.
//
//

#import <Foundation/Foundation.h>
#import "HCPBlock.h"

@interface HCPHelper : NSObject

/**
 *  初始化
 *
 *  @param webUrl 远程目录的url
 *
 *  @return self
 */
-(HCPHelper *)initWithWebUrl:(NSURL *)webUrl;

/**
 *  是否从外部加载
 *
 *  @return 是否从外部加载
 */
-(BOOL)loadFromExternalStorageFolder;


/**
 *  检查更新
 *
 *  @param block
 */
-(void)fetchUpdate:(FetchUpdateBlock)block;

/**
 *  下载更新
 *
 *  @param block
 */
-(void)downloadUpdate:(DownloadUpdateBlock)block;

/**
 *  安装更新
 *
 *  @param block
 */
-(void)installUpdate:(InstallUpdateBlock)block;

/**
 *  获取加载www的路径
 *
 *  @return 加载www的路径
 */
-(NSURL *)pathToWww;

//
///**
// *  Install update if any available.
// *
// *  @param command command with which the method is called
// */
//- (void)jsInstallUpdate:(CDVInvokedUrlCommand *)command;
//
///**
// *  Show dialog with request to update the application through the App Store.
// *
// *  @param command command with which the method is called
// */
//- (void)jsRequestAppUpdate:(CDVInvokedUrlCommand *)command;
//
///**
// *  Check if new version was loaded and can be installed.
// *
// *  @param command command with which the method is called
// */
//- (void)jsIsUpdateAvailableForInstallation:(CDVInvokedUrlCommand *)command;

@end
