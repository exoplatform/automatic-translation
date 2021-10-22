import './initComponents.js';


Vue.use(Vuetify);
const vuetify = new Vuetify(eXo.env.portal.vuetifyPreset);

document.dispatchEvent(new CustomEvent('displayTopBarLoading'));

const appId = 'automaticTranslationAdministration';

//getting language of the PLF
const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';

//should expose the locale ressources as REST API
const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.automaticTranslation.automaticTranslationAdministration-${lang}.json`;

export function init() {
  exoi18n.loadLanguageAsync(lang, url).then(i18n => {
    const appElement = document.createElement('div');
    appElement.id = appId;

    // init Vue app when locale ressources are ready
    new Vue({
      mounted() {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      },
      template: `<automatic-translation-administration-app v-cacheable id="${appId}" />`,
      vuetify,
      i18n
    }).$mount(appElement);
  });
}
