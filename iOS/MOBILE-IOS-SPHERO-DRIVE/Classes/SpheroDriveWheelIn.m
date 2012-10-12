//
//  SpheroDriveWheelIn.m
//  Sphero
//
//  Created by Jon Carroll on 9/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "SpheroDriveWheelIn.h"

static SpheroDriveWheelIn *sound = nil;

@implementation SpheroDriveWheelIn

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroDriveWheelIn alloc] initWithFilename:@"SpheroDriveWheelIn.wav"];
    }
    return sound;
}

@end
