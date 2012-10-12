//
//  DriveControlViewController.h
//  SpheroDrive
//
//  Created by Brian Smith on 12/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol DriveControllerDelegate;

@interface DriveControlViewController : UIViewController {
    UIButton    *boostButtonLeft;
    UIButton    *boostButtonRight;
    
    BOOL boosting;
    id<DriveControllerDelegate> delegate;
}

@property (nonatomic, retain) IBOutlet UIButton *boostButtonLeft;
@property (nonatomic, retain) IBOutlet UIButton *boostButtonRight;

@property (nonatomic, assign) id<DriveControllerDelegate> delegate;

- (id)initWithDelegate:(id<DriveControllerDelegate>)d;

- (IBAction)boostButtonDown:(UITapGestureRecognizer *)recognizer;
- (IBAction)boostButtonRepeat:(UITapGestureRecognizer *)recognizer;

- (void)handleBoostDidFinish:(NSNotification *)notification;

@end
