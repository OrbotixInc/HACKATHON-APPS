//
//  CircularSlider.h
//  Sphero
//
//  Created by Jon Carroll on 7/25/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//
//	A circular slider class somewhat similar to a UISlider with more options
//  Originally designed for RCDrive controls in the iPad Sphero drive app
//  Slider will let you rotate from left angle-right angle across 0.0
//  The left max angle will make value reflect it's minimum and the right
//  most angle will make the value reflect it's maximum.
//

#import <UIKit/UIKit.h>


@interface CircularSlider : UIView {
	float					leftMax; 
	float					rightMax; 
	float					origin; 
	BOOL					originAutoReturn; 
	BOOL					rotatePuck; 
	float					minValue;
	float					maxValue;
	float					value;
	
	@private
	UIImageView				*wheelView;
	UIImageView				*knobView;
	CGFloat					rotationRadius;
	CGPoint					wheelCenter;
	CGFloat					currentAngle;
	CGFloat					wheelBorderSize;
}

@property float				leftMax; //The leftmost limit in the circular slider (default = 270.0)
@property float				rightMax;//The rightmost limit in the circular slider (default = 90.0)
@property float				origin; ////Where the slider puck starts on the circle (default = 0.0)
@property BOOL				originAutoReturn; //YES to have slider puch auto return to origin when touches end (default = YES)
@property BOOL				rotatePuck; //YES to have puck image rotate automatically with the slide (default = YES)
@property float				minValue; //The min value returned when slider is at leftMax angle (default = 0.0)
@property float				maxValue; //The max value returned when slider is at rightMax angle (default = 1.0)
@property (readonly) float	value; //The current value represented by the slider position


- (void)setKnobImage:(UIImage*)image;
- (void)setWheelImage:(UIImage*)image;

- (void)commonInit;

- (void)rotateControlKnob;
- (void)positionControlKnob;

@end
