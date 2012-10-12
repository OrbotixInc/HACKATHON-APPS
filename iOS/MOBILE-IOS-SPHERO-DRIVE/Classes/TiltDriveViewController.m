//
//  TiltDriveViewController.m
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "TiltDriveViewController.h"
#import "DriveControllerDelegate.h"
#import "DriveAppSettings.h"

#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RUIColorIndicatorView.h>

@interface TiltDriveViewController ()


@end


@implementation TiltDriveViewController

@synthesize backgroundView;
@synthesize controlView;
@synthesize puckView;
@synthesize driveControlPadView;
@synthesize colorView;

@synthesize driveControl;

- (id)initWithDriveController:(RKDriveControl **)dc delegate:(id<DriveControllerDelegate>)d
{
    self = [super initWithDelegate:d];
    if (self) {
        driveControl = dc;
    }
    return self;
}

- (void)dealloc
{
    [backgroundView release]; backgroundView = nil;
    [controlView release]; controlView = nil;
    [puckView release]; puckView = nil;
    [driveControlPadView release]; driveControlPadView = nil;
    [colorView release]; colorView = nil;

    [super dealloc];
}


#pragma mark - DriveController implementation

- (DriveAppDriveType) getDriveType
{
    return DriveTypeTilt;
}

- (void)resumeDriving
{
    [(*driveControl) stopDriving];
    [self initializeDriveControl];
    (*driveControl).tiltOrientation = [UIApplication sharedApplication].statusBarOrientation;
    [(*driveControl) startDriving:RKDriveControlTilt];
}

- (void)updateUIForZeroSpeed
{
    CGPoint parent_center = 
    [self.driveControlPadView convertPoint:self.driveControlPadView.center
                                  fromView:self.driveControlPadView.superview];
    self.puckView.center = parent_center;
}

- (void)initializeDriveControl
{
    (*driveControl).stopOffset = 10.0 * M_PI/180.0;
    (*driveControl).tiltOrientation = [UIApplication sharedApplication].statusBarOrientation;
    (*driveControl).driveTarget = self;
    (*driveControl).driveConversionAction = @selector(updateMotionIndicator:);
}

- (void)updateMotionIndicator:(RKDriveAlgorithm*)driveAlgorithm
{
    if (driveAlgorithm.velocityScale < 0.01) return;
    if ( !(*driveControl).driving ) return;
    
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
    self.puckView.center = center;   
    [UIView setAnimationsEnabled:YES];
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

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
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
    if( animated )
        [self resumeDriving];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    
    self.backgroundView = nil;
    self.controlView = nil;
    self.puckView = nil;
    self.driveControlPadView = nil;
    self.colorView = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Don't allow interface orientation changes while tilting or the interface
    // will rotate when you are going forward.
    if ( !(*driveControl).driving ) { 
        return UIInterfaceOrientationIsLandscape(interfaceOrientation);
    } else
        return false;
}

@end
