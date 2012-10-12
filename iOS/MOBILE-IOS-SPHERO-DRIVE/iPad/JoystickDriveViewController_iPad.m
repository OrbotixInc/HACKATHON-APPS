//
//  JoystickDriveViewController_iPad.m
//  Sphero
//
//  Created by Jon Carroll on 7/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "JoystickDriveViewController_iPad.h"

#import <RobotKit/RobotKit.h>

//Alpha value for snap-to outline joystick will jump to if finger is lifted
#define kActiveOutlineAlpha		0.75
//Alpha value for snap-to outline joystick will NOT jump to if finger is lifted
#define kInactiveOutlineAlpha	0.2
//The scaling factor used to size the joystick when dragging in relation to the position it will snap-to
#define kSnapToScaleFactor		0.75
//Alpha value of the joystick when it is being drug around by the user
#define kJoystickDragAlpha		0.5

@interface JoystickDriveViewController_iPad ()

- (void)snapViewToOutline:(UIImageView*)controlOutline;

@end

@implementation JoystickDriveViewController_iPad

-(void)viewDidLoad {
	[super viewDidLoad];
	
	UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(wheelLongPress:)];
	longPress.minimumPressDuration = 1.0;
	[controlView addGestureRecognizer:longPress];
	[longPress release];
	
	longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(wheelLongPress:)];
	longPress.minimumPressDuration = 1.0;
	[driveControlPadView addGestureRecognizer:longPress];
	[longPress release];
	
	leftPositionOutline.alpha = 0.0;
	rightPositionOutline.alpha = 0.0;
	centerPositionOutline.alpha = 0.0;
}

//Callback from the long press on the joystick wheel to know when to start panning
-(void)wheelLongPress:(UIGestureRecognizer*)recognizer {
	
	if(recognizer.state == UIGestureRecognizerStateBegan) {
		lastOutline = nil;
		//Toggle view visiblility for dragging
		rightBoostButton.alpha = 0.0;
		leftBoostButton.alpha = 0.0;
		leftPositionOutline.alpha = kInactiveOutlineAlpha;
		rightPositionOutline.alpha = kInactiveOutlineAlpha;
		centerPositionOutline.alpha = kInactiveOutlineAlpha;
		controlView.alpha = kJoystickDragAlpha;
		joystickView.alpha = 0.0;
		leftBoostBG.alpha = 0.0;
		rightBoostBG.alpha = 0.0;
		
		//Scale down the size of the joystick for dragging
		CGPoint center = controlView.center;
		controlView.frame = CGRectMake(controlView.frame.origin.x, controlView.frame.origin.y, controlView.frame.size.width * kSnapToScaleFactor, controlView.frame.size.height * kSnapToScaleFactor);
		controlView.center = center;
		
	} else if(recognizer.state == UIGestureRecognizerStateChanged) {
		//Get the location of the touches
		CGPoint location = [recognizer locationInView:self.view];
		lastLocation = location;
		
		//Get the view we are snapping to based on the current location of the touches
		UIImageView *currentOutline = [self getOutlineForLocation:location];
		
		//If the outline the joystick will snap to has changed update the size accordingly
		if(lastOutline != currentOutline) {
			controlView.frame = CGRectMake(controlView.frame.origin.x, controlView.frame.origin.y, currentOutline.frame.size.width * kSnapToScaleFactor, currentOutline.frame.size.height * kSnapToScaleFactor);
		}
		lastOutline = currentOutline;
		
		//Move the joystick with the touches
		controlView.center = location;
		
		//Update the alhpa of the joystick image views based on which one we are going to snap to
		if(currentOutline != centerPositionOutline) centerPositionOutline.alpha = kInactiveOutlineAlpha;
		if(currentOutline != leftPositionOutline) leftPositionOutline.alpha = kInactiveOutlineAlpha;
		if(currentOutline != rightPositionOutline) rightPositionOutline.alpha = kInactiveOutlineAlpha;
		currentOutline.alpha = kActiveOutlineAlpha;
		
	} else if(recognizer.state == UIGestureRecognizerStateEnded) {
		//Get the image view to snap to based on the last known touch location
		UIImageView *currentOutline = [self getOutlineForLocation:lastLocation];
		[self snapViewToOutline:currentOutline];
    } 
}

- (void)snapViewToOutline:(UIImageView*)controlOutline
{
    //Toggle the visibility and location of views based on the location we are snapping to
    if(controlOutline == centerPositionOutline) {
        controlView.frame = centerPositionOutline.frame;
        rightBoostButton.alpha = 1.0;
        leftBoostButton.alpha = 1.0;
        leftBoostBG.alpha = 1.0;
        rightBoostBG.alpha = 1.0;
    } else if(controlOutline == leftPositionOutline) {
        controlView.frame = leftPositionOutline.frame;
        rightBoostButton.alpha = 1.0;
        leftBoostButton.alpha = 0.0;
        leftBoostBG.alpha = 0.0;
        rightBoostBG.alpha = 1.0;
    } else if(controlOutline == rightPositionOutline) {
        controlView.frame = rightPositionOutline.frame;
        leftBoostButton.alpha = 1.0;
        rightBoostButton.alpha = 0.0;
        leftBoostBG.alpha = 1.0;
        rightBoostBG.alpha = 0.0;
    }
    
    //Scale and position the inner joystick parts
    driveControlPadView.frame = CGRectMake(driveControlPadView.frame.origin.x, driveControlPadView.frame.origin.y, controlView.frame.size.width * 0.68, controlView.frame.size.height * 0.68);
    driveControlPadView.center = controlView.center;
    joystickView.frame = CGRectMake(joystickView.frame.origin.x, joystickView.frame.origin.y, driveControlPadView.frame.size.width * 0.54, driveControlPadView.frame.size.height * 0.54);
    joystickView.center = [self.driveControlPadView convertPoint:self.driveControlPadView.center fromView:self.driveControlPadView.superview];
    (*(self.driveControl)).joyStickSize = self.driveControlPadView.bounds.size;

    
    //Hide our snap-to outlines and make the puck visible again
    controlView.alpha = 1.0;
    joystickView.alpha = 1.0;
    leftPositionOutline.alpha = 0.0;
    rightPositionOutline.alpha = 0.0;
    centerPositionOutline.alpha = 0.0;
}

//Calculate the joystick image view to snap to based on the touch location passed in
-(UIImageView*)getOutlineForLocation:(CGPoint)location {
	//Smaller corner positions get preference in detection, default to center position
	if(location.x <= CGRectGetMaxX(leftPositionOutline.frame) && location.y >= CGRectGetMinY(leftPositionOutline.frame)) {
		return leftPositionOutline;
	} else if(location.x >= CGRectGetMinX(rightPositionOutline.frame) && location.y >= CGRectGetMinY(rightPositionOutline.frame)) {
		return rightPositionOutline;
	} else {
		return centerPositionOutline;
	}
}

- (void)prepareForTutorial
{
    // If the control view is not in the center then animate moving it there
    if( controlView.center.x != centerPositionOutline.center.x )
    {
        lastOutline = [self getOutlineForLocation:controlView.center];
        [UIView animateWithDuration:0.3
                              delay:0.0
                            options:UIViewAnimationCurveEaseOut
                         animations:^{
                             [self snapViewToOutline:centerPositionOutline];
                         }
                         completion:NULL];
    }
    else
    {
        lastOutline = nil;
    }
}

- (void)tutorialDidDismiss
{
    if( lastOutline != nil )
    {
        [UIView animateWithDuration:0.3
                              delay:0.0
                            options:UIViewAnimationCurveEaseOut
                         animations:^{
                             [self snapViewToOutline:lastOutline];
                         }
                         completion:NULL];
    }
}

@end
