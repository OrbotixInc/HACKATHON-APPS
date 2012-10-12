//
//  DriveControlViewController.m
//  SpheroDrive
//
//  Created by Brian Smith on 12/22/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import <RobotKit/RobotKit.h>

#import "DriveControlViewController.h"
#import "DriveControllerDelegate.h"

@implementation DriveControlViewController

@synthesize boostButtonLeft;
@synthesize boostButtonRight;
@synthesize delegate;

- (id)initWithDelegate:(id<DriveControllerDelegate>)d
{
    self = [super initWithNibName:nil bundle:nil];
    if (self == nil) return nil;
    
    delegate = d;
    boosting = NO;

    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(handleBoostDidFinish:)
                                                 name:RKRobotBoostDidFinishNotification 
                                               object:nil];

    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self 
                                                    name:RKRobotBoostDidFinishNotification 
                                                  object:nil];
    
    [boostButtonLeft release]; boostButtonLeft = nil;
    [boostButtonRight release]; boostButtonRight = nil;
    
    [super dealloc];
}

#pragma mark - IBActions
- (void)boostButtonDown:(UITapGestureRecognizer *)recognizer
{
    if (boosting) return;
    if (recognizer.state == UIGestureRecognizerStateRecognized) {
        boosting = YES;
        
        [self.delegate doBoost];
    }
}

- (void)boostButtonRepeat:(UITapGestureRecognizer *)recognizer
{
    if (boosting) return;
    if (recognizer.state == UIGestureRecognizerStateRecognized) {
        boosting = YES;
        
        [self.delegate boostUncontrolled];
    }
}

- (void)handleBoostDidFinish:(NSNotification *)notification
{
    boosting = NO;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Setup gesture recognizers for boost and uncontrolled boost
    UITapGestureRecognizer *singleTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self 
                                                                                       action:@selector(boostButtonDown:)];
    UITapGestureRecognizer *doubleTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                                                       action:@selector(boostButtonRepeat:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    [singleTapGesture requireGestureRecognizerToFail:doubleTapGesture];
    
    [self.boostButtonLeft addGestureRecognizer:singleTapGesture];
    [self.boostButtonLeft addGestureRecognizer:doubleTapGesture];
    [singleTapGesture release];
    [doubleTapGesture release];
    
    singleTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self 
                                                               action:@selector(boostButtonDown:)];
    doubleTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self
                                                               action:@selector(boostButtonRepeat:)];
    doubleTapGesture.numberOfTapsRequired = 2;
    [singleTapGesture requireGestureRecognizerToFail:doubleTapGesture];
    
    [self.boostButtonRight addGestureRecognizer:singleTapGesture];
    [self.boostButtonRight addGestureRecognizer:doubleTapGesture];
    
    [singleTapGesture release];
    [doubleTapGesture release];
}

- (void)viewDidUnload
{
    self.boostButtonLeft = nil;
    self.boostButtonRight = nil;
}

@end
