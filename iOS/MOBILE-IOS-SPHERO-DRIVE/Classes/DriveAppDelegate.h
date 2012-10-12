//
//  DriveAppDelegate.h
//  Drive
//
//  Created by Brian Smith on 11/17/10.
//  Copyright 2010 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MainMenuViewController;

@interface DriveAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    UINavigationController *navigationController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet UINavigationController *navigationController;

+(NSBundle*) getRobotUIKitResourcesBundle;

@end
