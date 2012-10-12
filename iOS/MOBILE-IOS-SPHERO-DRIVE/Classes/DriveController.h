//
//  DriveController.h
//  Sphero
//
//  Created by Brian Alexander on 7/14/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DriveAppSettings.h"

@class RUIColorIndicatorView;

@protocol DriveController <NSObject>

- (DriveAppDriveType)getDriveType;

- (void)resumeDriving;
- (void)updateUIForZeroSpeed;
- (void)initializeDriveControl;

- (UIView*)controlsView;
- (RUIColorIndicatorView*)colorIndicatorView;

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation;
- (void)prepareForTutorial;
- (void)tutorialDidDismiss;

@end
