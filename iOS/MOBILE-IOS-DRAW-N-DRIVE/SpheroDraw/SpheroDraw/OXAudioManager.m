//
//  OXAudioManager.m
//  Orbotix
//
//  Created by Jon Carroll on 7/27/11.
//  Copyright 2011 Orbotix, Inc. All rights reserved.
//

#import "OXAudioManager.h"

static OXAudioManager *sharedOXAudioManager = nil;

@implementation OXAudioManager

+(OXAudioManager*)sharedManager {
	if(sharedOXAudioManager == nil) {
		sharedOXAudioManager = [OXAudioManager new];
	}
	return sharedOXAudioManager;
}

-(void)dealloc {
	[playingAudio release];
	[super dealloc];
}

-(id)init {
	self = [super init];
	
	playingAudio = [[NSMutableArray alloc] init];
	
	// Registers this class as the delegate of the audio session.
	[[AVAudioSession sharedInstance] setDelegate: self];
	
	// The AmbientSound category allows application audio to mix with Media Player
	// audio. The category also indicates that application audio should stop playing 
	// if the Ring/Siilent switch is set to "silent" or the screen locks.
	[[AVAudioSession sharedInstance] setCategory: AVAudioSessionCategoryAmbient error: nil];
	/*
	 // Use this code instead to allow the app sound to continue to play when the screen is locked.
	 [[AVAudioSession sharedInstance] setCategory: AVAudioSessionCategoryPlayback error: nil];
	 
	 UInt32 doSetProperty = 0;
	 AudioSessionSetProperty (
	 kAudioSessionProperty_OverrideCategoryMixWithOthers,
	 sizeof (doSetProperty),
	 &doSetProperty
	 );
	 */
	
	// Registers the audio route change listener callback function
	/*AudioSessionAddPropertyListener (
									 kAudioSessionProperty_AudioRouteChange,
									 audioRouteChangeListenerCallback,
									 self
									 );*/
	
	// Activates the audio session.
	
	NSError *activationError = nil;
	[[AVAudioSession sharedInstance] setActive: YES error: &activationError];
	
	if(activationError) NSLog(@"OXAudioManager - Error activating AVAudioSession - %@", [activationError localizedDescription]);
	
	return self;
}

-(void)playingAudio:(OXAudioPlayer*)player {
	player.playing = YES;
	[playingAudio addObject:player];
}

#pragma mark AV Foundation delegate methods____________

- (void) audioPlayerDidFinishPlaying: (AVAudioPlayer *) appSoundPlayer successfully: (BOOL) flag {
	OXAudioPlayer *foundPlayer = nil;
	for(OXAudioPlayer *player in playingAudio) {
		if(player.player == appSoundPlayer) foundPlayer = player;
	}
	if(foundPlayer) [playingAudio removeObject:foundPlayer];
	foundPlayer.playing = NO;
}

- (void) audioPlayerBeginInterruption: player {

}

- (void) audioPlayerEndInterruption: player {
	// Reactivates the audio session, whether or not audio was playing
	//		when the interruption arrived.
	[[AVAudioSession sharedInstance] setActive: YES error: nil];
	
	for(OXAudioPlayer *player in playingAudio) {
		[player.player prepareToPlay];
		[player.player play];
	}
}

@end
