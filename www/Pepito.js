// Empty constructor
function Pepito() {}

// The function that passes work along to native shells
Pepito.prototype.pepito = function() {
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