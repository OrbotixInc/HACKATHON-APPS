//
//  SpheroTraySlideSound.m
//  Sphero
//
//  Created by Jon Carroll on 9/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "SpheroTraySlideSound.h"

static SpheroTraySlideSound *sound = nil;

@implementation SpheroTraySlideSound

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroTraySlideSound alloc] initWithFilename:@"SpheroTraySlide.wav"];
    }
    return sound;
}

@end
