
var exec = require('cordova/exec');

var PLUGIN_NAME = 'OnFido';

var onFido = {
  initOnfido: function(cb, applicantId) {
    exec(cb, null, PLUGIN_NAME, 'initOnfido', [applicantId]);
  }
};

module.exports = onFido;
