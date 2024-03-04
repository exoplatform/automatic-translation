/*
 * Copyright (C) 2024 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

export function fetchAutoTranslation(content, lang) {
  if (!lang){
    lang = eXo.env.portal.language;
  }
  const data = `message=${  encodeURIComponent(content)  }&locale=${ lang }`;
  return fetch(`${eXo.env.portal.context}/${eXo.env.portal.rest}/automatic-translation/translate`, {
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    },
    method: 'POST',
    body: data
  }).then(resp => {
    if (resp?.ok) {
      return resp.json();
    } else {
      throw new Error('Unable to get automatic translation result');
    }
  });
}
