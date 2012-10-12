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
#import "WEPopoverController.h"

@interface MainMenuViewController : UIViewController <RUIColorPickerDelegate, UIAlertViewDelegate>  {
	RKDeviceConnection*  connection;
    UIView*              exitView;
    UITextField*         nameEditView;
    UILabel*             nameLabel;
    IBOutlet UIView*     rollButton;
    IBOutlet UIView*     rollLabel;
    BOOL                 hideRollButton;
    WEPopoverController* popoverController;
    SEL                  chosenAction;
    id                   delegate;
}

@property (nonatomic, retain) IBOutlet UIView* exitView;
@property (nonatomic, retain) IBOutlet UITextField* nameEditView;
@property (nonatomic, retain) IBOutlet UILabel* nameLabel;
@property (nonatomic, retain) WEPopoverController* popoverController;
@property (nonatomic, assign) SEL chosenAction;
@property (nonatomic, assign) id delegate;

- (IBAction)leaderboard;
- (IBAction)settings;
- (IBAction)tutorial;
- (IBAction)info;
- (IBAction)close;
- (IBAction)nameFieldDidEndEdit:(UITextField*)textField;
- (IBAction)sleep:(id)sender;
- (IBAction)userGuide:(id)sender;

- (void)hideRollButton;

@end
