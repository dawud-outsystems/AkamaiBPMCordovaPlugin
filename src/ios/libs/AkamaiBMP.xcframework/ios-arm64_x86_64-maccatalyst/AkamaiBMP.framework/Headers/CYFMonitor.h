//
//  CYFMonitor.h
//  CyberFendSDK
//
//  Copyright (c) 2015 CyberFend. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#if __has_feature(modules) 
@import CoreTelephony; 
@import SystemConfiguration; 
@import CoreMotion; 
@import Accelerate; 
#endif
typedef NS_ENUM(NSInteger, CYFLogLevel) {
    CYFLogLevelInfo = 4,
    CYFLogLevelWarn,
    CYFLogLevelError,
    CYFLogLevelNone = 0xF
};

/*!
 Methods for managing the result of challenge action
 */
@protocol CYFChallengeActionDelegate<NSObject>

/*!
 Notifies the delegate that the challenge action was successful and the app can resubmit
 the HTTP request
 */
- (void)onChallengeActionSuccess;
@optional
/*!
 Notifies the delegate that the challenge action was cancelled by the user.
 */
- (void)onChallengeActionCancel;
/*!
 Notifies the delegate that an error has occurred and the challenge action has failed.
*/
- (void)onChallengeActionFailure:(NSString *)message;

@end

@interface CYFMonitor : NSObject

/*!
 Call this method to initialize the SDK.
 */
+ (void)configure;

/*!
Call this method to initialize the SDK with Proof of Work functionality

@param: baseUrl Base URL of the protected endpoint
        For example, the base URL for the endpoint https://api.customer.com/app1/rest/v1/login
        would be https://api.customer.com
*/
+ (void)configureWithUrl:(NSString *)baseUrl;

/*!
Call this method to enable Challenge Action functionality

@param: baseUrl Base URL of the protected endpoint
        For example, the base URL for the endpoint https://api.customer.com/app1/rest/v1/login
        would be https://api.customer.com
*/
+ (void)configureChallengeAction:(NSString *)baseUrl;

/*!
Call this method to show challenge action dialog

@param: context Akamai-BM-Challenge-Context response header value from the HTTP request
@param: title Title for the challenge action dialog, use this to communicate the reason for the
action.
@param: message Descriptive text that provides additional details about the reason for the action.
@param: cancelButtonTitle Title for the cancel button in the dialog
@param: delegate A delegate to execute and notify the result of challenge action, it must adopt the
            CYFChallengeActionDelegate protocol
 @return: Returns YES if the challenge action dialog is displayed, otherwise NO
*/
+ (BOOL)showChallengeAction:(NSString *)context
                      title:(NSString *)title
                    message:(NSString *)message
          cancelButtonTitle:(NSString *)cancelButtonTitle
                   delegate:(id<CYFChallengeActionDelegate>)delegate;

/*!
 Call this method to get the final sensor data string whenever you want to send this data along with
 your network requests.
 */
+ (NSString *)getSensorData;

/*! CyberFend SDK automatically starts collecting the touch events on application's key window as
 soon as application starts.

 Optionaly You can call this method if you want SDK to listen touch events for any
 additional window your application may create.Pass the window object as parameter

 Note: CyberFend SDK doesn't store the reference of this window object.
 */
+ (void)startCollectingTouchEventsOnWindow:(UIWindow *)window;

/*!
 Get the CyberFend SDK version
 */
+ (NSString *)getVersion;

/*!
 Set the log level used by the SDK.
 */
+ (void)setLogLevel:(CYFLogLevel)logLevel;

@end
