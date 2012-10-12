//
//  DriveAppSettings.m
//  Sphero
//
//  Created by Brian Alexander on 3/30/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "DriveAppSettings.h"
#import <RobotKit/RobotKit.h>

static NSString * const UserDefaultVelocityScaleKey        = @"velocityScale";
static NSString * const UserDefaultBoostTimeKey            = @"boostTime";
static NSString * const UserControlledBoostVelocityKey     = @"controlledBoostVelocity";
static NSString * const UserDefaultRedLEDBrightnessKey     = @"redLEDBrightness";
static NSString * const UserDefaultGreenLEDBrightnessKey   = @"greenLEDBrightness";
static NSString * const UserDefaultBlueLEDBrightnessKey    = @"blueLEDBrightness";
static NSString * const UserDefaultRotationRateKey         = @"rotationRate";
static NSString * const UserDefaultSensitivityLevel        = @"sensitivityLevel";
static NSString * const UserDefaultSettingsNameKey         = @"name";
static NSString * const UserDefaultFirmwareUpdateCheck     = @"firmwareUpdateCheck";
static NSString * const UserDefaultSpheroAnalytics         = @"analytics";
static NSString * const UserDefaultAutoPair                = @"autoPair";
static NSString * const UserDefaultGyroSteering            = @"gyroSteering";
static NSString * const UserDefaultDriveType               = @"driveType";

static NSString * const SensitivityLevel1Name              = @"level1";
static NSString * const SensitivityLevel2Name              = @"level2";
static NSString * const SensitivityLevel3Name              = @"level3";

static NSString * const UserDefaultSoundFXVolume           = @"soundFXVolume";

static NSString * const UserDefaultMainTutorial            = @"mainTutorial";
static NSString * const UserDefaultJoystickTutorial        = @"joystickTutorial";
static NSString * const UserDefaultTiltTutorial            = @"tiltTutorial";
static NSString * const UserDefaultRCTutorial              = @"rcTutorial";

static NSString * const UserDefaultHasRobotConnected       = @"hasRobotConnected";

static NSString * const UserDefaultShowCalibrationTutorial = @"showCalibrationTutorial";

static NSString * const UserDefaultColorCalloutShown       = @"colorCalloutShown";
static NSString * const UserDefaultSpeedCalloutShown       = @"speedCalloutShown";
static NSString * const UserDefaultDriveTypeCalloutShown   = @"driveTypeCalloutShown";
static NSString * const UserDefaultAlwaysShowCallouts      = @"alwaysShowCallouts";

@interface DriveAppSettings (Private)

+(NSString*) getSensitivityLevelName:(DriveAppSensitivityLevel)l;
+(NSString*) getSensitivityLevelDictionaryName:(NSString*)lvlName;

-(NSDictionary*) getSensitivitySettings:(DriveAppSensitivityLevel)l;
-(NSDictionary*) getCurrentSensitivitySettings;
-(void) saveCurrentSensitivitySetting:(NSString*)s value:(NSObject*)v;
-(void) saveSensitivity:(NSString*)sensitivityMapName setting:(NSString*)s value:(NSObject*)v;
-(void) sendChangeNotice:(NSString*)settingName;	

@end

@implementation DriveAppSettings

static DriveAppSettings* _defaultSettings = nil;

+(DriveAppSettings*) defaultSettings
{
	if( _defaultSettings == nil )
		_defaultSettings = [[self alloc] init];
	
	return _defaultSettings;
}

+(void) releaseDefaultSettings
{
	if( _defaultSettings != nil )
	{
		[_defaultSettings release];
		_defaultSettings = nil;
	}
}

+(NSDictionary*) getPredefinedDefaults
{
	NSString *defaults_path = [[NSBundle mainBundle] pathForResource:@"UserDefaults"
															  ofType:@"plist"];
	if (defaults_path) {
		return [NSDictionary dictionaryWithContentsOfFile:defaults_path];
	} else {
		return [NSDictionary dictionary];
	}
}

+(NSString*) getSensitivityLevelName:(DriveAppSensitivityLevel)l
{
	switch( l )
	{
		case DriveAppSensitivityLevel3:
			return SensitivityLevel3Name;
		case DriveAppSensitivityLevel2:
			return SensitivityLevel2Name;
		case DriveAppSensitivityLevel1:
			return SensitivityLevel1Name;
		default:
			NSLog(@"Given sensitivity level not a known enumeration value. " \
					"Using 'comfortable' instead.");
			return SensitivityLevel2Name;
	}
}

+(NSString*) getSensitivityLevelDictionaryName:(NSString*)lvlName
{
	return [lvlName stringByAppendingString:@"Settings"];
}

-(void) sendChangeNotice:(NSString*)settingName
{
	[[NSNotificationCenter defaultCenter]
	 postNotificationName:DriveAppSettingsDidChangeNotification
	 object:self
	 userInfo:[NSDictionary dictionaryWithObject:settingName
										  forKey:DriveAppSettingName]];
}

-(DriveAppSettingsRGB) robotLEDBrightness
{
	NSUserDefaults* userdef = [NSUserDefaults standardUserDefaults];
	
	DriveAppSettingsRGB rval;
	rval.red = [userdef floatForKey:UserDefaultRedLEDBrightnessKey];
	rval.green = [userdef floatForKey:UserDefaultGreenLEDBrightnessKey];
	rval.blue = [userdef floatForKey:UserDefaultBlueLEDBrightnessKey];
	return rval;
}

-(void) setRobotLEDBrightness:(DriveAppSettingsRGB)rgb
{
	NSUserDefaults* userdef = [NSUserDefaults standardUserDefaults];
	[userdef setFloat:rgb.red forKey:UserDefaultRedLEDBrightnessKey];
	[userdef setFloat:rgb.green forKey:UserDefaultGreenLEDBrightnessKey];
	[userdef setFloat:rgb.blue forKey:UserDefaultBlueLEDBrightnessKey];
	
	[self sendChangeNotice:@"robotLEDBrightness"];
}

-(DriveAppSensitivityLevel) sensitivityLevel
{
	NSString* lvlName = [[NSUserDefaults standardUserDefaults] stringForKey:UserDefaultSensitivityLevel];
	if( [lvlName isEqualToString:SensitivityLevel3Name] )
		return DriveAppSensitivityLevel3;
	if( [lvlName isEqualToString:SensitivityLevel2Name] )
		return DriveAppSensitivityLevel2;
	if( [lvlName isEqualToString:SensitivityLevel1Name] )
		return DriveAppSensitivityLevel1;
	
	NSLog(@"Sensitivity level in standard user defaults does not match a " \
			"known sensitivity level name. Returning 'level2'");
	return DriveAppSensitivityLevel2;
}

-(void) setSensitivityLevel:(DriveAppSensitivityLevel)l
{
    if(l==DriveAppSensitivityLevel3) [RKAchievement recordEvent:@"driveSensitivityModeCrazy"];
    
	NSString* lvlName = [DriveAppSettings getSensitivityLevelName:l];
	[[NSUserDefaults standardUserDefaults] setObject:lvlName forKey:UserDefaultSensitivityLevel];
	
	[self sendChangeNotice:@"sensitivityLevel"];
}

-(void) resetCurrentSensitivitySettingsToDefault
{
	NSUserDefaults* userdefs = [NSUserDefaults standardUserDefaults];
	NSString* sensitivityMapName = [DriveAppSettings getSensitivityLevelDictionaryName:
									[userdefs stringForKey:UserDefaultSensitivityLevel]];

 	NSDictionary* predefs = [DriveAppSettings getPredefinedDefaults];
	NSDictionary* defaults = (NSDictionary*)[predefs objectForKey:sensitivityMapName];
	[[NSUserDefaults standardUserDefaults] setObject:defaults forKey:sensitivityMapName];
	
	[self sendChangeNotice:@"sensitivityLevel"];
}

-(NSDictionary*) getSensitivitySettings:(DriveAppSensitivityLevel)l
{
	NSUserDefaults* userdefs = [NSUserDefaults standardUserDefaults];
	NSString* sensitivityMapName = [DriveAppSettings getSensitivityLevelDictionaryName:
									[DriveAppSettings getSensitivityLevelName:l]];
	return [userdefs dictionaryForKey:sensitivityMapName]; 
}

-(NSDictionary*) getCurrentSensitivitySettings
{
	NSUserDefaults* userdefs = [NSUserDefaults standardUserDefaults];
	NSString* sensitivityMapName = [DriveAppSettings getSensitivityLevelDictionaryName:
											  [userdefs stringForKey:UserDefaultSensitivityLevel]];
	return [userdefs dictionaryForKey:sensitivityMapName];
}

-(void) saveCurrentSensitivitySetting:(NSString*)s value:(NSObject*)v
{
	NSUserDefaults* userdefs = [NSUserDefaults standardUserDefaults];
	NSString* sensitivityMapName = [DriveAppSettings getSensitivityLevelDictionaryName:
											  [userdefs stringForKey:UserDefaultSensitivityLevel]];

	[self saveSensitivity:sensitivityMapName setting:s value:v];
}

-(void) saveSensitivity:(NSString*)sensitivityMapName setting:(NSString*)s value:(NSObject*)v
{
	NSUserDefaults* userdefs = [NSUserDefaults standardUserDefaults];
	NSDictionary* curSettings = [userdefs dictionaryForKey:sensitivityMapName];
	NSMutableDictionary* updated = [NSMutableDictionary dictionaryWithCapacity:
											  [curSettings count]];
	[updated addEntriesFromDictionary:curSettings];
	[updated setObject:v forKey:s];
	[userdefs setObject:updated forKey:sensitivityMapName];
}

-(float) velocityScale
{
	NSDictionary* sensitivitySettings = [self getCurrentSensitivitySettings];
	return [[sensitivitySettings objectForKey:UserDefaultVelocityScaleKey] floatValue];
}

-(void) setVelocityScale:(float)v
{
	[self saveCurrentSensitivitySetting:UserDefaultVelocityScaleKey 
											value:[NSNumber numberWithFloat:v]];
	[self sendChangeNotice:@"velocityScale"];	
}

-(float) boostTime
{
	NSDictionary* sensitivitySettings = [self getCurrentSensitivitySettings];
	return [[sensitivitySettings objectForKey:UserDefaultBoostTimeKey] floatValue];
}

-(void) setBoostTime:(float)b
{
	[self saveCurrentSensitivitySetting:UserDefaultBoostTimeKey
											value:[NSNumber numberWithFloat:b]];
	[self sendChangeNotice:@"boostTime"];	
}

-(float) controlledBoostVelocity
{
    NSDictionary* sensitivitySettings = [self getCurrentSensitivitySettings];
    return [[sensitivitySettings objectForKey:UserControlledBoostVelocityKey] floatValue];
}

- (void)setControlledBoostVelocity:(float)controlledBoostVelocity
{
    [self saveCurrentSensitivitySetting:UserControlledBoostVelocityKey value:[NSNumber numberWithFloat:controlledBoostVelocity]];
    [self sendChangeNotice:@"controlledBoostVelocity"];
}

-(float) rotationRate
{
	NSDictionary* sensitivitySettings = [self getCurrentSensitivitySettings];
	return [[sensitivitySettings objectForKey:UserDefaultRotationRateKey] floatValue];
}

-(void) setRotationRate:(float)r
{
	[self saveCurrentSensitivitySetting:UserDefaultRotationRateKey
											value:[NSNumber numberWithFloat:r]];
	[self sendChangeNotice:@"rotationRate"];	
}

-(NSString*) currentSettingsName
{
	NSDictionary* sensitivitySettings = [self getCurrentSensitivitySettings];
	return (NSString*)[sensitivitySettings objectForKey:UserDefaultSettingsNameKey];
}

-(void) setCurrentSettingsName:(NSString*)name
{
	[self saveCurrentSensitivitySetting:UserDefaultSettingsNameKey value:name];
}

-(NSString*) level1SettingsName
{
	NSDictionary* cautiousSettings = [self getSensitivitySettings:
									  DriveAppSensitivityLevel1];
	return (NSString*)[cautiousSettings objectForKey:UserDefaultSettingsNameKey];
}

-(void) setLevel1SettingsName:(NSString *)name
{
	NSString* dname = [DriveAppSettings getSensitivityLevelDictionaryName:
					   [DriveAppSettings getSensitivityLevelName:
						DriveAppSensitivityLevel1]];
	[self saveSensitivity:dname setting:UserDefaultSettingsNameKey value:name];
}

-(NSString*) level2SettingsName
{
	NSDictionary* comfortableSettings = [self getSensitivitySettings:
									  DriveAppSensitivityLevel2];
	return (NSString*)[comfortableSettings objectForKey:UserDefaultSettingsNameKey];
}

-(void) setLevel2SettingsName:(NSString *)name
{
	NSString* dname = [DriveAppSettings getSensitivityLevelDictionaryName:
					   [DriveAppSettings getSensitivityLevelName:
						DriveAppSensitivityLevel2]];
	[self saveSensitivity:dname setting:UserDefaultSettingsNameKey value:name];
}

-(NSString*) level3SettingsName
{
	NSDictionary* crazySettings = [self getSensitivitySettings:
									  DriveAppSensitivityLevel3];
	return (NSString*)[crazySettings objectForKey:UserDefaultSettingsNameKey];
}

-(void) setLevel3SettingsName:(NSString *)name
{
	NSString* dname = [DriveAppSettings getSensitivityLevelDictionaryName:
					   [DriveAppSettings getSensitivityLevelName:
						DriveAppSensitivityLevel3]];
	[self saveSensitivity:dname setting:UserDefaultSettingsNameKey value:name];
}

- (BOOL)autoUpdateCheck 
{
	return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultFirmwareUpdateCheck];
}

- (void)setAutoUpdateCheck:(BOOL)b
{
	[[NSUserDefaults standardUserDefaults] setBool:b forKey:UserDefaultFirmwareUpdateCheck];
}

- (BOOL)analyticsOn 
{
	return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultSpheroAnalytics];
}

- (void)setAnalyticsOn:(BOOL)b
{
	[[NSUserDefaults standardUserDefaults] setBool:b forKey:UserDefaultSpheroAnalytics];
}

- (BOOL)autoPairOn 
{
	return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultAutoPair];
}

- (void)setAutoPairOn:(BOOL)b
{
	[[NSUserDefaults standardUserDefaults] setBool:b forKey:UserDefaultAutoPair];
}

- (BOOL)gyroSteering 
{
	return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultGyroSteering];
}

- (void)setGyroSteering:(BOOL)b
{
	[[NSUserDefaults standardUserDefaults] setBool:b forKey:UserDefaultGyroSteering];
    [self sendChangeNotice:UserDefaultGyroSteering];
}

- (DriveAppDriveType)driveType
{
    return (DriveAppDriveType)[[[NSUserDefaults standardUserDefaults] objectForKey:UserDefaultDriveType] intValue];
}

- (void)setDriveType:(DriveAppDriveType)driveType
{
    [[NSUserDefaults standardUserDefaults] setInteger:driveType forKey:UserDefaultDriveType];
}

-(float) soundFXVolume
{
	return [[[NSUserDefaults standardUserDefaults]
             objectForKey:UserDefaultSoundFXVolume] floatValue];
}

-(void) setSoundFXVolume:(float)v
{
    [[NSUserDefaults standardUserDefaults] setFloat:v forKey:UserDefaultSoundFXVolume];
	[self sendChangeNotice:UserDefaultSoundFXVolume];	
}

-(BOOL) mainTutorial
{
	return [[NSUserDefaults standardUserDefaults]
             boolForKey:UserDefaultMainTutorial];
}

-(void) setMainTutorial:(BOOL)v
{
    [[NSUserDefaults standardUserDefaults] setBool:v forKey:UserDefaultMainTutorial];
	[self sendChangeNotice:UserDefaultMainTutorial];	
}

-(BOOL) joystickTutorial
{
	return [[NSUserDefaults standardUserDefaults]
             boolForKey:UserDefaultJoystickTutorial];
}

-(void) setJoystickTutorial:(BOOL)v
{
    [[NSUserDefaults standardUserDefaults] setBool:v forKey:UserDefaultJoystickTutorial];
	[self sendChangeNotice:UserDefaultJoystickTutorial];	
}

-(BOOL) tiltTutorial
{
	return [[NSUserDefaults standardUserDefaults]
             boolForKey:UserDefaultTiltTutorial];
}

-(void) setTiltTutorial:(BOOL)v
{
    [[NSUserDefaults standardUserDefaults] setBool:v forKey:UserDefaultTiltTutorial];
	[self sendChangeNotice:UserDefaultTiltTutorial];	
}

-(BOOL) rcTutorial
{
	return [[NSUserDefaults standardUserDefaults]
             boolForKey:UserDefaultRCTutorial];
}

-(void) setRcTutorial:(BOOL)v
{
    [[NSUserDefaults standardUserDefaults] setBool:v forKey:UserDefaultRCTutorial];
	[self sendChangeNotice:UserDefaultRCTutorial];	
}

-(BOOL)hasRobotConnected {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultHasRobotConnected];
}

-(void)setRobotConnected:(BOOL)setting {
    [[NSUserDefaults standardUserDefaults] setBool:setting forKey:UserDefaultHasRobotConnected];
}

-(BOOL)showCalibrateTutorial {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultShowCalibrationTutorial];
}

-(void)setShowCalibrateTutorial:(BOOL)setting {
    [[NSUserDefaults standardUserDefaults] setBool:setting forKey:UserDefaultShowCalibrationTutorial];
}

-(BOOL)colorCalloutShown {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultColorCalloutShown];
}

-(void)setColorCalloutShown:(BOOL)setting {
    [[NSUserDefaults standardUserDefaults] setBool:setting forKey:UserDefaultColorCalloutShown];
}

-(BOOL)speedCalloutShown {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultSpeedCalloutShown];
}

-(void)setSpeedCalloutShown:(BOOL)setting {
    [[NSUserDefaults standardUserDefaults] setBool:setting forKey:UserDefaultSpeedCalloutShown];
}

-(BOOL)driveTypeCalloutShown {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultDriveTypeCalloutShown];
}

-(void)setDriveTypeCalloutShown:(BOOL)setting {
    [[NSUserDefaults standardUserDefaults] setBool:setting forKey:UserDefaultDriveTypeCalloutShown];
}

-(BOOL)alwaysShowCallouts {
    return [[NSUserDefaults standardUserDefaults] boolForKey:UserDefaultAlwaysShowCallouts];
}

@end

#pragma mark -
#pragma mark Notifcation String Constants

NSString* const DriveAppSettingName = @"Setting";
NSString* const DriveAppSettingsDidChangeNotification = @"DriveAppSettingsDidChange";

