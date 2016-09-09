//
//  HCPApplicationConfigStorage.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPApplicationConfigStorage.h"
#import "HCPApplicationConfig.h"

@interface HCPApplicationConfigStorage() {
    NSString *_fileName;
}

@end

@implementation HCPApplicationConfigStorage

- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure {
    self = [super init];
    if (self) {
        _fileName = fileStructure.configFileName;
    }
    
    return self;
}

- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder {
    return [folder URLByAppendingPathComponent:_fileName];
}

- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject {
    return [HCPApplicationConfig instanceFromJsonObject:jsonObject];
}

@end
