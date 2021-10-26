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
  <v-app role="main" id="automaticTranslationAdministration">
    <v-alert
      v-model="alert"
      :type="type"
      dismissible>
      {{ message }}
    </v-alert>
    <v-main class="white rounded-lg pa-5">
      <v-container fluid>
        <v-row>
          <h4 class="font-weight-bold">
            {{ $t('automatic.translation.administration.title') }}
          </h4>
        </v-row>
        <v-row class="pt-5">
          {{ $t('automatic.translation.administration.header') }}
        </v-row>
        <v-row>
          <v-col cols="3" class="ps-0">
            <v-select
              v-model="selectedConnector"
              @change="changeConnector()"
              :items="connectors"
              class="pt-1"
              item-text="description"
              item-value="name"
              outlined
              dense />
          </v-col>
        </v-row>
        <v-row class="pt-5" v-if="this.selectedConnector!='' && this.selectedConnector!='none'">
          {{ $t('automatic.translation.administration.enterYourKey') }}
        </v-row>
        <v-row v-if="this.selectedConnector!='' && this.selectedConnector!='none'">
          <v-col cols="3" class="ps-0">
            <v-text-field
              :ref="apiKey"
              v-model="apiKey"
              :readonly="!editingApiKey"
              :placeholder="$t('automatic.translation.administration.apiKeyPlaceHolder')"
              class="mx-2 pa-0"
              dense>
              <template v-slot:append-outer>
                <v-slide-x-reverse-transition mode="out-in">
                  <i
                    :key="`icon-${apiKey}`"
                    :class="editingApiKey ? 'uiIcon uiIconTick clickable success--text mt-1' : 'uiIcon uiIconEdit clickable primary--text mt-1'"
                    @click="editApiKey()"></i>
                </v-slide-x-reverse-transition>
              </template>
            </v-text-field>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import {getConfiguration, setActiveConnector, setApiKey} from '../automaticTranslationServices';
export default {
  data: () => ({
    connectors: [],
    selectedConnector: '',
    alert: false,
    type: '',
    message: '',
    apiKey: '',
    editingApiKey: false
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  created() {
    this.$root.$on('show-alert', message => {
      this.displayMessage(message);
    });
    this.getConfiguration();
  },
  methods: {
    getConfiguration() {
      getConfiguration().then(data => {
        this.connectors=data.connectors;
        this.connectors.unshift({'name': 'none','description': this.$t('automatic.translation.administration.noconnectorselected')});
        this.selectedConnector = data.active === null ? 'none' : data.active;
        this.apiKey = data.activeApiKey === null ? '' : data.activeApiKey;
      });
    },
    changeConnector() {
      let connector = null;
      if (this.selectedConnector !== 'none') {
        connector=this.selectedConnector;
      }
      setActiveConnector(connector).then(resp => {
        let message='';
        let type='';
        if (resp && resp.ok) {
          message = this.$t('automatic.translation.administration.changeConnector.confirm');
          type='success';
        } else {
          type='error';
          message = this.$t('automatic.translation.administration.changeConnector.error');
        }
        this.$root.$emit('show-alert', {
          type: type,
          message: message
        });
        this.getConfiguration();
      });
    },
    editApiKey() {
      if (this.editingApiKey) {
        setApiKey(this.selectedConnector, this.apiKey).then(resp => {
          let message='';
          let type='';
          if (resp && resp.ok) {
            message = this.$t('automatic.translation.administration.changeApiKey.confirm');
            type='success';
          } else {
            type='error';
            message = this.$t('automatic.translation.administration.changeApiKey.error');
          }
          this.$root.$emit('show-alert', {
            type: type,
            message: message
          });
          this.getConfiguration();
        }).finally(() => this.editingApiKey = false);

      } else {
        this.editingApiKey = true;
        this.$nextTick(() => {
          const $input = this.$refs['apiKey'];
          if ($input) {
            $input.focus();
          }
        });
      }
    },
    displayMessage(message) {
      this.message=message.message;
      this.type=message.type;
      this.alert = true;
      window.setTimeout(() => this.alert = false, 5000);
    }
  }
};
</script>
