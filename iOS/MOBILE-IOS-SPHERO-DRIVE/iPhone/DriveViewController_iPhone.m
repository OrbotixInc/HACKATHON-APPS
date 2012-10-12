//
//  DriveViewController_iPhone.m
//  Sphero
//
//  Created by Brian Smith on 1/13/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "DriveViewController_iPhone.h"
#import "DriveAppSettings.h"
#import "RCDriveViewController.h"
#import "MainMenuViewController.h"
#import "JoystickDriveViewController.h"
#import "SensitivityViewController.h"

#import <QuartzCore/QuartzCore.h>

@implementation DriveViewController_iPhone

- (BOOL)switchToJoystickDrive
{
    if( [super switchToJoystickDrive] ) {
        [driveTypeButton setImage:[UIImage imageNamed:@"JoystickButton"]
                         forState:UIControlStateNormal];
        [driveTypeButton setImage:[UIImage imageNamed:@"JoystickButtonPressed"]
                         forState:UIControlStateHighlighted];
        
        id<DriveController> oldController = driveController;
        driveController = [[JoystickDriveViewController alloc] initWithDriveController:&driveControl delegate:self];
        UIView* controlsView = [driveController controlsView];
        controlsView.frame = self.view.bounds;
        
        [super transitionDriveControlsFromOldController:oldController];
        return YES;
    }
    return NO;
}

- (BOOL)switchToTiltDrive
{
    if( [super switchToTiltDrive] ) {
        [driveTypeButton setImage:[UIImage imageNamed:@"TiltButton"]
                         forState:UIControlStateNormal];
        [driveTypeButton setImage:[UIImage imageNamed:@"TiltButtonPressed"]
                         forState:UIControlStateHighlighted];
        return YES;
    }
    return NO;
}

- (BOOL)switchToRCDrive
{
    if( [super switchToRCDrive] ) {
        [driveTypeButton setImage:[UIImage imageNamed:@"RCButton"]
                         forState:UIControlStateNormal];
        [driveTypeButton setImage:[UIImage imageNamed:@"RCButtonPressed"]
                         forState:UIControlStateHighlighted];
        
        id<DriveController> oldController = driveController;
        driveController = [[RCDriveViewController alloc] initWithDriveController:&driveControl delegate:self];
        UIView* controlsView = [driveController controlsView];
        controlsView.frame = self.view.bounds;
        
        [super transitionDriveControlsFromOldController:oldController];
        return YES;
    }
    return NO;
}

- (void)presentOptionsMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction
{
	CATransition* transition = [CATransition animation];
	transition.duration = 0.5;
	transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type = kCATransitionMoveIn;
    transition.subtype = kCATransitionFromRight;
	[self.navigationController.view.layer addAnimation:transition forKey:nil];
	MainMenuViewController* mainmenu_viewcontroller =
	[[MainMenuViewController alloc] initWithNibName:nil bundle:nil];
    mainmenu_viewcontroller.delegate = self;
	[self.navigationController pushViewController:mainmenu_viewcontroller animated:NO];
	[mainmenu_viewcontroller release];
}

- (void)sensitivityLongPress:(UIGestureRecognizer*)recognizer {
    CATransition* transition = [CATransition animation];
	transition.duration = 0.5;
	transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type = kCATransitionMoveIn;
    transition.subtype = kCATransitionFromRight;
	[self.navigationController.view.layer addAnimation:transition forKey:nil];
    
    
    SensitivityViewController *controller = [[SensitivityViewController alloc] initWithNibName:@"SensitivityViewController" bundle:nil];
    [controller loadView];
    [controller viewDidLoad];
    controller.backLabel.alpha = 0.0;
    controller.backButton.alpha = 0.0;
    [self.navigationController pushViewController:controller animated:NO];
    
    [controller release];

}

@end
