var exec = require('cordova/exec');

var PLUGIN_NAME = 'Onfido';

var onFido = {
    initOnfido: function (cb, options) {
        exec(cb, null, PLUGIN_NAME, 'initOnfido', [options]);
    }
};

module.exports = onFido;
