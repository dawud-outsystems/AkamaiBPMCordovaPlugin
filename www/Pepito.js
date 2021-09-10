// Empty constructor
function Pepito() {}

// The function that passes work along to native shells
Pepito.prototype.pepito = function(userId, successCallback, errorCallback) {
  var options = {};
  options.userId = userId;
  //cordova.exec(successCallback, errorCallback, 'DynatraceCordovaPlugin', 'identifyUser', [options]);
  alert("something");
}

// Installation constructor that binds Pepito to window
Pepito.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.pepito = new Pepito();
  return window.plugins.pepito;
};
cordova.addConstructor(Pepito.install);