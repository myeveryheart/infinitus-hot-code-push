//
//  NSData+MD5.h
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <Foundation/Foundation.h>

/**
 *  扩展NSData
 */
@interface NSData (HCPMD5)

/**
 *  生成hash
 *
 *  @return hash
 */
- (NSString *)md5;

@end
