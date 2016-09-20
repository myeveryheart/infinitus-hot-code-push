//
//  UpdateViewController.m
//  HotCodePush
//
//  Created by M on 16/8/30.
//
//

#import "UpdateViewController.h"
#import "HCPHelper.h"

@interface UpdateViewController ()

@property (weak, nonatomic) IBOutlet UILabel *infoLabel;

@end

@implementation UpdateViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    NSURL *webUrl = [NSURL URLWithString:@"http://172.20.70.80/poc/"];
    HCPHelper *hcpHelper = [[HCPHelper alloc] initWithWebUrl:webUrl];
    
    NSLog(@"加载路径 %@",[hcpHelper pathToWww].absoluteString);
    
    if ([hcpHelper loadFromExternalStorageFolder])
    {
        //从外部加载
        self.infoLabel.text = @"从外部加载";
        [hcpHelper fetchUpdate:^(BOOL needUpdate, NSError *error) {
            if (needUpdate)
            {
                self.infoLabel.text = @"强制更新";
                [hcpHelper downloadUpdate:^(BOOL success, NSInteger totalFiles, NSInteger fileDownloaded, NSError *error) {
                    self.infoLabel.text = [NSString stringWithFormat:@"正在下载第%ld个，共%ld个",fileDownloaded,totalFiles];
                    if (success)
                    {
                        [hcpHelper installUpdate:^(BOOL success, NSError *error) {
                            if (success)
                            {
                                self.infoLabel.text = @"更新成功";
                            }
                            else
                            {
                                self.infoLabel.text = error.description;
                            }
                        }];
                    }
                    else if(error)
                    {
                        self.infoLabel.text = error.description;
                    }
                }];
            }
            else
            {
                self.infoLabel.text = @"没有强制更新";
            }
        }];
    }
    else
    {
        //从内部加载
        self.infoLabel.text = @"从内部加载";
    }
}


- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
