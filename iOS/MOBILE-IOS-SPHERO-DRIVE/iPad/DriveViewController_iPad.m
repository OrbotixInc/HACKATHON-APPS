//
//  DriveViewController_iPad.m
//  Sphero
//
//  Created by Brian Smith on 1/13/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "DriveViewController_iPad.h"
#import "DriveAppSettings.h"
#import "SensitivityViewController.h"
#import "InfoViewController.h"
#import "DriveAppDelegate.h"
#import "RCDriveViewController_iPad.h"
#import "JoystickDriveViewController_iPad.h"
#import "RUIColorIndicatorView.h"
#import "DriveTypeSelectorViewController.h"
#import "FlurryAPI.h"

@implementation DriveViewController_iPad

#pragma mark -
#pragma mark Actions

- (BOOL)switchToJoystickDrive
{
    if( [super switchToJoystickDrive] ) {
        [driveTypeButton setImage:[UIImage imageNamed:@"SpheroDrive-ipad-driveTypeJoystickInset.png"]
                         forState:UIControlStateNormal];
        
        id<DriveController> oldController = driveController;
        driveController = [[JoystickDriveViewController_iPad alloc] initWithDriveController:&driveControl delegate:self];
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
        [driveTypeButton setImage:[UIImage imageNamed:@"SpheroDrive-ipad-driveTypeTiltInset.png"]
                         forState:UIControlStateNormal];
        return YES;
    }
    return NO;
}

- (BOOL)switchToRCDrive
{
    if( [super switchToRCDrive] ) {
        [driveTypeButton setImage:[UIImage imageNamed:@"SpheroDrive-ipad-driveTypeRCInset.png"]
                         forState:UIControlStateNormal];
        
        id<DriveController> oldController = driveController;
        driveController = [[RCDriveViewController_iPad alloc] initWithDriveController:&driveControl delegate:self];
        UIView* controlsView = [driveController controlsView];
        controlsView.frame = self.view.bounds;
        
        [super transitionDriveControlsFromOldController:oldController];
        return YES;
    }
    return NO;
}

//Present the calibration view from a popover originating from where the long press was
- (void)handleLongPress:(UILongPressGestureRecognizer*)recognizer {
	if(!calibrationPopover) {
		[FlurryAPI logEvent:@"Calibrated"];
		NSBundle* RUIResourcesBundle = [DriveAppDelegate getRobotUIKitResourcesBundle];
		calibrationController = 
		[[RUICalibrationViewController alloc] initWithNibName:@"RUICalibrationViewController"
													   bundle:RUIResourcesBundle];
		calibrationController.robotControl = driveControl.robotControl;
		[calibrationController setDismissedTarget:self action:@selector(calibrationDismissed:)];
		[self pauseDriving];
		calibrationController.contentSizeForViewInPopover = CGSizeMake(480, 320);
		calibrationPopover = [[WEPopoverController alloc] initWithContentViewController:calibrationController];
		calibrationPopover.delegate = self;
		CGPoint location = [recognizer locationInView:self.view];
		[calibrationPopover presentPopoverFromRect:CGRectMake(location.x, location.y, 20.0, 20.0) inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
 		[calibrationController release];
		initialCalibration = YES;
		
	}
}

#pragma mark -
#pragma mark UI

//This method should only get called on first launch on the iPad and will display the iPad sized calibration view fullscreen
- (void)presentCalibrationView
{
    if( [RUIModalLayerViewController currentModalLayerViewController] != nil )
        return;
    
	NSBundle* RUIResourcesBundle = [DriveAppDelegate getRobotUIKitResourcesBundle];
    calibrationController = 
    [[RUICalibrationViewController alloc] initWithNibName:@"RUICalibrationViewController_ipad"
                                                   bundle:RUIResourcesBundle];
    calibrationController.robotControl = driveControl.robotControl;
	[calibrationController setDismissedTarget:self action:@selector(calibrationDismissed:)];
	[self pauseDriving];
    [self presentModalLayerViewController:calibrationController animated:YES];
    [calibrationController release];
	initialCalibration = YES;
}

//Present color picker in popover from color indicator view of current driveController
- (void)presentColorPickerView
{
    if(calloutTouchHandlerView) [self hideCallout];
    [[DriveAppSettings defaultSettings] setColorCalloutShown:YES];
    [FlurryAPI logEvent:@"ColorChanged"];
	colorIndicatorView = [driveController colorIndicatorView];
	RUIColorPickerViewController* colorpicker_controller =
    [[RUIColorPickerViewController alloc]
     initWithNibName:@"RUIColorPickerViewController"
     bundle:[DriveAppDelegate getRobotUIKitResourcesBundle]];
    
	colorpicker_controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
	DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
	[colorpicker_controller setRed:rgb.red green:rgb.green blue:rgb.blue];
	colorpicker_controller.delegate = self;
	[colorpicker_controller setDismissedTarget:self action:@selector(colorPickerDismissed:)];
	[self pauseDriving];
	cpcPopover = [[WEPopoverController alloc] initWithContentViewController:colorpicker_controller];
	cpcPopover.delegate = self;
	CGRect popoverRect = [self.view convertRect:colorIndicatorView.frame fromView:[driveController controlsView]];
	[cpcPopover presentPopoverFromRect:popoverRect inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
	cpc = colorpicker_controller;
    [colorpicker_controller showRollButton:NO];
    
    
}

-(void)showCalibrationTutorial { 
    [super showCalibrationTutorial];
    [menuPopover dismissPopoverAnimated:NO];
    menuPopover = nil;
}

- (void)appDidBecomeActive:(NSNotification*)notification {
    [super appDidBecomeActive:notification];
    [menuPopover dismissPopoverAnimated:NO];
    menuPopover = nil;
}

//Present the menu in a popover
- (void)presentOptionsMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction
{
    if( menuPopover == nil ) {
        [self pauseDriving];
        menuController = [[MainMenuViewController alloc] initWithNibName:nil bundle:nil];
        menuController.delegate = self;
		menuController.contentSizeForViewInPopover = CGSizeMake(480, 320);
        [menuController hideRollButton];
		UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:menuController];
		navController.navigationBarHidden = YES;
        menuPopover = [[WEPopoverController alloc]
                       initWithContentViewController:navController];
        menuController.popoverController = menuPopover;
        menuPopover.delegate = self;
        [menuPopover presentPopoverFromRect:fromArea
                                     inView:self.view 
                   permittedArrowDirections:direction 
                                   animated:YES];
		[navController release];
    }
}

#pragma mark -
#pragma mark Delegate Methods

//Done button pressed on color picker presented from color indicator view
- (void)colorPickerDidFinish:(UIViewController *)controller withRed:(CGFloat)r 
					   green:(CGFloat)g blue:(CGFloat)b
{
	DriveAppSettingsRGB settings = {r, g, b};
	[DriveAppSettings defaultSettings].robotLEDBrightness = settings;
	
	[super resumeDriving];
	[cpcPopover dismissPopoverAnimated:YES];
	[cpcPopover release];
	cpcPopover = nil;
	[cpc release];
	cpc = nil;
}

//Done button pressed on calibration view
- (void)calibrationChanged:(NSNotification*)notification {
	[super calibrationDismissed:calibrationController];
	[calibrationPopover release];
	calibrationPopover = nil;
	calibrationController = nil;
	[calibrationPopover dismissPopoverAnimated:YES];
}

//Take appropriate action if a popover is dismissed
- (void)popoverControllerDidDismissPopover:(id)popoverController 
{
    [self resumeDriving];
    if( popoverController == menuPopover ) {
        SEL selector = menuController.chosenAction;
        
        [menuPopover release];
        menuPopover = nil;
        [menuController release];
        menuController = nil;
        
        if( selector != NULL ) {
            if( [self respondsToSelector:selector] ) {
                [self performSelector:selector];
            } else {
                NSLog(@"DriveViewController does not implement menu option selector: %@", NSStringFromSelector(selector));
            }
        }
    } else if(popoverController==cpcPopover) {
		DriveAppSettingsRGB settings = {colorIndicatorView.red, colorIndicatorView.green, colorIndicatorView.blue};
		[DriveAppSettings defaultSettings].robotLEDBrightness = settings;
		[super resumeDriving];
		[cpcPopover release];
		cpcPopover = nil;
		[cpc release];
		cpc = nil;
	} else if(popoverController==calibrationPopover) {
		[super calibrationDismissed:calibrationController];
		[calibrationPopover release];
		calibrationPopover = nil;
		calibrationController = nil;
	} else if(popoverController==sensitivityPopover) {
        [sensitivityPopover release];
        sensitivityPopover = nil;
    } else {
        [super popoverControllerDidDismissPopover:popoverController];
    }
}

- (void)updateSensitivity:(id)source
{
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    driveControl.velocityScale = settings.velocityScale;
    boostTime = settings.boostTime;
    [RKRotationRateCommand sendCommandWithRate:settings.rotationRate];
}

- (void)sensitivityLongPress:(UIGestureRecognizer*)recognizer {
    
    
    if(sensitivityPopover==nil) {
        SensitivityViewController *controller = [[SensitivityViewController alloc] initWithNibName:@"SensitivityViewController" bundle:nil];
        [controller loadView];
        [controller viewDidLoad];
        controller.backLabel.alpha = 0.0;
        controller.backButton.alpha = 0.0;
        [controller hideRollButton];
        controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
        UIPopoverController *popover = [[UIPopoverController alloc] initWithContentViewController:controller];
        popover.delegate = self;
        [popover presentPopoverFromRect:sensitivityButton.frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
        sensitivityPopover = popover;
        [controller release];
    }
    
}

#pragma mark -
#pragma mark View Lifecycle

//Register for the calibration notifications and un-register when the view unloads
- (void)viewDidLoad {
	[super viewDidLoad];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(calibrationChanged:) name:@"RUICalibrationViewControllerCalibrationDone" object:nil];
}

- (void)viewDidUnload {
	[super viewDidUnload];
	[[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
