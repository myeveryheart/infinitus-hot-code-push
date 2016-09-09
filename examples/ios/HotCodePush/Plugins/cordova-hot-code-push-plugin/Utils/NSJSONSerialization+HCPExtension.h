//
//  NSJSONSerialization+HCPExtension.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  扩展NSJSONSerialization
 */
@interface NSJSONSerialization (HCPExtension)

/**
 *  文件转JSON
 *
 *  @param fileURL 文件url
 *  @param error 错误
 *
 *  @return JSON对象
 *
 */
+ (id)JSONObjectWithContentsFromFileURL:(NSURL *)fileURL error:(NSError **)error;

/**
 *  string转JSON
 *
 *  @param jsonString JSON格式的string
 *  @param error      错误
 *
 *  @return JSON对象
 */
+ (id)JSONObjectWithContentsFromString:(NSString *)jsonString error:(NSError **)error;

@end
