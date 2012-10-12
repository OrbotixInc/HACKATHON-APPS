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

@end

@implementation OptionsViewController

@synthesize delegate;

@synthesize maxSpeedSlider;
@synthesize maxSpeedLabel;
@synthesize boostTimeSlider;
@synthesize boostTimeLabel;
@synthesize boostVelocitySlider;
@synthesize boostVelocityLabel;
@synthesize rotationRateSlider;
@synthesize rotationRateLabel;
@synthesize nameField;

- (void) done
{
	[self.delegate optionsViewControllerDidFinish:self];
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
                                boost_time];
}

- (void)changeBoostVelocity:(UISlider *)slider
{
    float boost_velocity = [slider value];
    [DriveAppSettings defaultSettings].controlledBoostVelocity = boost_velocity;
    self.boostVelocityLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                    boost_velocity];
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
    float velocity_scale = userdefs.velocityScale;
    [self.maxSpeedSlider setValue:velocity_scale animated:NO];
    self.maxSpeedLabel.text = [NSString stringWithFormat:ValueLabelFormat, 
                               velocity_scale];
    
    float boost_time = userdefs.boostTime;
    [self.boostTimeSlider setValue:boost_time animated:NO];
    self.boostTimeLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                boost_time];
    
    float boost_velocity = userdefs.controlledBoostVelocity;
    [self.boostVelocitySlider setValue:boost_velocity animated:NO];
    self.boostVelocityLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                    boost_velocity];
    
    float rotation_rate = userdefs.rotationRate;
    [self.rotationRateSlider setValue:rotation_rate animated:NO];
    self.rotationRateLabel.text = [NSString stringWithFormat:ValueLabelFormat,
                                   rotation_rate];
    
    self.nameField.text = userdefs.currentSettingsName;
}

- (void)viewDidUnload {
    [super viewDidUnload];
    self.maxSpeedSlider = nil;
    self.maxSpeedLabel = nil;
    self.boostTimeSlider = nil;
    self.boostTimeLabel = nil;
    self.boostVelocitySlider = nil;
    self.boostVelocityLabel = nil;
    self.rotationRateSlider = nil;
    self.rotationRateLabel = nil;
    self.nameField = nil;
}

- (void)dealloc {
    [maxSpeedSlider release]; maxSpeedSlider = nil;
    [maxSpeedLabel release]; maxSpeedLabel =nil;
    [boostTimeSlider release]; boostTimeSlider = nil;
    [boostTimeLabel release]; boostTimeLabel =  nil;
    [boostVelocityLabel release]; boostVelocityLabel = nil;
    [boostVelocitySlider release]; boostVelocitySlider = nil;
    [rotationRateSlider release]; rotationRateSlider = nil;
    [rotationRateLabel release]; rotationRateLabel = nil;
    [nameField release]; nameField = nil;
    [super dealloc];
}


@end
