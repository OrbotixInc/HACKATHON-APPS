//
//  DriveControllerDelegate.h
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RUIColorIndicatorView;

@protocol DriveControllerDelegate <NSObject>

- (void)doBoost;
- (void)boostUncontrolled;

- (void)presentCalibrationViewForDriveController;
- (void)presentColorPickerViewForDriveController;
- (void)hideCallout;

@end
