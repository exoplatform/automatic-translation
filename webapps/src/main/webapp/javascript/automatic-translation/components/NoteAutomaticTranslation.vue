<template>
  <v-chip
    v-if="autoTranslatedContent"
    small
    close
    :outlined="!isAutoTranslationSelected"
    color="primary"
    class="my-auto mx-1"
    @click="setAutoTranslationSelected"
    @click:close="resetAutoTranslation">
    {{ $t('notes.automatic.translation.label') }}
  </v-chip>
  <v-btn
    v-else
    text
    color="primary"
    class="px-1 py-0 text-decoration-underline"
    @click="autoTranslate">
    {{ $t('UIActivity.label.translate') }}
  </v-btn>
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
      previouslySelectedVersion: null,
      autoTranslatedContent: null,
      autoTranslatedTitle: null
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
    }
  },
  methods: {
    autoTranslate() {
      this.isAutoTranslating = true;
      fetchAutoTranslation(this.note.title).then(translated => {
        this.handleTranslatedTitle(translated.translation);
        const content = this.note.content.replace(/&nbsp;/gi, '@nbsp@');
        fetchAutoTranslation(content).then(translated => {
          this.handleTranslatedContent(translated.translation);
        });
      });
    },
    setAutoTranslationSelected() {
      if (!this.isAutoTranslationSelected && this.autoTranslatedTitle && this.autoTranslatedContent) {
        this.handleTranslatedTitle(this.autoTranslatedTitle);
        this.handleTranslatedContent(this.autoTranslatedContent);
        this.updateSelectedTranslation(this.autoTranslation);
      }
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
      this.updateSelectedTranslation(this.previouslySelectedVersion);
      this.isResetAutoTranslating = false;
    },
    handleTranslatedTitle(translatedText) {
      this.autoTranslatedTitle = translatedText;
      this.updateNoteTitle(translatedText);
      this.checkAutoTranslatedStatus();
    },
    handleTranslatedContent(translatedText) {
      this.autoTranslatedContent = translatedText.replace(/@nbsp@/gi, '&nbsp;');
      this.updateNoteContent(this.autoTranslatedContent);
      this.previouslySelectedVersion = this.selectedTranslation;
      this.updateSelectedTranslation(this.autoTranslation);
      this.checkAutoTranslatedStatus();
    },
    checkAutoTranslatedStatus() {
      if (this.autoTranslatedTitle && this.autoTranslatedContent) {
        this.isAutoTranslating = false;
      }
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
