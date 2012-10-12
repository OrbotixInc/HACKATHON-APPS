//
//  RCDriveViewController.h
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DriveController.h"
#import "CircularSlider.h"

@class RKDriveControl;
@class RUIColorIndicatorView;
@protocol DriveControllerDelegate;

@interface RCDriveViewController : UIViewController <DriveController> 
{
    UIView* backgroundView;
    UIView* controlView;
    UIImageView* boostImageView;
    RUIColorIndicatorView* colorView;
    UIView* speedContainerView;
    UIView* speedController;
    UIView* turnContainerView;
    UIView* turnController;
    
    RKDriveControl** driveControl;
    
    id<DriveControllerDelegate> delegate;
    
    CGFloat speedValue;
    CGFloat turnValue;
    BOOL    boostOn;
	
	IBOutlet CircularSlider *leftSlider;
	IBOutlet CircularSlider *rightSlider;
}

@property (nonatomic, retain) IBOutlet UIView* backgroundView;
@property (nonatomic, retain) IBOutlet UIView* controlView;
@property (nonatomic, retain) IBOutlet UIImageView* boostImageView;
@property (nonatomic, retain) IBOutlet RUIColorIndicatorView* colorView;
@property (nonatomic, retain) IBOutlet UIView* speedContainerView;
@property (nonatomic, retain) IBOutlet UIView* speedController;
@property (nonatomic, retain) IBOutlet UIView* turnContainerView;
@property (nonatomic, retain) IBOutlet UIView* turnController;

@property (nonatomic, assign) RKDriveControl** driveControl;
@property (nonatomic, assign) id<DriveControllerDelegate> delegate;

- (id)initWithDriveController:(RKDriveControl**)dc delegate:(id<DriveControllerDelegate>)d;

@end
