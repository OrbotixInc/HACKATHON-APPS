//
//  ChangeNameViewController.m
//  Sphero
//
//  Created by Brian Alexander on 4/28/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "ChangeNameViewController.h"

@interface ChangeNameViewController ()

- (void)oldNameTapped:(UIGestureRecognizer*)recognizer;
- (BOOL)validateSpheroName:(NSString*)name;

@end

@implementation ChangeNameViewController

@synthesize oldNameLabel;
@synthesize nameField;
@synthesize robotControl;
@synthesize currentName;

- (IBAction) nameFieldDidEndEditing:(UITextField*)textField
{
	NSString* name = nameField.text;
	if( [self validateSpheroName:name] )
		[nameField resignFirstResponder];
}

- (IBAction) done
{
	// Send the new name to the robot.
	NSString* name = nameField.text;
	if( [self validateSpheroName:name] ) {
		self.robotControl.robot.name = name;
		[self dismissModalLayerViewControllerAnimated:YES];
	} else {
		[nameField becomeFirstResponder];
	}
}

- (void)oldNameTapped:(UIGestureRecognizer*)recognizer
{
	if( recognizer.state == UIGestureRecognizerStateEnded ) {
		nameField.text = oldNameLabel.text;
	}
}

- (BOOL)validateSpheroName:(NSString*)name
{
	// TODO: Actually validate the name - it can't be empty and it can't match
	//       any of our prohibited name patterns.
	return YES;
}

- (void)viewDidLoad {
    [super viewDidLoad];
	
	UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc] 
								   initWithTarget:self 
								   action:@selector(oldNameTapped)];
	[oldNameLabel addGestureRecognizer:tap];
	[tap release];
	
	oldNameLabel.text = self.currentName;
	nameField.text = self.currentName;
}

- (void)viewDidAppear:(BOOL)animated {
	[super viewDidAppear:animated];
	[nameField becomeFirstResponder];
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

	self.oldNameLabel = nil;
	self.nameField = nil;
	self.currentName = nil;
}


- (void)dealloc {
	[oldNameLabel release];
	[nameField release];
	[currentName release];
	
    [super dealloc];
}


@end
