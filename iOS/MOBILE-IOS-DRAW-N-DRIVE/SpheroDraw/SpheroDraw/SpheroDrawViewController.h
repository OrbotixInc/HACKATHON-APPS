//
//  SpheroDrawViewController.h
//  SpheroDraw
//
//  Created by Brandon Dorris on 8/19/11.
//  Copyright 2011 Orbotix. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotKit/RobotKit.h>
#import <RobotKit/Macro/RKMacroObject.h>
#import <RobotUIKit/RobotUIKit.h>
#import "DrawingView.h"
#import "RKCalibrateOverlayView.h"

typedef struct {
    float heading;
    float speed;
} Command;

@interface SpheroDrawViewController : UIViewController <PathDelegate, RUIHSColorPickerDelegate, RUICalibrateGestureHandlerProtocol> {
    CGPoint lastPoint;
    NSMutableArray *points;
    IBOutlet DrawingView *drawingView;
    UIColor *currentColor;
    UIRotationGestureRecognizer *rotationGestureRecognizer;
    bool rotating;
    IBOutlet UILabel *calibrateMessage;
    IBOutlet UIImageView *colorTray;
    IBOutlet UIImageView *paperBG;
    int calibrationCount;
    IBOutlet RUIHSColorPickerView *colorPicker;
    CGPoint gestureOffset;
    RKMacroObject *macroObject;
    BOOL macroFull;
    float firmwareVersion;
    IBOutlet UIImageView *calibrateUnderlay, *calibrateOverlay;
    RKCalibrateOverlayView *calibrateOverlayRings;
    IBOutlet UIImageView *calibrateCallout;
    UIView *calloutOverlay;
    RUICalibrateGestureHandler *calibrateHandler;
}

@property (nonatomic)           CGPoint         lastPoint;
@property (nonatomic, retain)   NSMutableArray  *points;

- (float)distanceBetweenPoint:(CGPoint)point1 andPoint:(CGPoint)point2;
- (void)handleConnectionOnline:(NSNotification*)notification;
- (float)headingFrom:(CGPoint)point1 to:(CGPoint)point2;


- (void)rotate:(UIRotationGestureRecognizer *)gestureRecognizer;

-(void)registerForRobotNotifications;
-(void)unregisterForRobotNotifications;

- (void)colorPickerDidChange:(RUIHSBColorPickerView*)view 
					 withRed:(CGFloat)r green:(CGFloat)g blue:(CGFloat)b;
-(IBAction)repeatPressed:(id)sender;

-(void)calibrateReminder;

-(void)showCalibrateCallout:(UIGestureRecognizer*)sender;
-(void)dismissCallout;

-(IBAction)userGuidePressed:(id)sender;

@end
