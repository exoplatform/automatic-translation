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

<script>

export default {
  created() {
    document.addEventListener('translation-added', (event) => {
      this.autoTranslate(event.detail);
    });
  },

  methods: {
    autoTranslate(noteContent) {
      const promises = [
        {
          promise: this.$automaticTranslationExtensionsService.fetchAutoTranslation(noteContent.title, noteContent.lang)
            .then(translated => this.updateNoteTitle(translated.translation)),
          errorKey: 'NotesEditor.translate.title.error',
          restore: () => this.updateNoteTitle(noteContent.title)
        },
        noteContent?.properties?.summary && {
          promise: this.$automaticTranslationExtensionsService.fetchAutoTranslation(noteContent.properties.summary, noteContent.lang)
            .then(translated => this.updateNoteSummary(translated.translation)),
          errorKey: 'NotesEditor.translate.summary.error',
          restore: () => this.updateNoteSummary(noteContent.properties.summary)
        },
        noteContent.content && {
          promise: this.fetchContentTranslation(noteContent),
          errorKey: 'NotesEditor.translate.content.error',
          restore: () => this.updateNoteContent(noteContent.content)
        }
      ].filter(Boolean);

      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      Promise.allSettled(promises.map(t => t.promise)).then(results => {
        const failedIndices = results
          .map((result, i) => (result.status === 'rejected' ? i : null))
          .filter(i => i !== null);

        failedIndices.forEach(i => promises[i].restore());

        if (failedIndices.length > 1) {
          const errors = failedIndices.map(i => results[i].reason);
          this.$root.$emit('show-alert', {
            type: 'error',
            message: this.$t('NotesEditor.translate.error', {0: errors.map(e => e.message || e).join(', ')})
          });
        } else if (failedIndices.length === 1) {
          const i = failedIndices[0];
          const error = results[i].reason;
          this.$root.$emit('show-alert', {
            type: 'error',
            message: this.$t(promises[i].errorKey, {0: [error.message || error]})
          });
        }
      }).finally(() => {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
      });
    },
    fetchContentTranslation(note) {
      const content = this.excludeHtmlSpaceEntities(note.content);
      document.dispatchEvent(new CustomEvent('displayTopBarLoading'));
      return this.$automaticTranslationExtensionsService.fetchAutoTranslation(content, note.lang).then(translated => {
        const translatedContent = this.restoreHtmlSpaceEntities(translated.translation);
        this.updateNoteContent(translatedContent);
      }).finally(() => {
        document.dispatchEvent(new CustomEvent('hideTopBarLoading'));
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
    updateNoteSummary(summary) {
      this.$root.$emit('update-note-summary', summary);
    },
    updateNoteTitle(title) {
      this.$root.$emit('update-note-title', title);
    },
  }
};
</script>
