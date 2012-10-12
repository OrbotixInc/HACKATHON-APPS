//
//  DriveTypeSelectorViewController.m
//  Sphero
//
//  Created by Brian Alexander on 7/11/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "DriveTypeSelectorViewController.h"
#import "SpheroButtonPressSound.h"

@interface DriveTypeSelectorViewController ()

- (void)doAutoSelection:(NSValue*)value;

@end

@implementation DriveTypeSelectorViewController

@synthesize delegate;
@synthesize joystickButton;
@synthesize tiltButton;
@synthesize rcButton;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (IBAction)switchToJoystickDrive 
{
    [[SpheroButtonPressSound sharedSound] play];
    rcButton.highlighted = NO;
    tiltButton.highlighted = NO;
    [delegate performSelector:@selector(switchToJoystickDrive) withObject:nil afterDelay:0.01];
}

- (IBAction)switchToTiltDrive
{
    [[SpheroButtonPressSound sharedSound] play];
    rcButton.highlighted = NO;
    joystickButton.highlighted = NO;
    [delegate performSelector:@selector(switchToTiltDrive) withObject:nil afterDelay:0.01];
}

- (IBAction)switchToRCDrive
{
    [[SpheroButtonPressSound sharedSound] play];
    joystickButton.highlighted = NO;
    tiltButton.highlighted = NO;
    [delegate  performSelector:@selector(switchToRCDrive) withObject:nil afterDelay:0.01];
}

- (void)doAutoSelection:(NSValue*)value
{
    // My value has to be a selector
    if( strcmp([value objCType], @encode(SEL)) == 0 ) {
        SEL selector;
        [value getValue:&selector];
        [self performSelector:selector];
    }
}

- (void)hilightDriveType:(DriveAppDriveType)next withDelay:(float)delay
{
    SEL selector;
    switch( next )
    {
        case DriveTypeJoystick:
            selector = @selector(switchToJoystickDrive);
            [joystickButton setHighlighted:YES];
            break;
        case DriveTypeRC:
            selector = @selector(switchToRCDrive);
            [rcButton setHighlighted:YES];
            break;
        case DriveTypeTilt:
            selector = @selector(switchToTiltDrive);
            [tiltButton setHighlighted:YES];
            break;
    }
    NSValue* option = [NSValue valueWithBytes:&selector objCType:@encode(SEL)];
    [self performSelector:@selector(doAutoSelection:) 
               withObject:option
               afterDelay:delay];
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
    // Do any additional setup after loading the view from its nib.
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    if(settings.driveType==DriveTypeJoystick) {
        joystickButton.highlighted = YES;
    } else if(settings.driveType==DriveTypeTilt) {
        tiltButton.highlighted = YES;
    } else {
        rcButton.highlighted = YES;
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    
    self.joystickButton = nil;
    self.tiltButton = nil;
    self.rcButton = nil;
}

- (void)dealloc
{
    [super dealloc];
    
    [joystickButton release];
    [tiltButton release];
    [rcButton release];
}

@end
