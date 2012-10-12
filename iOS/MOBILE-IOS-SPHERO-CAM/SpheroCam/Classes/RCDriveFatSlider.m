//
//  RCDriveFatSlider.m
//  RCDrive
//
//  Created by Jon Carroll on 6/13/11.
//  Copyright 2011 Orbotix, Inc. All rights reserved.
//

#import "RCDriveFatSlider.h"


@implementation RCDriveFatSlider

#define SIZE_EXTENSION_Y -30

- (BOOL) pointInside:(CGPoint)point withEvent:(UIEvent*)event {
    CGRect bounds = self.bounds;
    bounds = CGRectInset(bounds, 0, SIZE_EXTENSION_Y);
    return CGRectContainsPoint(bounds, point);
}

@end
