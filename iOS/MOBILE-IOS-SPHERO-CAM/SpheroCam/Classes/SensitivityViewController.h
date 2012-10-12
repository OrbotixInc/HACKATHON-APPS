//
//  SensitivityViewController.h
//  Sphero
//
//  Created by Brian Alexander on 4/3/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "OptionsViewController.h"


@interface SensitivityViewController : RUIModalLayerViewController <OptionsViewControllerDelegate> {
	UIImageView* level1Btn;
	UIImageView* level2Btn;
	UIImageView* level3Btn;
	UILabel*     level1Lbl;
	UILabel*     level2Lbl;
	UILabel*     level3Lbl;
    UISwitch*            autoAdjustSwitch;
    UISlider*            volumeSlider;
    IBOutlet UIView*     backButton;
    IBOutlet UIView*     backLabel;
    IBOutlet UIView*     rollButton;
    IBOutlet UIView*     rollLabel;
    BOOL                 hideRollButton;
	
	OptionsViewController* optionsController;
}

@property (nonatomic, retain) IBOutlet UIImageView* level1Btn;
@property (nonatomic, retain) IBOutlet UIImageView* level2Btn;
@property (nonatomic, retain) IBOutlet UIImageView* level3Btn;
@property (nonatomic, retain) IBOutlet UILabel*     level1Lbl;
@property (nonatomic, retain) IBOutlet UILabel*     level2Lbl;
@property (nonatomic, retain) IBOutlet UILabel*     level3Lbl;
@property (nonatomic, retain) IBOutlet UISwitch*    autoAdjustSwitch;
@property (nonatomic, retain) IBOutlet UISlider*    volumeSlider;

-(IBAction) back;
-(IBAction) done;

- (void)hideRollButton;

@end
