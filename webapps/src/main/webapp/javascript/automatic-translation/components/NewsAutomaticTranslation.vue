<template>
  <v-tooltip bottom>
    <template #activator="{ on, attrs }">
      <v-btn
        icon
        v-bind="attrs"
        v-on="on"
        @click="autoTranslate">
        <v-icon
          size="22"
          :class="hasAutoTranslation && 'primary--text' || ''">
          fa-language
        </v-icon>
      </v-btn>
    </template>
    <span>
      {{ $t('notes.automatic.translation.label') }}
    </span>
  </v-tooltip>
</template>

<script>

import {
  fetchAutoTranslation
} from '../../../vue-apps/automatic-translation-administration/automaticTranslationServices.js';

export default {
  data() {
    return {
      isResetAutoTranslating: false,
      isAutoTranslating: false,
      autoTranslatedContent: null,
      autoTranslatedTitle: null,
      autoTranslatedSummary: null
    };
  },
  props: {
    news: {
      news: {
        type: Object,
        default: null
      }
    }
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
    hasAutoTranslation() {
      return this.autoTranslatedTitle && this.autoTranslatedContent && this.autoTranslatedSummary;
    }
  },
  methods: {
    autoTranslate() {
      if (this.hasAutoTranslation) {
        this.resetAutoTranslation();
      } else {
        this.isAutoTranslating = true;
        fetchAutoTranslation(this.news.title).then(translated => {
          this.handleTranslatedTitle(translated.translation);
          fetchAutoTranslation(this.news.summary).then(translated => {
            this.handleTranslatedSummary(translated.translation);
            fetchAutoTranslation(this.news.body).then(translated => {
              this.handleTranslatedContent(translated.translation);
            });
          });
        });
      }
    },
    resetAutoTranslation() {
      this.isResetAutoTranslating = true;
      this.autoTranslatedTitle = this.autoTranslatedSummary = this.autoTranslatedContent = null;
      this.updateNewsTitle(this.news.title);
      this.updateNewsSummary(this.news.title);
      this.updateNewsContent(this.news.body);
      this.isResetAutoTranslating = false;
    },
    handleTranslatedTitle(translatedText) {
      this.autoTranslatedTitle = translatedText;
      this.updateNewsTitle(translatedText);
    },
    handleTranslatedSummary(translatedText) {
      this.autoTranslatedSummary = translatedText;
      this.updateNewsSummary(translatedText);
    },
    handleTranslatedContent(translatedText) {
      this.autoTranslatedContent = translatedText.replace('<p></p>', '<p>&nbsp;</p>');
      this.updateNewsContent(this.autoTranslatedContent);
      this.checkAutoTranslatedStatus();
    },
    updateNewsTitle(title) {
      this.$root.$emit('update-news-title', title);
    },
    updateNewsSummary(translation) {
      this.$root.$emit('update-news-summary', translation);
    },
    updateNewsContent(content) {
      this.$root.$emit('update-news-body', content);
    },
    checkAutoTranslatedStatus() {
      if (this.autoTranslatedTitle && this.autoTranslatedContent && this.autoTranslatedSummary) {
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