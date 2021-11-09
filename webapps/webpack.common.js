const path = require('path');

let config = {
  context: path.resolve(__dirname, '.'),
  // set the entry point of the application
  // can use multiple entry
  entry: {
    automaticTranslationAdministration : './src/main/webapp/vue-apps/automatic-translation-administration/main.js',
    automaticTranslationExtension : './src/main/webapp/javascript/automatic-translation/extensions.js',
    automaticTranslationExtensionDisplay : './src/main/webapp/javascript/automatic-translation/main.js'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  externals: {
    vuetify: 'Vuetify',
    vue: 'Vue',
    jquery: '$'
  }
};

module.exports = config;
