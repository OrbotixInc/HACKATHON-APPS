//
//  CalibrationTutorialViewController.m
//  Sphero
//
//  Created by Jon Carroll on 9/19/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "CalibrationTutorialViewController.h"
#import <RobotKit/RobotKit.h>
#import "FlurryAPI.h"
#import "CalibrateOverlayStartSound.h"
#import "CalibrateOverlayLoop.h"
#import "CalibrateOverlayEnd.h"
#import <RobotKit/Macro/RKAbortMacroCommand.h>
#import <RobotKit/Macro/RKRunMacroCommand.h>
#import <RobotKit/Macro/RKSaveTemporaryMacroCommand.h>

@implementation CalibrationTutorialViewController

@synthesize delegate, state;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
        currentArrowIndex = 0;
        state = CalibrateTutorialStateFloor;
        rotating = NO;
        scrollingOnTheirOwn = NO;
        bounceAnimationStarted = NO;
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    [FlurryAPI logEvent:@"CalibrationTutorialShown"];
    // Do any additional setup after loading the view from its nib.
    calibrateHandler = [[RUICalibrateGestureHandler alloc] initWithView:self.view];
    calibrateHandler.delegate = self;
    [RKBackLEDOutputCommand sendCommandWithBrightness:1.0];
    [RKRGBLEDOutputCommand sendCommandWithRed:1.0 green:1.0 blue:1.0];
    [self performSelector:@selector(turnWhite) withObject:nil afterDelay:0.3];
    leftLabel.alpha = 0.0;
    rightLabel.alpha = 0.0;
    spheroImage.alpha = 0.0;
    blueDotImage.alpha = 0.0;
    dot1.alpha = 0.0;
    dot2.alpha = 0.0;
    midButton.alpha = 0.0;
    bottomButton.alpha = 0.0;
    handView.alpha = 0.0;
    dontShowAgainButton.alpha = 0.0;
    [self.view performSelector:@selector(setDelegate:) withObject:self];
    //Use larger fonts on the iPad
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        UIFont* bold36 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:40];
        rightLabel.font = bold36;
        leftLabel.font = bold36;
        bottomButton.titleLabel.font = bold36;
        bottomButton.titleLabel.textColor = [UIColor whiteColor];
        midButton.titleLabel.font = bold36;
        midButton.titleLabel.textColor = [UIColor whiteColor];
    } else {
        UIFont* bold28 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:28];
        rightLabel.font = bold28;
        leftLabel.font = bold28;
        bottomButton.titleLabel.font = bold28;
        bottomButton.titleLabel.textColor = [UIColor whiteColor];
        midButton.titleLabel.font = bold28;
        midButton.titleLabel.textColor = [UIColor whiteColor];
        
    }
    
    
}

-(void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    confirmationView.frame = self.view.bounds;
    [self performSelector:@selector(updateUIForState)];
}

-(void)updateUIForState {
    [RKAbortMacroCommand sendCommand];
    [UIView animateWithDuration:0.15 
                          delay:0.0
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut) 
                     animations:^{
                         leftLabel.alpha = 0.0;
                         rightLabel.alpha = 0.0;
                         spheroImage.alpha = 0.0;
                         blueDotImage.alpha = 0.0;
                         dot1.alpha = 0.0;
                         dot2.alpha = 0.0;
                         midButton.alpha = 0.0;
                         bottomButton.alpha = 0.0;
                         dontShowAgainButton.alpha = 0.0;
                         confirmationView.alpha = 0.0;
                         handView.alpha = 0.0;
                         blueBox1.alpha = 1.0;
                         line1.alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         [UIView animateWithDuration:0.15
                                               delay:0.0
                                             options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut) 
                                          animations:^{
                                              [confirmationView removeFromSuperview];
                                              if(state == CalibrateTutorialStateFloor) {
                                                  handView.alpha = 1.0;
                                                  rightLabel.alpha = 1.0;
                                                  rightLabel.text = @"Place Sphero\non the floor.";
                                                  [bottomButton setTitle:@"Next" forState:UIControlStateNormal];
                                                  bottomButton.alpha = 1.0;
                                              } else if(state == CalibrateTutorialStateTaillight) {
                                                  NSData *spinMacro = [NSData dataWithContentsOfFile:[[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"calibrateSpin.sphero"]];
                                                  [RKSaveTemporaryMacroCommand sendCommandWithMacro:spinMacro flags:RKMacroFlagNone];
                                                  [RKRunMacroCommand sendCommandWithId:255];
                                                  //spheroImage.alpha = 1.0;
                                                  rightLabel.alpha = 1.0;
                                                  rightLabel.text = @"Sphero has a tail light.\n\nSee the blue light?";
                                                  [bottomButton setTitle:@"Next" forState:UIControlStateNormal];
                                                  bottomButton.alpha = 1.0;
                                              } else if(state == CalibrateTutorialStateFingersDown) {
                                                  dot1.alpha = 1.0;
                                                  dot2.alpha = 1.0;
                                                  leftLabel.text = @"Place\ntwo fingers on those white dots.";
                                                  leftLabel.alpha = 1.0;
                                              } else if(state == CalibrateTutorialStateRotate) {
                                                  leftLabel.alpha = 1.0;
                                                  leftLabel.text = @"Spin\nyour fingers.";
                                              } else if(state == CalibrateTutorialStateAim) {
                                                  leftLabel.alpha = 1.0;
                                                  leftLabel.text = @"Aim!\n\nSphero's blue dot at you.";
                                                  bottomButton.alpha = 1.0;
                                                  [bottomButton setTitle:@"Test" forState:UIControlStateNormal];
                                              } else if(state == CalibrateTutorialStateForward) {
                                                  blueBox1.alpha = 0.0;
                                                  line1.alpha = 0.0;
                                                  
                                                  [self.view addSubview:confirmationView];
                                                  UIFont* bold28 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:28];
                                                  confirmationLabel.font = bold28;
                                                  confirmationView.alpha = 1.0;
                                              } else if(state == CalibrateTutorialStateRemember) {
                                                  confirmationView.alpha = 0.0;
                                                  leftLabel.alpha = 1.0;
                                                  leftLabel.text = @"Remember!\n\nYou can aim Sphero anytime.";
                                                  midButton.alpha = 1.0;
                                                  [midButton setTitle:@"Got It!" forState:UIControlStateNormal];
                                                  dontShowAgainButton.alpha = 1.0;
                                              }
                                          }
                                          completion:^(BOOL finished) {
                                               if(state == CalibrateTutorialStateForward) {
                                                   [self startAnimation];
                                                   [RKRollCommand sendCommandWithHeading:0.0 velocity:0.6];
                                                   RKRollCommand *roll = [[RKRollCommand alloc] initWithHeading:0.0 velocity:0.0];
                                                   [roll sendCommandWithDelay:2.5];
                                                   [roll release];
                                               } else if(state == CalibrateTutorialStateFloor) {
                                                   [self performSelector:@selector(bounceAnimation)];
                                               } else if(state == CalibrateTutorialStateTaillight) {
                                                   //[self performSelector:@selector(spinAnimation)];
                                               }
                                          }];
                         
                     }];
    
}

-(void)turnWhite {
    [RKRGBLEDOutputCommand sendCommandWithRed:1.0 green:1.0 blue:1.0];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    [calibrateHandler release];
    calibrateHandler = nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

-(void)twoFingerPress:(NSNumber*)touchstate {
    if([touchstate boolValue]) {
        if(state==CalibrateTutorialStateFingersDown) {
            state++;
            [self updateUIForState];
        }
    } else {
        if(state==CalibrateTutorialStateRotate) {
            state--;
            [self updateUIForState];
        }
        
    }
}

-(BOOL)calibrateGestureHandlerShouldAllowCalibration:(RUICalibrateGestureHandler*)sender {

    return YES;
}


-(void)calibrateGestureHandlerBegan:(RUICalibrateGestureHandler*)sender {
    UIView *overlayView = [sender getOverlayView];
    overlayView.backgroundColor = [UIColor clearColor];
    if(state != CalibrateTutorialStateAim) {
        state = CalibrateTutorialStateAim;
        [self performSelector:@selector(updateUIForState)];
    }
    dot1.alpha = 0.0;
    dot2.alpha = 0.0;
    [[CalibrateOverlayStartSound sharedSound] play];
    [self performSelector:@selector(startCalibrateLoopSound) withObject:nil afterDelay:0.3];
}

-(void)startCalibrateLoopSound {
    if([RUICalibrateGestureHandler isCalibrating]) [[[CalibrateOverlayLoop sharedSound] player] play];
}

-(void)calibrateGestureHandlerEnded:(RUICalibrateGestureHandler*)sender {
    dot1.alpha = 1.0;
    dot2.alpha = 1.0;
    [[[CalibrateOverlayLoop sharedSound] player] stop];
    [[CalibrateOverlayEnd sharedSound] play];
}

-(IBAction)donePressed:(id)sender {
    if(state == CalibrateTutorialStateRemember) {
        [delegate calibrationTutorialFinished];
    } else {
        state++;
        [self performSelector:@selector(updateUIForState)];
    }
}

-(void)startAnimation {
    if(!confirmationView.superview) return;
    currentArrowIndex++;
    
    [UIView animateWithDuration:0.4 
                          delay:0.1
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseInOut) 
                     animations:^{
                         arrow1.alpha = 0.3;
                         arrow2.alpha = 0.3;
                         arrow3.alpha = 0.3;
                         arrow4.alpha = 0.3;
                         [self getCurrentArrow].alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         [self startAnimation];
                     }];
    
    
}

-(void)bounceAnimation {
    if(state!=CalibrateTutorialStateFloor) {
        handView.alpha = 0.0;
        [self performSelector:@selector(spinAnimation)];
        return;
    }
    if(bounceAnimationStarted) return;
    bounceAnimationStarted = YES;
    
    handStart = handView.center;
    [UIView animateWithDuration:0.3 
                          delay:0.8
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseOut) 
                     animations:^{
                         handView.alpha = 0.0;
                         
                     }
                     completion:^(BOOL finished) {
                         handView.center = CGPointMake(handView.center.x, 0.0-(handView.frame.size.height * 0.5));
                         handView.alpha = 1.0;
                         [UIView animateWithDuration:1.0 
                                               delay:0.0
                                             options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveEaseOut) 
                                          animations:^{
                                              handView.center = handStart;
                                              
                                          }
                                          completion:^(BOOL finished) {
                                              bounceAnimationStarted = NO;
                                              [self bounceAnimation];
                                          }];
                     }];
}

-(void)spinAnimation {
    if(state!=CalibrateTutorialStateTaillight) return;
    
    spheroImage.alpha = 1.0;
    blueDotImage.alpha = 0.0;
    blueDotImage.center = CGPointMake(spheroImage.frame.origin.x - 10.0, spheroImage.frame.origin.y + (spheroImage.frame.size.height * 0.5) + 10.0);
    
    
    [UIView animateWithDuration:0.5 
                          delay:1.0
                        options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveLinear) 
                     animations:^{
                         blueDotImage.center = CGPointMake(spheroImage.frame.origin.x + (spheroImage.frame.size.width * 0.5), spheroImage.frame.origin.y + (spheroImage.frame.size.height * 0.5) + 10.0);
                         blueDotImage.alpha = 1.0;
                     }
                     completion:^(BOOL finished) {
                         [UIView animateWithDuration:0.5 
                                               delay:0.0
                                             options:(UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveLinear) 
                                          animations:^{
                                              blueDotImage.center = CGPointMake(spheroImage.frame.origin.x + spheroImage.frame.size.width + 10.0, spheroImage.frame.origin.y + (spheroImage.frame.size.height * 0.5) + 10.0);
                                              blueDotImage.alpha = 0.0;
                                          }
                                          completion:^(BOOL finished) {
                                              [self spinAnimation];
                                          }];
                     }];
}

-(UIImageView*)getCurrentArrow {
    if(currentArrowIndex==0) {
        return arrow1;
    } else if(currentArrowIndex==1) {
        return arrow2;
    } else if(currentArrowIndex==2) {
        return arrow3;
    } else if(currentArrowIndex==3) {
        return arrow4;
    } else {
        currentArrowIndex = 0;
        return arrow1;
    }
}

-(IBAction)yesPressed:(id)sender {
    state++;
    [self performSelector:@selector(updateUIForState)];
}

-(IBAction)noPressed:(id)sender {
    state = CalibrateTutorialStateFingersDown;
    [self performSelector:@selector(updateUIForState)];
}

-(IBAction)dontShowAgainPressed:(id)sender {
    [FlurryAPI logEvent:@"ClickedDontShowAgain"];
    [RKBackLEDOutputCommand sendCommandWithBrightness:0.0];
    
    [delegate calibrationTutorialFinishedDontShowAgain];
}




@end
