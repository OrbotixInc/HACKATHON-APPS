//
//  DriveAppDelegate.m
//  Drive
//
//  Created by Brian Smith on 11/17/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import <RobotKit/RobotKit.h>
#import "DriveAppDelegate.h"
#import "DriveAppSettings.h"
#import "DriveViewController.h"
#import "FlurryAPI.h"

@implementation DriveAppDelegate

@synthesize window;
@synthesize navigationController;

//Uncaught exception handler to send exceptions to Flurry servers for logging
void uncaughtExceptionHandler(NSException *exception) {
    [FlurryAPI logError:@"Uncaught" message:@"Crash!" exception:exception];
}

#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application 
    didFinishLaunchingWithOptions:(NSDictionary *)launchOptions 
{    
	//Register the uncaught exception handler
    NSSetUncaughtExceptionHandler(&uncaughtExceptionHandler);
	
	//Disable idle timer
	[UIApplication sharedApplication].idleTimerDisabled = YES;
    
    // Register default values for NSUserDefaults
	NSDictionary* defaults = [DriveAppSettings getPredefinedDefaults];
	[[NSUserDefaults standardUserDefaults] registerDefaults:defaults]; 
	
	//Setup analytics if they are enabled
	if([DriveAppSettings defaultSettings].analyticsOn) {
		if(kDebug) NSLog(@"Analytics Enabled");
		[FlurryAPI startSession:@"KDK124LK2LFCM7AWCZVP"];
	} else {
		if(kDebug) NSLog(@"Analytics Disabled");
	}
    
    //Set the SpheroWorld application and secret
    [RKSpheroWorldAuth setAppID:@"sphe58f7717a8d053dd035340646806fe191" secret:@"Hg6F7f5qWRi8gLzhiENJ"];

    // Setup the game control singleton
    [[RKDriveControl sharedDriveControl] setup];
    
    // Add the view controller's view to the window and display.
    [self.window addSubview:navigationController.view];
    [self.window makeKeyAndVisible];

    return YES;
}

#pragma mark -
#pragma mark RobotUIKit Resources

static NSBundle *_RUIResourcesBundle = nil;

+ (NSBundle*)getRobotUIKitResourcesBundle {
   if( _RUIResourcesBundle == nil ) {
      NSString* rootpath = [[NSBundle mainBundle] bundlePath];
      NSString* ruirespath = [NSBundle pathForResource:@"RobotUIKit"
                                                ofType:@"bundle"
                                           inDirectory:rootpath];
      _RUIResourcesBundle = [NSBundle bundleWithPath:ruirespath];
   }

   return _RUIResourcesBundle;
}

#pragma mark -
#pragma mark Memory management

- (void)dealloc {
    [navigationController release];
    [window release];
    [super dealloc];
}

@end
