//
//  CalibrateTutorialView.m
//  Sphero
//
//  Created by Jon Carroll on 11/16/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "CalibrateTutorialView.h"

@implementation CalibrateTutorialView

@synthesize delegate;

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    [super touchesBegan:touches withEvent:event];
    //if([touches count] > 1) {
        [delegate performSelector:@selector(twoFingerPress:) withObject:[NSNumber numberWithBool:YES]];
    //}
}

-(void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    [super touchesMoved:touches withEvent:event];
    //if([touches count] > 1) {
        [delegate performSelector:@selector(twoFingerPress:) withObject:[NSNumber numberWithBool:YES]];
    //}
}

-(void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    [super touchesEnded:touches withEvent:event];
    [delegate performSelector:@selector(twoFingerPress:) withObject:[NSNumber numberWithBool:NO]];
}

-(void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event {
    [super touchesCancelled:touches withEvent:event];
    [delegate performSelector:@selector(twoFingerPress:) withObject:[NSNumber numberWithBool:NO]];
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
