//
//  ColorWandView.m
//  Sphero
//
//  Created by Jon Carroll on 7/26/11.
//  Copyright 2011 Orbotix Inc. All rights reserved.
//

#import "ColorWandView.h"


@implementation ColorWandView

-(void)initDefaults {

	backgroundView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"SpheroDrive-ipad-colorBtnBG.png"]];
	[self addSubview:backgroundView];
	
	wandView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"SpheroDrive-ipad-colorWand.png"]];
	[self addSubview:wandView];
	
	[super initDefaults];
}

-(void)layoutSubviews {
	[super layoutSubviews];
	
	backgroundView.frame = self.bounds;
	wandView.frame = CGRectMake(16 * (self.frame.size.width / backgroundView.image.size.width), 13 * (self.frame.size.height / backgroundView.image.size.height), wandView.image.size.width * ((self.frame.size.height) / wandView.image.size.height) - (28 * (self.frame.size.height / backgroundView.image.size.height)), self.frame.size.height - (28 * (self.frame.size.height / backgroundView.image.size.height)));
}

- (void)drawRect:(CGRect)rect {
    //Overriding superclass implementation so it doesn't draw colored square
}

//Override superclass implementation but pass up so we know when to update wand color
-(void) updateHue:(CGFloat)h saturation:(CGFloat)s brightness:(CGFloat)b {
	[super updateHue:h saturation:s brightness:b];
	[self updateWandColor];
}

-(void)updateWandColor {
	UIImage *image = wandView.image;
	UIColor *color = [UIColor colorWithRed:[super red] green:[super green] blue:[super blue] alpha:1.0];
	CGRect contextRect;
	contextRect.origin.x = 0.0f;
	contextRect.origin.y = 0.0f;
	contextRect.size = [image size];
	// Retrieve source image and begin image context
	CGSize itemImageSize = [image size];
	CGPoint itemImagePosition; 
	itemImagePosition.x = ceilf((contextRect.size.width - itemImageSize.width) / 2);
	itemImagePosition.y = ceilf((contextRect.size.height - itemImageSize.height) );
	UIGraphicsBeginImageContext(contextRect.size);
	CGContextRef c = UIGraphicsGetCurrentContext();
	// Setup shadow
	// Setup transparency layer and clip to mask
	CGContextBeginTransparencyLayer(c, NULL);
	CGContextScaleCTM(c, 1.0, -1.0);
	CGContextClipToMask(c, CGRectMake(itemImagePosition.x, -itemImagePosition.y, itemImageSize.width, -itemImageSize.height), [image CGImage]);
	// Fill and end the transparency layer
	const float* colors = CGColorGetComponents( color.CGColor );
	CGContextSetRGBFillColor(c, colors[0], colors[1], colors[2], colors[3]);
	contextRect.size.height = -contextRect.size.height;
	contextRect.size.height -= 15;
	CGContextFillRect(c, contextRect);
	CGContextEndTransparencyLayer(c);
	UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
	UIGraphicsEndImageContext();
	wandView.image = img;
}


- (void)dealloc {
	[wandView release];
	[backgroundView release];
    [super dealloc];
}


@end
