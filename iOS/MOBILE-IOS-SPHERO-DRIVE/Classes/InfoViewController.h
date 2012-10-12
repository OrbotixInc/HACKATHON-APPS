//
//  InfoViewController.h
//  Sphero
//
//  Created by Brian Alexander on 5/2/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RobotKit/RobotKit.h>
#import <RobotUIKit/RobotUIKit.h>

@interface InfoViewController : RUIModalLayerViewController 
             <UIWebViewDelegate> 
{
	UILabel*     softwareVersionLabel;
}

@property (nonatomic, retain) IBOutlet UILabel* softwareVersionLabel;

- (IBAction)done;
- (IBAction)back;

@end
