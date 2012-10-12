//
//  UserGuideViewController.m
//  Sphero
//
//  Created by Jon Carroll on 12/30/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "UserGuideViewController.h"

@implementation UserGuideViewController

@synthesize delegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}


-(IBAction)rollPressed:(id)sender {
    if(self.navigationController) [self.navigationController popViewControllerAnimated:YES];
    else [delegate dismissModalViewControllerAnimated:YES];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    NSURL* infoURL = [[NSBundle mainBundle] URLForResource:@"SpheroCamUserGuide" withExtension:@"html"];
    NSURLRequest* request = [NSURLRequest requestWithURL:infoURL];
    [webView loadRequest:request];
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

@end
