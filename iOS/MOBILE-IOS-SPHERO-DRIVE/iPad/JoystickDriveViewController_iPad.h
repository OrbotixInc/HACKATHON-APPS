//
//  JoystickDriveViewController_iPad.h
//  Sphero
//
//  Created by Jon Carroll on 7/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "JoystickDriveViewController.h"

@interface JoystickDriveViewController_iPad : JoystickDriveViewController {
	IBOutlet UIImageView	*leftPositionOutline;
	IBOutlet UIImageView	*rightPositionOutline;
	IBOutlet UIImageView	*centerPositionOutline;
	IBOutlet UIButton		*leftBoostButton;
	IBOutlet UIButton		*rightBoostButton;
	IBOutlet UIImageView	*leftBoostBG;
	IBOutlet UIImageView	*rightBoostBG;
	CGPoint					lastLocation;
	UIImageView				*lastOutline;
}

-(UIImageView*)getOutlineForLocation:(CGPoint)location;

@end
