/*
  Copyright (C) 2024 eXo Platform SAS.

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

export function initExt() {
  fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/isEnabled`, {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.text();
    } else {
      throw new Error('Unable to get automatic translation configuration');
    }
  }).then(result => {
    if (result === 'true') {
      getFeaturesOptions().then(data => {
        let featuresOptions = {
          notesTranslateView: true,
        };
        if (data){
          featuresOptions = data;
        }
        initExtensions(featuresOptions);
      });
    }
  });
}

export function getFeaturesOptions() {
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/getFeaturesOptions`, {
    headers: {
      'Content-Type': 'application/json'
    },
    method: 'GET'
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      throw new Error('Unable to get automatic translation Features Options');
    }
  });   
}

export function initExtensions(featuresOptions) {
  const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.automaticTranslation.automaticTranslationExtension-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url);

  if (featuresOptions?.notesTranslateView){
    extensionRegistry.registerExtension('notes', 'translation-menu-extension', {
      id: 'notes-auto-translate',
      rank: 1000,
      componentOptions: {
        vueComponent: Vue.options.components['note-automatic-translation'],
      },
    });
  }

  document.dispatchEvent(new CustomEvent('automatic-translation-extensions-updated'));
}