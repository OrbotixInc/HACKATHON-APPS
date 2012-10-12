/*
     File: AVCamViewController.m
 Abstract: A view controller that coordinates the transfer of information between the user interface and the capture manager.
  Version: 1.2
 
 Disclaimer: IMPORTANT:  This Apple software is supplied to you by Apple
 Inc. ("Apple") in consideration of your agreement to the following
 terms, and your use, installation, modification or redistribution of
 this Apple software constitutes acceptance of these terms.  If you do
 not agree with these terms, please do not use, install, modify or
 redistribute this Apple software.
 
 In consideration of your agreement to abide by the following terms, and
 subject to these terms, Apple grants you a personal, non-exclusive
 license, under Apple's copyrights in this original Apple software (the
 "Apple Software"), to use, reproduce, modify and redistribute the Apple
 Software, with or without modifications, in source and/or binary forms;
 provided that if you redistribute the Apple Software in its entirety and
 without modifications, you must retain this notice and the following
 text and disclaimers in all such redistributions of the Apple Software.
 Neither the name, trademarks, service marks or logos of Apple Inc. may
 be used to endorse or promote products derived from the Apple Software
 without specific prior written permission from Apple.  Except as
 expressly stated in this notice, no other rights or licenses, express or
 implied, are granted by Apple herein, including but not limited to any
 patent rights that may be infringed by your derivative works or by other
 works in which the Apple Software may be incorporated.
 
 The Apple Software is provided by Apple on an "AS IS" basis.  APPLE
 MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 FOR A PARTICULAR PURPOSE, REGARDING THE APPLE SOFTWARE OR ITS USE AND
 OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 
 IN NO EVENT SHALL APPLE BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 MODIFICATION AND/OR DISTRIBUTION OF THE APPLE SOFTWARE, HOWEVER CAUSED
 AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 STRICT LIABILITY OR OTHERWISE, EVEN IF APPLE HAS BEEN ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 
 Copyright (C) 2011 Apple Inc. All Rights Reserved.
 
 */

#import "AVCamViewController.h"
#import "AVCamCaptureManager.h"
#import "AVCamRecorder.h"
#import <AVFoundation/AVFoundation.h>
#import <RobotKit/RobotKit.h>
#import "MainMenuViewController.h"
#import "SpheroButtonPressSound.h"
#import "DriveAppSettings.h"
#import "SpheroCamRecordStop.h"
#import "SpheroCamRecordStart.h"
#import "NoSpheroAlertManager.h"
#import "FlurryAnalytics.h"
#import "CalibrateOverlayEnd.h"
#import "CalibrateOverlayLoop.h"
#import "CalibrateOverlayStartSound.h"

static void *AVCamFocusModeObserverContext = &AVCamFocusModeObserverContext;
static BOOL wheelResized = false;

@interface AVCamViewController () <UIGestureRecognizerDelegate>
@end

@interface AVCamViewController (InternalMethods)
- (CGPoint)convertToPointOfInterestFromViewCoordinates:(CGPoint)viewCoordinates;
- (void)tapToAutoFocus:(UIGestureRecognizer *)gestureRecognizer;
- (void)tapToContinouslyAutoFocus:(UIGestureRecognizer *)gestureRecognizer;
- (void)updateButtonStates;
@end

@interface AVCamViewController (AVCamCaptureManagerDelegate) <AVCamCaptureManagerDelegate>
@end

@implementation AVCamViewController

@synthesize captureManager;
@synthesize cameraToggleButton;
@synthesize recordButton;
@synthesize stillButton;
@synthesize focusModeLabel;
@synthesize videoPreviewView;
@synthesize captureVideoPreviewLayer;
@synthesize popover;

- (NSString *)stringForFocusMode:(AVCaptureFocusMode)focusMode
{
	NSString *focusString = @"";
	
	switch (focusMode) {
		case AVCaptureFocusModeLocked:
			focusString = @"locked";
			break;
		case AVCaptureFocusModeAutoFocus:
			focusString = @"auto";
			break;
		case AVCaptureFocusModeContinuousAutoFocus:
			focusString = @"continuous";
			break;
	}
	
	return focusString;
}

- (void)dealloc
{
    [self removeObserver:self forKeyPath:@"captureManager.videoInput.device.focusMode"];
	[captureManager release];
    [videoPreviewView release];
	[captureVideoPreviewLayer release];
    [cameraToggleButton release];
    [recordButton release];
    [stillButton release];	
	[focusModeLabel release];
	
    [super dealloc];
}

-(void)viewWillAppear:(BOOL)animated {
    
   
    // Send the saved rotation rate to the ball
    DriveAppSettings *app_settings = [DriveAppSettings defaultSettings];
    [RKRotationRateCommand sendCommandWithRate:app_settings.rotationRate];
    DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
    [RKRGBLEDOutputCommand sendCommandWithRed:rgb.red green:rgb.green blue:rgb.blue];
    [RKDriveControl sharedDriveControl].velocityScale = app_settings.velocityScale;
    [RKDriveControl sharedDriveControl].robotControl.driveAlgorithm.velocityScale = app_settings.velocityScale;
}

-(void)handleDidGainControl:(NSNotification*)notification {
    //NSLog(@"DriveViewController handleDidGainControl");
    if(!robotInitialized) return;
    [NoSpheroAlertManager dismissAlert];
    [[RKRobotProvider sharedRobotProvider] openRobotConnection];
}

-(void)handleConnectionOnline:(NSNotification*)notification {
    //This is the notificaiton we get when we find out the robot is online
    //Robot will not respond to commands until this notification is recieved
    //[self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.4];
    //NSLog(@"Robot Online");
    [NoSpheroAlertManager dismissAlert];
    [[DriveAppSettings defaultSettings] setRobotConnected:YES];
    
    // Send the saved rotation rate to the ball
    DriveAppSettings *app_settings = [DriveAppSettings defaultSettings];
    [RKRotationRateCommand sendCommandWithRate:app_settings.rotationRate];
    DriveAppSettingsRGB rgb = [DriveAppSettings defaultSettings].robotLEDBrightness;
    [RKRGBLEDOutputCommand sendCommandWithRed:rgb.red green:rgb.green blue:rgb.blue];
    [RKDriveControl sharedDriveControl].velocityScale = app_settings.velocityScale;
    
    //[RKRGBLEDOutputCommand sendCommandWithRed:0.0 green:0.9 blue:0.0];
    //[RKBackLEDOutputCommand sendCommandWithBrightness:1.0];
    
    [self.view addSubview:driveWheel];
    float xOffet = self.view.frame.size.height - (driveWheel.frame.size.width * 0.5);
    if([[NSUserDefaults standardUserDefaults] boolForKey:@"lefthanded"]) {
        xOffet = (driveWheel.frame.size.width * 0.5);
    }
    driveWheel.center = CGPointMake(xOffet , self.view.frame.size.width - (driveWheel.frame.size.height * 0.5));
    
    
    [RKDriveControl sharedDriveControl].joyStickSize = circularView.bounds.size;
    [RKDriveControl sharedDriveControl].driveTarget = self;
    [RKDriveControl sharedDriveControl].driveConversionAction = @selector(updateMotionIndicator:);
    [[RKDriveControl sharedDriveControl] startDriving:RKDriveControlJoyStick];
    [RKDriveControl sharedDriveControl].velocityScale = app_settings.velocityScale;
    
    // start processing the puck's movements
    UIPanGestureRecognizer* panGesture =
    [[UIPanGestureRecognizer alloc] initWithTarget:self 
                                            action:@selector(handleJoystickMotion:)];
    [drivePuck addGestureRecognizer:panGesture];
    [panGesture release];
}

- (void)handleConnectionOffline:(NSNotification *)notification
{
    NSLog(@"Drive View Conroller handleConnectionOffline");
}

- (void)handleJoystickMotion:(id)sender
{
    
    if (![RKDriveControl sharedDriveControl].driving) return;
    
    UIPanGestureRecognizer *pan_recognizer = (UIPanGestureRecognizer *)sender;
    CGRect parent_bounds = circularView.bounds;
    CGPoint parent_center = [circularView 
                             convertPoint:circularView.center
                             fromView:circularView.superview] ;
    
    if (pan_recognizer.state == UIGestureRecognizerStateEnded || pan_recognizer.state == UIGestureRecognizerStateCancelled || pan_recognizer.state == UIGestureRecognizerStateFailed || pan_recognizer.state == UIGestureRecognizerStateBegan) {
        ballMoving = NO;
        [[RKDriveControl sharedDriveControl].robotControl stopMoving];
        drivePuck.center = parent_center;
    } else if (pan_recognizer.state == UIGestureRecognizerStateChanged) {
        ballMoving = YES;
        CGPoint translate = [pan_recognizer translationInView:circularView];
        CGPoint drag_point = parent_center;
        drag_point.x += translate.x;
        drag_point.y += translate.y;
        drag_point.x = [self clampWithValue:drag_point.x min:CGRectGetMinX(parent_bounds) max:CGRectGetMaxX(parent_bounds)];
        drag_point.y = [self clampWithValue:drag_point.y min:CGRectGetMinY(parent_bounds) max:CGRectGetMaxY(parent_bounds)];
        [[RKDriveControl sharedDriveControl] driveWithJoyStickPosition:drag_point];        
    } else if (pan_recognizer.state == UIGestureRecognizerStateBegan) {
		// Do nothing
    }
}

- (void)updateMotionIndicator:(RKDriveAlgorithm*)driveAlgorithm {
    if (driveAlgorithm.velocityScale < 0.01) return;
    if ( ![RKDriveControl sharedDriveControl].driving || !ballMoving) return;
    
    CGRect bounds = circularView.bounds;
    
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
    drivePuck.center = center;   
    [UIView setAnimationsEnabled:YES];
}

- (float)clampWithValue:(float)value min:(float)min max:(float)max {
    if (value < min) {
        return min;
    } else if (value > max) {
        return max;
    } else {
        return value;
    }
}

-(IBAction)menuPressed:(id)sender {
    if(popover) {
        [self.popover dismissPopoverAnimated:YES];
        self.popover = nil;
    }
    [[SpheroButtonPressSound sharedSound] play];
    MainMenuViewController *controller = [[MainMenuViewController alloc] initWithNibName:@"MainMenuViewController" bundle:nil];
    controller.delegate = self;
    UINavigationController *navController = [[UINavigationController alloc] initWithRootViewController:controller];
    navController.navigationBarHidden = YES;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        [self presentModalViewController:navController animated:YES];
    } else {
        controller.contentSizeForViewInPopover = CGSizeMake(480, 320);
        navController.contentSizeForViewInPopover = CGSizeMake(480, 320);
        UIPopoverController *popoverController = [[UIPopoverController alloc] initWithContentViewController:navController];
        self.popover = popoverController;
        controller.popover = popoverController;
        [popover presentPopoverFromRect:menuButton.frame inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
        [popoverController release];
    }
    [controller release];
    [navController release];
}

- (BOOL)popoverControllerShouldDismissPopover:(UIPopoverController *)popoverController {
    return YES;
}

/* Called on the delegate when the user has taken action to dismiss the popover. This is not called when -dismissPopoverAnimated: is called directly.
 */
- (void)popoverControllerDidDismissPopover:(UIPopoverController *)popoverController {
    self.popover = nil;
}

-(BOOL)calibrateGestureHandlerShouldAllowCalibration:(RUICalibrateGestureHandler*)sender {
    return YES;
}


-(void)calibrateGestureHandlerBegan:(RUICalibrateGestureHandler*)sender {
    [[CalibrateOverlayStartSound sharedSound] play];
    [self performSelector:@selector(startCalibrateLoopSound) withObject:nil afterDelay:0.3];
}

-(void)startCalibrateLoopSound {
    if([RUICalibrateGestureHandler isCalibrating]) [[[CalibrateOverlayLoop sharedSound] player] play];
}

-(void)calibrateGestureHandlerEnded:(RUICalibrateGestureHandler*)sender {
    [RKAchievement recordEvent:@"2fingerRotate"];
    [FlurryAnalytics logEvent:@"Calibrated"];
    [[[CalibrateOverlayLoop sharedSound] player] stop];
    [[CalibrateOverlayEnd sharedSound] play];
    
}

- (void)viewDidLoad
{
    //NSLog(@"View did load");
    robotInitialized = NO;
    
    // Register default values for NSUserDefaults
	NSDictionary* defaults = [DriveAppSettings getPredefinedDefaults];
	[[NSUserDefaults standardUserDefaults] registerDefaults:defaults]; 
    
    heading = 0.0;
	speed = 0.0;
	calibrating = NO;
    rotating = NO;
	
	CGRect speedSliderRect = CGRectMake(30, 100, 23, 220);
	
	CGAffineTransform transform = CGAffineTransformMakeRotation(-M_PI_2);
	speedSlider.transform = transform;
	speedSlider.frame = speedSliderRect;
	
	UIImage *largeThumb = [UIImage imageNamed:@"Sphero-KittyCam-paw.png"];
    UIImage *pawLeft = [UIImage imageNamed:@"Sphero-KittyCam-pawLeft.png"];
	[speedSlider setThumbImage:pawLeft forState:UIControlStateNormal];
	[directionSlider setThumbImage:largeThumb forState:UIControlStateNormal];
    
    UIImage *leftImage = [UIImage imageNamed:@"Sphero-KittyCam-track-2.png"];
    leftImage = [leftImage stretchableImageWithLeftCapWidth:100 topCapHeight:10];
    [speedSlider setMinimumTrackImage:leftImage forState:UIControlStateNormal];
    [directionSlider setMinimumTrackImage:leftImage forState:UIControlStateNormal];
    
    UIImage *rightImage = [UIImage imageNamed:@"Sphero-KittyCam-track-2.png"];
    rightImage = [rightImage stretchableImageWithLeftCapWidth:100 topCapHeight:10];
    [speedSlider setMaximumTrackImage:rightImage forState:UIControlStateNormal];
    [directionSlider setMaximumTrackImage:rightImage forState:UIControlStateNormal];
    
    // Watch for online notification to start driving
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleConnectionOnline:) name:RKDeviceConnectionOnlineNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDidGainControl:) name:RKRobotDidGainControlNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(handleConnectionOffline:)
                                                 name:RKDeviceConnectionOfflineNotification
                                               object:nil];
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad && !wheelResized) {
        wheelResized = true;
        driveWheel.frame = CGRectMake(driveWheel.frame.origin.x, driveWheel.frame.origin.x, driveWheel.frame.size.width * 1.5, driveWheel.frame.size.height * 1.5);
        drivePuck.center = CGPointMake(driveWheel.frame.size.width * 0.5, driveWheel.frame.size.height * 0.5);
    }
    
    //Attempt to control the connected robot so we get the notification if one is connected
	RKRobotProvider *robot_provider = [RKRobotProvider sharedRobotProvider];
    //    robot_provider.autoOpenRobotConnection = NO;
    if ([robot_provider isRobotUnderControl]) {
        [robot_provider openRobotConnection];
    } else {
        [NoSpheroAlertManager showAlertWithType:(NoSpheroAlertManagerType)[[DriveAppSettings defaultSettings] hasRobotConnected]];
    }
    robotInitialized = YES;
    
    calibrateHandler = [[RUICalibrateGestureHandler alloc] initWithView:videoPreviewView];
    calibrateHandler.delegate = self;
    
	/*if (robotControl == nil) {
		
		
        // Get the connected robot and create a RKDeviceConnection for it.
        RKRobotProvider *robot_provider = [[RKRobotProvider alloc] init];
        if ([robot_provider.robots count] > 0) {
            // There is a robot connected
            RKRobot *robot = [robot_provider.robots objectAtIndex:0];
            robotControl = [[RKRobotControl alloc] initWithRobot:robot];
			[self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.2];
        } else {
            // show alert that the Sphero is not connected
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Sphero Not Connected"
                                                            message:@"Need to connect in settings"
                                                           delegate:nil
                                                  cancelButtonTitle:@"OK"
                                                  otherButtonTitles:nil];
            [alert show];
            [alert release];
			
			//[self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.2];
        }
    }*/
	
	//[[self cameraToggleButton] setTitle:NSLocalizedString(@"Camera", @"Toggle camera button title")];
    //[[self recordButton] setTitle:NSLocalizedString(@"Record", @"Toggle recording button record title")];
    //[[self stillButton] setTitle:NSLocalizedString(@"Photo", @"Capture still image button title")];
    
	if ([self captureManager] == nil) {
		AVCamCaptureManager *manager = [[AVCamCaptureManager alloc] init];
		[self setCaptureManager:manager];
		[manager release];
		
		[[self captureManager] setDelegate:self];

		if ([[self captureManager] setupSession]) {
            // Create video preview layer and add it to the UI
			AVCaptureVideoPreviewLayer *newCaptureVideoPreviewLayer = [[AVCaptureVideoPreviewLayer alloc] initWithSession:[[self captureManager] session]];
			//newCaptureVideoPreviewLayer.orientation = AVCaptureVideoOrientationLandscapeRight;
			UIView *view = [self videoPreviewView];
			CALayer *viewLayer = [view layer];
			[viewLayer setMasksToBounds:YES];
			
			//CGRect bounds = [view bounds];
            CGRect bounds = [[UIScreen mainScreen] bounds];
            bounds = CGRectMake(0, 0, bounds.size.height, bounds.size.width);
			//CGRect bounds = CGRectMake(0, 0, 480, 320);
			//NSLog(@"Bounds: %1.1f, %1.1f", bounds.size.width, bounds.size.height);
			[newCaptureVideoPreviewLayer setFrame:bounds];
			
			if ([newCaptureVideoPreviewLayer isOrientationSupported]) {
				[newCaptureVideoPreviewLayer setOrientation:AVCaptureVideoOrientationLandscapeRight];
			}
			
			[newCaptureVideoPreviewLayer setVideoGravity:AVLayerVideoGravityResizeAspectFill];
			
			[viewLayer insertSublayer:newCaptureVideoPreviewLayer below:[[viewLayer sublayers] objectAtIndex:0]];
			
			[self setCaptureVideoPreviewLayer:newCaptureVideoPreviewLayer];
            [newCaptureVideoPreviewLayer release];
			
            // Start the session. This is done asychronously since -startRunning doesn't return until the session is running.
			dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
				[[[self captureManager] session] startRunning];
			});
			
            [self updateButtonStates];
			
            // Create the focus mode UI overlay
			/*UILabel *newFocusModeLabel = [[UILabel alloc] initWithFrame:CGRectMake(10, 10, viewLayer.bounds.size.width - 20, 20)];
			[newFocusModeLabel setBackgroundColor:[UIColor clearColor]];
			[newFocusModeLabel setTextColor:[UIColor colorWithRed:1.0 green:1.0 blue:1.0 alpha:0.50]];
			AVCaptureFocusMode initialFocusMode = [[[captureManager videoInput] device] focusMode];
			[newFocusModeLabel setText:[NSString stringWithFormat:@"focus: %@", [self stringForFocusMode:initialFocusMode]]];
			[view addSubview:newFocusModeLabel];
			[self addObserver:self forKeyPath:@"captureManager.videoInput.device.focusMode" options:NSKeyValueObservingOptionNew context:AVCamFocusModeObserverContext];
			[self setFocusModeLabel:newFocusModeLabel];
            [newFocusModeLabel release];
             */
            
            // Add a single tap gesture to focus on the point tapped, then lock focus
			UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapToAutoFocus:)];
			[singleTap setDelegate:self];
			[singleTap setNumberOfTapsRequired:1];
			[view addGestureRecognizer:singleTap];
			
            // Add a double tap gesture to reset the focus mode to continuous auto focus
			UITapGestureRecognizer *doubleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapToContinouslyAutoFocus:)];
			[doubleTap setDelegate:self];
			[doubleTap setNumberOfTapsRequired:2];
			[singleTap requireGestureRecognizerToFail:doubleTap];
			[view addGestureRecognizer:doubleTap];
			
			[doubleTap release];
			[singleTap release];
		} else {
            NSLog(@"Error starting capture session");
        }
	}
		
    [super viewDidLoad];
}


-(void)robotControlLoop {
	
	
	speed = speedSlider.value*speedSlider.value;
	//NSLog(@"Setting speed to: %1.1f", speedSlider.value*speedSlider.value);
	
	//send new heading and speed to robot
	if(speed > 0.0) {
		if(calibrating) {
			//[robotControl stopCalibrated:YES];
			calibrating = NO;
		}
		
		//adjust heading
		float headingAdjustment = 25.0 * directionSlider.value;
		heading += headingAdjustment;
		if(heading < 0.0) heading += 360.0;
		if(heading >= 360.0) heading -= 360.0;
		
		//[robotControl rollAtHeading:heading velocity:speed];
        [RKRollCommand sendCommandWithHeading:heading velocity:speed];
	} else if(directionSlider.value != 0.0) {
		//[robotControl stopMoving];
		if(!calibrating) {
			//[robotControl startCalibration];
			calibrating = YES;
		}
		
		//adjust heading
		float headingAdjustment = 30.0 * directionSlider.value;
		heading += headingAdjustment;
		if(heading < 0.0) heading += 360.0;
		if(heading >= 360.0) heading -= 360.0;
		
		[RKRollCommand sendCommandWithHeading:heading velocity:0.0];
	} else {
		if(!calibrating) [RKRollCommand sendCommandWithHeading:heading velocity:0.0];
	}
	
	//keep the loop going
	[self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.2];
}

-(IBAction)sliderChanged:(id)sender {
	//Callback when a slider has changed
	/*
	 if(sender==speedSlider) {
	 //do nothing for now
	 } else if(sender==directionSlider) {
	 //do nothing for now
	 }*/
	
}

//Callback when a user stops touching a slider
-(IBAction)sliderTouchUp:(id)sender {
	if(sender==speedSlider) {
		[speedSlider setValue:0.0 animated:YES];
		speed = 0.0;
	} else if(sender==directionSlider) {
		[directionSlider setValue:0.0 animated:YES];
	}
}


// Override to allow orientations other than the default portrait orientation.
- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
// Return YES for supported orientations.
	return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}
 

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context
{
    if (context == AVCamFocusModeObserverContext) {
        // Update the focus UI overlay string when the focus mode changes
		[focusModeLabel setText:[NSString stringWithFormat:@"focus: %@", [self stringForFocusMode:(AVCaptureFocusMode)[[change objectForKey:NSKeyValueChangeNewKey] integerValue]]]];
	} else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

#pragma mark Toolbar Actions
- (IBAction)toggleCamera:(id)sender
{
    // Toggle between cameras when there is more than one
    [[self captureManager] toggleCamera];
    
    // Do an initial focus
    [[self captureManager] continuousFocusAtPoint:CGPointMake(.5f, .5f)];
}

- (IBAction)toggleRecording:(id)sender
{
    //NSLog(@"Toggle recording");
    // Start recording if there isn't a recording running. Stop recording if there is.
    [[self recordButton] setEnabled:NO];
	[newRecordButton setEnabled:NO];
    if (![[[self captureManager] recorder] isRecording]) {
        [[SpheroCamRecordStart sharedSound] play];
        [[self captureManager] startRecording];
    } else {
        [[SpheroCamRecordStop sharedSound] play];
        savingOverlay.frame = CGRectMake(0, 0, self.view.frame.size.height, self.view.frame.size.width);
        //NSLog(@"self.view %@", self.view);
        [self.view insertSubview:savingOverlay belowSubview:driveWheel];
		[newRecordButton setTitle:@"SAVING..." forState:UIControlStateNormal];
        [[self captureManager] stopRecording];
		
	}
}

- (IBAction)captureStillImage:(id)sender
{
    // Capture a still image
    [[self stillButton] setEnabled:NO];
    [[self captureManager] captureStillImage];
    
    // Flash the screen white and fade it out to give UI feedback that a still image was taken
    UIView *flashView = [[UIView alloc] initWithFrame:self.view.frame];
    [flashView setBackgroundColor:[UIColor whiteColor]];
    [[[self view] window] addSubview:flashView];
    
    [UIView animateWithDuration:.4f
                     animations:^{
                         [flashView setAlpha:0.f];
                     }
                     completion:^(BOOL finished){
                         [flashView removeFromSuperview];
                         [flashView release];
                     }
     ];
}

@end

@implementation AVCamViewController (InternalMethods)

// Convert from view coordinates to camera coordinates, where {0,0} represents the top left of the picture area, and {1,1} represents
// the bottom right in landscape mode with the home button on the right.
- (CGPoint)convertToPointOfInterestFromViewCoordinates:(CGPoint)viewCoordinates 
{
    CGPoint pointOfInterest = CGPointMake(.5f, .5f);
    CGSize frameSize = [[self videoPreviewView] frame].size;
    
    if ([captureVideoPreviewLayer isMirrored]) {
        viewCoordinates.x = frameSize.width - viewCoordinates.x;
    }    

    if ( [[captureVideoPreviewLayer videoGravity] isEqualToString:AVLayerVideoGravityResize] ) {
		// Scale, switch x and y, and reverse x
        pointOfInterest = CGPointMake(viewCoordinates.y / frameSize.height, 1.f - (viewCoordinates.x / frameSize.width));
    } else {
        CGRect cleanAperture;
        for (AVCaptureInputPort *port in [[[self captureManager] videoInput] ports]) {
            if ([port mediaType] == AVMediaTypeVideo) {
                cleanAperture = CMVideoFormatDescriptionGetCleanAperture([port formatDescription], YES);
                CGSize apertureSize = cleanAperture.size;
                CGPoint point = viewCoordinates;

                CGFloat apertureRatio = apertureSize.height / apertureSize.width;
                CGFloat viewRatio = frameSize.width / frameSize.height;
                CGFloat xc = .5f;
                CGFloat yc = .5f;
                
                if ( [[captureVideoPreviewLayer videoGravity] isEqualToString:AVLayerVideoGravityResizeAspect] ) {
                    if (viewRatio > apertureRatio) {
                        CGFloat y2 = frameSize.height;
                        CGFloat x2 = frameSize.height * apertureRatio;
                        CGFloat x1 = frameSize.width;
                        CGFloat blackBar = (x1 - x2) / 2;
						// If point is inside letterboxed area, do coordinate conversion; otherwise, don't change the default value returned (.5,.5)
                        if (point.x >= blackBar && point.x <= blackBar + x2) {
							// Scale (accounting for the letterboxing on the left and right of the video preview), switch x and y, and reverse x
                            xc = point.y / y2;
                            yc = 1.f - ((point.x - blackBar) / x2);
                        }
                    } else {
                        CGFloat y2 = frameSize.width / apertureRatio;
                        CGFloat y1 = frameSize.height;
                        CGFloat x2 = frameSize.width;
                        CGFloat blackBar = (y1 - y2) / 2;
						// If point is inside letterboxed area, do coordinate conversion. Otherwise, don't change the default value returned (.5,.5)
                        if (point.y >= blackBar && point.y <= blackBar + y2) {
							// Scale (accounting for the letterboxing on the top and bottom of the video preview), switch x and y, and reverse x
                            xc = ((point.y - blackBar) / y2);
                            yc = 1.f - (point.x / x2);
                        }
                    }
                } else if ([[captureVideoPreviewLayer videoGravity] isEqualToString:AVLayerVideoGravityResizeAspectFill]) {
					// Scale, switch x and y, and reverse x
                    if (viewRatio > apertureRatio) {
                        CGFloat y2 = apertureSize.width * (frameSize.width / apertureSize.height);
                        xc = (point.y + ((y2 - frameSize.height) / 2.f)) / y2; // Account for cropped height
                        yc = (frameSize.width - point.x) / frameSize.width;
                    } else {
                        CGFloat x2 = apertureSize.height * (frameSize.height / apertureSize.width);
                        yc = 1.f - ((point.x + ((x2 - frameSize.width) / 2)) / x2); // Account for cropped width
                        xc = point.y / frameSize.height;
                    }
                }
                
                pointOfInterest = CGPointMake(xc, yc);
                break;
            }
        }
    }
    
    return pointOfInterest;
}

// Auto focus at a particular point. The focus mode will change to locked once the auto focus happens.
- (void)tapToAutoFocus:(UIGestureRecognizer *)gestureRecognizer
{
    if ([[[captureManager videoInput] device] isFocusPointOfInterestSupported]) {
        CGPoint tapPoint = [gestureRecognizer locationInView:[self videoPreviewView]];
        CGPoint convertedFocusPoint = [self convertToPointOfInterestFromViewCoordinates:tapPoint];
        [captureManager autoFocusAtPoint:convertedFocusPoint];
    }
}

// Change to continuous auto focus. The camera will constantly focus at the point choosen.
- (void)tapToContinouslyAutoFocus:(UIGestureRecognizer *)gestureRecognizer
{
    if ([[[captureManager videoInput] device] isFocusPointOfInterestSupported])
        [captureManager continuousFocusAtPoint:CGPointMake(.5f, .5f)];
}

// Update button states based on the number of available cameras and mics
- (void)updateButtonStates
{
	NSUInteger cameraCount = [[self captureManager] cameraCount];
	NSUInteger micCount = [[self captureManager] micCount];
    
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        if (cameraCount < 2) {
            [[self cameraToggleButton] setEnabled:NO]; 
            
            if (cameraCount < 1) {
                [[self stillButton] setEnabled:NO];
                
                if (micCount < 1)
                    [[self recordButton] setEnabled:NO];
                else
                    [[self recordButton] setEnabled:YES];
            } else {
                [[self stillButton] setEnabled:YES];
                [[self recordButton] setEnabled:YES];
            }
        } else {
            [[self cameraToggleButton] setEnabled:YES];
            [[self stillButton] setEnabled:YES];
            [[self recordButton] setEnabled:YES];
        }
    });
}

@end

@implementation AVCamViewController (AVCamCaptureManagerDelegate)

- (void)captureManager:(AVCamCaptureManager *)captureManager didFailWithError:(NSError *)error
{
    NSLog(@"captureManager didFailWithError: %@", error);
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:[error localizedDescription]
                                                            message:[error localizedFailureReason]
                                                           delegate:nil
                                                  cancelButtonTitle:NSLocalizedString(@"OK", @"OK button title")
                                                  otherButtonTitles:nil];
        [alertView show];
        [alertView release];
    });
}

- (void)captureManagerRecordingBegan:(AVCamCaptureManager *)captureManager
{
    //NSLog(@"captureManagerRecordingBegan");
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        [[self recordButton] setTitle:NSLocalizedString(@"Stop", @"Toggle recording button stop title")];
        [[self recordButton] setEnabled:YES];
		//[newRecordButton setTitle:@"STOP" forState:UIControlStateNormal];
        [newRecordButton setImage:[UIImage imageNamed:@"SpheroCam-VideoRed@2x.png"] forState:UIControlStateNormal];
		[newRecordButton setEnabled:YES];
        [FlurryAnalytics logEvent:@"VideoRecorded" timed:YES];
    });
}

- (void)captureManagerRecordingFinished:(AVCamCaptureManager *)captureManager
{
    //NSLog(@"captureManagerRecordingFinished");
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        [[self recordButton] setTitle:NSLocalizedString(@"", @"Toggle recording button record title")];
        [[self recordButton] setEnabled:YES];
		[newRecordButton setTitle:@"" forState:UIControlStateNormal];
        [newRecordButton setImage:[UIImage imageNamed:@"SpheroCam-Video@2x.png"] forState:UIControlStateNormal];
		[newRecordButton setEnabled:YES];
        [savingOverlay removeFromSuperview];
        [FlurryAnalytics endTimedEvent:@"VideoRecorded" withParameters:nil];
    });
}

- (void)captureManagerStillImageCaptured:(AVCamCaptureManager *)captureManager
{
    //NSLog(@"captureManagerStillImageCaptured");
    CFRunLoopPerformBlock(CFRunLoopGetMain(), kCFRunLoopCommonModes, ^(void) {
        [[self stillButton] setEnabled:YES];
        [FlurryAnalytics logEvent:@"PictureTaken"];
    });
}

- (void)captureManagerDeviceConfigurationChanged:(AVCamCaptureManager *)captureManager
{
    //NSLog(@"captureManagerDeviceConfigurationChanged");
	[self updateButtonStates];
}

@end
