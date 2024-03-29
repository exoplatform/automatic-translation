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
      Vue.prototype.$automaticTranslationExtensionsService.getFeaturesOptions().then(data => {
        let featuresOptions = {
          streamTranslateShort: true,
          streamTranslateComment: true,
          newsTranslateView: true,
        };
        if (data){
          featuresOptions = data;
        }
        initExtensions(featuresOptions);
      });
    }
  });
}

export function initExtensions(featuresOptions) {
  const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
  const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.automaticTranslation.automaticTranslationExtension-${lang}.json`;
  exoi18n.loadLanguageAsync(lang, url);
  if (featuresOptions?.streamTranslateShort) {
    extensionRegistry.registerExtension('activity', 'action', {
      id: 'translate',
      rank: 9007199254740992,
      isEnabled: () => true,
      labelKey: 'UIActivity.label.translate',
      icon: 'fa-globe-americas',
      click: (activity, activityTypeExtension) => {
        const event = {
          id: activity.id,
          type: 'activity',
          spaceId: activity && activity.activityStream && activity.activityStream.space && activity.activityStream.space.id
        };
        if (!activity.translatedBody) {
          fetchTranslation(activityTypeExtension.getBody(activity),event).then(translated => {
            activity.translatedBody = translated.translation;
            activityTypeExtension.getTranslatedBody = getTranslatedBody;
            document.dispatchEvent(new CustomEvent('activity-translated',{ detail: event }));
          });
        } else {
          document.dispatchEvent(new CustomEvent('activity-translated',{ detail: event }));
        }
      },
    });      
  }

  if (featuresOptions?.streamTranslateComment) {
    extensionRegistry.registerExtension('activity', 'comment-action', {
      id: 'translate',
      rank: 9007199254740992,
      isEnabled: () => true,
      labelKey: 'UIActivity.label.translate',
      icon: 'fa-globe-americas',
      click: (activity, comment, activityTypeExtension) => {
        const event = {
          id: comment.id,
          type: 'comment',
          spaceId: activity && activity.activityStream && activity.activityStream.space && activity.activityStream.space.id
        };
        if (!comment.translatedBody) {
          fetchTranslation(activityTypeExtension.getBody(comment),event).then(translated => {
            comment.translatedBody = translated.translation;
            activityTypeExtension.getCommentTranslatedBody = getTranslatedBody;
            document.dispatchEvent(new CustomEvent('activity-comment-translated',{ detail: event }));
          });
        } else {
          document.dispatchEvent(new CustomEvent('activity-comment-translated',{ detail: event }));
        }
      },
    });
  }

  if (featuresOptions?.newsTranslateView) {
    extensionRegistry.registerExtension('news', 'translation-menu-extension', {
      id: 'news-auto-translate',
      rank: 1000,
      componentOptions: {
        vueComponent: Vue.options.components['news-automatic-translation'],
      },
    });
  }

  if (featuresOptions?.streamTranslateShort){
    extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
      id: 'translatedBody',
      vueComponent: Vue.options.components['activity-translated-body'],
      rank: 1,
    });
  }

  if (featuresOptions?.streamTranslateComment){
    extensionRegistry.registerComponent('ActivityContent', 'activity-content-extensions', {
      id: 'commentTranslatedBody',
      vueComponent: Vue.options.components['activity-comment-translated-body'],
      rank: 1,
    });
  }
  document.dispatchEvent(new CustomEvent('automatic-translation-extensions-updated'));
}


export function getTranslatedBody(activity) {
  return activity.translatedBody;
}

export function fetchTranslation(content, event) {
  let data = `message=${encodeURIComponent(content)}&locale=${eXo.env.portal.language}`;
  if (event?.type) {
    data += `&contentType=${event.type}`;
  }
  if (event?.spaceId) {
    data += `&spaceId=${event.spaceId}`;
  }
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/translate`, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    method: 'POST',
    body: data
  }).then(resp => {
    if (resp && resp.ok) {
      return resp.json();
    } else {
      dispatchError(event);
      throw new Error('Unable to get automatic translation result');
    }
  });
}

export function dispatchError(event) {
  document.dispatchEvent(new CustomEvent('activity-translation-error',{ detail: event }));
}

