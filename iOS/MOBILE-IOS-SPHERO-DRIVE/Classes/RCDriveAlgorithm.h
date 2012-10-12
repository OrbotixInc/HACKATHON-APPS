//
//  RCDriveAlgorithm.h
//  Sphero
//
//  Created by Brian Alexander on 7/11/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <RobotKit/RKDriveAlgorithm.h>


@interface RCDriveAlgorithm : RKDriveAlgorithm {
    @private
    float  maxTurnRate;
}

@property (nonatomic, assign) float maxTurnRate;

@end
