//
//  OXAudioManager.h
//  Orbotix
//
//  Created by Jon Carroll on 7/27/11.
//  Copyright 2011 Orbotix, Inc. All rights reserved.
//
//	A shared singleton audio session manager to handle the AVAudioSession
//  setup.  Also manages XOAudioPlayer instances that are playing and
//  resuming them after an interruption.
//  OXAudioPlayer class instances should handle all the necessary interaction with this
//  class.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import "OXAudioPlayer.h"

@interface OXAudioManager : NSObject <AVAudioPlayerDelegate> {
	NSMutableArray *playingAudio;
}

+(OXAudioManager*)sharedManager;

-(void)playingAudio:(OXAudioPlayer*)player;

@end
