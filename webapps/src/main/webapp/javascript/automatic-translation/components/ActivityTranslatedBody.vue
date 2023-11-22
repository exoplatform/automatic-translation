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
  <div
    v-if="isTranslatedBodyNotEmpty && !translationHidden">
    <v-divider class="mb-2" />
    <dynamic-html-element
      v-sanitized-html="translatedBody"
      :element="element"
      class="reset-style-box text-break overflow-hidden font-italic text-light-color"
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
</template>

<script>
export default {
  props: {
    activity: {
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
    translationHidden: false,
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
    document.addEventListener('activity-translated', (event) => {
      if (event.detail.id === this.activity.id) {
        this.translationHidden=false;
        this.retrieveActivityProperties();
      }
    });
    document.addEventListener('activity-translation-error', (event) => {
      if (event.detail.id === this.activity.id) {
        this.$root.$emit('alert-message', this.$t('automaticTranslation.errorTranslation'), 'error');
      }
    });
    this.retrieveActivityProperties();
  },
  methods: {
    retrieveActivityProperties() {
      if (this.activityTypeExtension && this.activityTypeExtension.getTranslatedBody) {
        this.translatedBody = this.activityTypeExtension.getTranslatedBody(this.activity);
      }
    },
    hideTranslation() {
      this.translationHidden=true;
    }
  },
};
</script>
