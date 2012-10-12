//
//  RKCalibrateOverlayView.m
//  SpheroDraw
//
//  Created by Jon Carroll on 11/4/11.
//  Copyright (c) 2011 Orbotix. All rights reserved.
//

#import "RKCalibrateOverlayView.h"
#import <QuartzCore/QuartzCore.h>
#import "CalibrateOverlayEnd.h"
#import "CalibrateOverlayLoop.h"
#import "CalibrateOverlayStartSound.h"

@interface RKCalibrateOverlayView ()
-(void)animationLoop;
@end

@implementation RKCalibrateOverlayView

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
        innerRing = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-InnerCalibrateRing.png"]];
        middleRing = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-MiddleCalibrateRing.png"]];
        outerRing = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-OuterCalibrateRing.png"]];
        
        //dot1 = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-TwoFingerCalibrateTouchGlowBlue.png"]];
        //dot2 = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-TwoFingerCalibrateTouchGlowBlue.png"]];
        dot1 = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-TwoFingerCalibrateTouchGlowWhite.png"]];
        dot2 = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"Sphero-TwoFingerCalibrateTouchGlowWhite.png"]];
        
        [self addSubview:middleRing];
        [self addSubview:innerRing];
        [self addSubview:outerRing];
        [self addSubview:dot1];
        [self addSubview:dot2];
        
        middleRing.frame = CGRectMake(0, 0, MAX(frame.size.width, frame.size.height), MAX(frame.size.width, frame.size.height));
        innerRing.frame = CGRectMake(0, 0, MAX(frame.size.width, frame.size.height), MAX(frame.size.width, frame.size.height));
        outerRing.frame = CGRectMake(0, 0, MAX(frame.size.width, frame.size.height), MAX(frame.size.width, frame.size.height));
        middleRing.center = CGPointMake(CGRectGetMidX(frame), CGRectGetMidY(frame));
        innerRing.center = CGPointMake(CGRectGetMidX(frame), CGRectGetMidY(frame));
        outerRing.center = CGPointMake(CGRectGetMidX(frame), CGRectGetMidY(frame));
        
        innerRing.layer.anchorPoint = CGPointMake(0.5, 0.5);
        outerRing.layer.anchorPoint = CGPointMake(0.5, 0.5);
        
        [innerRing release];
        [middleRing release];
        [outerRing release];
        [dot1 release];
        [dot2 release];
        
        self.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.6];
        self.userInteractionEnabled = YES;
        
        animating = NO;
        innerRotation = 0.0;
        outerRotation = 0.0;
        
    }
    return self;
}

-(void)point1Moved:(CGPoint)p1 point2Moved:(CGPoint)p2 withRotation:(float)rotation {
    point1 = p1;
    point2 = p2;
    if(!animating) {
        dot1.center = point1;
        dot2.center = point2;
        [[CalibrateOverlayStartSound sharedSound] play];
        [self performSelector:@selector(startAudioLoop) withObject:nil afterDelay:0.3];
        lastRotation = rotation;
        currentRotation = rotation;
        rotationDif = 0;
        animating = YES;
        [self animationLoop];
    } else {
        currentRotation = rotation;
    }
}

-(void)startAudioLoop {
    if(!animating) return;
    [[[CalibrateOverlayLoop sharedSound] player] play];
}

-(void)calibrationDone {
    animating = NO;
    [[[CalibrateOverlayLoop sharedSound] player] stop];
    [[CalibrateOverlayEnd sharedSound] play];
    [UIView animateWithDuration:0.2
                          delay:0.0
                        options:(/* UIViewAnimationOptionBeginFromCurrentState | */UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveLinear )
                     animations:^{
                         dot1.alpha = 0.0;
                         dot2.alpha = 0.0;
                         innerRing.transform = CGAffineTransformScale(innerRing.transform, 4.0, 4.0);
                         outerRing.transform = CGAffineTransformScale(outerRing.transform, 4.0, 4.0);
                         middleRing.transform = CGAffineTransformScale(middleRing.transform, 4.0, 4.0);
                     }
                     completion:^(BOOL complete){
                     }];
}

-(void)animationLoop {
    if(!animating) return;
    
    CGPoint newCenter;
    float xDif = MAX(point1.x, point2.x) - MIN(point1.x, point2.x);
    newCenter.x = MIN(point1.x, point2.x) + (xDif * 0.5);
    float yDif = MAX(point1.y, point2.y) - MIN(point1.y, point2.y);
    newCenter.y = MIN(point1.y, point2.y) + (yDif * 0.5);
    float size = sqrtf((xDif*xDif) + (yDif*yDif));
    float scale = size / middleRing.frame.size.width;
    rotationDif = lastRotation - currentRotation;
    lastRotation = currentRotation;
    
    if(rotationDif > 1.0 || rotationDif < -1.0) {
        innerRotation = (rotationDif * (M_PI/180.0)) * 0.25;
        outerRotation = (rotationDif * (M_PI/180.0)) * -0.25;
    } else {
        outerRotation *= 0.0;
        innerRotation *= 0.0;
    }
    
    
    [UIView animateWithDuration:0.1
                          delay:0.0
                        options:(/* UIViewAnimationOptionBeginFromCurrentState | */UIViewAnimationOptionAllowUserInteraction | UIViewAnimationCurveLinear )
                     animations:^{
                         outerRing.center = newCenter;
                         innerRing.center = newCenter;
                         middleRing.center = newCenter;
                         
                         dot1.center = point1;
                         dot2.center = point2;
                         
                         innerRing.transform = CGAffineTransformRotate(innerRing.transform, innerRotation * M_PI);
                         outerRing.transform = CGAffineTransformRotate(outerRing.transform, outerRotation * M_PI);
                         innerRing.transform = CGAffineTransformScale(innerRing.transform, scale, scale);
                         outerRing.transform = CGAffineTransformScale(outerRing.transform, scale, scale);
                         middleRing.transform = CGAffineTransformScale(middleRing.transform, scale, scale);
                     }
                     completion:^(BOOL complete){
                         [self animationLoop];
                     }];
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
