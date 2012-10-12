//
//  ColorWandView.h
//  Sphero
//
//  Created by Jon Carroll on 7/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RUIColorIndicatorView.h>

@interface ColorWandView : RUIColorIndicatorView {
	UIImageView *wandView;
	UIImageView *backgroundView;

}

-(void)initDefaults;
-(void)updateWandColor;

@end
