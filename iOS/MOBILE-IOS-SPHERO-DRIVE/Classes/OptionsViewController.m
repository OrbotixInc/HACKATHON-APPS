//
//  OptionsViewController.m
//  Sphero
//
//  Created by Brian Smith on 12/16/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import "OptionsViewController.h"
#import "DriveAppSettings.h"

static NSString * const ValueLabelFormat = @"%.1f";

@interface OptionsViewController (Private)

-(void) initializeControls;
- (void)initializeForLevel1;
- (void)initializeForLevel2;
- (void)initializeForLevel3;

- (float)boostTime;

@end

@implementation OptionsViewController

@synthesize delegate;

@synthesize maxSpeedSlider;
@synthesize maxSpeedLabel;
@synthesize boostTimeSlider;
@synthesize boostTimeLabel;
@synthesize rotationRateSlider;
@synthesize rotationRateLabel;
@synthesize nameField;

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationLandscapeRight);
}

- (void) done
{
	[self.delegate optionsViewControllerDidFinish:self];
}

- (float)boostTime
{
    // 3 second boost range
	DriveAppSettings* userdefs = [DriveAppSettings defaultSettings];
    float boostTime = userdefs.boostTime;
    
    boostTime *= 3.0f;
    
    return boostTime;
}

- (IBAction)changeMaxSpeedValue:(UISlider *)slider
{
    float velocity_scale = [slider value];
	[DriveAppSettings defaultSettings].velocityScale = velocity_scale;
    self.maxSpeedLabel.text = [NSString stringWithFormat:ValueLabelFormat, 
                               velocity_scale];
}

- (void) changeBoostTimeValue:(UISlider *)slider
{
    float boost_time = [slider value];
	[DriveAppSettings defaultSettings].boostTime = boost_time;
    self.boostTimeLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                [self boostTime]];
}

- (void) changeRotationRateValue:(UISlider *)slider
{
    float rate = [slider value];
	[DriveAppSettings defaultSettings].rotationRate = rate;
    self.rotationRateLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                   rate];
}

- (void) nameFieldDidEndEditing:(UITextField *)textField
{
	[DriveAppSettings defaultSettings].currentSettingsName = textField.text;
	[textField resignFirstResponder];
}

- (void) resetSettings
{
	[[DriveAppSettings defaultSettings] resetCurrentSensitivitySettingsToDefault];
	[self initializeControls];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
	[self initializeControls];
}

- (void) initializeControls
{
	DriveAppSettings* userdefs = [DriveAppSettings defaultSettings];
    
    if (userdefs.sensitivityLevel == DriveAppSensitivityLevel1) {
        [self initializeForLevel1];
    } else if(userdefs.sensitivityLevel == DriveAppSensitivityLevel2) {
        [self initializeForLevel2];
    } else {
        [self initializeForLevel3];
    }
    
    float velocity_scale = userdefs.velocityScale;
    [self.maxSpeedSlider setValue:velocity_scale animated:NO];
    self.maxSpeedLabel.text = [NSString stringWithFormat:ValueLabelFormat, 
                               velocity_scale];
    
    float boost_time = userdefs.boostTime;
    [self.boostTimeSlider setValue:boost_time animated:NO];
    self.boostTimeLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                [self boostTime]];
    
    float rotation_rate = userdefs.rotationRate;
    [self.rotationRateSlider setValue:rotation_rate animated:NO];
    self.rotationRateLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                   rotation_rate];
    
    self.nameField.text = userdefs.currentSettingsName;
}

- (void)initializeForLevel1 {
    self.maxSpeedSlider.minimumValue = 0.0;
    self.maxSpeedSlider.maximumValue = 0.5;
    self.rotationRateSlider.minimumValue = 0.0;
    self.rotationRateSlider.maximumValue = 0.6;
}

- (void)initializeForLevel2 {
    self.maxSpeedSlider.minimumValue = 0.5;
    self.maxSpeedSlider.maximumValue = 0.8;
    self.rotationRateSlider.minimumValue = 0.3;
    self.rotationRateSlider.maximumValue = 0.8;
}

- (void)initializeForLevel3 {
    self.maxSpeedSlider.minimumValue = 0.7;
    self.maxSpeedSlider.maximumValue = 1.0;
    self.rotationRateSlider.minimumValue = 0.5;
    self.rotationRateSlider.maximumValue = 1.0;
}

- (void)viewDidUnload {
    [super viewDidUnload];
    self.maxSpeedSlider = nil;
    self.maxSpeedLabel = nil;
    self.boostTimeSlider = nil;
    self.boostTimeLabel = nil;
    self.rotationRateSlider = nil;
    self.rotationRateLabel = nil;
    self.nameField = nil;
}

- (void)dealloc {
    [maxSpeedSlider release]; maxSpeedSlider = nil;
    [maxSpeedLabel release]; maxSpeedLabel =nil;
    [boostTimeSlider release]; boostTimeSlider = nil;
    [boostTimeLabel release]; boostTimeLabel =  nil;
    [rotationRateSlider release]; rotationRateSlider = nil;
    [rotationRateLabel release]; rotationRateLabel = nil;
    [nameField release]; nameField = nil;
    [super dealloc];
}


@end
