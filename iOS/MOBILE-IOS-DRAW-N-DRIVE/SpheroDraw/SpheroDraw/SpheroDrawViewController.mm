//
//  SpheroDrawViewController.m
//  SpheroDraw
//
//  Created by Brandon Dorris on 8/19/11.
//  Copyright 2011 Orbotix. All rights reserved.
//

#import "SpheroDrawViewController.h"
#import "DrawingView.h"
#import <math.h>
#include <RobotKit/Macro/RKMacro.h>
#import "NoSpheroAlertManager.h"
#import "SpheroTraySlideSound.h"
#import "SpheroItemSelectSound.h"
#import "FlurryAnalytics.h"
#import "CalibrateOverlayEnd.h"
#import "CalibrateOverlayLoop.h"
#import "CalibrateOverlayStartSound.h"
#import "UserGuideViewController.h"

#define kMinimumPixelDistance 10.0
#define kDelayBetweenCommands 250.0

static BOOL recording = NO;
static RKMacro macro;
static int lastHeading;
static int firstHeading;
static BOOL firstTime = YES;

@implementation SpheroDrawViewController

@synthesize lastPoint;
@synthesize points;

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - helper methods

- (float)distanceBetweenPoint:(CGPoint)point1 andPoint:(CGPoint)point2 {
    float xDifference = point1.x - point2.x;
    float yDifference = point1.y - point2.y;
    return sqrtf(xDifference*xDifference + yDifference*yDifference);
}

- (float)headingFrom:(CGPoint)point1 to:(CGPoint)point2 {
    float xDifference = point2.x - point1.x;
    // use opposite order for the y values so that the values act like cartesian coords
    float yDifference = point2.y - point1.y;
    
    if (xDifference == 0.0) {
        if (yDifference > 0) {
            //return 0.0;
            return 180.0;
        } else {
            //return 180.0;
            return 0.0;
        }
    }
    
    float radians = atanf(yDifference/xDifference);
    float degrees = (radians * 180.0)/M_PI;
    if (xDifference < 0) {
        degrees += 180.0;
    }
    float actualHeading = degrees - 90.0;
    //Jon added the minus 180
    int actualHeadingInt = (int)actualHeading - 180;
    actualHeadingInt = actualHeadingInt % 360;
    if (actualHeadingInt < 0) {
        actualHeadingInt += 360;
    }
    
    if (actualHeadingInt == 360) {
        actualHeadingInt = 0;
    }
    
    return actualHeadingInt;
}

#pragma mark - Path Delegate methods

- (void)pathDidStart:(CGPoint)point {
    lastPoint = point;
    macroFull = NO;
    recording = YES;
    [FlurryAnalytics logEvent:@"DrawShape" timed:YES];
    //macro = RKMacro::RKMacro();
    if(macroObject==nil) {
        macroObject = [[RKMacroObject alloc] init];
        //Check firmware version and set to appropriate streaming/temp macro mode
        if(firmwareVersion <= 0.95) {
            NSLog(@"Setting temp macro mode, fw version %1.2f", firmwareVersion);
            macroObject.mode = RKMacroObjectModeNormal;
        } else {
            NSLog(@"Setting streaming macro mode, fw version %1.2f", firmwareVersion);
            macroObject.mode = RKMacroObjectModeCachedStreaming;
        }
    } else if(firmwareVersion <= 0.95) {
        [macroObject.commands removeAllObjects];
    }
    //macro.setSD1(kDelayBetweenCommands);
    [macroObject addCommand:[RKMCSD1 commandWithDelay:kDelayBetweenCommands]];
    //macro.setSPD1(0.6); //driving speed
    [macroObject addCommand:[RKMCSPD1 commandWithSpeed:0.6]];
    //macro.setSD2(0);
    [macroObject addCommand:[RKMCSD2 commandWithDelay:0]];
    //macro.setSPD2(0.0); //Speed for turning
    [macroObject addCommand:[RKMCSPD2 commandWithSpeed:0.0]];
    [macroObject addCommand:[RKMCRotationRate commandWithRate:0.8]];
    const float *comps = CGColorGetComponents(drawingView.drawingPenColor.CGColor);
    [macroObject addCommand:[RKMCRGBSD2 commandWithRed:comps[0] green:comps[1] blue:comps[2]]];
}
- (void)pathDidChange:(CGPoint)point {
    if (recording && [self distanceBetweenPoint:lastPoint andPoint:point] > kMinimumPixelDistance) {
        //NSLog(@"Distance between points: %1.1f", [self distanceBetweenPoint:lastPoint andPoint:point]);
        //Calculate an additional delay based on distance between points
        int delay = (([self distanceBetweenPoint:lastPoint andPoint:point] / (kMinimumPixelDistance * 2.5)) * kDelayBetweenCommands) - kDelayBetweenCommands;
        
        float heading = [self headingFrom:lastPoint to:point];
        if (firstTime) {
            firstHeading = heading;
            lastHeading = firstHeading;
            firstTime = NO;
            [macroObject addCommand:[RKMCRollSD1SPD2 commandWithHeading:heading]];
            [macroObject addCommand:[RKMCDelay commandWithDelay:500]];
        }
        float lastHeadingDifference = fabsf(heading - lastHeading);
        if (lastHeadingDifference > 180.0) {
            lastHeadingDifference = 360 - lastHeadingDifference;
        }
        //NSLog(@"last heading difference: %f", lastHeadingDifference);
        
        /*if(macro.macroLength() >= 245) {  //be sure our last command is a stop command if macro is full
            if(!macroFull) {
                //macro.rollSD1SPD2(lastHeading);
                [macroObject addCommand:[RKMCRollSD1SPD2 commandWithHeading:lastHeading]];
            }
            macroFull = YES;
            return;
        }*/ //Streaming macro can't be full
        
        if (lastHeadingDifference > 45.0) {
            //macro.roll(0.0, lastHeading, 254.0);
            //macro.roll(0.0, lastHeading, 254.0);
            //macro.roll(0.0, heading, 254.0);
            
            [macroObject addCommand:[RKMCRoll commandWithSpeed:0.0 heading:lastHeading delay:0]];
            [macroObject addCommand:[RKMCWaitUntilStop commandWithDelay:1000]];
            //macro.rollSD1SPD2(heading);
            [macroObject addCommand:[RKMCRollSD1SPD2 commandWithHeading:heading]];
            [macroObject addCommand:[RKMCWaitUntilStop commandWithDelay:1000]];
            //macro.rollSD1SPD1(heading);
            [macroObject addCommand:[RKMCRollSD1SPD1 commandWithHeading:heading]];
            if(delay > 0) {
                //macro.delay(delay);
                [macroObject addCommand:[RKMCDelay commandWithDelay:delay]];
            }
        } else {
            //macro.rollSD1SPD1(heading);
            [macroObject addCommand:[RKMCRollSD1SPD1 commandWithHeading:heading]];
            if(delay > 0) {
                //macro.delay(delay);
                [macroObject addCommand:[RKMCDelay commandWithDelay:delay]];
            }
            
        }
        
        lastHeading = heading;
        NSLog(@"heading: %f", heading);
        lastPoint = point;
    }
}

- (void)pathDidEnd:(CGPoint)point {
    NSLog(@"Touches ended");
    recording = NO;


    [macroObject addCommand:[RKMCRollSD1SPD2 commandWithHeading:lastHeading]];
    const float *comps = CGColorGetComponents(drawingView.drawingPenColor.CGColor);
    [macroObject addCommand:[RKMCRGBSD2 commandWithRed:comps[0] green:comps[1] blue:comps[2]]];
    

    [FlurryAnalytics endTimedEvent:@"DrawShape" withParameters:nil];
    [macroObject playMacro];
    firstTime = YES;
    calibrationCount++;
    if(calibrationCount == 5) {
        [self calibrateReminder];
    }
}

-(void)calibrateReminder {
    calibrateUnderlay.alpha = 1.0;
    
    [UIView animateWithDuration:1.0
                          delay:0.3
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut)
                     animations:^{
                         if(calibrateOverlay.alpha == 0.0) {
                             calibrateOverlay.alpha = 1.0;
                         } else {
                             calibrateOverlay.alpha = 0.0;
                         }
                     }
                     completion:^(BOOL finished) {
                         if(calibrateUnderlay.alpha == 0.0) {
                             calibrateOverlay.alpha = 0.0;
                         } else {
                             [self calibrateReminder];
                         }
                     }];
    
}

- (void)didClearCanvas {
    [macroObject stopMacro];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    firmwareVersion = 0.0;
    
    calibrateMessage.hidden = YES;
    calibrationCount = 0;
    
    firstTime = YES;
    
    calibrateUnderlay.alpha = 0.0;
    calibrateOverlay.alpha = 0.0;
    
    colorPicker.delegate = self;
    
    rotating = false;
    currentColor = [[UIColor blueColor] retain];
    
    drawingView = [[[DrawingView alloc] initWithFrame:self.view.frame] autorelease];
    drawingView.delegate = self;
    [self.view insertSubview:drawingView aboveSubview:paperBG];
    
    UIPanGestureRecognizer *tap = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(toggleColorTray:)];
    [colorTray addGestureRecognizer:tap];
    [tap release];
    
    UITapGestureRecognizer *touch = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(toggleColorTray:)];
    [touch requireGestureRecognizerToFail:tap];
    [colorTray addGestureRecognizer:touch];
    [touch release];
    
    UITapGestureRecognizer *calloutTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showCalibrateCallout:)];
    [calibrateUnderlay addGestureRecognizer:calloutTap];
    [calloutTap release];
    
    
    drawingView.drawingPenColor = currentColor;
    [colorPicker setHue:1.0 saturation:1.0 brightness:1.0];
    [colorPicker setRed:0.0 green:0.0 blue:1.0];
    
    
    calibrateHandler = [[RUICalibrateGestureHandler alloc] initWithView:drawingView];
    calibrateHandler.delegate = self;
    
	// Do any additional setup after loading the view, typically from a nib.
    
    
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    [calibrateHandler release];
    calibrateHandler = nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    // Watch for online notification to start driving
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(handleConnectionOnline:)
                                                 name:RKDeviceConnectionOnlineNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(calibrationDismissed:) name:@"RUICalibrationViewControllerCalibrationDone" object:nil];
    
    
    NSLog(@"here");
    //Attempt to control the connected robot so we get the notification if one is connected
    if([[RKRobotProvider sharedRobotProvider] isRobotUnderControl]) {
        [[RKRobotProvider sharedRobotProvider] openRobotConnection];
    } else {
        [NoSpheroAlertManager showAlertWithType:(NoSpheroAlertManagerType)[[NSUserDefaults standardUserDefaults] boolForKey:@"hasRobotConnected"]];
    }
    
	
    
    [drawingView becomeFirstResponder];
    
    //[self presentCalibrationView];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return UIInterfaceOrientationIsLandscape(interfaceOrientation);
    } else {
        return YES;
    }
}

-(void)showCalibrateCallout:(UIGestureRecognizer*)sender {
    if(calloutOverlay) return;
    [FlurryAnalytics logEvent:@"CalibrateReminderTapped"];
    calibrateCallout.hidden = NO;
    calloutOverlay = [[UIView alloc] initWithFrame:self.view.bounds];
    calloutOverlay.backgroundColor = [UIColor clearColor];
    calloutOverlay.userInteractionEnabled = YES;
    [self.view addSubview:calloutOverlay];
    UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(dismissCallout)];
    [calloutOverlay addGestureRecognizer:tap];
    [tap release];
    
}

-(void)dismissCallout {
    if(!calloutOverlay) return;
    calibrateCallout.hidden = YES;
    [calloutOverlay removeFromSuperview];
    [calloutOverlay release];
    calloutOverlay = nil;
}

- (void)colorPickerDidChange:(RUIHSBColorPickerView*)view 
					 withRed:(CGFloat)r green:(CGFloat)g blue:(CGFloat)b {
    
    drawingView.drawingPenColor = [[UIColor colorWithRed:r green:g blue:b alpha:1.0] retain];
    
    if(recording) {
        //macro.rgbSD2(r, g, b);
        [macroObject addCommand:[RKMCRGBSD2 commandWithRed:r green:g blue:b]];
        [FlurryAnalytics logEvent:@"ColorChangedWhileDrawing"];
    } else if(macroObject.mode==RKMacroObjectModeNormal) {
        [FlurryAnalytics logEvent:@"ColorChanged"];
        [RKRGBLEDOutputCommand sendCommandWithRed:r green:g blue:b];
    } else {
        [FlurryAnalytics logEvent:@"ColorChanged"];
        [RKRGBLEDOutputCommand sendCommandWithRed:r green:g blue:b];
        [macroObject addCommand:[RKMCRGB commandWithRed:r green:g blue:b delay:0]];
        //[macroObject addCommand:[RKMCRGB commandWithRed:r green:g blue:b delay:0]];
        //if(macroObject.running) {
            [macroObject playMacro];
        //}
        
    }

}

-(void)toggleColorTray:(UIPanGestureRecognizer*)sender {
    if(sender.state == UIGestureRecognizerStateBegan || sender.state == UIGestureRecognizerStateChanged) {
        return;
    }
    CGPoint offset;
    if(colorTray.center.x < 0.0) {
        offset = CGPointMake(100, -100);
        [FlurryAnalytics logEvent:@"ColorTrayShown"];
    } else {
        offset = CGPointMake(-100, 100);
        [FlurryAnalytics logEvent:@"ColorTrayHidden"];
    }
    
    [[SpheroTraySlideSound sharedSound] play];
    [UIView animateWithDuration:0.5
                          delay:0.0
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut)
                     animations:^{
                         colorTray.center = CGPointMake(colorTray.center.x + offset.x, colorTray.center.y + offset.y);
                         colorPicker.center = CGPointMake(colorPicker.center.x + offset.x, colorPicker.center.y + offset.y);
                     }
                     completion:nil];

}



-(void)willAnimateRotationToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation duration:(NSTimeInterval)duration {
    drawingView.frame = self.view.bounds;
    calibrateOverlayRings.frame = self.view.bounds;
    /*if([RUIModalLayerViewController currentModalLayerViewController]) {
        [RUIModalLayerViewController currentModalLayerViewController].view.frame = self.view.bounds;
    }*/
}

-(IBAction)repeatPressed:(id)sender {
    NSLog(@"Repeat pressed");
    [FlurryAnalytics logEvent:@"RepeatPressed"];
    //NSLog(@"Macro Length: %d", macro.macroLength());
    [macroObject playMacro];
    firstTime = YES;
    calibrationCount++;
    [[SpheroItemSelectSound sharedSound] play];
    if(calibrationCount == 5) {
        [self calibrateReminder];
    }
}



- (void)motionEnded:(UIEventSubtype)motion
          withEvent:(UIEvent *)event

{
    if (motion == UIEventSubtypeMotionShake)
	{
		NSLog(@"shake ended");
        drawingView.drawImage.image = nil;
	}
}

-(void)shakeNotification:(NSNotification*)notification {
    drawingView.drawImage.image = nil;
    NSLog(@"Shake detected");
}



-(BOOL)calibrateGestureHandlerShouldAllowCalibration:(RUICalibrateGestureHandler*)sender {
    return YES;
}


-(void)calibrateGestureHandlerBegan:(RUICalibrateGestureHandler*)sender {
    [self dismissCallout];
    recording = NO;
    [macroObject stopMacro];
    const float *comps = CGColorGetComponents(drawingView.drawingPenColor.CGColor);
    [RKRGBLEDOutputCommand sendCommandWithRed:comps[0] green:comps[1] blue:comps[2]];
    calibrateMessage.hidden = YES;
    calibrateUnderlay.alpha = 0.0;
    calibrationCount = 0;
    drawingView.drawImage.image = nil;
    [[CalibrateOverlayStartSound sharedSound] play];
    [self performSelector:@selector(startCalibrateLoopSound) withObject:nil afterDelay:0.3];
}

-(void)startCalibrateLoopSound {
    if([RUICalibrateGestureHandler isCalibrating]) [[[CalibrateOverlayLoop sharedSound] player] play];
}

-(void)calibrateGestureHandlerEnded:(RUICalibrateGestureHandler*)sender {
    [RKAchievement recordEvent:@"2fingerRotate"];
    [FlurryAnalytics logEvent:@"Calibration"];
    drawingView.drawImage.image = nil;
    [[[CalibrateOverlayLoop sharedSound] player] stop];
    [[CalibrateOverlayEnd sharedSound] play];
    
}

-(IBAction)userGuidePressed:(id)sender {
    [[SpheroItemSelectSound sharedSound] play];
    UserGuideViewController *guide = [[UserGuideViewController alloc] initWithNibName:@"UserGuideViewController" bundle:nil];
    guide.delegate = self;
    [self presentModalViewController:guide animated:YES];
    [guide release];
}

#pragma mark - Robot Connection States

-(void)registerForRobotNotifications {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleDidGainControl:) name:RKRobotDidGainControlNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleConnectionOnline:) name:RKDeviceConnectionOnlineNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleConnectionOffline:) name:RKDeviceConnectionOfflineNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleConnectionMainAppCorrupt:) name:RKDeviceConnectionMainAppCorruptNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleRobotDidLossControl:) name:RKRobotDidLossControlNotification object:nil];
}

-(void)unregisterForRobotNotifications {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKDeviceConnectionOnlineNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKDeviceConnectionOfflineNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKDeviceConnectionMainAppCorruptNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKRobotDidGainControlNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:RKRobotDidLossControlNotification object:nil];
}

-(void)handleDidGainControl:(NSNotification*)notification {
    NSLog(@"SpheroViewController handleDidGainControl");
    [NoSpheroAlertManager dismissAlert];
    if([[RKRobotProvider sharedRobotProvider] isRobotUnderControl]) {
        [[RKRobotProvider sharedRobotProvider] openRobotConnection];
    }
}

- (void)handleConnectionOnline:(NSNotification *)notification
{
    NSLog(@"Received Online Notification");
    [NoSpheroAlertManager dismissAlert];
    [[NSUserDefaults standardUserDefaults] setBool:YES forKey:@"hasRobotConnected"];
    // Start by getting the version from the ball
    [[RKDeviceMessenger sharedMessenger] addResponseObserver:self 
                                                    selector:@selector(handleVersioningResponse:)];
    [RKVersioningCommand sendCommand];
    
    //This is the notificaiton we get when we find out the robot is online
    //Robot will not respond to commands until this notification is recieved
    NSLog(@"there");
    [RKRotationRateCommand sendCommandWithRate:0.6];
    [RKRGBLEDOutputCommand sendCommandWithRed:0.0 green:0.0 blue:0.8];
    [RKBackLEDOutputCommand sendCommandWithBrightness:0.0];
}

- (void)handleVersioningResponse:(RKDeviceResponse *)response
{
    if ([response isKindOfClass:[RKVersioningResponse class]]) {
        [[RKDeviceMessenger sharedMessenger] removeResponseObserver:self];
        if (response.code == RKResponseCodeOK) {
            // save the device's firmware version
            firmwareVersion = [((RKVersioningResponse *)response).mainApplicationVersion floatValue];
            
        } 
    }
}

- (void)handleConnectionOffline:(NSNotification *)notification
{
    NSLog(@"Received Offline Notification");
}

- (void)handleConnectionMainAppCorrupt:(NSNotification *)notification
{
    NSLog(@"Main App Corrupt Notification");
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Firmware Corrupt" message:@"Sphero's brains are messed up, launch the Sphero application to reload the latest Sphero firmware." delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
    [alert release];
}

- (void) handleRobotDidLossControl:(NSNotification *)notification
{
    [FlurryAnalytics logEvent:@"LostSpheroConnection"];
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Connection Lost" message:@"The connection to Sphero was lost.  This probably happened because Sphero went to sleep, drove out of range or ran out of battery." delegate:self cancelButtonTitle:@"OK" otherButtonTitles:nil];
    [alert show];
    [alert release];
}

@end
