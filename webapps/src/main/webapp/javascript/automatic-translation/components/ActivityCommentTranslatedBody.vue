<!--
Copyright (C) 2021 eXo Platform SAS.

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
  <div>
    <div
      v-if="isTranslatedBodyNotEmpty && !translationHidden">
      <dynamic-html-element
        v-sanitized-html="translatedBody"
        :element="element"
        class="reset-style-box text-break overflow-hidden font-italic text-light-color translationContent"
        dir="auto" />
      <div
        class="font-italic text-light-color clickable caption"
        :class="$vuetify.rtl ? 'float-left' : 'float-right'"
        @click="hideTranslation">
        <v-icon size="12">mdi-translate</v-icon>
        <span>
          {{ $t('automaticTranslation.hideTranslation') }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    activity: {
      type: Object,
      default: null,
    },
    comment: {
      type: Object,
      default: null,
    },
    activityTypeExtension: {
      type: Object,
      default: null,
    },
  },
  data: () => ({
    translatedBody: null,
    translationHidden: true,
  }),
  computed: {
    isTranslatedBodyNotEmpty() {
      return this.translatedBody && this.$utils.trim(this.translatedBody).length;
    },
    useParagraph() {
      return !this.translatedBody.includes('</p>');
    },
    element() {
      return this.useParagraph && 'p' || 'div';
    },
  },
  created() {
    document.addEventListener('activity-comment-translated', (event) => {
      if (event.detail.id === this.activity.id) {
        this.retrieveCommentProperties();
        if (this.translatedBody) {
          this.showTranslation();
        }
      }
    });
    document.addEventListener('activity-translation-error', (event) => {
      if (event.detail.id === this.activity.id) {
        this.$root.$emit('alert-message', this.$t('automaticTranslation.errorTranslation'), 'error');
      }
    });
    this.retrieveCommentProperties();
  },
  methods: {
    retrieveCommentProperties() {
      if (this.activityTypeExtension
        && this.activityTypeExtension.getCommentTranslatedBody
        && this.activityTypeExtension.getCommentTranslatedBody(this.activity)) {
        this.translatedBody = this.activityTypeExtension.getCommentTranslatedBody(this.activity);
      }
    },
    hideTranslation() {
      this.translationHidden=true;
      this.$el.parentNode.parentNode.firstChild.classList.remove('hide');
    },
    showTranslation() {
      this.translationHidden=false;
      this.$el.parentNode.parentNode.firstChild.classList.add('hide');
    },
  },
};
</script>
