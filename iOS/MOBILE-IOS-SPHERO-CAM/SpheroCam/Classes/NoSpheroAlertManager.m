//
//  NoSpheroAlertManager.m
//  Sphero
//
//  Created by Jon Carroll on 10/31/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import "NoSpheroAlertManager.h"

static NoSpheroAlertManager *sharedManager = nil;

@implementation NoSpheroAlertManager

@synthesize type, alert;

+(void)showAlertWithType:(NoSpheroAlertManagerType)_type {
    if(sharedManager!=nil) {
        [sharedManager.alert dismissWithClickedButtonIndex:0 animated:YES];
        sharedManager.alert = nil;
        [sharedManager release];
        sharedManager = nil;
    }
    sharedManager = [[NoSpheroAlertManager alloc] init];
    sharedManager.type = _type;
    NSString *message;
    NSString *title;
    NSString *button1, *button2;
    if(_type == NoSpheroAlertManagerTypeNeverConnected) {
        message = @"Go to Bluetooth settings to connect your Sphero or get one at gosphero.com";
        button1 = @"Settings";
        button2 = @"Get Sphero";
        if([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"prefs:root=General&path=Bluetooth"]]) {
            sharedManager.alert = [[UIAlertView alloc] initWithTitle:@"No Sphero Connected" message:message delegate:sharedManager cancelButtonTitle:button1 otherButtonTitles:button2, nil];
        } else {
            sharedManager.alert = [[UIAlertView alloc] initWithTitle:@"No Sphero Connected" message:message delegate:sharedManager cancelButtonTitle:@"OK" otherButtonTitles:button2, nil];
        }
        
    } else {
        message = @"Shake Sphero to wake up or go to Bluetooth settings to reconnect Sphero.";
        
        button1 = @"Settings";
        if([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"prefs:root=General&path=Bluetooth"]]) {
            sharedManager.alert = [[UIAlertView alloc] initWithTitle:@"No Sphero Connected" message:message delegate:sharedManager cancelButtonTitle:@"OK" otherButtonTitles:button1, nil];
        } else {
            sharedManager.alert = [[UIAlertView alloc] initWithTitle:@"No Sphero Connected" message:message delegate:sharedManager cancelButtonTitle:@"OK" otherButtonTitles:nil];
        }
    }
    [sharedManager.alert show];
    [sharedManager.alert release];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
    if(buttonIndex==[alertView cancelButtonIndex] && type==NoSpheroAlertManagerTypeNeverConnected && [[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:@"prefs:root=General&path=Bluetooth"]]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"prefs:root=General&path=Bluetooth"]];
    } else if([[alertView buttonTitleAtIndex:buttonIndex] isEqualToString:@"Settings"]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"prefs:root=General&path=Bluetooth"]];
    } else if([[alertView buttonTitleAtIndex:buttonIndex] isEqualToString:@"Get Sphero"]) {
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"http://gosphero.com"]];
    }
    self.alert = nil;
    [self autorelease];
    sharedManager = nil;
}

       
+(void)dismissAlert {
    if(sharedManager!=nil) {
        [sharedManager.alert dismissWithClickedButtonIndex:0 animated:YES];
        sharedManager.alert = nil;
        [sharedManager release];
        sharedManager = nil;
    }
}

@end
