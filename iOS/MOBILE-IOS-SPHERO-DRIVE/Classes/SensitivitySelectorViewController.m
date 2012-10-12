//
//  SensitivitySelectorViewController.m
//  SpheroDrive
//
//  Created by Brian Alexander on 9/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "SensitivitySelectorViewController.h"
#import "DriveAppSettings.h"
#import "SpheroButtonPressSound.h"
#import "FlurryAPI.h"


@implementation SensitivitySelectorViewController

@synthesize delegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)dealloc
{
    [super dealloc];
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

- (void)dismissPopup
{
    if( [delegate respondsToSelector:@selector(dismissSensitivityPopup)] )
        [delegate performSelector:@selector(dismissSensitivityPopup)];
}

- (void)switchToCautious
{
    [FlurryAPI logEvent:@"HomeScreenSetSensitivity1"];
    level1Button.highlighted = NO;
    level2Button.highlighted = YES;
    level3Button.highlighted = YES;
    [[SpheroButtonPressSound sharedSound] play];
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    settings.sensitivityLevel = DriveAppSensitivityLevel1;
    [self dismissPopup];
}

- (void)switchToComfortable
{
    [FlurryAPI logEvent:@"HomeScreenSetSensitivity2"];
    level1Button.highlighted = YES;
    level2Button.highlighted = NO;
    level3Button.highlighted = YES;
    [[SpheroButtonPressSound sharedSound] play];
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    settings.sensitivityLevel = DriveAppSensitivityLevel2;
    [self dismissPopup];
}

- (void)switchToCrazy
{
    [FlurryAPI logEvent:@"HomeScreenSetSensitivity3"];
    level1Button.highlighted = YES;
    level2Button.highlighted = YES;
    level3Button.highlighted = NO;
    [[SpheroButtonPressSound sharedSound] play];
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    settings.sensitivityLevel = DriveAppSensitivityLevel3;    
    [self dismissPopup];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    DriveAppSettings* settings = [DriveAppSettings defaultSettings];
    level1Label.text = [settings.level1SettingsName uppercaseString];
    level2Label.text = [settings.level2SettingsName uppercaseString];
    level3Label.text = [settings.level3SettingsName uppercaseString];
    if(settings.sensitivityLevel == DriveAppSensitivityLevel1) {
        level1Button.highlighted = NO;
        level2Button.highlighted = YES;
        level3Button.highlighted = YES;
    } else if(settings.sensitivityLevel == DriveAppSensitivityLevel2) {
        level1Button.highlighted = YES;
        level2Button.highlighted = NO;
        level3Button.highlighted = YES;
    } else {
        level1Button.highlighted = YES;
        level2Button.highlighted = YES;
        level3Button.highlighted = NO;
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

@end
