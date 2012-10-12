//
//  SpheroCamRecordStop.m
//  AVCam
//
//  Created by Jon Carroll on 11/1/11.
//  Copyright (c) 2011 Orbotix, Inc. All rights reserved.
//

#import "SpheroCamRecordStop.h"

static SpheroCamRecordStop *sound = nil;

@implementation SpheroCamRecordStop

+(OXAudioPlayer*)sharedSound {
    if(sound==nil) {
        sound = [[SpheroCamRecordStop alloc] initWithFilename:@"SpheroCamRecordStop.wav"];
    }
    return sound;
}

@end
