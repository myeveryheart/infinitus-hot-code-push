//
//  NSData+MD5.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import <CommonCrypto/CommonDigest.h>
#import "NSData+HCPMD5.h"

@implementation NSData (HCPMD5)

- (NSString *)md5 {
    // Create byte array of unsigned chars
    unsigned char md5Buffer[CC_MD5_DIGEST_LENGTH];
    
    // Create 16 byte MD5 hash value, store in buffer
    CC_MD5(self.bytes, (uint)self.length, md5Buffer);
    
    // Convert unsigned char buffer to NSString of hex values
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [output appendFormat:@"%02x",md5Buffer[i]];
    }
    
    return output;
}

@end
