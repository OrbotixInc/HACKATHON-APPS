//
//  SpheroCamRecordStart.m
//  AVCam
//
//  Created by Jon Carroll on 11/1/11.
//  Copyright (c) 2011 Orbotix, Inc. All rights reserved.
//

#import "SpheroCamRecordStart.h"

static SpheroCamRecordStart *sound = nil;

@implementation SpheroCamRecordStart

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroCamRecordStart alloc] initWithFilename:@"SpheroCamRecordStart.wav"];
    }
    return sound;
}

@end
