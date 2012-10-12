//
//  CalibrateOverlayStartSound.m
//  SpheroDraw
//
//  Created by Jon Carroll on 11/5/11.
//  Copyright (c) 2011 Orbotix. All rights reserved.
//

#import "CalibrateOverlayStartSound.h"

static CalibrateOverlayStartSound *sound = nil;

@implementation CalibrateOverlayStartSound

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[CalibrateOverlayStartSound alloc] initWithFilename:@"2-finger HUD_in.wav"];
    }
    return sound;
}

@end
