/*
 * Copyright (C) 2023 eXo Platform SAS.
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
package org.exoplatform.automatic.translation.api.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AutomaticTranslationFeaturesOptions implements Serializable {

  private Boolean streamTranslateShort;

  private Boolean streamTranslateComment;

  private Boolean newsTranslateView;

  private Boolean notesTranslateEdition;

  private Boolean notesTranslateView;

  public AutomaticTranslationFeaturesOptions() {
    this.streamTranslateShort = true;
    this.streamTranslateComment = true;
    this.newsTranslateView = true;
    this.notesTranslateEdition = true;
    this.notesTranslateView = true;
  }
}
