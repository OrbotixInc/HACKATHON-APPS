//
//  ChangeNameViewController.h
//  Sphero
//
//  Created by Brian Alexander on 4/28/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>

@interface ChangeNameViewController : RUIModalLayerViewController 
                                      <UITextFieldDelegate> {
	@private
	UILabel*         oldNameLabel;
	UITextField*     nameField;
	RKRobotControl*  robotControl;
	NSString*        currentName;
}

@property (nonatomic, retain) IBOutlet UILabel*      oldNameLabel;
@property (nonatomic, retain) IBOutlet UITextField*  nameField;
@property (nonatomic, assign) RKRobotControl*        robotControl;
@property (nonatomic, retain) NSString*              currentName;

- (IBAction) nameFieldDidEndEditing:(UITextField*)textField;
- (IBAction) done;

- (void)setCurrentName:(NSString*)name;

@end
