<!--
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
    {{ $t('article.automatic.translation.label') }}
    <span
      v-if="sourceLanguageVersion"
      class="text-uppercase ms-1">
      ({{ sourceLanguageVersion }})
    </span>
  </v-chip>
</template>

<script>

export default {
  data() {
    return {
      isAutoTranslating: false,
      isResetAutoTranslating: false,
      autoTranslation: { value: 'autoTranslation', text: this.$t('article.automatic.translation.label') },
      previouslyTranslatedVersion: null,
      autoTranslatedContent: null,
      autoTranslatedTitle: null,
      autoTranslatedSummary: null,
      hasSummary: false
    };
  },
  props: {
    news: {
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
        this.$automaticTranslationExtensionsService.fetchAutoTranslation(this.news?.title).then(translated => {
          this.handleTranslatedTitle(translated.translation);
          const summary = this.news?.properties?.summary;
          if (summary) {
            this.hasSummary = true;
            this.$automaticTranslationExtensionsService.fetchAutoTranslation(summary).then(translated => {
              this.handleTranslatedSummary(translated.translation);
              this.fetchBodyTranslation();
              this.setAutoTranslationSelected();
              this.previouslyTranslatedVersion = this.selectedTranslation;
              this.isAutoTranslating = false;
            }).catch(() => this.isAutoTranslating = false);
          } else {
            this.hasSummary = false;
            this.fetchBodyTranslation();
            this.setAutoTranslationSelected();
            this.previouslyTranslatedVersion = this.selectedTranslation;
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
    fetchBodyTranslation() {
      const content = this.excludeHtmlSpaceEntities(this.toHtml(this.news.body));
      this.$automaticTranslationExtensionsService.fetchAutoTranslation(this.toHtml(content)).then(translated => {
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
      const summary = this.news?.properties?.summary;
      this.updateNewsSummary(summary);
      this.updateNewsContent(this.news?.body);
      this.updateSelectedTranslation(this.previouslyTranslatedVersion);
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
      if (this.autoTranslatedSummary) {
        this.handleTranslatedSummary(this.autoTranslatedSummary);
      }
      this.updateSelectedTranslation(this.autoTranslation);
    },
    updateSelectedTranslation(translation) {
      this.$root.$emit('update-content-selected-translation', translation);
    },
  }
};
</script>
