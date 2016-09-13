//
//  HCPFileDownloader.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPFileDownloader.h"
#import "HCPManifestFile.h"
#import "NSData+HCPMD5.h"
#import "NSError+HCPExtension.h"

@implementation HCPFileDownloader

#pragma mark Public API

- (void) downloadDataFromUrl:(NSURL*) url completionBlock:(HCPDataDownloadCompletionBlock) block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    NSURLSessionDataTask* dowloadTask = [session dataTaskWithURL:url completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        block(data, error);
    }];
    
    [dowloadTask resume];
}

- (void) downloadFiles:(NSArray *)filesList fromURL:(NSURL *)contentURL toFolder:(NSURL *)folderURL completionBlock:(DownloadUpdateBlock)block {
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    configuration.requestCachePolicy = NSURLRequestReloadIgnoringLocalCacheData;
    NSURLSession *session = [NSURLSession sessionWithConfiguration:configuration];
    
    __block NSMutableSet* startedTasks = [NSMutableSet set];
    __block BOOL canceled = NO;
    __block NSInteger fileDownloaded = 0;
    for (HCPManifestFile *file in filesList) {
        NSURL *url = [contentURL URLByAppendingPathComponent:file.name];
        __block NSURLSessionDataTask *downloadTask = [session dataTaskWithURL:url completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
            __weak __typeof(self) weakSelf = self;
            if (!error) {
                if ([weakSelf saveData:data forFile:file toFolder:folderURL error:&error]) {
                    NSLog(@"Loaded file %@ from %@", file.name, url.absoluteString);
                    fileDownloaded++;
                    block(NO, filesList.count, fileDownloaded, error);
                    [startedTasks removeObject:downloadTask];
                }
            }
            
            if (error) {
                [session invalidateAndCancel];
                [startedTasks removeAllObjects];
            }
            
            // 操作完成
            if (!canceled && (startedTasks.count == 0 || error)) {
                if (error) {
                    canceled = YES; // 不再发送其他错误
                }
                
                block(YES, filesList.count, fileDownloaded, error);
            }
        }];
        
        [startedTasks addObject:downloadTask];
        [downloadTask resume];
    }
}

#pragma Private API

/**
 *  检查数据的正确性
 *
 *  @param data     要检查的数据
 *  @param checksum 应该的checksum
 *  @param error    错误
 *
 *  @return <code>YES</code> 数据不正确; <code>NO</code> 数据正确
 */
- (BOOL)isDataCorrupted:(NSData *)data forFile:(HCPManifestFile *)file error:(NSError **)error {
    *error = nil;
    NSString *dataHash = [data md5];
    NSString *fileChecksum = file.md5Hash;
    if ([dataHash isEqualToString:fileChecksum]) {
        return NO;
    }
    
    NSString *errorMsg = [NSString stringWithFormat:@"Hash %@ of the file %@ doesn't match the checksum %@", dataHash, file.name, fileChecksum];
    *error = [NSError errorWithCode:kHCPFailedToDownloadUpdateFilesErrorCode description:errorMsg];
    
    return YES;
}

/**
 *  把数据保存到文件
 *
 *  @param data      数据
 *  @param file      文件
 *  @param folderURL 文件夹
 *  @param error     错误
 *
 *  @return <code>YES</code> - 保存成功; <code>NO</code> - 保存失败
 */
- (BOOL)saveData:(NSData *)data forFile:(HCPManifestFile *)file toFolder:(NSURL *)folderURL error:(NSError **)error {
    if ([self isDataCorrupted:data forFile:file error:error]) {
        return NO;
    }
    
    NSURL *filePath = [folderURL URLByAppendingPathComponent:file.name];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    // 删除旧文件
    if ([fileManager fileExistsAtPath:filePath.path]) {
        [fileManager removeItemAtURL:filePath error:nil];
    }
    
    // 创建目录
    [fileManager createDirectoryAtPath:[filePath.path stringByDeletingLastPathComponent]
           withIntermediateDirectories:YES
                            attributes:nil
                                 error:nil];
    
    // 写数据
    return [data writeToURL:filePath options:kNilOptions error:error];
}

@end
