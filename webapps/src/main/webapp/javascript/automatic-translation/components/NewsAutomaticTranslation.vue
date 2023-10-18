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
      autoTranslatedSummary: null,
      hasSummary: false,
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
      return this.hasSummary && this.autoTranslatedTitle && this.autoTranslatedContent && this.autoTranslatedSummary
          || this.autoTranslatedTitle && this.autoTranslatedContent;
    }
  },
  methods: {
    autoTranslate() {
      if (this.hasAutoTranslation) {
        this.resetAutoTranslation();
      } else {
        this.isAutoTranslating = true;
        fetchAutoTranslation(this.news?.title).then(translated => {
          this.handleTranslatedTitle(translated.translation);
          if (this.news?.summary) {
            this.hasSummary = true;
            fetchAutoTranslation(this.news?.summary).then(translated => {
              this.handleTranslatedSummary(translated.translation);
              this.fetchBodyTranslation();
            }).catch(() => this.isAutoTranslating = false);
          } else {
            this.hasSummary = false;
            this.fetchBodyTranslation();
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
    fetchBodyTranslation() {
      const content = this.excludeHtmlSpaceEntities(this.toHtml(this.news.body));
      fetchAutoTranslation(this.toHtml(content)).then(translated => {
        this.handleTranslatedContent(translated.translation);
        this.isAutoTranslating = false;
      }).catch(() => this.isAutoTranslating = false);
    },
    toHtml(content) {
      const domParser = new DOMParser();
      const docElement = domParser.parseFromString(content, 'text/html').documentElement;
      return docElement?.children[1].innerHTML;
    },
    resetAutoTranslation() {
      this.isResetAutoTranslating = true;
      this.autoTranslatedTitle = this.autoTranslatedSummary = this.autoTranslatedContent = null;
      this.updateNewsTitle(this.news.title);
      this.updateNewsSummary(this.news.summary);
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
      this.autoTranslatedContent = this.restoreHtmlSpaceEntities(translatedText);
      this.updateNewsContent(this.autoTranslatedContent);
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