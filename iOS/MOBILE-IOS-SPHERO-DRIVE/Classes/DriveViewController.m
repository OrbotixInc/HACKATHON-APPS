//
//  DriveViewController.m
//  Drive
//
//  Created by Brian Smith on 11/19/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import "DriveViewController.h"
#import "DriveAppDelegate.h"
#import "DriveAppSettings.h"
#import "MainMenuViewController.h"
#import "DriveTypeSelectorViewController.h"
#import "SensitivitySelectorViewController.h"
#import "DriveController.h"
#import "RCDriveAlgorithm.h"
#import "JoystickDriveViewController.h"
#import "JoystickDriveViewController_iPad.h"
#import "TiltDriveViewController.h"
#import "RCDriveViewController.h"
#import "RCDriveViewController_iPad.h"
#import "TutorialViewController.h"

#import <AudioToolbox/AudioToolbox.h>
#import <QuartzCore/QuartzCore.h>
#import <RobotUIKit/RobotUIKit.h>
#import <RobotUIKit/RUIColorIndicatorView.h>
#import <CoreLocation/CoreLocation.h>
#import "FlurryAPI.h"
#import "SpheroDropDownAlertSound.h"
#import "SpheroDriveWheelIn.h"
#import "SpheroButtonPressSound.h"
#import "SpheroItemSelectSound.h"
#import "NoSpheroAlertManager.h"
#import "CalibrateOverlayEnd.h"
#import "CalibrateOverlayLoop.h"
#import "CalibrateOverlayStartSound.h"

#define BOOST_TIME_SCALE 25.5

@interface DriveViewController ()

- (void)initializeRobot;
- (void)showNoSpheroAlert;
- (void)handleConnectionOnline:(NSNotification *)notification;
- (void)handleConnectionOffline:(NSNotification *)notification;

- (void)colorPickerDismissed:(id)sender;
- (void)calibrationDismissed:(id)sender;
- (void)appDidBecomeActive:(NSNotification *)notification;
- (void)appWillResignActive:(NSNotification *)notification;
- (void)presentOptionsMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction;
- (void)presentDriveTypeMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction;
- (void)presentDriveTypeMenu:(CGRect)fromArea 
                   direction:(UIPopoverArrowDirection)direction
                   highlight:(DriveAppDriveType)next
                       delay:(float)d;
- (void)analyticsEndTimedDriveTypeEvent;
- (void)presentTutorial;
- (void)presentCalibrationView;

@end

@interface DriveTypeOptionData : NSObject
{
    DriveAppDriveType driveType;
    float delay;
    DriveTypeSelectorViewController* controller;
}

@property (nonatomic, assign) DriveAppDriveType driveType;
@property (nonatomic, assign) float delay;
@property (nonatomic, assign) DriveTypeSelectorViewController* controller;

- (id)initWithController:(DriveTypeSelectorViewController*)c driveType:(DriveAppDriveType)dt delay:(float)delay;
@end

@implementation DriveTypeOptionData
@synthesize driveType;
@synthesize delay;
@synthesize controller;

- (id)initWithController:(DriveTypeSelectorViewController*)c driveType:(DriveAppDriveType)dt delay:(float)d
{
    self = [super init];
    if( self != nil ) {
        self.controller = c;
        self.driveType = dt;
        self.delay = d;
    }
    return self;
}
@end

@implementation DriveViewController

@synthesize menuButton;
@synthesize driveTypeButton;
@synthesize sensitivityButton;
@synthesize calibrationButton;

#pragma mark -
#pragma mark Actions

- (IBAction)menuButtonUp
{
    [[SpheroButtonPressSound sharedSound] play];
	[FlurryAPI logEvent:@"Menu"];
    [self presentOptionsMenu:menuButton.frame 
                   direction:UIPopoverArrowDirectionUp];
}

- (IBAction)driveTypeButtonUp
{
    [[SpheroButtonPressSound sharedSound] play];
    [[DriveAppSettings defaultSettings] setDriveTypeCalloutShown:YES];
    if(calloutTouchHandlerView) [self hideCallout];
    
    [self presentDriveTypeMenu:driveTypeButton.frame 
                     direction:UIPopoverArrowDirectionUp];
}

- (IBAction)calibrationButtonUp
{
    [[SpheroButtonPressSound sharedSound] play];
    [self presentCalibrationViewForDriveController];
}

- (IBAction)sensitivityButtonUp
{
    [[SpheroButtonPressSound sharedSound] play];
    if( miniPopoverController != nil ) {
        [miniPopoverController dismissPopoverAnimated:YES];
        miniPopoverController = nil;
    } else {
        [self pauseDriving];
        [[DriveAppSettings defaultSettings] setSpeedCalloutShown:YES];
        if(calloutTouchHandlerView) [self hideCallout];
        
        SensitivitySelectorViewController* contentViewController = 
        [[SensitivitySelectorViewController alloc]
         initWithNibName:nil bundle:nil];
        if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
            contentViewController.contentSizeForViewInPopover = CGSizeMake(71, 300);
        } else {
            contentViewController.contentSizeForViewInPopover = CGSizeMake(47, 182);
            
        }
        
        contentViewController.delegate = self;
        miniPopoverController = [[WEPopoverController alloc] 
                                      initWithContentViewController:contentViewController];
        miniPopoverController.delegate = self;
        [miniPopoverController presentPopoverFromRect:sensitivityButton.frame
                                                    inView:self.view 
                                  permittedArrowDirections:UIPopoverArrowDirectionUp 
                                                  animated:YES];
        [contentViewController release];
    }    
}

- (void)dismissSensitivityPopup
{
    if( miniPopoverController != nil ) {
        [miniPopoverController dismissPopoverAnimated:YES];
        [miniPopoverController release];
        miniPopoverController = nil;
        [driveController resumeDriving];
    }
}

- (void)presentFirmwareUploadViewController
{
    // handle by iphone and ipad subclasses
}

- (void)presentOptionsMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction
{
    // handle by iphone and ipad subclasses.
}

- (void)presentDriveTypeMenu:(CGRect)fromArea direction:(UIPopoverArrowDirection)direction
{
    [self presentDriveTypeMenu:fromArea direction:direction highlight:-1
                         delay:-1];
}

- (void) presentDriveTypeMenu:(CGRect)fromArea 
                    direction:(UIPopoverArrowDirection)direction
                    highlight:(DriveAppDriveType)next
                        delay:(float)delay
{
    if( miniPopoverController != nil ) {
        [miniPopoverController dismissPopoverAnimated:YES];
        miniPopoverController = nil;
    } else {
        [self pauseDriving];
        DriveTypeSelectorViewController* contentViewController = 
        [[DriveTypeSelectorViewController alloc]
         initWithNibName:nil bundle:nil];
        if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
            contentViewController.contentSizeForViewInPopover = CGSizeMake(71, 300);
        } else {
            contentViewController.contentSizeForViewInPopover = CGSizeMake(47, 182);
            
        }
        contentViewController.delegate = self;
        
        miniPopoverController = [[WEPopoverController alloc] 
                                      initWithContentViewController:contentViewController];
        miniPopoverController.delegate = self;
        [miniPopoverController presentPopoverFromRect:fromArea
                                                    inView:self.view 
                                  permittedArrowDirections:direction 
                                                  animated:YES];
        if( delay > 0 )
        {
            [self.view.superview setUserInteractionEnabled:NO];
            [self performSelector:@selector(doHilightDriveType:) 
                       withObject:[[DriveTypeOptionData alloc] initWithController:contentViewController driveType:next delay:delay] afterDelay:0.5];
        }
        [contentViewController release];
    }    
}

- (void)doHilightDriveType:(DriveTypeOptionData*)data
{
    [data.controller hilightDriveType:data.driveType withDelay:data.delay];
}


- (void)popoverControllerDidDismissPopover:(id)popoverController
{
    if( popoverController == miniPopoverController ) {
        [miniPopoverController release];
        miniPopoverController = nil;
        [driveController resumeDriving];
    }
}

- (BOOL)popoverControllerShouldDismissPopover:(id)popoverController
{
    return YES;
}

- (void)addDriveControlsView:(BOOL)animated
{
    UIView* newControlsView = [driveController controlsView];
    [self.view insertSubview:newControlsView belowSubview:menuButton];
    
    [(UIViewController*)(driveController) viewWillAppear:animated];    
}

- (BOOL)presentTutorialIfRequired
{
    return NO;
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    BOOL showTutorial = settings.mainTutorial;
    switch( settings.driveType ) {
        case DriveTypeJoystick:
            showTutorial |= settings.joystickTutorial;
            break;
        case DriveTypeTilt:
            showTutorial |= settings.tiltTutorial;
            break;
        case DriveTypeRC:
            showTutorial |= settings.rcTutorial;
            break;
    }
    
    if( showTutorial )
    {
        [self presentTutorial];
        return YES;
    } else {
        //if( !initialCalibration )
            //[self presentCalibrationView];
        return NO;
    }
}

- (void)transitionDriveControlsToNewController
{
    // Add the new drive controls view to the main view offscreen
    CGRect parent_frame = self.view.frame;
    UIView* newControlsView = [driveController controlsView];
    CGRect new_child_bounds = newControlsView.bounds;
    newControlsView.center = CGPointMake(CGRectGetMidX(parent_frame), 
                                        CGRectGetMaxY(parent_frame) + 
                                        CGRectGetMidY(new_child_bounds));    
    [self addDriveControlsView:YES];
         
    // Animate it coming into place
    [UIView animateWithDuration:0.3
                          delay:0.0
                        options:UIViewAnimationCurveEaseOut
                     animations:^{
                         newControlsView.frame = self.view.bounds;
                                 }
                     completion:^(BOOL finished){
                         [(UIViewController*)(driveController) viewDidAppear:YES];
                         [self presentTutorialIfRequired];
                     }];
}

- (void)transitionDriveControlsFromOldController:(id<DriveController>)oldController
{
    if( oldController != nil ) {
        [[SpheroDriveWheelIn sharedSound] play];
        [(UIViewController*)(oldController) viewWillDisappear:YES];
        UIView* oldControlsView = [oldController controlsView];
    
        [UIView animateWithDuration:0.3
                          delay:0.0 
                        options:UIViewAnimationCurveEaseIn 
                     animations:^{ 
                         CGRect b = self.view.bounds;
                         oldControlsView.frame = CGRectMake(b.origin.x, 
                                                            b.origin.y + b.size.height,
                                                            b.size.width,
                                                            b.size.height);
                                 } 
                     completion:^(BOOL finished){
                         [(UIViewController*)(oldController) viewDidDisappear:YES];
                         [oldController release];
                         [self transitionDriveControlsToNewController];
                                 }];
    } else {
        [self addDriveControlsView:NO];
        [(UIViewController*)(driveController) viewDidAppear:NO];
        [self presentTutorialIfRequired];
    }
}

//Should be called when switching drive types to end the timed analytics collection for that drive type
- (void)analyticsEndTimedDriveTypeEvent {
	if([driveController class] == [JoystickDriveViewController class]) {
		[FlurryAPI endTimedEvent:@"JoystickDrive" withParameters:nil];
	} else if([driveController class] == [TiltDriveViewController class]) {
		[FlurryAPI endTimedEvent:@"TiltDrive" withParameters:nil];
	} else if([driveController class] == [RCDriveViewController class]) {
		[FlurryAPI endTimedEvent:@"RCDrive" withParameters:nil];
	}
}

- (BOOL)switchToJoystickDrive
{
    [miniPopoverController dismissPopoverAnimated:YES];
    miniPopoverController = nil;
    [self.view.superview setUserInteractionEnabled:YES];
    
    
    
    joystickUsed = YES;
    if(joystickUsed && tiltUsed && rcDriveUsed) [RKAchievement recordEvent:@"allDriveModesUsed"];
    
    // Check to see if we're currently on joystick drive
    if( (driveController != nil) && ([driveController getDriveType] == DriveTypeJoystick) ) {
        if( ![self presentTutorialIfRequired] )
        {
            [driveController resumeDriving];
        }
        return NO;
    }
	
	[self analyticsEndTimedDriveTypeEvent];
    
    [DriveAppSettings defaultSettings].driveType = DriveTypeJoystick;
	[FlurryAPI logEvent:@"JoystickDrive" timed:YES];
    
    return YES;
}

- (BOOL)switchToTiltDrive
{
    [miniPopoverController dismissPopoverAnimated:YES];
    miniPopoverController = nil;
    [self.view.superview setUserInteractionEnabled:YES];
    
    tiltUsed = YES;
    if(joystickUsed && tiltUsed && rcDriveUsed) [RKAchievement recordEvent:@"allDriveModesUsed"];
    
    // Check to see if we're currently on tilt drive
    if( [driveController getDriveType] == DriveTypeTilt ) {
        if( ![self presentTutorialIfRequired] )
            [driveController resumeDriving];
        return NO;
    }
	
	[self analyticsEndTimedDriveTypeEvent];
    
    [RKAchievement recordEvent:@"tiltDriveMode"];
    
    [DriveAppSettings defaultSettings].driveType = DriveTypeTilt;
	[FlurryAPI logEvent:@"TiltDrive" timed:YES];
    
    id<DriveController> oldController = driveController;
    driveController = [[TiltDriveViewController alloc] initWithDriveController:&driveControl delegate:self];
    UIView* controlsView = [driveController controlsView];
    controlsView.frame = self.view.bounds;
    
    [self transitionDriveControlsFromOldController:oldController];
    if( oldController == nil ) {
        // If we're initializing to the tilt controller then we
        // have to tell it to start driving in a second after the
        // UI has been correctly oriented.
        [self performSelector:@selector(resumeDriving) withObject:nil afterDelay:0.5];
    }
    
    return YES;
}

- (BOOL)switchToRCDrive
{
    [miniPopoverController dismissPopoverAnimated:YES];
    miniPopoverController = nil;
    [self.view.superview setUserInteractionEnabled:YES];
    
    [RKAchievement recordEvent:@"rcDriveModeSelect"];
    
    rcDriveUsed = YES;
    if(joystickUsed && tiltUsed && rcDriveUsed) [RKAchievement recordEvent:@"allDriveModesUsed"];
    
    // Check to see if we're currently on RC drive
    if( [driveController getDriveType] == DriveTypeRC ) {
        if( ![self presentTutorialIfRequired] )
            [driveController resumeDriving]; 
        return NO;
    }
	
	[self analyticsEndTimedDriveTypeEvent];
    
    [DriveAppSettings defaultSettings].driveType = DriveTypeRC;
	[FlurryAPI logEvent:@"RCDrive" timed:YES];
    
    return YES;
}

- (void)tutorialDismissed:(id)sender
{
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    DriveAppDriveType next = settings.driveType;
    
    [driveController tutorialDidDismiss];
    settings.mainTutorial = NO;
    BOOL showingTutorialForDriveType = NO;    
    switch( settings.driveType )
    {
        case DriveTypeJoystick:
            settings.joystickTutorial = NO;
            next = DriveTypeTilt;
            //showingTutorialForDriveType = settings.tiltTutorial;
            break;
        case DriveTypeRC:
            settings.rcTutorial = NO;
            next = DriveTypeJoystick;
            //showingTutorialForDriveType = settings.joystickTutorial;
            break;
        case DriveTypeTilt:
            settings.tiltTutorial = NO;
            next = DriveTypeRC;
            //showingTutorialForDriveType = settings.rcTutorial;
            break;
    }
    
    if( showingTutorialForDriveType ) {
        // Show the drive-type switch popup, highlight the next
        // drive type we're switching to and do the switch.
        [self presentDriveTypeMenu:driveTypeButton.frame 
                         direction:UIPopoverArrowDirectionUp
                         highlight:next
                             delay:0.5];
    } else {
        //if( !initialCalibration )
        //    [self presentCalibrationView];
        //else
            [self resumeDriving];
    }
}

- (void)presentTutorial
{
    TutorialViewController* tvc = [[TutorialViewController alloc] 
                                   initWithNibName:nil bundle:nil];
    [tvc setDismissedTarget:self action:@selector(tutorialDismissed:)];
	[self pauseDriving];
    [driveController prepareForTutorial];
	[self presentModalLayerViewController:tvc animated:YES];
	[tvc release];
}

- (void)resumeDriving
{
    //if( !driveControl.driving )
        [driveController resumeDriving];
}

- (void)pauseDriving
{
    // Disallow the robot to drive
    [driveControl stopDriving];
    [driveController updateUIForZeroSpeed];
}

- (void)doBoost
{
	[FlurryAPI logEvent:@"Boost"];
    [RKAchievement recordEvent:@"driveBoost"];

    DriveAppSettings *settings = [DriveAppSettings defaultSettings];
    driveControl.robotControl.controlledBoostVelocity = settings.controlledBoostVelocity;
    driveControl.robotControl.boostTimeScale = settings.boostTime;
    
    [driveControl.robotControl startControlledBoost];
    
    AudioServicesPlaySystemSound (kSystemSoundID_Vibrate);
}

- (void)boostUncontrolled
{
 	[FlurryAPI logEvent:@"Boost"];
    [RKAchievement recordEvent:@"driveBoost"];

    driveControl.robotControl.boostTimeScale = 1.0;
    [driveControl.robotControl jump];
    
    AudioServicesPlaySystemSound (kSystemSoundID_Vibrate);
}

- (void)setRobotLEDValues
{
	DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
    [RKRGBLEDOutputCommand sendCommandWithRed:rgb.red green:rgb.green blue:rgb.blue];
}

#pragma mark -
#pragma mark UI

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
	DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
	[colorpicker_controller setRed:rgb.red green:rgb.green blue:rgb.blue];
	colorpicker_controller.delegate = self;
	[colorpicker_controller setDismissedTarget:self action:@selector(colorPickerDismissed:)];
	[self pauseDriving];
	[self presentModalLayerViewController:colorpicker_controller animated:YES];
	[colorpicker_controller release];
}

- (void)colorPickerDidChange:(UIViewController *)controller withRed:(CGFloat)r 
					   green:(CGFloat)g blue:(CGFloat)b
{
    [RKRGBLEDOutputCommand sendCommandWithRed:r green:g blue:b];

	// Set the color in the our color view.
	[colorIndicatorView updateRed:r green:g blue:b];
}

- (void)colorPickerDidFinish:(UIViewController *)controller withRed:(CGFloat)r 
					   green:(CGFloat)g blue:(CGFloat)b
{
	DriveAppSettingsRGB settings = {r, g, b};
	[DriveAppSettings defaultSettings].robotLEDBrightness = settings;

	// Set the color in the our color view.
	[colorIndicatorView updateRed:r green:g blue:b];
}

- (void)colorPickerDismissed:(id)sender
{
	[self resumeDriving];
	//if( !initialCalibration && (driveControl.robotControl != nil) )
	//	[self presentCalibrationView];
}

- (BOOL)canBecomeFirstResponder {
    return YES; // need for capturing motion events
}

- (void)motionBegan:(UIEventSubtype)motion withEvent:(UIEvent *)event
{
	if( driveControl.driving ) {
		if (motion == UIEventSubtypeMotionShake) {
            // Do not boost on shake anymore.
			// [self doBoost];
		}
	}
}

- (void)presentColorPickerViewForDriveController
{
    [self presentColorPickerView];
}

- (void)presentCalibrationViewForDriveController
{
    /*if( ![RUIModalLayerViewController currentModalLayerViewController]) {
        [FlurryAPI logEvent:@"Calibrated"];
        [self presentCalibrationView];
    }*/
}

- (void)presentCalibrationView
{
    // Don't show the calibration view if we don't have a robot to calibrate.
    if( [RUIModalLayerViewController currentModalLayerViewController] != nil )
        return;
    
	NSBundle* RUIResourcesBundle = [DriveAppDelegate getRobotUIKitResourcesBundle];
    calibrationController = 
    [[RUICalibrationViewController alloc] initWithNibName:@"RUICalibrationViewController"
                                                   bundle:RUIResourcesBundle];
    calibrationController.robotControl = driveControl.robotControl;
	[calibrationController setDismissedTarget:self action:@selector(calibrationDismissed:)];
	[self pauseDriving];
    [self presentModalLayerViewController:calibrationController animated:YES];
    [calibrationController release];
	initialCalibration = YES;
}

- (void)setupGyroSteering
{
    // Setup for changes in yaw
    if( motionManager == nil) {
        motionManager = [[CMMotionManager alloc] init];
        [motionManager setDeviceMotionUpdateInterval:0.3];
    }
    //NSLog(@"Checking for gyro");
    if([motionManager isGyroAvailable]) { //verify that this device has a gyro
        //NSLog(@"Gyro available, starting updates");
        calibratedYaw = motionManager.deviceMotion.attitude.yaw;
        NSOperationQueue *queue = [[NSOperationQueue alloc] init];
		
        [motionManager startDeviceMotionUpdatesToQueue:queue
										   withHandler:^(CMDeviceMotion *motion, NSError *error){
                                               double angle_change = 0;
                                               double new_yaw = motion.attitude.yaw;
											   
                                               if (new_yaw >= calibratedYaw) {
                                                   angle_change = new_yaw - calibratedYaw;
                                               } else {
                                                   angle_change = new_yaw + (2 * M_PI - calibratedYaw);
                                               }
                                               // Create a transform matrix
                                               driveControl.robotControl.driveAlgorithm.correctionAngle = angle_change;
                                           }];
        [queue release];
    } else if([CLLocationManager headingAvailable]) { //if no gyro check for magnetometer
        //NSLog(@"Heading available, starting updates");
        calibratedYaw = -1.0;
        driveControl.robotControl.driveAlgorithm.correctionAngle = 0.0;
        if(!locationManager) {
            locationManager = [[CLLocationManager alloc] init];
            locationManager.delegate = self;
            [locationManager startUpdatingHeading];
        }
    }    
}

- (void)calibrationDismissed:(id)sender
{
	calibrationController = nil;
	if( [DriveAppSettings defaultSettings].gyroSteering ) {
        [self setupGyroSteering];
	}
	[self resumeDriving];
}

- (void)handleSettingsChanges:(NSNotification*)notification {
    NSString* name = (NSString*)[[notification userInfo] objectForKey:DriveAppSettingName];
    if( [name isEqualToString:@"gyroSteering"] ) {
        DriveAppSettings* settings = [DriveAppSettings defaultSettings];
        if( settings.gyroSteering ) {
            // Turn on gyro steering
            [self setupGyroSteering];
        } else {
            // Turn off gyro steering
            [motionManager stopDeviceMotionUpdates];            
        }
    } else if( [name isEqualToString:@"sensitivityLevel"] ) {
        DriveAppSettings* settings = [DriveAppSettings defaultSettings];
        driveControl.velocityScale = settings.velocityScale;
        boostTime = Clamp(settings.boostTime, 0, 0.99);
        controlledBoostVelocity = settings.controlledBoostVelocity;
        [RKRotationRateCommand sendCommandWithRate:settings.rotationRate];        
    }
}

- (void)handleRobotDidLossControl:(NSNotification *)notification
{
    NSLog(@"handleRobotDidLossControl");
    if (!robotInitialized) return;

	if( calibrationController != nil ) {
		[calibrationController dismissModalLayerViewControllerAnimated:YES];
	}
	    
	initialCalibration = NO;
	//robotInitialized = NO;
	[driveController updateUIForZeroSpeed];
	
	[self performSelector:@selector(showLostControlAlert) withObject:nil afterDelay:8.0];
}

-(void)showLostControlAlert {
    NSLog(@"showLostControlAlert called");
    if([[RKRobotProvider sharedRobotProvider] isRobotUnderControl]) return;
    NSLog(@"showLostControlAlertDisplayed");
    UIAlertView* disconnectAlert = [[UIAlertView alloc]
									initWithTitle:NSLocalizedString(@"Lost Connection to Sphero", @"Drive lost connection")
									message:NSLocalizedString(@"The connection with the Sphero has been lost. Go to the bluetooth settings to connect to a Sphero", @"Drive lost connection instructions")
									delegate:self
									cancelButtonTitle:NSLocalizedString(@"OK", @"OK button")
									otherButtonTitles:nil];
    lostConnectionAlert = disconnectAlert;
	[disconnectAlert show];
	[disconnectAlert release];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        if( driveController != nil )return [driveController shouldAutorotateToInterfaceOrientation:interfaceOrientation];
        return (UIInterfaceOrientationIsLandscape(interfaceOrientation));
    } else {
        return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
    }
}

- (void)initializeRobot
{
    NSLog(@"Initializing Robot");
	if( robotInitialized )
		return;
    
    driveControl = [RKDriveControl sharedDriveControl];
    [driveController initializeDriveControl];
    
	if ([driveControl.robotProvider isRobotUnderControl]) {
        robotInitialized = YES;
        [driveControl.robotProvider openRobotConnection];
	} else {
        [self performSelector:@selector(showNoSpheroAlert) withObject:nil afterDelay:1.0];
        robotInitialized = YES;
        noSpheroAlerted = YES;
    }
}

-(void)handleDidGainControl:(NSNotification*)notification {
    NSLog(@"DriveViewController handleDidGainControl");
    if(!robotInitialized) return;
    [NoSpheroAlertManager dismissAlert];
    if(lostConnectionAlert) [lostConnectionAlert dismissWithClickedButtonIndex:[lostConnectionAlert cancelButtonIndex] animated:NO];
    [[RKRobotProvider sharedRobotProvider] openRobotConnection];
}

- (void)handleConnectionOnline:(NSNotification *)notification
{
    NSLog(@"Handle connection online");
    noSpheroAlerted = NO;
    robotInitialized = YES;
    
    // Send the saved rotation rate to the ball
    DriveAppSettings *app_settings = [DriveAppSettings defaultSettings];
    [RKRotationRateCommand sendCommandWithRate:app_settings.rotationRate];
    
    [[DriveAppSettings defaultSettings] setRobotConnected:YES];
    
	[NoSpheroAlertManager dismissAlert];
    
    RKRobotProvider *robot_provider = [RKRobotProvider sharedRobotProvider];
    [FlurryAPI setUserID:robot_provider.robotControl.robot.bluetoothAddress];
	
    [self setRobotLEDValues];
    driveControl.robotControl.boostTimeScale = boostTime;
    [self becomeFirstResponder];
    
    // setup the robot for driving.
    [self resumeDriving];
    
    if([[DriveAppSettings defaultSettings] showCalibrateTutorial]) {
        [self showCalibrationTutorial];
    } else {
        [self showColorCalllout];
    }
    
	    
    if (!initialCalibration && (driveControl.robotControl != nil) && 
		([RUIModalLayerViewController currentModalLayerViewController] == nil)) {
        // Do the initial calibration
        //[self presentCalibrationView];
    }
}

- (void)handleConnectionOffline:(NSNotification *)notification
{
    NSLog(@"Drive View Conroller handleConnectionOffline");
    [self showNoSpheroAlert];
}

- (void)showNoSpheroAlert 
{
    NSLog(@"showNoSpheroAlertCalled");
    if( noSpheroAlert != nil ) return;
    if([[RKRobotProvider sharedRobotProvider] isRobotUnderControl]) return;
    [NoSpheroAlertManager showAlertWithType:(NoSpheroAlertManagerType)[[DriveAppSettings defaultSettings] hasRobotConnected]];
    
}

- (void)sensitivityLongPress:(UIGestureRecognizer*)recognizer {
    NSLog(@"SensitivityLongPress: should be implemented in subsclass");
}

#pragma mark - Tutorial Related

-(void)showCalibrationTutorial {
    
    if(calibrationTutorialController) return;  //Do nothing if we are already presenting the calibration tutorial for some reason
    if( miniPopoverController != nil ) {
        [miniPopoverController dismissPopoverAnimated:YES];
        miniPopoverController = nil;
    }
    calibrationTutorialController = [[CalibrationTutorialViewController alloc] initWithNibName:@"CalibrationTutorialViewController" bundle:nil];
    calibrationTutorialController.delegate = self;
    calibrationTutorialController.view.frame = self.view.frame;
    [self.view addSubview:calibrationTutorialController.view];
    [calibrationTutorialController viewDidAppear:NO];
    [self pauseDriving];
}

-(void)calibrationTutorialFinished {
    [calibrationTutorialController.view removeFromSuperview];
    [calibrationTutorialController release];
    calibrationTutorialController = nil;
    [self showColorCalllout];
    [self resumeDriving];
    [self setRobotLEDValues];
}

-(void)calibrationTutorialFinishedDontShowAgain {
    [[DriveAppSettings defaultSettings] setShowCalibrateTutorial:NO];
    [calibrationTutorialController.view removeFromSuperview];
    [calibrationTutorialController release];
    calibrationTutorialController = nil;
    [self showColorCalllout];
    [self resumeDriving];
    [self setRobotLEDValues];
}

-(void)showColorCalllout {
    if(calloutTouchHandlerView) return;
    if([[DriveAppSettings defaultSettings] colorCalloutShown] && ![[DriveAppSettings defaultSettings] alwaysShowCallouts]) {
        [self performSelector:@selector(showSpeedCallout) withObject:nil afterDelay:60];
        return;
    }
    calloutTouchHandlerView = [[UIView alloc] initWithFrame:self.view.bounds];
    calloutTouchHandlerView.backgroundColor = [UIColor clearColor];
    calloutTouchHandlerView.userInteractionEnabled = YES;
    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideCallout:)];
    [calloutTouchHandlerView addGestureRecognizer:tapRecognizer];
    [tapRecognizer release];
    
    UIView *view = [driveController colorIndicatorView];
    if([driveController getDriveType]== DriveTypeJoystick || [driveController getDriveType] == DriveTypeRC) view = [driveController controlsView];
    [self.view insertSubview:calloutTouchHandlerView belowSubview:view];
    
    [UIView animateWithDuration:0.4 
                          delay:0.0
                        options:UIViewAnimationCurveEaseInOut 
                     animations:^{
                        colorCallout.alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         
                     }];
    [calloutTouchHandlerView release];
}

-(void)showSpeedCallout {
    if(calloutTouchHandlerView) {
        [self performSelector:@selector(showSpeedCallout) withObject:nil afterDelay:60];
        return;
    }
    if([[DriveAppSettings defaultSettings] speedCalloutShown] && ![[DriveAppSettings defaultSettings] alwaysShowCallouts]) {
        [self performSelector:@selector(showDriveTypeCallout) withObject:nil afterDelay:60];
        return;
    }
    
    calloutTouchHandlerView = [[UIView alloc] initWithFrame:self.view.bounds];
    calloutTouchHandlerView.backgroundColor = [UIColor clearColor];
    calloutTouchHandlerView.userInteractionEnabled = YES;
    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideCallout:)];
    [calloutTouchHandlerView addGestureRecognizer:tapRecognizer];
    [tapRecognizer release];
    [self.view insertSubview:calloutTouchHandlerView belowSubview:[driveController controlsView]];
    [UIView animateWithDuration:0.4 
                          delay:0.0
                        options:UIViewAnimationCurveEaseInOut 
                     animations:^{
                         speedCallout.alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         
                     }];
    [calloutTouchHandlerView release];
    
}

-(void)showDriveTypeCallout {
    if(calloutTouchHandlerView) {
        [self performSelector:@selector(showDriveTypeCallout) withObject:nil afterDelay:60];
        return;
    }
    if([[DriveAppSettings defaultSettings] driveTypeCalloutShown] && ![[DriveAppSettings defaultSettings] alwaysShowCallouts]) return;
    
    calloutTouchHandlerView = [[UIView alloc] initWithFrame:self.view.bounds];
    calloutTouchHandlerView.backgroundColor = [UIColor clearColor];
    calloutTouchHandlerView.userInteractionEnabled = YES;
    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(hideCallout:)];
    [calloutTouchHandlerView addGestureRecognizer:tapRecognizer];
    [tapRecognizer release];    
    [self.view insertSubview:calloutTouchHandlerView belowSubview:[driveController controlsView]];
    [UIView animateWithDuration:0.4 
                          delay:0.0
                        options:UIViewAnimationCurveEaseInOut 
                     animations:^{
                         driveTypeCallout.alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         
                     }];
    
    [calloutTouchHandlerView release];
    
}

-(void)hideCallout:(UIGestureRecognizer*)sender {
    [self hideCallout];
}

-(void)hideCallout {
    [calloutTouchHandlerView removeFromSuperview];
    calloutTouchHandlerView = nil;
    
    if(colorCallout.alpha > 0.0) {
        [self performSelector:@selector(showSpeedCallout) withObject:nil afterDelay:60];
    }
    if(speedCallout.alpha > 0.0) {
        [self performSelector:@selector(showDriveTypeCallout) withObject:nil afterDelay:60];
    }
    
    [UIView animateWithDuration:0.4 
                          delay:0.0
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut) 
                     animations:^{
                         colorCallout.alpha = 0.0;
                         speedCallout.alpha = 0.0;
                         driveTypeCallout.alpha = 0.0;
                     }
                     completion:^(BOOL finished) {
                         
                     }];
    
    
    
}


#pragma mark -
#pragma mark Achievement Related

-(void)achievementEarned:(NSNotification*)sender {
    NSLog(@"Achievement earned notification");
    RKAchievement *achievement = [sender.userInfo objectForKey:RKAchievementEarnedAchievementKey];
    [self performSelector:@selector(showAchievementNotification:) withObject:achievement];
}

-(void)showAchievementNotification:(RKAchievement*)achievement {
    if(achievementNotificationOnScreen) {
        [self performSelector:@selector(showAchievementNotification:) withObject:achievement afterDelay:2.0];
        return;
    }
    achievementNotificationOnScreen = YES;
    [self performSelector:@selector(achievementNotificationGone) withObject:nil afterDelay:6.0];
    RUIAchievementEarnedViewController *controller = [[RUIAchievementEarnedViewController alloc] initWithNibName:@"RUIAchievementEarnedViewController" bundle:[DriveAppDelegate getRobotUIKitResourcesBundle]];
    [controller setAchievement:achievement];
    controller.view.frame = CGRectMake(0, 0, controller.view.frame.size.width, self.view.frame.size.height);
    controller.view.center = self.view.center;
    [controller viewWillAppear:YES];
    [self.view addSubview:controller.view];
    [controller viewDidAppear:YES];
    [controller release];
    [[SpheroDropDownAlertSound sharedSound] play];
}

-(void)achievementNotificationGone {
    achievementNotificationOnScreen = NO;
}

-(void)achievementDriveTime:(NSNotification*)sender {
    int time = [[sender.userInfo objectForKey:RKDriveStatValueKey] intValue];
    [RKAchievement recordEvent:@"driveTimeGeneric" withCount:time];
    
    if([driveController class] == [JoystickDriveViewController class] || [driveController class] == [JoystickDriveViewController_iPad class]) {
        [RKAchievement recordEvent:@"driveTimeJoystick" withCount:time];
    } else if([driveController class] == [TiltDriveViewController class]) {
        [RKAchievement recordEvent:@"driveTimeTilt" withCount:time];
    } else if([driveController class] == [RCDriveViewController class] || [driveController class] == [RCDriveViewController_iPad class]) {
        [RKAchievement recordEvent:@"driveTimeRC" withCount:time];     
    }
}


-(void)achievementDriveDistance:(NSNotification*)sender {
    int distance = [[sender.userInfo objectForKey:RKDriveStatValueKey] intValue];
    [RKAchievement recordEvent:@"driveDistanceTotal" withCount:distance];
    
    if([driveController class] == [JoystickDriveViewController class] || [driveController class] == [JoystickDriveViewController_iPad class]) {
        [RKAchievement recordEvent:@"driveDistanceJoystickTotal" withCount:distance];
    } else if([driveController class] == [TiltDriveViewController class]) {
        [RKAchievement recordEvent:@"driveDistanceRCTotal" withCount:distance];
    } else if([driveController class] == [RCDriveViewController class] || [driveController class] == [RCDriveViewController_iPad class]) {
        [RKAchievement recordEvent:@"driveDistanceTiltTotal" withCount:distance];     
    }
}

-(void)achievementColorChanged:(NSNotification*)sender {
    [RKAchievement recordEvent:@"driveColorChange"];
}

#pragma mark -
#pragma mark Delegate Methods

- (void)alertViewCancel:(UIAlertView*)av {
	if( av == noSpheroAlert ) {
		[noSpheroAlert release];
		noSpheroAlert = nil;
	} else if(av == lostConnectionAlert) {
        lostConnectionAlert = nil;
    }
    [self resumeDriving];
}

- (void)alertView:(UIAlertView*)av didDismissWithButtonIndex:(NSInteger)buttonIndex {
	if( av == noSpheroAlert ) {
		[noSpheroAlert release];
		noSpheroAlert = nil;
	} else if (av == firmwareUpdateAlert) {
        [firmwareUpdateAlert release];
        firmwareUpdateAlert = nil;
        if (buttonIndex == 1) {
            [self presentFirmwareUploadViewController];
        }
    } else if(av == lostConnectionAlert) {
        lostConnectionAlert = nil;
    }
}


#pragma mark -
#pragma mark View Lifecycle

-(void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
	
	// Set ourselves up to get notified when the application changes
	// active status.
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(appDidBecomeActive:)
												 name:UIApplicationDidBecomeActiveNotification
											   object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(appWillResignActive:)
												 name:UIApplicationWillResignActiveNotification
											   object:nil];
	// Get notification in case we lose control of the robot.
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleRobotDidLossControl:)
                                                 name:RKRobotDidLossControlNotification
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKRobotDidGainControlNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDidGainControl:) name:RKRobotDidGainControlNotification object:nil];

    
    // Get notification when the user changes settings.
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleSettingsChanges:)
                                                 name:DriveAppSettingsDidChangeNotification
                                               object:nil];	
    
    // set drive parameters that can change
	DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    driveControl.velocityScale = settings.velocityScale; 
    boostTime = Clamp(settings.boostTime, 0.0, 0.99);
    controlledBoostVelocity = settings.controlledBoostVelocity;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	[self initializeRobot];
	
	// Setup our initial driving controls
    switch( [DriveAppSettings defaultSettings].driveType ) {
        case DriveTypeTilt:
            [self switchToTiltDrive];
            break;
        case DriveTypeRC:
            [self switchToRCDrive];
            break;
        case DriveTypeJoystick:
        default:
            [self switchToJoystickDrive];
            break;
    }
}


- (void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
	
	[motionManager stopDeviceMotionUpdates];
    [driveControl stopDriving];    

    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:RKRobotDidLossControlNotification
                                                  object:nil];
	[[NSNotificationCenter defaultCenter] removeObserver:self 
												 name:UIApplicationDidBecomeActiveNotification
											   object:nil];
	[[NSNotificationCenter defaultCenter] removeObserver:self
													name:UIApplicationWillResignActiveNotification
												  object:nil];

	robotInitialized = NO;
}

-(BOOL)calibrateGestureHandlerShouldAllowCalibration:(RUICalibrateGestureHandler*)sender {
    return YES;
}


-(void)calibrateGestureHandlerBegan:(RUICalibrateGestureHandler*)sender {
    [self pauseDriving];
    [driveControl.robotControl startCalibration];
    [[CalibrateOverlayStartSound sharedSound] play];
    [self performSelector:@selector(startCalibrateLoopSound) withObject:nil afterDelay:0.3];
}

-(void)startCalibrateLoopSound {
    if([RUICalibrateGestureHandler isCalibrating]) [[[CalibrateOverlayLoop sharedSound] player] play];
}

-(void)calibrateGestureHandlerEnded:(RUICalibrateGestureHandler*)sender {
    [RKAchievement recordEvent:@"2fingerRotate"];
    [FlurryAPI logEvent:@"Calibration"];
    [[[CalibrateOverlayLoop sharedSound] player] stop];
    [[CalibrateOverlayEnd sharedSound] play];
    [driveControl.robotControl stopCalibrated:YES];
    driveControl.robotControl.driveAlgorithm.angle = 0.0;
    [self resumeDriving];
}

- (void) viewDidLoad
{
    [super viewDidLoad];
    initialCalibration = NO;
    achievementNotificationOnScreen = NO;
    driveControl = [RKDriveControl sharedDriveControl];
    
    
	// We'll handle alerting the driver if we lose robot control.
	driveControl.showsRobotLostControlAlert = NO;
    
	yawCorrection = CGAffineTransformIdentity;
	
	robotInitialized = NO;
    
    calibrateGestureHandler = [[RUICalibrateGestureHandler alloc] initWithView:self.view];
    calibrateGestureHandler.delegate = self;
    
    UILongPressGestureRecognizer *longPressRecognizer = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(sensitivityLongPress:)];
    [sensitivityButton addGestureRecognizer:longPressRecognizer];
    [longPressRecognizer release];
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(achievementEarned:) name:RKAchievementEarnedNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(achievementDriveTime:) name:RKDriveTimeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(achievementDriveDistance:) name:RKDriveDistanceNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(achievementColorChanged:) name:RKDriveColorChangeNotification object:nil];

}

- (void) viewDidUnload
{
    [super viewDidUnload];
	
	[self analyticsEndTimedDriveTypeEvent];
	
	self.menuButton = nil;
    self.driveTypeButton = nil;
    self.sensitivityButton = nil;
    self.calibrationButton = nil;
    
    [driveController release];
    driveController = nil;
}

#pragma mark -
#pragma mark Object Lifecycle

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle: nibBundleOrNil];
    if( self != nil )
    {
        noSpheroAlerted = NO;
        joystickUsed = tiltUsed = rcDriveUsed = NO;
    }
    return self;
}

- (void)appDidBecomeActive:(NSNotification*)notification
{
    // Watch for online notification to start driving
    joystickUsed = tiltUsed = rcDriveUsed = NO;
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(handleConnectionOnline:)
                                                 name:RKDeviceConnectionOnlineNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKRobotDidGainControlNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDidGainControl:) name:RKRobotDidGainControlNotification object:nil];
     [[NSNotificationCenter defaultCenter] addObserver:self 
                                              selector:@selector(handleConnectionOffline:)
                                                  name:RKDeviceConnectionOfflineNotification
                                                object:nil];
    [driveControl setup];

	[self initializeRobot];    
    [self resumeDriving];
}

- (void)appWillResignActive:(NSNotification*)notification
{
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:RKDeviceConnectionOnlineNotification
                                                  object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKRobotDidLossControlNotification object:nil];

    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:RKDeviceConnectionOfflineNotification
                                                  object:nil];
    
    if(calibrationTutorialController) {
        [self calibrationTutorialFinished];
    }
    
	if( calibrationController != nil ) {
		[calibrationController done];
	}
    
    [driveControl tearDown];
    
	initialCalibration = NO;
	robotInitialized = NO;
	[motionManager stopDeviceMotionUpdates];
    [driveControl stopDriving];
    [driveController updateUIForZeroSpeed];
    
    [NoSpheroAlertManager dismissAlert];
    
    [[RKRobotProvider sharedRobotProvider] closeRobotConnection];

    noSpheroAlerted = NO;
}

- (void) dealloc
{
	[menuButton release]; menuButton = nil;
    [driveTypeButton release]; driveTypeButton = nil;
    [sensitivityButton release]; sensitivityButton = nil;
    [calibrationButton release]; calibrationButton = nil;
    
    [driveController release];
    driveController = nil;
	
	[motionManager release]; motionManager = nil;
    
	[locationManager stopUpdatingHeading]; 
    [locationManager release]; 
    locationManager = nil;
    
    [super dealloc];
}

#pragma mark -
#pragma mark CLLocationManager Delegate Methods

- (void)locationManager:(CLLocationManager *)manager didUpdateHeading:(CLHeading *)newHeading {
	//NSLog(@"didUpdateHeading: %@", newHeading);
	if(newHeading.headingAccuracy < 0.0 || newHeading.headingAccuracy > 30.0) return; //Ignore if data is innacurate
	
	if(calibratedYaw==-1.0) { //If this is our first magnetic reading assume it is the starting point
		calibratedYaw = newHeading.magneticHeading;
	} else { //Adjust heading
		double angleChange = 0.0;
		
		if(newHeading.magneticHeading < calibratedYaw) {
			angleChange = calibratedYaw - newHeading.magneticHeading;
		} else if(newHeading.magneticHeading > calibratedYaw) {
			angleChange = newHeading.magneticHeading - calibratedYaw;
		}
		angleChange = angleChange / 180.0 * M_PI;
		driveControl.robotControl.driveAlgorithm.correctionAngle = -angleChange;
	}
}

- (BOOL)locationManagerShouldDisplayHeadingCalibration:(CLLocationManager *)manager {
	return YES;
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
	NSLog(@"locationManager didFailWithError: %@", error);
}

@end
