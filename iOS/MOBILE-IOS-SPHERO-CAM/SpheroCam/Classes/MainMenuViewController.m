//
//  MainMenuViewController.m
//  Drive
//
//  Created by Brian Smith on 11/18/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import "MainMenuViewController.h"
#import "SensitivityViewController.h"
//#import "InfoViewcontroller.h"
//#import "DriveAppDelegate.h"
#import "DriveAppSettings.h"

#import <QuartzCore/QuartzCore.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "FlurryAnalytics.h"
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
@synthesize chosenAction;
@synthesize nameEditView;
@synthesize nameLabel;
@synthesize popover;
@synthesize delegate;

- (void) close
{
    [[SpheroItemSelectSound sharedSound] play];
    if(popover) {
        [popover dismissPopoverAnimated:YES];
    } else {
        [self dismissModalViewControllerAnimated:YES];
    }
}

- (void) settings
{
    [[SpheroItemSelectSound sharedSound] play];
	[FlurryAnalytics logEvent:@"Menu-SettingsPressed"];
    
	SensitivityViewController* sensitivity_controller = [[SensitivityViewController alloc]
														 initWithNibName:nil bundle:nil];
    sensitivity_controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        [sensitivity_controller hideRollButton];
    }
    [self.navigationController pushViewController:sensitivity_controller animated:YES];
	//[self presentModalViewController:sensitivity_controller animated:YES];
	[sensitivity_controller release];
}

- (void) leaderboard
{
    [[SpheroItemSelectSound sharedSound] play];
    //Using the leaderboard button to present SpheroWorld oAuth for now
    [FlurryAnalytics logEvent:@"SpheroWorldPressed"];
    [self.popover dismissPopoverAnimated:YES];
    RKSpheroWorldAuth *auth = [[RKSpheroWorldAuth alloc] init];
    auth.delegate = delegate;
    [delegate presentModalViewController:auth animated:YES];
    [auth release];
    return;
}

- (IBAction)tutorial {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Sharing" message:@"Go to the Photos app on your device to view and share the photos and video you have created with SpheroCam" delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
    [alert release];
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
    
    [FlurryAnalytics logEvent:@"Menu-SpheroNameChanged"];
    
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
    [FlurryAnalytics logEvent:@"Menu-SleepPressed"];
    NSString* rootpath = [[NSBundle mainBundle] bundlePath];
    NSString* ruirespath = [NSBundle pathForResource:@"RobotUIKit"
                                              ofType:@"bundle"
                                         inDirectory:rootpath];
    RUISlideToSleepViewController *controller = [[RUISlideToSleepViewController alloc] initWithNibName:@"RUISlideToSleepViewController" bundle:[NSBundle bundleWithPath:ruirespath]];
    [controller loadView];
    [controller viewDidLoad];
    [self presentModalLayerViewController:controller animated:YES];
    [controller release];
    
}

-(IBAction)colorPressed:(id)sender {
    [[SpheroItemSelectSound sharedSound] play];
    [FlurryAnalytics logEvent:@"Menu-ColorPressed"];
    NSString* rootpath = [[NSBundle mainBundle] bundlePath];
    NSString* ruirespath = [NSBundle pathForResource:@"RobotUIKit"
                                              ofType:@"bundle"
                                         inDirectory:rootpath];
    RUIColorPickerViewController *controller = [[RUIColorPickerViewController alloc] initWithNibName:@"RUIColorPickerViewController" bundle:[NSBundle bundleWithPath:ruirespath]];
    controller.delegate = self;
    [controller showBackButton:YES];
    controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
    
    [controller setBackButtonTarget:self action:@selector(colorPickerBackPressed:)];
    DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
    [controller setRed:rgb.red green:rgb.green blue:rgb.blue];
    
    [self.navigationController pushViewController:controller animated:YES];
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        [controller showRollButton:NO];
    }
    [controller release];
}

-(void) colorPickerDidChange:(UIViewController*)controller 
					 withRed:(CGFloat)r green:(CGFloat)g blue:(CGFloat)b {
    [RKRGBLEDOutputCommand sendCommandWithRed:r green:g blue:b];
}

/*!
 * This method is called when the user has indicated that they are done with the
 * color-picker and the current color is the one that should be used.
 *
 * @param controller The color-picker view controller sending the message.
 * @param r The red component of the current color, between 0.0 and 1.0
 * @param g The green component of the current color, between 0.0 and 1.0
 * @param b The blue component of the current color, between 0.0 and 1.0
 */
-(void) colorPickerDidFinish:(UIViewController*)controller 
					 withRed:(CGFloat)r green:(CGFloat)g blue:(CGFloat)b {
    DriveAppSettingsRGB settings = {r, g, b};
	[DriveAppSettings defaultSettings].robotLEDBrightness = settings;
    [self dismissModalViewControllerAnimated:YES];
}

-(void)colorPickerBackPressed:(RUIColorPickerViewController*)sender {
    [self.navigationController popViewControllerAnimated:YES];
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

-(IBAction)userGuide:(id)sender {
    [[SpheroItemSelectSound sharedSound] play];
    UserGuideViewController *guide = [[UserGuideViewController alloc] initWithNibName:@"UserGuideViewController" bundle:nil];
    guide.delegate = self;
    [self.navigationController pushViewController:guide animated:YES];
    [guide release];
}

- (void)appWillResignActive:(NSNotification*)notification
{
	connection = nil;
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
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        rollLabel.alpha = 0.0;
        rollButton.alpha = 0.0;
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
	
	// Get notification in case the robot loses control.
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handleRobotDidLossControl:)
                                                 name:RKRobotDidLossControlNotification
                                               object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(appDidBecomeActive:)
												 name:UIApplicationDidBecomeActiveNotification
											   object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self
											 selector:@selector(appWillResignActive:)
												 name:UIApplicationWillResignActiveNotification
											   object:nil];	
    
    
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:RKRobotDidLossControlNotification
                                                  object:nil];
	[[NSNotificationCenter defaultCenter] removeObserver:self 
													name:UIApplicationDidBecomeActiveNotification
												  object:nil];
	[[NSNotificationCenter defaultCenter] removeObserver:self
													name:UIApplicationWillResignActiveNotification
												  object:nil];	
}

- (void) dealloc
{
    [exitView release]; exitView = nil;
    [nameEditView release]; nameEditView = nil;
    [nameLabel release]; nameLabel = nil;
    
    [super dealloc];
}

@end
