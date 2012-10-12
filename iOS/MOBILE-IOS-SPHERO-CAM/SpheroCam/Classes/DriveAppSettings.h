//
//  DriveAppSettings.h
//  Sphero
//
//  Created by Brian Alexander on 3/30/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef enum {
	DriveAppSensitivityLevel1,
	DriveAppSensitivityLevel2,
	DriveAppSensitivityLevel3
} DriveAppSensitivityLevel;

typedef struct {
	float red;
	float green;
	float blue;
} DriveAppSettingsRGB;

typedef enum {
    DriveTypeJoystick,
    DriveTypeTilt,
    DriveTypeRC
} DriveAppDriveType;

/*!
 * Changes to settings properties cause DriveAppSettingsDidChangeNotification
 * notifications to be sent from the notification center.  The name of the
 * setting that changed is in the notification user data under the key:
 * DriveAppSettingName.
 */
@interface DriveAppSettings : NSObject {

}

@property (nonatomic, assign) DriveAppSettingsRGB      robotLEDBrightness;
@property (nonatomic, assign) DriveAppSensitivityLevel sensitivityLevel;
@property (nonatomic, assign) float                    velocityScale;
@property (nonatomic, assign) float                    boostTime;
@property (nonatomic, assign) float                    controlledBoostVelocity;
@property (nonatomic, assign) float                    rotationRate;
@property (nonatomic, assign) NSString*                currentSettingsName;
@property (nonatomic, assign) NSString*                level1SettingsName;
@property (nonatomic, assign) NSString*                level2SettingsName;
@property (nonatomic, assign) NSString*                level3SettingsName;
@property (nonatomic, assign) BOOL                     autoUpdateCheck;
@property (nonatomic, assign) BOOL                     analyticsOn;
@property (nonatomic, assign) BOOL                     autoPairOn;
@property (nonatomic, assign) BOOL                     gyroSteering;
@property (nonatomic, assign) DriveAppDriveType        driveType;
@property (nonatomic, assign) float                    soundFXVolume;
@property (nonatomic, assign) BOOL                     mainTutorial;
@property (nonatomic, assign) BOOL                     joystickTutorial;
@property (nonatomic, assign) BOOL                     tiltTutorial;
@property (nonatomic, assign) BOOL                     rcTutorial;

-(BOOL)hasRobotConnected;
-(void)setRobotConnected:(BOOL)setting;

/*!
 * Get the default settings object for the Drive App
 */
+(DriveAppSettings*) defaultSettings;

/*!
 * Release the default settings object to free its memory.
 */
+(void) releaseDefaultSettings;

/*!
 * Get the predefined default values for all settings.  This can be used to
 * set the user defaults on application startup.
 */
+(NSDictionary*) getPredefinedDefaults;

/*!
 * Reset the sensitivity level settings for the current sensitivity level.
 */
-(void) resetCurrentSensitivitySettingsToDefault;

@end

/*!
 * Key in notification object for the name of the setting that changed.
 */
extern NSString* const DriveAppSettingName;

/*!
 * Notifications string posted when settings change
 */
extern NSString* const DriveAppSettingsDidChangeNotification;

