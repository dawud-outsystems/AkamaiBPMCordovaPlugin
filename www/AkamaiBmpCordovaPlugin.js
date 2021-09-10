var exec = require('cordova/exec');

// define Akamai BMP SDK Log Level
exports.LOG_LEVEL_INFO = 4;
exports.LOG_LEVEL_WARN = 5;
exports.LOG_LEVEL_ERROR = 6;
exports.LOG_LEVEL_NONE = 0xf;

// define setLogLevel method in Akamai BMP SDK
exports.setLogLevel = function (logLevel) {
    exec(null, null, 'AkamaiBmpCordovaPlugin', 'setLogLevel', [logLevel]);
};

// define getSensorData method in Akamai BMP SDK
exports.getSensorData = function (success) {
    exec(success, null, 'AkamaiBmpCordovaPlugin', 'getSensorData');
};

exports.initialize = function (baseUrl) {
    exec(null, null, 'AkamaiBmpCordovaPlugin', 'initialize', [baseUrl]);
};

exports.collectTestData = function(success) {
	exec(success, null, 'AkamaiBmpCordovaPlugin', 'collectTestData');
};

exports.configureChallengeAction = function (baseUrl) {
    exec(null, null, 'AkamaiBmpCordovaPlugin', 'configureChallengeAction', [baseUrl]);
};

exports.showChallengeAction = function (params) {

	let ccaObject = {
		actionCancelCallback: null,
		actionSuccessCallback: null,
		actionFailureCallback: null,

		actionCancel: function(param) {
			this.actionCancelCallback = param;
			return this;
		},
		actionSuccess: function(param) {
			this.actionSuccessCallback = param;
			return this;
		},
		actionFailure: function(param) {
			this.actionFailureCallback = param;
			return this;
		},

		execSuccess: function(response) {
			if (response.status == 0 && this.actionCancelCallback) {
				this.actionCancelCallback(response);
			} else if (response.status == 1 && this.actionSuccessCallback) {
				this.actionSuccessCallback(response);
			} else if (response.status == -1 && this.actionFailureCallback) {
				this.actionFailureCallback(response);
			}
		},
		execFailure: function(response) {
			console.error("CCA Failure: "+response);
		}
	};

	exec(function(param) {
			ccaObject.execSuccess(param);
		},
		function(param) {
			ccaObject.execFailure(param);
		},
		'AkamaiBmpCordovaPlugin',
		'showChallengeAction',
		[params]
	);
	return ccaObject;
};