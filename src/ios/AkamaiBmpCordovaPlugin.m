/********* AkamaiBmpCordovaPlugin.m Cordova Plugin Implementation *******/

#import <AkamaiBMP/CYFMonitor.h>
#import <Cordova/CDV.h>


@interface CYFMonitor ()
+ (NSDictionary *)collectTestData;
@end

@interface AkamaiBmpCordovaPlugin : CDVPlugin <CYFChallengeActionDelegate> {
}

@property NSString *callbackId;
@end

@implementation AkamaiBmpCordovaPlugin

- (void)setLogLevel:(CDVInvokedUrlCommand *)command {
    NSString *ll = [command.arguments objectAtIndex:0];
    NSInteger logLevel = ll.integerValue;
    [CYFMonitor setLogLevel:(CYFLogLevel)logLevel];
}

- (void)getSensorData:(CDVInvokedUrlCommand *)command {
    CDVPluginResult *pluginResult = nil;
    NSString *sd = [CYFMonitor getSensorData];
    if ([sd length] > 0) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:sd];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }

    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)initialize:(CDVInvokedUrlCommand *)command {
	NSString *url = [command.arguments objectAtIndex:0];
	/**	CYFMonitor internally takes care of configure call	*/
	[CYFMonitor configureWithUrl:url];
	/**	posting notification so that the listeners will start capturing events	*/
	[[NSNotificationCenter defaultCenter] postNotificationName:@"CYF_BMP_SDK_INITIALIZED"
														object:nil
													  userInfo:nil];
}

- (void)collectTestData:(CDVInvokedUrlCommand *)command {
	CDVPluginResult *pluginResult = nil;
	NSDictionary *testData = [CYFMonitor collectTestData];

	if (testData) {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:testData];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
	[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)configureChallengeAction:(CDVInvokedUrlCommand *)command {
	NSString *url = [command.arguments objectAtIndex:0];
	[CYFMonitor configureChallengeAction:url];
}

- (void)showChallengeAction:(CDVInvokedUrlCommand *)command {
	NSDictionary *params = [command.arguments objectAtIndex:0];
	NSString *context = params[@"context"];
	NSString *title = params[@"title"];
	NSString *message = params[@"message"];
	NSString *cancelButtonTitle = params[@"cancelButtonTitle"];

	if (context != nil && [context stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceCharacterSet]].length != 0) {
		self.callbackId = command.callbackId;

		[CYFMonitor showChallengeAction:context title:title message:message cancelButtonTitle:cancelButtonTitle delegate:self];

		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
		[pluginResult setKeepCallback:[NSNumber numberWithBool:YES]];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	} else {
		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString: @"Context is missing"];
		[self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
	}
}

- (void)notifyStatus:(int)status message:(NSString *)message {
	NSMutableDictionary *response = [NSMutableDictionary dictionary];
	[response setObject:[NSNumber numberWithInt:status] forKey:@"status"];
	if (message) {
		[response setObject:message forKey:@"message"];
	}

	CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:response];
	[pluginResult setKeepCallback:[NSNumber numberWithBool:NO]];
	[self.commandDelegate sendPluginResult:pluginResult callbackId:self.callbackId];
}

- (void)onChallengeActionSuccess {
	[self notifyStatus:1 message:nil];
}

- (void)onChallengeActionCancel {
	[self notifyStatus:0 message:nil];
}

- (void)onChallengeActionFailure:(NSString *)message {
	[self notifyStatus:-1 message:message];
}

@end
