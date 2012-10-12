//
//  SensitivitySelectorViewController.h
//  SpheroDrive
//
//  Created by Brian Alexander on 9/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface SensitivitySelectorViewController : UIViewController {
    id delegate;
    IBOutlet UILabel *level1Label, *level2Label, *level3Label;
    IBOutlet UIButton *level1Button, *level2Button, *level3Button;
}

@property (nonatomic, assign) id delegate;

- (IBAction)switchToCautious;
- (IBAction)switchToComfortable;
- (IBAction)switchToCrazy;

@end
