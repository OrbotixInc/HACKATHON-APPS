//
//  DrawingView.h
//  Draw Path
//
//  Created by Brandon Dorris on 8/19/11.
//  Copyright 2011 Orbotix. All rights reserved.
//

#import <UIKit/UIKit.h>

@protocol PathDelegate <NSObject>

- (void)pathDidStart:(CGPoint)point;
- (void)pathDidChange:(CGPoint)point;
- (void)pathDidEnd:(CGPoint)point;
- (void)didClearCanvas;

@end

@interface DrawingView : UIView {
    CGPoint lastPoint;
    UIImageView *drawImage;
    BOOL mouseSwiped;   
    int mouseMoved;
    UIColor *drawingPenColor;
    id <PathDelegate> delegate;
    float pixelDensityMultiplyer;
}

@property (nonatomic, retain)   UIColor             *drawingPenColor;
@property (nonatomic, retain)   UIImageView         *imageView;
@property (nonatomic, retain)   UIImageView         *drawImage;
@property (nonatomic, assign)   id <PathDelegate>   delegate;

@end
