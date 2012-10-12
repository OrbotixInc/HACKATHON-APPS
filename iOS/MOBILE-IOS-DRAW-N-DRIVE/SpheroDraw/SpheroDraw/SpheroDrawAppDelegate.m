//
//  SpheroDrawAppDelegate.m
//  SpheroDraw
//
//  Created by Brandon Dorris on 8/19/11.
//  Copyright 2011 Orbotix. All rights reserved.
//

#import "SpheroDrawAppDelegate.h"

#import "SpheroDrawViewController.h"
#import "FlurryAnalytics.h"
#import <RobotKit/Macro/RKAbortMacroCommand.h>

@implementation SpheroDrawAppDelegate

@synthesize window = _window;
@synthesize viewController = _viewController;

void uncaughtExceptionHandler(NSException *exception) {
    [FlurryAnalytics logError:@"Uncaught" message:@"Crash!" exception:exception];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    NSSetUncaughtExceptionHandler(&uncaughtExceptionHandler);
    [FlurryAnalytics startSession:@"CGVI62JD5DPV1Q7WXKKZ"];
    
    // Set SpheroWorld application id and secret
    [RKSpheroWorldAuth setAppID:@"drawacecb4ef4c5b2d2446248f348dad8f4d" secret:@"zvJ3sGVTY13UAFZLKbvq"];
    
    NSDictionary *defaults = [NSDictionary dictionaryWithObject:[NSNumber numberWithBool:NO] forKey:@"hasRobotConnected"];
    [[NSUserDefaults standardUserDefaults] registerDefaults:defaults];
    
    application.applicationSupportsShakeToEdit = YES;
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    // Override point for customization after application launch.
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        self.viewController = [[SpheroDrawViewController alloc] initWithNibName:@"SpheroDrawViewController_iPhone" bundle:nil]; 
    } else {
        self.viewController = [[SpheroDrawViewController alloc] initWithNibName:@"SpheroDrawViewController_iPad" bundle:nil]; 
    }
    self.window.rootViewController = self.viewController;
    [self.window makeKeyAndVisible];
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
     */
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    /*
     Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
     */
    //[[RKRobotProvider sharedRobotProvider] controlConnectedRobot];
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
    //[[RKRobotProvider sharedRobotProvider] controlConnectedRobot];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    [RKAbortMacroCommand sendCommand];
    [RKBackLEDOutputCommand sendCommandWithBrightness:0.0];
    [[RKRobotProvider sharedRobotProvider] closeRobotConnection];
    /*
     Called when the application is about to terminate.
     Save data if appropriate.
     See also applicationDidEnterBackground:.
     */
}

@end
