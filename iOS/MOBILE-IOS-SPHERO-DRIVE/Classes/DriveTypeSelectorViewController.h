//
//  DriveTypeSelectorViewController.h
//  Sphero
//
//  Created by Brian Alexander on 7/11/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "DriveAppSettings.h"
#import <UIKit/UIKit.h>


@interface DriveTypeSelectorViewController : UIViewController {
    id delegate;
    UIButton* joystickButton;
    UIButton* tiltButton;
    UIButton* rcButton;
}

@property (nonatomic, assign) id delegate;
@property (nonatomic, retain) IBOutlet UIButton* joystickButton;
@property (nonatomic, retain) IBOutlet UIButton* tiltButton;
@property (nonatomic, retain) IBOutlet UIButton* rcButton;

- (IBAction)switchToJoystickDrive;
- (IBAction)switchToTiltDrive;
- (IBAction)switchToRCDrive;

- (void)hilightDriveType:(DriveAppDriveType)next withDelay:(float)delay;

@end
