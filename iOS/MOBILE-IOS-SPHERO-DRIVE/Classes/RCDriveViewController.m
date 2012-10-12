//
//  RCDriveViewController.m
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "RCDriveViewController.h"
#import "DriveControllerDelegate.h"
#import "DriveAppSettings.h"
#import "RCDriveAlgorithm.h"

#import <RobotKit/RobotKit.h>
#import <RobotKit/RKRobotControl.h>
#import <RobotUIKit/RUIColorIndicatorView.h>

@interface RCDriveViewController ()

- (void)robotControlLoop;

@end


@implementation RCDriveViewController

@synthesize backgroundView;
@synthesize controlView;
@synthesize boostImageView;
@synthesize colorView;
@synthesize speedContainerView;
@synthesize speedController;
@synthesize turnContainerView;
@synthesize turnController;

@synthesize driveControl;
@synthesize delegate;

- (id)initWithDriveController:(RKDriveControl **)dc 
                     delegate:(id<DriveControllerDelegate>)d
{
    self = [super initWithNibName:nil bundle:nil];
    if (self) {
        driveControl = dc;
        delegate = d;
        speedValue = 0.0;
        turnValue = 0.0;
        boostOn = NO;
    }
    return self;
}

- (void)dealloc
{
    [backgroundView release]; backgroundView = nil;
    [controlView release]; controlView = nil;
    [boostImageView release]; boostImageView = nil;
    [colorView release]; colorView = nil;
    [speedContainerView release]; speedContainerView = nil;
    [speedController release]; speedController = nil;
    [turnContainerView release]; turnContainerView = nil;
    [turnController release]; turnController = nil;
    
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - DriveController implementation

- (DriveAppDriveType) getDriveType
{
    return DriveTypeRC;
}

- (void)resumeDriving
{
    [(*driveControl) stopDriving];
    [self initializeDriveControl];
    [(*driveControl) startDriving:RKDriveControlUserDefined];
    [self robotControlLoop];
}

- (void)updateUIForZeroSpeed
{
    CGRect speed_bounds = self.speedContainerView.bounds;
    CGSize control_size = self.speedController.frame.size;
    CGPoint speed_center = CGPointMake(CGRectGetMidX(speed_bounds),
                                       CGRectGetMaxY(speed_bounds) - (control_size.height / 2.0));
    self.speedController.center = speed_center;
    
    CGRect turn_bounds = self.turnContainerView.bounds;
    CGPoint turn_center = CGPointMake(CGRectGetMidX(turn_bounds), 
                                      CGRectGetMidY(turn_bounds));
    self.turnController.center = turn_center;
    
    boostImageView.hidden = YES;
    
    speedValue = 0.0;
    turnValue = 0.0;
    boostOn = NO;
}

- (void)initializeDriveControl
{
    RCDriveAlgorithm* rcDrive = [[RCDriveAlgorithm alloc] init];
    rcDrive.velocityScale = (*driveControl).velocityScale;
    (*driveControl).robotControl.driveAlgorithm = rcDrive;
    [rcDrive release];
    (*driveControl).driveTarget = self;
    (*driveControl).driveConversionAction = @selector(updateMotionIndicator:);
}

- (void)updateMotionIndicator:(RKDriveAlgorithm*)driveAlgorithm
{
    // Do nothing for now:
    return;
}

- (void)handleColorTap:(UIGestureRecognizer*)recognizer
{
    if( recognizer.state == UIGestureRecognizerStateEnded )
        [delegate presentColorPickerViewForDriveController];
}

- (void)handleBackgroundPress:(UIGestureRecognizer*)recognizer
{
    if( recognizer.state == UIGestureRecognizerStateBegan) 
        [delegate presentCalibrationViewForDriveController];
}

- (UIView*)controlsView
{
    return self.view;
}

- (UIView*)colorIndicatorView
{
    return self.colorView;
}

- (void)prepareForTutorial
{
}

- (void)tutorialDidDismiss
{
}

#pragma mark - User Interaction

- (void)boostOn
{
    boostOn = YES;
    
    // Turn on the boost indicator in the UI
    boostImageView.hidden = NO;
    
    [delegate doBoost];
}

- (void)boostOff
{
    boostOn = NO;
    
    // Turn off the boost indicator in the UI
    boostImageView.hidden = YES;
}

- (void)handleSpeedMotion:(UIGestureRecognizer*)recognizer
{
    if( !(*driveControl).driving ) return;
    
    [delegate hideCallout];
    
    UIPanGestureRecognizer* pan_recognizer = (UIPanGestureRecognizer*)recognizer;
    CGRect parent_bounds = self.speedContainerView.bounds;
    CGSize control_size = self.speedController.frame.size;
    CGFloat half_height = control_size.height / 2.0;
    CGPoint origin = CGPointMake(CGRectGetMidX(parent_bounds),
                                 CGRectGetMaxY(parent_bounds) -
                                 half_height);
    
    if( pan_recognizer.state == UIGestureRecognizerStateBegan ) {
        // do nothing;
    } else if( pan_recognizer.state == UIGestureRecognizerStateChanged ) {
        CGPoint translate = [pan_recognizer translationInView:self.speedContainerView];
        CGPoint drag_point = origin;
        drag_point.y += translate.y;
        drag_point.y = Clamp(drag_point.y, CGRectGetMinY(parent_bounds) + half_height,
                             CGRectGetMaxY(parent_bounds) - half_height);
        
        // Check for boost switching
        if( boostOn ) {
            drag_point.x = CGRectGetMaxX(parent_bounds);
            // Look for the user to turn boost off
            if( translate.x < (parent_bounds.size.width / 6.0) ) {
                drag_point.x = origin.x;
                [self boostOff];
            }
        } else {
            if( translate.x > ((parent_bounds.size.width / 2.0) - (parent_bounds.size.width / 6.0)) ) {
                drag_point.x = CGRectGetMaxX(parent_bounds);
                [self boostOn];
            }
        }
        self.speedController.center = drag_point;
        CGFloat yChange = origin.y - drag_point.y;
        speedValue = Clamp(yChange / (parent_bounds.size.height - control_size.height), 0.0, 1.0); 
    } else if( pan_recognizer.state == UIGestureRecognizerStateEnded ) {
        speedValue = 0.0;
        self.speedController.center = origin;
        [self boostOff];
    }
}

- (void)handleTurnMotion:(UIGestureRecognizer*)recognizer
{
    if( !(*driveControl).driving ) return;
    
    [delegate hideCallout];
    
    UIPanGestureRecognizer* pan_recognizer = (UIPanGestureRecognizer*)recognizer;
    CGRect parent_bounds = self.turnContainerView.bounds;
    CGPoint origin = CGPointMake(CGRectGetMidX(parent_bounds),
                                 CGRectGetMidY(parent_bounds));
    
    if( pan_recognizer.state == UIGestureRecognizerStateBegan ) {
        // do nothing;
    } else if( pan_recognizer.state == UIGestureRecognizerStateChanged ) {
        CGPoint translate = [pan_recognizer translationInView:self.turnContainerView];
        CGPoint drag_point = origin;
        drag_point.x += translate.x;
        drag_point.x = Clamp(drag_point.x, CGRectGetMinX(parent_bounds),
                             CGRectGetMaxX(parent_bounds));
        self.turnController.center = drag_point;
        turnValue = Clamp((translate.x / (parent_bounds.size.width / 2.0)), -1.0, 1.0); 
    } else if( pan_recognizer.state == UIGestureRecognizerStateEnded ) {
        turnValue = 0.0;
        self.turnController.center = origin;
    }
}

- (void)robotControlLoop
{
    if( !(*driveControl).driving ) return;
	[(*driveControl).robotControl driveWithCoord1:turnValue coord2:speedValue coord3:0.0];
    [self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.2];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    [self updateUIForZeroSpeed];

    // Setup our gesture recognizer to allow the user to control speed.
    UIPanGestureRecognizer* panGesture =
    [[UIPanGestureRecognizer alloc] initWithTarget:self 
                                            action:@selector(handleSpeedMotion:)];
    [self.speedController addGestureRecognizer:panGesture];
    [panGesture release];
    
    // Setup our gesture recognizer to allow the user to control turning.
    panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self
                                                         action:@selector(handleTurnMotion:)];
    [self.turnController addGestureRecognizer:panGesture];
    [panGesture release];
    
	// Setup a tap on the color indicator to bring up the color picker.
	UITapGestureRecognizer* colortap = [[UITapGestureRecognizer alloc] 
										initWithTarget:self action:@selector(handleColorTap:)];
	colortap.numberOfTapsRequired = 1;
	[self.colorView addGestureRecognizer:colortap];
	[colortap release];
	
	[colorView autoUpdateColor];

    // Setup a long press on the background to bring up the calibration view.
    UILongPressGestureRecognizer* longPress = [[UILongPressGestureRecognizer alloc]
                                               initWithTarget:self action:@selector(handleBackgroundPress:)];
    longPress.minimumPressDuration = 1.0;
    [self.backgroundView addGestureRecognizer:longPress];
    [longPress release];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
	DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
	[colorView updateRed:rgb.red green:rgb.green blue:rgb.blue];
    
    [self initializeDriveControl];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    [self resumeDriving];
    [self robotControlLoop];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [NSObject cancelPreviousPerformRequestsWithTarget:self
                                             selector:@selector(robotControlLoop)
                                               object:nil];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    
    self.backgroundView = nil;
    self.controlView = nil;
    self.boostImageView = nil;
    self.colorView = nil;
    self.speedContainerView = nil;
    self.speedController = nil;
    self.turnContainerView = nil;
    self.turnController = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if([RKRollCommand currentVelocity] > 0.0) return NO;
    if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        return (UIInterfaceOrientationIsLandscape(interfaceOrientation));
    } else {
        return (UIInterfaceOrientationIsLandscape(interfaceOrientation));
    }
}

@end
