//
//  RCDriveViewController_iPad.m
//  Sphero
//
//  Created by Jon Carroll on 7/25/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "RCDriveViewController_iPad.h"
#import "DriveViewController.h"


@implementation RCDriveViewController_iPad

#pragma mark - View lifecycle

- (void)viewDidLoad
{
	[super viewDidLoad];
	
	leftSlider.leftMax = 345.0;
	leftSlider.rightMax = 110.0;
	leftSlider.origin = 110.0;
	leftSlider.maxValue = 0.0;
	leftSlider.minValue = 1.0;

	
	rightSlider.leftMax = 250.0;
	rightSlider.rightMax = 15.0;
	rightSlider.origin = 312.5;
	rightSlider.minValue = -1.0;
	rightSlider.maxValue = 1.0;

}

-(void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];
	
	[leftSlider rotateControlKnob];
	[rightSlider rotateControlKnob];
}

- (void)robotControlLoop
{
    if( !(*driveControl).driving ) return;
	[(*driveControl).robotControl driveWithCoord1:rightSlider.value coord2:leftSlider.value coord3:0.0];
    [self performSelector:@selector(robotControlLoop) withObject:nil afterDelay:0.2];
}


@end
