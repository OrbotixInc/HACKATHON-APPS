//
//  DriveViewController_iPad.h
//  Sphero
//
//  Created by Brian Smith on 1/13/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "DriveViewController.h"
#import "WEPopoverController.h"
#import "MainMenuViewController.h"


@interface DriveViewController_iPad : DriveViewController <UIPopoverControllerDelegate> {
@private
    WEPopoverController* menuPopover;
	WEPopoverController* cpcPopover;
	WEPopoverController* calibrationPopover;
    UIPopoverController* sensitivityPopover;
    MainMenuViewController* menuController;
	RUIColorPickerViewController *cpc;
}

- (void)presentCalibrationView;
- (void)presentColorPickerView;

@end
