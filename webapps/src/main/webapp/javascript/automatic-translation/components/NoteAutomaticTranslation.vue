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
<template>
  <v-chip
    small
    :outlined="!isAutoTranslationSelected"
    :close="!!autoTranslatedContent"
    color="primary"
    class="my-auto mx-1"
    @click="autoTranslate"
    @click:close="resetAutoTranslation">
    {{ $t('notes.automatic.translation.label') }}
    <span
      v-if="sourceLanguageVersion"
      class="text-uppercase ms-1">
      ({{ sourceLanguageVersion }})
    </span>
  </v-chip>
</template>

<script>

import {
  fetchAutoTranslation
} from '../../../vue-apps/automatic-translation-administration/automaticTranslationServices.js';

export default {
  data() {
    return {
      isAutoTranslating: false,
      isResetAutoTranslating: false,
      autoTranslation: { value: 'autoTranslation', text: this.$t('notes.automatic.translation.label') },
      previouslyTranslatedVersion: null,
      autoTranslatedContent: null,
      autoTranslatedTitle: null,
      hasContent: false
    };
  },
  props: {
    note: {
      type: Object,
      default: null,
    },
    selectedTranslation: {
      type: Object,
      default: null,
    },
  },
  watch: {
    isAutoTranslating() {
      this.toggleTopBarLoading(this.isAutoTranslating);
    },
    isResetAutoTranslating() {
      this.toggleTopBarLoading(this.isResetAutoTranslating);
    }
  },
  computed: {
    isAutoTranslationSelected() {
      return this.selectedTranslation?.value === this.autoTranslation?.value;
    },
    sourceLanguageVersion() {
      return !!this.autoTranslatedContent && this.previouslyTranslatedVersion?.value;
    }
  },
  methods: {
    autoTranslate() {
      if (this.autoTranslatedContent) {
        this.setAutoTranslationSelected();
      } else {
        this.isAutoTranslating = true;
        fetchAutoTranslation(this.note.title).then(translated => {
          this.handleTranslatedTitle(translated.translation);
          if (this.note?.content) {
            this.hasContent = true;
            const content = this.excludeHtmlSpaceEntities(this.note.content);
            fetchAutoTranslation(content).then(translated => {
              this.handleTranslatedContent(translated.translation);
              this.setAutoTranslationSelected();
              this.previouslyTranslatedVersion = this.selectedTranslation;
              this.isAutoTranslating = false;
            }).catch(() => this.isAutoTranslating = false);
          } else {
            this.hasContent = false;
            this.setAutoTranslationSelected();
            this.isAutoTranslating = false;
          }
        }).catch(() => this.isAutoTranslating = false);
      }
    },
    excludeHtmlSpaceEntities(content) {
      return content.replace(/&nbsp;/gi, '<span class="notranslate">&nbsp;</span>');
    },
    restoreHtmlSpaceEntities(content) {
      return content.replace(/<span class="notranslate">&nbsp;<\/span>/gi, '&nbsp;');
    },
    setAutoTranslationSelected() {
      if (this.isAutoTranslationSelected) {
        return;
      }
      if (this.autoTranslatedTitle) {
        this.handleTranslatedTitle(this.autoTranslatedTitle);
      }
      if (this.autoTranslatedContent) {
        this.handleTranslatedContent(this.autoTranslatedContent);
      }
      this.updateSelectedTranslation(this.autoTranslation);
    },
    updateNoteContent(content) {
      this.$root.$emit('update-note-content', content);
    },
    updateNoteTitle(title) {
      this.$root.$emit('update-note-title', title);
    },
    updateSelectedTranslation(translation) {
      this.$root.$emit('update-selected-translation', translation);
    },
    resetAutoTranslation() {
      this.isResetAutoTranslating = true;
      this.autoTranslatedContent = this.autoTranslatedTitle = null;
      this.updateNoteTitle(this.note.title);
      this.updateNoteContent(this.note.content);
      this.updateSelectedTranslation(this.previouslyTranslatedVersion);
      this.isResetAutoTranslating = false;
    },
    handleTranslatedTitle(translatedText) {
      this.autoTranslatedTitle = translatedText;
      this.updateNoteTitle(translatedText);
    },
    handleTranslatedContent(translatedText) {
      this.autoTranslatedContent = this.restoreHtmlSpaceEntities(translatedText);
      this.updateNoteContent(this.autoTranslatedContent);
      this.updateSelectedTranslation(this.autoTranslation);
    },
    toggleTopBarLoading(loading) {
      if (loading) {
        document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      } else {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      }
    },
  }
};
</script>
