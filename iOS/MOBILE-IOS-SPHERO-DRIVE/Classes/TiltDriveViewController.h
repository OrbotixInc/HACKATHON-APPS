//
//  TiltDriveViewController.h
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

@interface TiltDriveViewController : DriveControlViewController  <DriveController> {
    UIView* backgroundView;
    UIView* controlView;
    UIView* puckView;
    UIView* driveControlPadView;
    RUIColorIndicatorView* colorView;
    
    RKDriveControl** driveControl;
    
}

@property (nonatomic, retain) IBOutlet UIView* backgroundView;
@property (nonatomic, retain) IBOutlet UIView* controlView;
@property (nonatomic, retain) IBOutlet UIView* puckView;
@property (nonatomic, retain) IBOutlet UIView* driveControlPadView;
@property (nonatomic, retain) IBOutlet RUIColorIndicatorView* colorView;

@property (nonatomic, assign) RKDriveControl** driveControl;

- (id)initWithDriveController:(RKDriveControl**)dc delegate:(id<DriveControllerDelegate>)d;

@end
