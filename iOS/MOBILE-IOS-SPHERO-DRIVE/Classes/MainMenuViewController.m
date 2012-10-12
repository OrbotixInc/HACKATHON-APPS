//
//  MainMenuViewController.m
//  Drive
//
//  Created by Brian Smith on 11/18/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import "MainMenuViewController.h"
#import "SensitivityViewController.h"
#import "InfoViewcontroller.h"
#import "DriveAppDelegate.h"
#import "DriveAppSettings.h"

#import <QuartzCore/QuartzCore.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "FlurryAPI.h"
#import "SpheroItemSelectSound.h"
#import "UserGuideViewController.h"

#define SLEEP_ALERT_BOX    100

@interface MainMenuViewController ()

- (void)handleRobotDidLossControl:(NSNotification *)notification;
- (void)appDidBecomeActive:(NSNotification*)notification;
- (void)appWillResignActive:(NSNotification*)notification;

@end

@implementation MainMenuViewController

@synthesize exitView;
@synthesize popoverController;
@synthesize chosenAction;
@synthesize nameEditView;
@synthesize nameLabel;
@synthesize delegate;

- (void) close
{
    [[SpheroItemSelectSound sharedSound] play];
	CATransition* transition = [CATransition animation];
	transition.duration = 0.5;
	transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
	transition.type = kCATransitionReveal;
	[self.navigationController.view.layer addAnimation:transition forKey:nil];
	[self.navigationController popViewControllerAnimated:NO];
}

- (void) tutorial
{
    [[SpheroItemSelectSound sharedSound] play];
    [FlurryAPI logEvent:@"Menu-TutorialPressed"];
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    settings.mainTutorial = YES;
    settings.joystickTutorial = (settings.driveType == DriveTypeJoystick);
    settings.rcTutorial = (settings.driveType == DriveTypeRC);
    settings.tiltTutorial = (settings.driveType == DriveTypeTilt);
    
    if( self.popoverController != nil ) {
        self.chosenAction = @selector(showCalibrationTutorial);
        [self.popoverController dismissPopoverAnimated:YES];
        [self.popoverController.delegate popoverControllerDidDismissPopover:self.popoverController];
        return;
    }
    
    [delegate performSelector:@selector(showCalibrationTutorial) withObject:nil afterDelay:0.2];
    
    [self close];
}

- (void) settings
{
    [[SpheroItemSelectSound sharedSound] play];
	[FlurryAPI logEvent:@"Menu-SettingsPressed"];
    if( self.popoverController != nil ) {
        SensitivityViewController* sensitivity_controller = [[SensitivityViewController alloc]
															 initWithNibName:nil bundle:nil];
		sensitivity_controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
        [sensitivity_controller hideRollButton];
		[self.navigationController pushViewController:sensitivity_controller animated:YES];
		[sensitivity_controller release];
		
        return;
    }
	SensitivityViewController* sensitivity_controller = [[SensitivityViewController alloc]
														 initWithNibName:nil bundle:nil];
    [self.navigationController pushViewController:sensitivity_controller animated:YES];
	//[self presentModalLayerViewController:sensitivity_controller animated:YES];
	[sensitivity_controller release];
}

- (void) leaderboard
{
    [[SpheroItemSelectSound sharedSound] play];
    [FlurryAPI logEvent:@"SpheroWorldPressed"];
    //Using the leaderboard button to present SpheroWorld oAuth for now
    if(self.popoverController != nil) {
        RKSpheroWorldAuth *auth = [[RKSpheroWorldAuth alloc] init];
        auth.delegate = self;
        [self.navigationController pushViewController:auth animated:YES];
        [auth release];
        return;

    }
    
    RKSpheroWorldAuth *auth = [[RKSpheroWorldAuth alloc] init];
    auth.delegate = self;
    [self presentModalViewController:auth animated:YES];
    [auth release];
    return;
}

- (IBAction)userGuide:(id)sender {
    [[SpheroItemSelectSound sharedSound] play];
    
    if(self.popoverController != nil) {
        UserGuideViewController *guide = [[UserGuideViewController alloc] initWithNibName:@"UserGuideViewController" bundle:nil];
        guide.delegate = self;
        [self.navigationController pushViewController:guide animated:YES];
        [guide release];
        return;
        
    }
    
    UserGuideViewController *guide = [[UserGuideViewController alloc] initWithNibName:@"UserGuideViewController" bundle:nil];
    guide.delegate = self;
    [self presentModalViewController:guide animated:YES];
    [guide release];
}

- (void) info
{
    [[SpheroItemSelectSound sharedSound] play];
	[FlurryAPI logEvent:@"Menu-InfoPressed"];
    if( self.popoverController != nil ) {
        InfoViewController* info_controller =
		[[InfoViewController alloc] initWithNibName:nil bundle:nil];
		[self.navigationController pushViewController:info_controller animated:YES];
		[info_controller release];
        return;
    }
	InfoViewController* info_controller =
	[[InfoViewController alloc] initWithNibName:nil bundle:nil];
    [self.navigationController pushViewController:info_controller animated:YES];
	//[self presentModalLayerViewController:info_controller animated:YES];
	[info_controller release];
}

- (void)startNameEdit:(UIGestureRecognizer*)gesture
{
    if( gesture.state == UIGestureRecognizerStateEnded )
    {
        RKDriveControl* driveControl = [[RKDriveControl class] sharedDriveControl];
        if( driveControl.robotControl != nil ) {
            nameEditView.text = nameLabel.text;
            nameLabel.hidden = YES;
            nameEditView.hidden = NO;
            
            [nameEditView becomeFirstResponder];
        }
    }
}

- (void)nameFieldDidEndEdit:(UITextField *)textField
{
    [nameEditView resignFirstResponder];
    
    [FlurryAPI logEvent:@"Menu-SpheroNameChanged"];
    
    nameLabel.text = nameEditView.text;
    nameEditView.hidden = YES;
    nameLabel.hidden = NO;
    
    // Change the robot's name to that in the text field.
	RKDriveControl* driveControl = [[RKDriveControl class] sharedDriveControl];
	if( driveControl.robotControl != nil ) {
        driveControl.robotControl.robot.name = nameLabel.text;
    }
}

- (void)sleep:(id)sender
{
    [[SpheroItemSelectSound sharedSound] play];
    [FlurryAPI logEvent:@"Menu-SleepPressed"];
    [self dismissModalViewControllerAnimated:NO];
    RUISlideToSleepViewController *controller = [[RUISlideToSleepViewController alloc] initWithNibName:@"RUISlideToSleepViewController" bundle:[DriveAppDelegate getRobotUIKitResourcesBundle]];
    [controller loadView];
    [controller viewDidLoad];
    [self presentModalLayerViewController:controller animated:YES];
    [controller release];
}

- (void)handleRobotDidLossControl:(NSNotification *)notification
{
    connection = nil;
}

- (void)appDidBecomeActive:(NSNotification*)notification
{
	if ([[RKRobotProvider sharedRobotProvider] isRobotUnderControl]) {
        [[RKRobotProvider sharedRobotProvider] openRobotConnection];
	} 
}

- (void)appWillResignActive:(NSNotification*)notification
{
	connection = nil;
    [self.navigationController popToRootViewControllerAnimated:NO];
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

- (void)hideRollButton
{
    hideRollButton = YES;
    rollButton.hidden = YES;
    rollLabel.hidden = YES;
}

- (void)viewDidLoad
{
    // Do not allow the user to change the name in the Drive app.
    /*
    UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc] 
                                   initWithTarget:self action:@selector(startNameEdit:)];
    [nameLabel addGestureRecognizer:tap];
    [tap release];
    */
    UIFont* font12 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:12];
    for( UIView* view in self.view.subviews ) {
        if( ![view isKindOfClass:[UILabel class]] )
            continue;
        
        UILabel* label = (UILabel*)view;
        if( label.tag == 100 ) {
            [label setFont:font12];
        }
    }
}

- (void)viewWillAppear:(BOOL)animated
{
    rollButton.hidden = hideRollButton;
    rollLabel.hidden = hideRollButton;
    
	[super viewWillAppear:animated];

	RKDriveControl* driveControl = [[RKDriveControl class] sharedDriveControl];
    connection = driveControl.robotControl.deviceConnection;
    nameLabel.text = driveControl.robotControl.robot.name;
	
	
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(appWillResignActive:)
												 name:UIApplicationWillResignActiveNotification
											   object:nil];	
    
    if( popoverController != nil )
    {
        self.exitView.hidden = YES;
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    
	[[NSNotificationCenter defaultCenter] removeObserver:self
													name:UIApplicationWillResignActiveNotification
												  object:nil];	
}

- (void) dealloc
{
    [exitView release]; exitView = nil;
    [popoverController release]; popoverController = nil;
    [nameEditView release]; nameEditView = nil;
    [nameLabel release]; nameLabel = nil;
    
    [super dealloc];
}

@end
