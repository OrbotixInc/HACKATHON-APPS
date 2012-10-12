//
//  CircularSlider.m
//  Sphero
//
//  Created by Jon Carroll on 7/25/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "CircularSlider.h"

#define WHEEL_BORDER_RATIO 0.0

@implementation CircularSlider

@synthesize leftMax, rightMax, origin, originAutoReturn, rotatePuck, minValue, maxValue, value;

- (id)initWithFrame:(CGRect)frame {
    
    self = [super initWithFrame:frame];
    if (self) {
        [self commonInit];
    }
    return self;
}

- (id)initWithCoder:(NSCoder*)aDecoder {
	self = [super initWithCoder:aDecoder];
	if( self != nil )
	{
		[self commonInit];
	}
	return self;
}

- (id)init {
	self = [super init];
	if(self) {
		[self commonInit];
	}
	return self;
}

-(void)commonInit {
	self.leftMax = 270.0;
	self.rightMax = 90.0;
	self.origin = 0.0;
	self.originAutoReturn = YES;
	self.rotatePuck = YES;
	self.minValue = 0.0;
	self.maxValue = 0.0;
	value = 0.5;
	
	self.contentMode = UIViewContentModeScaleToFill;
	self.opaque = NO;
	self.backgroundColor = [UIColor clearColor];
	
	// Create our wheel view, as large as will fit with the correct aspect in
	// our frame and center it.
	float wheelSize = MIN(self.bounds.size.width, self.bounds.size.height);
	
	wheelView = [[UIImageView alloc] init];
	wheelView.bounds = CGRectMake(0, 0, wheelSize, wheelSize);
	CGPoint pos = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
	wheelView.center = pos;
	[self addSubview:wheelView];
	
	
	knobView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"SpheroDrive-ipad-RCControlSlider.png"]];
	knobView.bounds = CGRectMake(0, 0, 131, 130);
	knobView.userInteractionEnabled = YES;
	
	
	wheelBorderSize = (wheelSize * WHEEL_BORDER_RATIO);
	rotationRadius = (wheelSize / 2.0) - wheelBorderSize - (130.0 / 2.0);
	wheelCenter = CGPointMake(CGRectGetMidX(wheelView.frame), 
							  CGRectGetMidY(wheelView.frame));
	currentAngle = 0.0;
	knobView.center = CGPointMake(wheelCenter.x, wheelCenter.y - rotationRadius);
	[self addSubview:knobView];
	
	
	UIPanGestureRecognizer* pan_recognizer =
	[[UIPanGestureRecognizer alloc] initWithTarget:self 
											action:@selector(handlePanGesture:)];
	[knobView addGestureRecognizer:pan_recognizer];
	[pan_recognizer release];
}

- (void)setWheelImage:(UIImage*)image {
	[wheelView setImage:image];
}

- (void)setKnobImage:(UIImage*)image {
	[knobView setImage:image];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code.
}
*/
 
-(void)setOrigin:(float)o {
	origin = o;
	currentAngle = o * (M_PI / 180);
}

-(void)rotateControlKnob {
	[self positionControlKnob];
}

- (void)positionControlKnob {
	if( !rotatePuck ) {
		CGPoint point_on_radius = CGPointMake(rintf(rotationRadius * sin(currentAngle)),
											  rintf(rotationRadius * cos(currentAngle)));
	knobView.center = CGPointMake(point_on_radius.x + wheelCenter.x,
								  wheelCenter.y - point_on_radius.y);
	} else {
		self.transform = CGAffineTransformMakeRotation(currentAngle);
	}
	
	//convert angle to degrees for calculations
	float degrees = (currentAngle >= 0.0) ? 
	currentAngle : 2.0 * M_PI + currentAngle;
	degrees *= (180.0 / M_PI);
	
	//Shift our degrees up in the case of a zero crossing
	float shiftedDegrees = (leftMax >= rightMax && degrees < leftMax) ? degrees + 360.0 : degrees;
	float shiftedRight = (leftMax > rightMax) ? rightMax + 360.0 : rightMax;
	float newValue = (shiftedDegrees - leftMax) / (shiftedRight - leftMax);
	//NSLog(@"New value: %1.3f", newValue);
	value = ((maxValue - minValue) * newValue ) + minValue;
	//NSLog(@"Calculated value: %1.3f", value);
}

- (void)handlePanGesture:(UIPanGestureRecognizer *)recognizer {
	// Keep the control knob's center on the radial path in the wheel's bounds
	if( recognizer.state == UIGestureRecognizerStateEnded ) {
		//Return the puck for the origin if autoReturn is enabled
		if(self.originAutoReturn) currentAngle = self.origin * (M_PI / 180);
		
		[self positionControlKnob];
		
		return;
	} else if( recognizer.state == UIGestureRecognizerStateChanged ) {
		CGPoint change_vector = [recognizer locationInView:self.superview];
		CGPoint current_point = CGPointMake(change_vector.x - self.center.x,
											self.center.y - change_vector.y);
		//get current angle in radians for rotation
		currentAngle = atan2(current_point.x, current_point.y);
		
		//convert angle to degrees for calculations
		float degrees = (currentAngle >= 0.0) ? 
		currentAngle : 2.0 * M_PI + currentAngle;
		degrees *= (180.0 / M_PI);
		
		NSLog(@"Current angle: %1.3f", degrees);
		
		//check that we are within the controls angle limits
		if(degrees < self.leftMax && degrees > self.rightMax) return;
		
		[self positionControlKnob];
	}
}

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent*)event {
	if( CGRectContainsPoint(knobView.frame, point) )
		return YES;
	if( wheelView.image != nil )
		return CGRectContainsPoint(wheelView.frame, point);
	return NO;
}


- (void)dealloc {
	[knobView release]; knobView = nil;
	[wheelView release]; wheelView = nil;
	
    [super dealloc];
}


@end
