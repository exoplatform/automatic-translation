import AutomaticTranslationAdministrationApp from './components/AutomaticTranslationAdministrationApp.vue';

const components = {
  'automatic-translation-administration-app': AutomaticTranslationAdministrationApp,
};

for (const key in components) {
  Vue.component(key, components[key]);
}
