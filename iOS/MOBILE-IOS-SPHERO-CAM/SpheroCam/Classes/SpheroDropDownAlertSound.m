//
//  SpheroDropDownAlertSound.m
//  Sphero
//
//  Created by Jon Carroll on 10/5/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "SpheroDropDownAlertSound.h"

static SpheroDropDownAlertSound *sound = nil;

@implementation SpheroDropDownAlertSound

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroDropDownAlertSound alloc] initWithFilename:@"SpheroDropDownAlert.wav"];
    }
    return sound;
}

@end
