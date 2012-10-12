//
//  NoSpheroAlertManager.h
//  Sphero
//
//  Created by Jon Carroll on 10/31/11.
//  Copyright (c) 2011 Orbotix Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum NoSpheroAlertManagerType {
    NoSpheroAlertManagerTypeNeverConnected = 0,
    NoSpheroAlertManagerTypeConnected = 1
} NoSpheroAlertManagerType;

@interface NoSpheroAlertManager : NSObject <UIAlertViewDelegate> {
    NoSpheroAlertManagerType type;
    UIAlertView *alert;
}

@property NoSpheroAlertManagerType type;
@property (nonatomic, retain) UIAlertView *alert;

+(void)showAlertWithType:(NoSpheroAlertManagerType)_type;
+(void)dismissAlert;

@end
