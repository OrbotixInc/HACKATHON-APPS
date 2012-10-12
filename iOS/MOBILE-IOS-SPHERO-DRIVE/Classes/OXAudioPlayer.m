//
//  OXAudioPlayer.m
//  Orbotix
//
//  Created by Jon Carroll on 7/27/11.
//  Copyright 2011 Orbotix, Inc. All rights reserved.
//

#import "OXAudioPlayer.h"
#import "OXAudioManager.h"
#import <AudioToolbox/AudioToolbox.h>
#import "DriveAppSettings.h"

@implementation OXAudioPlayer

@synthesize player, playing, soundFileURL;

-(void)play {
    if([self deviceIsSilenced]) return;
	if(playing) return;
	[[OXAudioManager sharedManager] playingAudio:self];
    [player setVolume:[DriveAppSettings defaultSettings].soundFXVolume];
	[player play];
}

- (BOOL)deviceIsSilenced
{
#if TARGET_IPHONE_SIMULATOR
    // return NO in simulator. Code causes crashes for some reason.
    return NO;
#endif
    
    CFStringRef state;
    UInt32 propertySize = sizeof(CFStringRef);
    AudioSessionInitialize(NULL, NULL, NULL, NULL);
    AudioSessionGetProperty(kAudioSessionProperty_AudioRoute, &propertySize, &state);
    
    return (CFStringGetLength(state) <= 0);
}

-(id)initWithFilename:(NSString*)filename {
	self = [super init];
	NSString *soundFilePath = [[NSBundle mainBundle] pathForResource:filename ofType:nil];
	NSURL *url = [[NSURL alloc] initFileURLWithPath:soundFilePath];
	self.soundFileURL = url;
	[url release];
	
	player = [[AVAudioPlayer alloc] initWithContentsOfURL: soundFileURL error: nil];
	
	playing = NO;
	
    // Must set delegate first to initialize audio manager correctly.
	[player setDelegate:[OXAudioManager sharedManager]];
	[player prepareToPlay];
	[player setVolume:1.0];
	
	return self;
}

-(void)dealloc {
	[soundFileURL release]; soundFileURL = nil;
	[player release]; player = nil;
	[super dealloc];
}

@end
