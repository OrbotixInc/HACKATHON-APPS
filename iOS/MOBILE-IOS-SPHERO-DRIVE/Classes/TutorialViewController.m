//
//  TutorialViewController.m
//  SpheroDrive
//
//  Created by Brian Alexander on 8/17/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "TutorialViewController.h"
#import <QuartzCore/QuartzCore.h>
#import "DriveAppSettings.h"

@interface TutorialImageData : NSObject
{
    NSString* imageName;
    CGPoint   dismissCenter;
}

@property (nonatomic, retain) NSString* imageName;
@property (nonatomic, assign) CGPoint dismissCenter;

+ (TutorialImageData*)createWithImage:(NSString*)imgBase phonePos:(CGPoint)ph padPos:(CGPoint)pad;

@end

@implementation TutorialImageData

@synthesize imageName, dismissCenter;

+ (TutorialImageData*)createWithImage:(NSString *)imgBase phonePos:(CGPoint)ph padPos:(CGPoint)pad {
    TutorialImageData* rval = [[TutorialImageData alloc] init];
    if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ) {
        rval.imageName = [imgBase stringByAppendingString:@"~ipad"];
        rval.dismissCenter = pad;
    } else {
        rval.imageName = imgBase;
        rval.dismissCenter = ph;
    }
    return [rval autorelease]; 
}

@end



@interface TutorialViewController ()

- (void)initTutorialImages;

@end

@implementation TutorialViewController

@synthesize imageView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Get the series of images based on our preferences and state.
        [self initTutorialImages];
        
        messageIndex = 0;
        self.backgroundAlpha = 0.0;
    }
    return self;
}

- (void)initTutorialImages
{
    NSMutableArray* seq = [[NSMutableArray alloc] initWithCapacity:5];
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    if( settings.mainTutorial ) {
        //[seq addObject:[self getImageName:@"SpheroDrive-Tutorial-01"]];
        //[seq addObject:[self getImageName:@"SpheroDrive-Tutorial-02"]];
    }
    
    switch( settings.driveType )
    {
        case DriveTypeJoystick:
            [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-Joystick-Tutorial-01" 
                                                     phonePos:CGPointMake(240, 300) 
                                                       padPos:CGPointMake(512, 748)]];
            if( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone ) {
                [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-Joystick-Tutorial-02"
                                                         phonePos:CGPointMake(240, 20)
                                                           padPos:CGPointMake(512, 20)]];
            }
            [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-Joystick-Tutorial-03"
                                                     phonePos:CGPointMake(240, 300)
                                                       padPos:CGPointMake(512, 748)]];
            break;
        case DriveTypeRC:
            [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-RC-Tutorial-01"
                                                     phonePos:CGPointMake(240, 300)
                                                       padPos:CGPointMake(512, 20)]];
            [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-RC-Tutorial-02"
                                                     phonePos:CGPointMake(240, 300)
                                                       padPos:CGPointMake(512, 20)]];

            break;
        case DriveTypeTilt:
            [seq addObject:[TutorialImageData createWithImage:@"SpheroDrive-Tilt-Tutorial-01"
                                                     phonePos:CGPointMake(240, 300)
                                                       padPos:CGPointMake(512, 748)]];

            break;
    }
    
    [imageSequence release];
    imageSequence = seq;
}

- (void)dealloc
{
    [imageView release]; imageView = nil;
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    TutorialImageData* data = [imageSequence objectAtIndex:messageIndex];
    self.imageView.image = [UIImage imageNamed:data.imageName];
    dismissButton.center = data.dismissCenter;
    
    UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc]
                                   initWithTarget:self
                                   action:@selector(transitionToNext)];
    [self.imageView addGestureRecognizer:tap];
    [tap release];
}

- (void)transitionToNext {
    ++messageIndex;
    if( messageIndex >= [imageSequence count] ) {
        [self dismiss];
    } else {
        TutorialImageData* data = [imageSequence objectAtIndex:messageIndex];
        self.imageView.image = [UIImage imageNamed:data.imageName];
        CATransition* transition = [CATransition animation];
        transition.duration = 0.3f;
        transition.timingFunction = [CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut];
        transition.type = kCATransitionFade;
        [imageView.layer addAnimation:transition forKey:nil];
        dismissButton.center = data.dismissCenter;
    }
}

- (void)dismiss {
    [self dismissModalLayerViewControllerAnimated:YES];
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    
    self.imageView = nil;
}

@end
