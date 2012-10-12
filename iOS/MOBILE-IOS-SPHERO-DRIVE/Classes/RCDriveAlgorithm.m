//
//  RCDriveAlgorithm.m
//  Sphero
//
//  Created by Brian Alexander on 7/11/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "RCDriveAlgorithm.h"

#import <RobotKit/RKMath.h>

@implementation RCDriveAlgorithm

@synthesize maxTurnRate;

- (id)init
{
    self = [super init];
    if( self == nil )
        return nil;
    
    maxTurnRate = 30.0;
    
    return self;
}

- (void)convertWithCoord1:(double)x coord2:(double)y coord3:(double)z
{
    // The x coordinate translates to an amount by which to rotate our
    // current heading.
    x = Clamp(x, -1.0, 1.0);
    float curHeading = self.angle;
    curHeading += (x * maxTurnRate);
    if( curHeading < 0 )
        curHeading += 360;
    if( curHeading >= 360 )
        curHeading -= 360;
    self.angle = curHeading;
    
    // The y coordinate translates to our forward velocity.
    // No need to move backwards since we can rotate in place.
    y = Clamp(y, 0.0, 1.0);
    self.velocity = self.velocityScale * (y * y);
    
    // NOTE: z is not used.

    // Perform any necessary actions now that we're done with the conversion.
    [self performConversionFinishedAction];
}

@end
