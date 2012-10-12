//
//  JoystickDriveViewController.h
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DriveController.h"
#import "DriveControlViewController.h"

@class RKDriveControl;
@class RUIColorIndicatorView;
@protocol DriveControllerDelegate;

@interface JoystickDriveViewController : DriveControlViewController <DriveController>
{
    UIView* backgroundView;
    UIView* controlView;
    UIView* joystickView;
    UIView* driveControlPadView;
    RUIColorIndicatorView* colorView;
    
    RKDriveControl** driveControl;
    BOOL ballMoving;
    
}

@property (nonatomic, retain) IBOutlet UIView* backgroundView;
@property (nonatomic, retain) IBOutlet UIView* controlView;
@property (nonatomic, retain) IBOutlet UIView* joystickView;
@property (nonatomic, retain) IBOutlet UIView* driveControlPadView;
@property (nonatomic, retain) IBOutlet RUIColorIndicatorView* colorView;

@property (nonatomic, assign) RKDriveControl** driveControl;

- (id)initWithDriveController:(RKDriveControl**)dc delegate:(id<DriveControllerDelegate>)d;

@end
