//
//  SpheroButtonPressSound.m
//  Sphero
//
//  Created by Jon Carroll on 9/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "SpheroButtonPressSound.h"

static SpheroButtonPressSound *sound = nil;

@implementation SpheroButtonPressSound

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroButtonPressSound alloc] initWithFilename:@"SpheroButtonPress.wav"];
    }
    return sound;
}

@end
