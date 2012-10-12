//
//  SpheroButtonPressSound.h
//  Sphero
//
//  Created by Jon Carroll on 9/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "OXAudioPlayer.h"

@interface SpheroButtonPressSound : OXAudioPlayer

+(OXAudioPlayer*)sharedSound;

@end
