//
//  CalibrateOverlayEnd.m
//  SpheroDraw
//
//  Created by Jon Carroll on 11/5/11.
//  Copyright (c) 2011 Orbotix. All rights reserved.
//

#import "CalibrateOverlayEnd.h"

static CalibrateOverlayEnd *sound = nil;

@implementation CalibrateOverlayEnd

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[CalibrateOverlayEnd alloc] initWithFilename:@"2-finger HUD_out.wav"];
    }
    return sound;
}

@end
