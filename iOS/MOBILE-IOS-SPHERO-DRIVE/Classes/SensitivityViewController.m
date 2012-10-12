//
//  SensitivityViewController.m
//  Sphero
//
//  Created by Brian Alexander on 4/3/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "SensitivityViewController.h"
#import "DriveAppSettings.h"
#import <RobotKit/RobotKit.h>
#import "FlurryAPI.h"
#import "SpheroItemSelectSound.h"
@interface SensitivityViewController (Private)

-(void) initializeControls;
-(void) updateLevelTitles;
-(void) setSelectedLevel:(DriveAppSensitivityLevel)l;
-(void) setupGestures;
-(void) addDoubleTapGesture:(UIView*)view;
-(void) addSingleTapGesture:(UIView*)view;
-(void) handleDoubleTap:(UIGestureRecognizer*)recognizer;
-(void) handleSingleTap:(UIGestureRecognizer*)recognizer;

@end

@implementation SensitivityViewController

@synthesize level1Btn;
@synthesize level2Btn;
@synthesize level3Btn;
@synthesize level1Lbl;
@synthesize level2Lbl;
@synthesize level3Lbl;
@synthesize autoAdjustSwitch;
@synthesize volumeSlider;
@synthesize backButton;
@synthesize backLabel;

- (void)saveSettings
{
    DriveAppSettings *app_settings = [DriveAppSettings defaultSettings];
    if( app_settings.soundFXVolume != self.volumeSlider.value ) {
        app_settings.soundFXVolume = self.volumeSlider.value;
        [FlurryAPI logEvent:@"Menu-SoundVolumeChanged" withParameters:
         [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithFloat:self.volumeSlider.value], @"level", nil]];
    }
    if( app_settings.gyroSteering != self.autoAdjustSwitch.on ) {
        app_settings.gyroSteering = self.autoAdjustSwitch.on;
        [FlurryAPI logEvent:(self.autoAdjustSwitch.on ? @"Menu-gyrosteering-ON" : @"Menu-gyrosteering-OFF")];
    }
    [RKRotationRateCommand sendCommandWithRate:app_settings.rotationRate];    
}

-(void) done
{
    [self saveSettings];
    [[SpheroItemSelectSound sharedSound] play];
    
	if(self.navigationController) {
        CATransition* transition = [CATransition animation];
        transition.duration = 0.5;
        transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
        transition.type = kCATransitionReveal;
        [self.navigationController.view.layer addAnimation:transition forKey:nil];
        [self.navigationController popToViewController:[[self.navigationController viewControllers] objectAtIndex:0] animated:NO];
		return;
	}
	[self dismissModalLayerViewControllerAnimated:YES];
}

-(void) back
{
    [self saveSettings];
    [[SpheroItemSelectSound sharedSound] play];
    
	if(self.navigationController) {
		[self.navigationController popViewControllerAnimated:YES];
		return;
	}
	[self dismissModalLayerViewControllerAnimated:YES];    
}

-(void) optionsViewControllerDidFinish:(OptionsViewController *)controller
{
	[self updateLevelTitles];
	[UIView transitionFromView:optionsController.view toView:self.view 
					  duration:1.0 
					   options:UIViewAnimationOptionShowHideTransitionViews | 
							   UIViewAnimationOptionTransitionFlipFromLeft
					completion:^(BOOL finished)
						 { 
							 [optionsController release];
							 optionsController = nil;
						 }];
}

-(IBAction) volumeChanged:(id)sender {
    [FlurryAPI logEvent:@"VolumeChanged"];
    UISlider *slider = (UISlider*)sender;
    [DriveAppSettings defaultSettings].soundFXVolume = slider.value;
}

- (void)hideRollButton
{
    hideRollButton = YES;
    rollButton.hidden = YES;
    rollLabel.hidden = YES;
}

-(void) viewWillAppear:(BOOL)animated
{
	[super viewWillAppear:animated];
    rollButton.hidden = hideRollButton;
    rollLabel.hidden = hideRollButton;
	[self initializeControls];
	[self setupGestures];
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

-(void) initializeControls
{
	[self updateLevelTitles];
    [self setSelectedLevel:[DriveAppSettings defaultSettings].sensitivityLevel];

	DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    self.autoAdjustSwitch.on = settings.gyroSteering;
    self.volumeSlider.value = settings.soundFXVolume;
}

-(void) updateLevelTitles
{
	DriveAppSettings* settings = [DriveAppSettings defaultSettings];
	self.level1Lbl.text = settings.level1SettingsName;
	self.level2Lbl.text = settings.level2SettingsName;
	self.level3Lbl.text = settings.level3SettingsName;
}

-(void) setupGestures
{
	[self addDoubleTapGesture:self.level1Btn];
	[self addDoubleTapGesture:self.level2Btn];
	[self addDoubleTapGesture:self.level3Btn];
	[self addSingleTapGesture:self.level1Btn];
	[self addSingleTapGesture:self.level2Btn];
	[self addSingleTapGesture:self.level3Btn];
}

-(void)addDoubleTapGesture:(UIView*)view
{
	UITapGestureRecognizer* doubleTap = [[UITapGestureRecognizer alloc]
										 initWithTarget:self action:@selector(handleDoubleTap:)];
	[doubleTap setNumberOfTapsRequired:2];
	[view addGestureRecognizer:doubleTap];
	[doubleTap release];
}

-(void)addSingleTapGesture:(UIView*)view
{
	UITapGestureRecognizer* singleTap = [[UITapGestureRecognizer alloc]
										 initWithTarget:self action:@selector(handleSingleTap:)];
	[view addGestureRecognizer:singleTap];
	[singleTap release];
}

-(void)handleDoubleTap:(UIGestureRecognizer*)recognizer
{
	[FlurryAPI logEvent:@"Menu-Sensitivity-LevelCustomized"];
	optionsController = [[OptionsViewController alloc] 
							initWithNibName:@"OptionsViewController" bundle:nil];
	optionsController.delegate = self;

	optionsController.view.hidden = YES;
	optionsController.view.center = self.view.center;
	[self.view.superview addSubview:optionsController.view];
	[optionsController viewWillAppear:YES];
	[UIView transitionFromView:self.view toView:optionsController.view 
					duration:1.0 
					options:UIViewAnimationOptionShowHideTransitionViews | UIViewAnimationOptionTransitionFlipFromRight
					completion:^(BOOL finished){[optionsController viewDidAppear:YES];}];
}

-(void)handleSingleTap:(UIGestureRecognizer*)recognizer
{
	if( recognizer.state == UIGestureRecognizerStateEnded )
	{
        [[SpheroItemSelectSound sharedSound] play];
		DriveAppSettings* settings = [DriveAppSettings defaultSettings];
		CGPoint pnt = [recognizer locationInView:self.view];
		
		if( CGRectContainsPoint(self.level1Btn.frame, pnt) ) {
			settings.sensitivityLevel = DriveAppSensitivityLevel1;
			[FlurryAPI logEvent:@"Menu-Sensitivity-Level1Set"];
		} else if( CGRectContainsPoint(self.level2Btn.frame, pnt) ) {
			settings.sensitivityLevel = DriveAppSensitivityLevel2;
			[FlurryAPI logEvent:@"Menu-Sensitivity-Level2Set"];
		} else if( CGRectContainsPoint(self.level3Btn.frame, pnt) ) {
			settings.sensitivityLevel = DriveAppSensitivityLevel3;
			[FlurryAPI logEvent:@"Menu-Sensitivity-Level3Set"];
		}
		[self setSelectedLevel:settings.sensitivityLevel];
	}
}

-(void) setSelectedLevel:(DriveAppSensitivityLevel)l
{
	CGFloat level1_alpha = ((l == DriveAppSensitivityLevel1) ? 1.0 : 0.3);
	self.level1Btn.alpha = level1_alpha;
	self.level1Lbl.alpha = level1_alpha;
	
	CGFloat level2_alpha = ((l == DriveAppSensitivityLevel2) ? 1.0 : 0.3);
	self.level2Btn.alpha = level2_alpha;
	self.level2Lbl.alpha = level2_alpha;
	
	CGFloat level3_alpha = ((l == DriveAppSensitivityLevel3) ? 1.0 : 0.3);
	self.level3Btn.alpha = level3_alpha;
	self.level3Lbl.alpha = level3_alpha;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    UIFont* font16 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:16];
    UIFont* font12 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:12];
    UIFont* bold28 = [UIFont fontWithName:@"HelveticaRounded LT Bold" size:28];
    [self.level1Lbl setFont:font16];
    [self.level2Lbl setFont:font16];
    [self.level3Lbl setFont:font16];
    
    for( UIView* view in self.view.subviews ) {
        if( ![view isKindOfClass:[UILabel class]] )
            continue;
        
        UILabel* label = (UILabel*)view;
        if( label.tag == 100 ) {
            [label setFont:font12];
        } else if( label.tag == 200 ) {
            [label setFont:bold28];
        }
    }
    
    // Customize the sound slider appearance.
    UIImage* minImage = [UIImage imageNamed:@"SliderLeftCap.png"];
    UIImage* maxImage = [UIImage imageNamed:@"SliderRightCap.png"];
    UIImage* thumbImage = [UIImage imageNamed:@"SliderThumb.png"];
    
    minImage = [minImage stretchableImageWithLeftCapWidth:6 topCapHeight:0];
    maxImage = [maxImage stretchableImageWithLeftCapWidth:6 topCapHeight:0];
    [volumeSlider setMinimumTrackImage:minImage forState:UIControlStateNormal];
    [volumeSlider setMaximumTrackImage:maxImage forState:UIControlStateNormal];
    [volumeSlider setThumbImage:thumbImage forState:UIControlStateNormal];
}

- (void)viewDidUnload {
    [super viewDidUnload];
	
	self.level1Btn = nil;
	self.level2Btn = nil;
	self.level3Btn = nil;
    self.level1Lbl = nil;
    self.level2Lbl = nil;
    self.level3Lbl = nil;
}

- (void)dealloc {
    [level1Btn release];
    [level2Btn release];
    [level3Btn release];
    [level1Lbl release];
    [level2Lbl release];
    [level3Lbl release];
    [super dealloc];
}


@end
