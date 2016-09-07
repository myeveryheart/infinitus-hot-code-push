//
//  HCPContentManifestStorage.m
//
//  InfinitusHotCodePush
//
//  Created by M on 16/8/30.
//

#import "HCPContentManifestStorage.h"
#import "HCPContentManifest.h"

@interface HCPContentManifestStorage() {
    NSString *_fileName;
}

@end

@implementation HCPContentManifestStorage

- (instancetype)initWithFileStructure:(HCPFilesStructure *)fileStructure {
    self = [super init];
    if (self) {
        _fileName = fileStructure.manifestFileName;
    }
    
    return self;
}

- (NSURL *)getFullUrlToFileInFolder:(NSURL *)folder {
    return [folder URLByAppendingPathComponent:_fileName];
}

- (id<HCPJsonConvertable>)getInstanceFromJson:(id)jsonObject {
    return [HCPContentManifest instanceFromJsonObject:jsonObject];
}

@end
