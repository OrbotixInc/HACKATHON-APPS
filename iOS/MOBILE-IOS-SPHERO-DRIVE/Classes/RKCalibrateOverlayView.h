//
//  RKCalibrateOverlayView.h
//  SpheroDraw
//
//  Created by Jon Carroll on 11/4/11.
//  Copyright (c) 2011 Orbotix. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RKCalibrateOverlayView : UIView {
    @private
    UIImageView *outerRing, *middleRing, *innerRing;
    UIImageView *dot1, *dot2;
    BOOL animating;
    CGPoint point1, point2;
    float innerRotation, outerRotation;
    float rotationDif, lastRotation, currentRotation;
}

-(void)point1Moved:(CGPoint)p1 point2Moved:(CGPoint)p2 withRotation:(float)rotation;
-(void)calibrationDone;

@end
