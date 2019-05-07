var exec = require('cordova/exec');

var PLUGIN_NAME = 'Onfido';

var onFido = {
    init: function (cb, options) {
        exec(cb, null, PLUGIN_NAME, 'init', [options]);
    }
};

module.exports = onFido;
