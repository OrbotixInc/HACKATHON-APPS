//
//  JoystickDriveViewController.m
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "JoystickDriveViewController.h"
#import "DriveControllerDelegate.h"
#import "DriveAppSettings.h"

#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RUIColorIndicatorView.h>


@implementation JoystickDriveViewController

@synthesize backgroundView;
@synthesize controlView;
@synthesize joystickView;
@synthesize driveControlPadView;
@synthesize colorView;

@synthesize driveControl;

- (id)initWithDriveController:(RKDriveControl **)dc 
                     delegate:(id<DriveControllerDelegate>)d
{
    [super initWithDelegate:d];
    
    if (self) {
        driveControl = dc;
    }
    return self;
}

- (void)dealloc
{
    [backgroundView release]; backgroundView = nil;
    [controlView release]; controlView = nil;
    [joystickView release]; joystickView = nil;
    [driveControlPadView release]; driveControlPadView = nil;
    [colorView release]; colorView = nil;
    
    [super dealloc];
}




#pragma mark - DriveController implementation

- (DriveAppDriveType) getDriveType
{
    return DriveTypeJoystick;
}

- (void)resumeDriving
{
    [(*driveControl) stopDriving];
    [self initializeDriveControl];
    [(*driveControl) startDriving:RKDriveControlJoyStick];
}

- (void)handleJoystickMotion:(id)sender
{
    if (!(*driveControl).driving) return; // do nothing with full stop set
    
    [self.delegate hideCallout];
    
    UIPanGestureRecognizer *pan_recognizer = (UIPanGestureRecognizer *)sender;
    CGRect parent_bounds = self.driveControlPadView.bounds;
    CGPoint parent_center = [self.driveControlPadView 
                             convertPoint:self.driveControlPadView.center
                             fromView:self.driveControlPadView.superview];
    
    if (pan_recognizer.state == UIGestureRecognizerStateEnded || pan_recognizer.state == UIGestureRecognizerStateCancelled || pan_recognizer.state == UIGestureRecognizerStateFailed || pan_recognizer.state == UIGestureRecognizerStateBegan) {
        ballMoving = NO;
        [(*driveControl).robotControl stopMoving];
        self.joystickView.center = parent_center;
    } else if (pan_recognizer.state == UIGestureRecognizerStateChanged) {
        ballMoving = YES;
        CGPoint translate = [pan_recognizer translationInView:self.driveControlPadView];
        CGPoint drag_point = parent_center;
        drag_point.x += translate.x;
        drag_point.y += translate.y;
        drag_point.x = Clamp(drag_point.x, CGRectGetMinX(parent_bounds),
                             CGRectGetMaxX(parent_bounds));
        drag_point.y = Clamp(drag_point.y, CGRectGetMinY(parent_bounds),
                             CGRectGetMaxY(parent_bounds));
        [(*driveControl) driveWithJoyStickPosition:drag_point];        
    } else if (pan_recognizer.state == UIGestureRecognizerStateBegan) {
		// Do nothing
    }
}

- (void)updateUIForZeroSpeed
{
    CGPoint parent_center = 
    [self.driveControlPadView convertPoint:self.driveControlPadView.center
                                  fromView:self.driveControlPadView.superview];
    self.joystickView.center = parent_center;
}

- (void)initializeDriveControl
{
    (*driveControl).joyStickSize = self.driveControlPadView.bounds.size;
    (*driveControl).driveTarget = self;
    (*driveControl).driveConversionAction = @selector(updateMotionIndicator:);
}

- (void)updateMotionIndicator:(RKDriveAlgorithm*)driveAlgorithm
{
    if (driveAlgorithm.velocityScale < 0.01) return;
    if ( !(*driveControl).driving || !ballMoving ) return;
    
    CGRect bounds = self.driveControlPadView.bounds;
    
    double velocity = driveAlgorithm.velocity/driveAlgorithm.velocityScale;
	double angle = driveAlgorithm.angle + (driveAlgorithm.correctionAngle * 180.0/M_PI);
	if (angle > 360.0) {
		angle -= 360.0;
	}
    
    double x = ((CGRectGetMaxX(bounds) - CGRectGetMinX(bounds))/2.0) *
    (1.0 + velocity * sin(angle * M_PI/180.0));
    double y = ((CGRectGetMaxY(bounds) - CGRectGetMinY(bounds))/2.0) *
    (1.0 - velocity * cos(angle * M_PI/180.0));
	
    CGPoint center = CGPointMake(floor(x), floor(y));
    
    [UIView setAnimationsEnabled:NO];
    self.joystickView.center = center;   
    [UIView setAnimationsEnabled:YES];
}

- (void)handleColorTap:(UIGestureRecognizer*)recognizer
{
    if( recognizer.state == UIGestureRecognizerStateEnded )
        [self.delegate presentColorPickerViewForDriveController];
}

- (void)handleBackgroundPress:(UIGestureRecognizer*)recognizer
{
    if( recognizer.state == UIGestureRecognizerStateBegan) 
        [self.delegate presentCalibrationViewForDriveController];
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

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];

    // Setup our gesture recognizer to allow the user to move the joystick.
    UIPanGestureRecognizer* panGesture =
    [[UIPanGestureRecognizer alloc] initWithTarget:self 
                                            action:@selector(handleJoystickMotion:)];
    [self.joystickView addGestureRecognizer:panGesture];
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
}

- (void)viewDidUnload
{
    [super viewDidUnload];

    self.backgroundView = nil;
    self.controlView = nil;
    self.joystickView = nil;
    self.driveControlPadView = nil;
    self.colorView = nil;
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
