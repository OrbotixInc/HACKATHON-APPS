//
//  CalibrationTutorialViewController.h
//  Sphero
//
//  Created by Jon Carroll on 9/19/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotUIKit/RobotUIKit.h>
#import "CalibrateTutorialView.h"

@protocol CalibrationTutorialViewControllerDelegate <NSObject>
@required
-(void)calibrationTutorialFinished;
-(void)calibrationTutorialFinishedDontShowAgain;
@end

typedef enum CalibrateTutorialState {
    CalibrateTutorialStateFloor =       1,
    CalibrateTutorialStateTaillight =   2,
    CalibrateTutorialStateFingersDown = 3,
    CalibrateTutorialStateRotate =      4,
    CalibrateTutorialStateAim =         5,
    CalibrateTutorialStateForward =     6,
    CalibrateTutorialStateRemember =    7
} CalibrateTutorialState;

@interface CalibrationTutorialViewController : RUIModalLayerViewController <UIScrollViewDelegate, RUICalibrateGestureHandlerProtocol> {
    IBOutlet UIImageView *dot1, *dot2;
    IBOutlet UIButton *midButton, *bottomButton, *dontShowAgainButton;
    IBOutlet UIImageView *blueBox1;
    IBOutlet UIImageView *line1;
    IBOutlet UILabel *leftLabel, *rightLabel;
    IBOutlet UIImageView *spheroImage, *blueDotImage;
    
    IBOutlet UIImageView *arrow1, *arrow2, *arrow3, *arrow4;
    IBOutlet UIView *confirmationView;
    IBOutlet UILabel *confirmationLabel;
    
    IBOutlet UIImageView *handView;
    
    UILongPressGestureRecognizer *longPress;
    UIRotationGestureRecognizer *rotationGesture;
    UIView                      *longPressView;
    CGPoint handStart;

    BOOL rotating;
    BOOL scrollingOnTheirOwn;
    id <CalibrationTutorialViewControllerDelegate> delegate;
    int currentArrowIndex;
    NSArray *arrowArray;
    RUICalibrateOverlayView *calibrateOverlayRings;
    CalibrateTutorialState state;
    RUICalibrateGestureHandler *calibrateHandler;
    BOOL bounceAnimationStarted;
}

@property (nonatomic, assign) id <CalibrationTutorialViewControllerDelegate> delegate;
@property CalibrateTutorialState state;

-(IBAction)donePressed:(id)sender;
-(IBAction)yesPressed:(id)sender;
-(IBAction)noPressed:(id)sender;
-(IBAction)dontShowAgainPressed:(id)sender;
-(void)startAnimation;
-(UIImageView*)getCurrentArrow;

@end
