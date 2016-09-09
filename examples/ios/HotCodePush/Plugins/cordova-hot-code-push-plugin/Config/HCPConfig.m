//
//  HCPXmlConfig.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPConfig.h"

#define kConfigFile @"hcp.json"

@implementation HCPConfig

- (instancetype)init {
    self = [super init];
    if (self) {
        _configUrl = nil;
        _nativeInterfaceVersion = 1;
    }
    
    return self;
}

-(NSURL *)configUrl
{
    return [self.webUrl URLByAppendingPathComponent:kConfigFile];
}

@end