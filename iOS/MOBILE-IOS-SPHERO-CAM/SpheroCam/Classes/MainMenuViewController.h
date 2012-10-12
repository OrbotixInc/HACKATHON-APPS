//
//  MainMenuViewController.h
//  Drive
//
//  Created by Brian Smith on 11/18/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "OptionsViewController.h"

@interface MainMenuViewController : UIViewController <RUIColorPickerDelegate, UIAlertViewDelegate>  {
	RKDeviceConnection*  connection;
    UIView*              exitView;
    UITextField*         nameEditView;
    UILabel*             nameLabel;
    IBOutlet UIView*     rollButton;
    IBOutlet UIView*     rollLabel;
    BOOL                 hideRollButton;

    SEL                  chosenAction;
    UIPopoverController *popover;
    UIViewController *delegate;
}

@property (nonatomic, retain) IBOutlet UIView* exitView;
@property (nonatomic, retain) IBOutlet UITextField* nameEditView;
@property (nonatomic, retain) IBOutlet UILabel* nameLabel;
@property (nonatomic, assign) SEL chosenAction;
@property (nonatomic, assign) UIPopoverController *popover;
@property (nonatomic, assign) UIViewController *delegate;

- (IBAction)leaderboard;
- (IBAction)settings;
- (IBAction)tutorial;
- (IBAction)info;
- (IBAction)close;
- (IBAction)nameFieldDidEndEdit:(UITextField*)textField;
- (IBAction)sleep:(id)sender;
-(IBAction)colorPressed:(id)sender;
-(IBAction)userGuide:(id)sender;

- (void)hideRollButton;

@end
