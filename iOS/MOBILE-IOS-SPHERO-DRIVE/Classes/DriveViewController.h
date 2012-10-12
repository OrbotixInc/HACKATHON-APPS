//
//  DriveViewController.h
//  Drive
//
//  Created by Brian Smith on 11/19/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreMotion/CoreMotion.h>
#import <CoreLocation/CoreLocation.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RUIColorPickerDelegate.h>
#import <RobotUIKit/RobotUIKit.h>
#import "WEPopoverController.h"
#import "DriveControllerDelegate.h"
#import "CalibrationTutorialViewController.h"
#import "RKCalibrateOverlayView.h"

@class RKDriveControl;
@class RUICalibrationViewController;
@class RUIColorIndicatorView;
@protocol DriveController;

@interface DriveViewController : UIViewController 
								 <UIAlertViewDelegate, 
								  RUIColorPickerDelegate,
                                  CLLocationManagerDelegate,
                                  WEPopoverControllerDelegate,
                                  DriveControllerDelegate,
                                  CalibrationTutorialViewControllerDelegate,
                                  RUICalibrateGestureHandlerProtocol>
{
    UIButton                      *menuButton;
    UIButton                      *driveTypeButton;
    UIButton                      *sensitivityButton;
    UIButton                      *calibrationButton;
    id<DriveController>           driveController;

    RKDriveControl                *driveControl;
	
    BOOL                          initialCalibration;
	BOOL                          robotInitialized;
    BOOL                          noSpheroAlerted;
	UIAlertView                   *noSpheroAlert;
    UIAlertView                   *firmwareUpdateAlert;
    
	RUICalibrationViewController  *calibrationController;    
    WEPopoverController           *miniPopoverController;
    RUIColorIndicatorView         *colorIndicatorView;
    float						  boostTime;
    float                         controlledBoostVelocity;
	CMMotionManager				  *motionManager;
	CLLocationManager			  *locationManager;
	double						  calibratedYaw;
	CGAffineTransform			  yawCorrection;
    
    //Tracking for achievements
    BOOL                          joystickUsed, tiltUsed, rcDriveUsed;
    BOOL                          achievementNotificationOnScreen;
    
    CalibrationTutorialViewController *calibrationTutorialController; 
    
    IBOutlet UIImageView          *colorCallout, *speedCallout, *driveTypeCallout;
    UIView                        *calloutTouchHandlerView;
    
    RKCalibrateOverlayView        *calibrateOverlayRings;
    RUICalibrateGestureHandler    *calibrateGestureHandler;
    
    UIAlertView *lostConnectionAlert;
}

@property (nonatomic, retain) IBOutlet UIButton	*menuButton;
@property (nonatomic, retain) IBOutlet UIButton *driveTypeButton;
@property (nonatomic, retain) IBOutlet UIButton *sensitivityButton;
@property (nonatomic, retain) IBOutlet UIButton *calibrationButton;

- (IBAction)menuButtonUp;
- (IBAction)driveTypeButtonUp;
- (IBAction)calibrationButtonUp;
- (IBAction)sensitivityButtonUp;

- (void)pauseDriving;
- (void)resumeDriving;

- (void)calibrationDismissed:(id)sender;

- (BOOL)switchToRCDrive;
- (BOOL)switchToTiltDrive;
- (BOOL)switchToJoystickDrive;

- (void)dismissSensitivityPopup;
- (void)sensitivityLongPress:(UIGestureRecognizer*)recognizer;

- (void)appDidBecomeActive:(NSNotification*)notification;

- (void)transitionDriveControlsFromOldController:(id <DriveController>)oldController;

-(void)showCalibrationTutorial;
-(void)calibrationTutorialFinished;
-(void)calibrationTutorialFinishedDontShowAgain;
-(void)showColorCalllout;
-(void)showSpeedCallout;
-(void)showDriveTypeCallout;
-(void)hideCallout;

@end
