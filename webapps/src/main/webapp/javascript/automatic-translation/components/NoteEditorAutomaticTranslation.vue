<!--
Copyright (C) 2023 eXo Platform SAS.

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
-->

<script>

import {
  fetchAutoTranslation
} from '../../../vue-apps/automatic-translation-administration/automaticTranslationServices.js';

export default {
  created() {
    this.$root.$on('translation-added', (noteContent) => {
      this.autoTranslate(noteContent);
    });
  },

  methods: {
    autoTranslate(noteContent) {
      fetchAutoTranslation(noteContent.title,noteContent.lang).then(translated => {
        this.updateNoteTitle(translated.translation);
        if (noteContent.content) {
          const content = this.excludeHtmlSpaceEntities(noteContent.content);
          fetchAutoTranslation(content,noteContent.lang).then(translated => {
            const translatedContent = this.restoreHtmlSpaceEntities(translated.translation);
            this.updateNoteContent(translatedContent);
          });
        } 
      }); 
    },
    excludeHtmlSpaceEntities(content) {
      return content.replace(/&nbsp;/gi, '<span class="notranslate">&nbsp;</span>');
    },
    restoreHtmlSpaceEntities(content) {
      return content.replace(/<span class="notranslate">&nbsp;<\/span>/gi, '&nbsp;');
    },

    updateNoteContent(content) {
      this.$root.$emit('update-note-content', content);
    },

    updateNoteTitle(title) {
      this.$root.$emit('update-note-title', title);
    },
  }
};
</script>
