//
//  InfoViewController.m
//  Sphero
//
//  Created by Brian Alexander on 5/2/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "InfoViewController.h"
#import "SpheroItemSelectSound.h"

@implementation InfoViewController

@synthesize softwareVersionLabel;

- (void)done {
    [[SpheroItemSelectSound sharedSound] play];
	if([RUIModalLayerViewController currentModalLayerViewController])
		[self dismissModalLayerViewControllerAnimated:YES];
	if(self.navigationController)
        [self.navigationController popToViewController:[[self.navigationController viewControllers] objectAtIndex:0] animated:YES];
}

- (void)back {
    [[SpheroItemSelectSound sharedSound] play];
	if([RUIModalLayerViewController currentModalLayerViewController])
		[self dismissModalLayerViewControllerAnimated:YES];
	if(self.navigationController)
		[self.navigationController popViewControllerAnimated:YES];
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
	softwareVersionLabel.text = [[[NSBundle mainBundle]
						objectForInfoDictionaryKey:@"CFBundleShortVersionString"]
					   retain];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    return (UIInterfaceOrientationIsLandscape(interfaceOrientation));
}

- (void)didReceiveMemoryWarning {
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc. that aren't in use.
}

- (void)viewDidUnload {
    [super viewDidUnload];
	self.softwareVersionLabel = nil;
}

- (void)dealloc {
	[softwareVersionLabel release];
    [super dealloc];
}


@end
