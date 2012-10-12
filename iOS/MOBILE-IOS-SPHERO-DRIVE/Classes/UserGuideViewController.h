//
//  UserGuideViewController.h
//  Sphero
//
//  Created by Jon Carroll on 12/30/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UserGuideViewController : UIViewController {
    IBOutlet UIWebView *webView;
    UIViewController *__unsafe_unretained delegate;
}

@property (nonatomic, unsafe_unretained) UIViewController *delegate;

-(IBAction)rollPressed:(id)sender;

@end
