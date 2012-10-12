//
//  OptionsViewController.h
//  Sphero
//
//  Created by Brian Smith on 12/16/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotUIKit/RobotUIKit.h>

@protocol OptionsViewControllerDelegate;

@interface OptionsViewController : UIViewController {
	id<OptionsViewControllerDelegate> delegate;
    
	UISlider    *maxSpeedSlider;
    UILabel     *maxSpeedLabel;
    UISlider    *boostTimeSlider;
    UILabel     *boostTimeLabel;
    UISlider    *boostVelocitySlider;
    UILabel     *boostVelocityLabel;
    UISlider    *rotationRateSlider;
    UILabel     *rotationRateLabel;
    UITextField *nameField;
}

@property (nonatomic, assign) id<OptionsViewControllerDelegate> delegate;

@property (nonatomic, retain) IBOutlet UISlider    *maxSpeedSlider;
@property (nonatomic, retain) IBOutlet UILabel     *maxSpeedLabel;
@property (nonatomic, retain) IBOutlet UISlider    *boostTimeSlider;
@property (nonatomic, retain) IBOutlet UILabel     *boostTimeLabel;
@property (nonatomic, retain) IBOutlet UISlider    *boostVelocitySlider;
@property (nonatomic, retain) IBOutlet UILabel     *boostVelocityLabel;
@property (nonatomic, retain) IBOutlet UISlider    *rotationRateSlider;
@property (nonatomic, retain) IBOutlet UILabel     *rotationRateLabel;
@property (nonatomic, retain) IBOutlet UITextField *nameField;

- (IBAction)done;
- (IBAction)changeMaxSpeedValue:(UISlider *)slider;
- (IBAction)changeBoostTimeValue:(UISlider *)slider;
- (IBAction)changeBoostVelocity:(UISlider *)slider;
- (IBAction)changeRotationRateValue:(UISlider *)slider;
- (IBAction)nameFieldDidEndEditing:(UITextField*)textField;
- (IBAction)resetSettings;

@end

@protocol OptionsViewControllerDelegate

-(void) optionsViewControllerDidFinish:(OptionsViewController*)controller;

@end
