//
//  DrawingView.m
//  Draw Path
//
//  Created by Brandon Dorris on 8/19/11.
//  Copyright 2011 Orbotix. All rights reserved.
//

#import "DrawingView.h"
#import <RobotKit/RobotKit.h>
#import <RobotKit/Macro/RKMacro.h>
#import <RobotKit/Macro/RKAbortMacroCommand.h>

@implementation DrawingView

@synthesize drawImage;
@synthesize drawingPenColor;
@synthesize imageView;
@synthesize delegate;

-(void)initialize{
    pixelDensityMultiplyer = 1.0;
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        pixelDensityMultiplyer = 2.0;
    } 
    drawImage = [[UIImageView alloc] initWithImage:nil];
    self.autoresizesSubviews = YES;
    drawImage.autoresizingMask = (UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight);
    drawImage.frame = self.bounds;
    [self addSubview:drawImage];
    self.backgroundColor = [UIColor clearColor];
    mouseMoved = 0;
    drawingPenColor = [[UIColor alloc] initWithWhite:0.0 alpha:1.0];
    [self becomeFirstResponder];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(shakeNotification:)
                                                 name:@"UIEventSubtypeMotionShakeEnded" object:nil];
}

-(BOOL)canBecomeFirstResponder {
    return YES;
}

-(void)maskImage:(UIImage *)image withMask:(UIImage *)maskImage 
{
    CGImageRef maskRef = maskImage.CGImage; 
    CGImageRef mask = CGImageMaskCreate(CGImageGetWidth(maskRef),
                                        CGImageGetHeight(maskRef),
                                        CGImageGetBitsPerComponent(maskRef),
                                        CGImageGetBitsPerPixel(maskRef),
                                        CGImageGetBytesPerRow(maskRef),
                                        CGImageGetDataProvider(maskRef), NULL, false);
    CGImageRef masked = CGImageCreateWithMask([image CGImage], mask);
    UIImage *tempImage = [[UIImage alloc] initWithCGImage:masked];
    //self.clippedImage = tempImage;
    [tempImage release];
    CFRelease(masked);
    CFRelease(mask);
}

-(void)awakeFromNib{
    [self initialize];
}

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        [self initialize];
    }
    return self;
}

-(id)init {
    self = [super init];
    [self initialize];
    return self;
}

-(id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    [self initialize];
    return self;
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    mouseSwiped = NO;
    UITouch *touch = [touches anyObject];
    
    /*if ([touch tapCount] == 2) {
        drawImage.image = nil;
        return;
    }*/
    
    lastPoint = [touch locationInView:self];
    [self.delegate pathDidStart:lastPoint];
    //lastPoint.y -= 20;
    NSLog(@"touches begin");
    
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event {
    mouseSwiped = YES;
    
    UITouch *touch = [touches anyObject];   
    CGPoint currentPoint = [touch locationInView:self];
    [self.delegate pathDidChange:currentPoint];
    //currentPoint.y -= 20;
    
    
    UIGraphicsBeginImageContext(CGSizeMake(self.bounds.size.width * pixelDensityMultiplyer, self.bounds.size.height * pixelDensityMultiplyer));
    [drawImage.image drawInRect:CGRectMake(0, 0, self.bounds.size.width * pixelDensityMultiplyer, self.bounds.size.height * pixelDensityMultiplyer)];
    CGContextSetLineCap(UIGraphicsGetCurrentContext(), kCGLineCapRound);
    CGContextSetLineWidth(UIGraphicsGetCurrentContext(), 5.0 * pixelDensityMultiplyer);
    
    CGContextSetStrokeColorWithColor(UIGraphicsGetCurrentContext(), [drawingPenColor CGColor]);
    
    CGContextBeginPath(UIGraphicsGetCurrentContext());
    CGContextMoveToPoint(UIGraphicsGetCurrentContext(), lastPoint.x * pixelDensityMultiplyer, lastPoint.y * pixelDensityMultiplyer);
    CGContextAddLineToPoint(UIGraphicsGetCurrentContext(), currentPoint.x * pixelDensityMultiplyer, currentPoint.y * pixelDensityMultiplyer);
    CGContextStrokePath(UIGraphicsGetCurrentContext());
    drawImage.image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    lastPoint = currentPoint;
    
    mouseMoved++;
    
    if (mouseMoved == 10) {
        mouseMoved = 0;
    }
    
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch *touch = [touches anyObject];
    [self.delegate pathDidEnd:[touch locationInView:self]];
    /*if ([touch tapCount] == 2) {
        drawImage.image = nil;
        [delegate didClearCanvas];
        return;
    }*/
    
    
    if(!mouseSwiped) {
        UIGraphicsBeginImageContext(self.bounds.size);
        [drawImage.image drawInRect:CGRectMake(0, 0, self.bounds.size.width, self.bounds.size.height)];
        CGContextSetLineCap(UIGraphicsGetCurrentContext(), kCGLineCapRound);
        CGContextSetLineWidth(UIGraphicsGetCurrentContext(), 5.0);
        CGContextMoveToPoint(UIGraphicsGetCurrentContext(), lastPoint.x, lastPoint.y);
        CGContextAddLineToPoint(UIGraphicsGetCurrentContext(), lastPoint.x, lastPoint.y);

        CGContextSetStrokeColorWithColor(UIGraphicsGetCurrentContext(), [drawingPenColor CGColor]);

        CGContextStrokePath(UIGraphicsGetCurrentContext());
        CGContextFlush(UIGraphicsGetCurrentContext());
        drawImage.image = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
    }
}

- (void)motionBegan:(UIEventSubtype)motion
          withEvent:(UIEvent *)event
{
    NSLog(@"Motion began");
	if (motion == UIEventSubtypeMotionShake)
	{
		NSLog(@"shake started");
        drawImage.image = nil;
        [delegate didClearCanvas];
        
	}
}

- (void)motionEnded:(UIEventSubtype)motion
          withEvent:(UIEvent *)event

{
    NSLog(@"Motion Ended");
    if (motion == UIEventSubtypeMotionShake)
	{
		NSLog(@"shake ended");
        drawImage.image = nil;
        
	}
}

-(void)shakeNotification:(NSNotification*)notification {
    NSLog(@"shake detected");
    drawImage.image = nil;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
