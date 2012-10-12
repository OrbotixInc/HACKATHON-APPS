//
//  TutorialViewController.h
//  SpheroDrive
//
//  Created by Brian Alexander on 8/17/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotUIKit/RUIModalLayerViewController.h>


@interface TutorialViewController : RUIModalLayerViewController {
    @private
    UIImageView* imageView;
    NSUInteger messageIndex;
    NSArray* imageSequence;
    CGPoint  positions[10];
    IBOutlet UIButton* dismissButton;
}

@property (nonatomic, retain) IBOutlet UIImageView* imageView;

- (IBAction)dismiss;

@end
