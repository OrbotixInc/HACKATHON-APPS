//
//  CircularUIButton.h
//  Sphero
//
//  Created by Brian Alexander on 4/25/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>

/*!
 * UIButton subclass that overrides pointInside:withEvent: to ensure that the
 * event occurs within the circular area of the button.
 */
@interface CircularUIButton : UIButton {

}

@end
