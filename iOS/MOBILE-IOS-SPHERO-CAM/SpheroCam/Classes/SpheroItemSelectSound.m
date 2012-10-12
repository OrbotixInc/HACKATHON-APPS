//
//  SpheroItemSelectSound.m
//  Sphero
//
//  Created by Jon Carroll on 9/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "SpheroItemSelectSound.h"

static SpheroItemSelectSound *sound = nil;

@implementation SpheroItemSelectSound

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroItemSelectSound alloc] initWithFilename:@"SpheroItemSelect.wav"];
    }
    return sound;
}

@end
