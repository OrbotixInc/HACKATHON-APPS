//
//  CalibrateOverlayLoop.m
//  SpheroDraw
//
//  Created by Jon Carroll on 11/5/11.
//  Copyright (c) 2011 Orbotix. All rights reserved.
//

#import "CalibrateOverlayLoop.h"

static CalibrateOverlayLoop *sound = nil;

@implementation CalibrateOverlayLoop

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[CalibrateOverlayLoop alloc] initWithFilename:@"2-finger HUD_hum.wav"];
        sound.player.numberOfLoops = 99999;
        sound.player.volume = 0.6;
    }
    return sound;
}

@end
