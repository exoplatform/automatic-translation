const path = require('path');
const { merge } = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

// the display name of the war
const app = 'automatic-translation';

const config = merge(webpackCommonConfig, {
  mode: 'production',
  output: {
    path: path.resolve(__dirname, `./target/${app}/`),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  }
});

module.exports = config;
