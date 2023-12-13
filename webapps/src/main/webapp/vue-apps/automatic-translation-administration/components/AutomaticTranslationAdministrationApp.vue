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
    <v-main class="white card-border-radius pa-5">
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
          <v-col cols="4" class="ps-0">
            <v-select
              id="connectorSelector"
              ref="connectorSelector"
              v-model="selectedConnector"
              @change="changeConnector()"
              @blur="closeSelect()"
              :items="connectors"
              class="pt-1"
              item-text="description"
              item-value="name"
              outlined
              height="40"
              dense />
          </v-col>
        </v-row>
        <v-row class="pt-5" v-if="this.selectedConnector!='' && this.selectedConnector!='none'">
          {{ $t('automatic.translation.administration.enterYourKey') }}
        </v-row>
        <v-row v-if="this.selectedConnector!='' && this.selectedConnector!='none'">
          <v-col cols="4" class="ps-0">
            <div class="d-flex">
              <v-text-field
                :ref="apiKey"
                v-model="apiKey"
                :placeholder="$t('automatic.translation.administration.apiKeyPlaceHolder')"
                class="pa-0 my-auto"
                outlined
                dense />
              <v-btn
                class="btn btn-primary ms-8"
                @click="editApiKey"
                height="42">
                {{ $t('automatic.translation.apikey.button.save') }}
              </v-btn>
            </div>
          </v-col>
        </v-row>
        <v-row
          class="ms-n3"
          no-gutters>
          <v-col>
            <p class="font-weight-bold mb-0">
              {{ $t('automatic.translation.administration.modules.label') }}
            </p>
            <p class="text-caption mb-0">
              {{ $t('automatic.translation.administration.modules.description') }}
            </p>
          </v-col>
        </v-row>
        <v-row
          class="ms-n6 translation-module">
          <v-col>
            <span class="font-weight-bold">
              {{ $t('automatic.translation.administration.module.stream.label') }}
            </span>
            <v-row
              no-gutters
              class="ps-2">
              <v-col class="my-auto">
                {{ $t('automatic.translation.administration.module.stream.short.message') }}
              </v-col>
              <v-col class="my-auto">
                <v-switch
                  v-model="featuresOptions.streamTranslateShort"
                  class="pa-0 mb-0  mt-2"
                  @click="updateFeaturesOptions" />
              </v-col>
            </v-row>
            <v-row
              no-gutters
              class="ps-2">
              <v-col class="my-auto">
                {{ $t('automatic.translation.administration.module.stream.comments') }}
              </v-col>
              <v-col class="my-auto">
                <v-switch
                  v-model="featuresOptions.streamTranslateComment"
                  class="pa-0 mb-0  mt-2"
                  @click="updateFeaturesOptions" />
              </v-col>
            </v-row>
          </v-col>
        </v-row>
        <v-row
          class="ms-n6 mt-n3 translation-module">
          <v-col class="my-auto">
            <span class="font-weight-bold">
              {{ $t('automatic.translation.administration.module.news.label') }}
            </span>
            <v-row
              no-gutters
              class="ps-2">
              <v-col class="my-auto">
                {{ $t('automatic.translation.administration.module.news.view') }}
              </v-col>
              <v-col class="my-auto">
                <v-switch
                  v-model="featuresOptions.newsTranslateView"
                  class="pa-0 mb-0  mt-2"
                  @click="updateFeaturesOptions" />
              </v-col>
            </v-row>
          </v-col>
        </v-row>
        <v-row
          class="ms-n6 mt-n3 translation-module">
          <v-col>
            <span class="font-weight-bold">
              {{ $t('automatic.translation.administration.module.notes.label') }}
            </span>
            <v-row
              no-gutters
              class="ps-2">
              <v-col class="my-auto">
                {{ $t('automatic.translation.administration.module.notes.write') }}
              </v-col>
              <v-col class="my-auto">
                <v-switch
                  v-model="featuresOptions.notesTranslateEdition"
                  class="pa-0 mb-0  mt-2"
                  @click="updateFeaturesOptions" />
              </v-col>
            </v-row>
            <v-row
              no-gutters
              class="ps-2">
              <v-col class="my-auto">
                {{ $t('automatic.translation.administration.module.notes.view') }}
              </v-col>
              <v-col class="my-auto">
                <v-switch
                  v-model="featuresOptions.notesTranslateView"
                  class="pa-0 mb-0  mt-2"
                  @click="updateFeaturesOptions" />
              </v-col>
            </v-row>
          </v-col>
        </v-row>
      </v-container>
    </v-main>
  </v-app>
</template>

<script>
import {getConfiguration, setActiveConnector, setApiKey, updateFeaturesOptions} from '../automaticTranslationServices';
export default {
  data: () => ({
    connectors: [],
    selectedConnector: '',
    apiKey: '',
    featuresOptions: {}
  }),
  mounted() {
    this.$nextTick().then(() => this.$root.$emit('application-loaded'));
  },
  created() {
    this.getConfiguration();
  },
  methods: {
    getConfiguration() {
      getConfiguration().then(data => {
        this.connectors = data?.connectors || [];
        this.connectors.unshift({'name': 'none','description': this.$t('automatic.translation.administration.noconnectorselected')});
        this.selectedConnector = data?.activeConnector || 'none';
        this.apiKey = data?.activeApiKey || '';
        this.featuresOptions = data?.featuresOptions || {};
      });
    },
    closeSelect() {
      if (this.$refs.connectorSelector?.isMenuActive) {
        window.setTimeout(() => {
          this.$refs.connectorSelector.isMenuActive = false;
        }, 100);
      }
    },
    changeConnector() {
      let connector = null;
      if (this.selectedConnector !== 'none') {
        connector=this.selectedConnector;
      }
      setActiveConnector(connector).then(resp => {
        let message='';
        let type='';
        if (resp?.ok) {
          if (connector === null) {
            message = this.$t('automatic.translation.administration.changeConnector.noconnector.confirm');
          } else {
            message = this.$t('automatic.translation.administration.changeConnector.confirm');
          }
          type='success';
        } else {
          type='error';
          message = this.$t('automatic.translation.administration.changeConnector.error');
        }
        this.$root.$emit('alert-message', message, type);
        this.getConfiguration();
      });
    },
    editApiKey() {
      setApiKey(this.selectedConnector, this.apiKey).then(resp => {
        let message='';
        let type='';
        if (resp?.ok) {
          if (this.apiKey === '') {
            message = this.$t('automatic.translation.administration.changeApiKey.emptykey.confirm');
          } else {
            message = this.$t('automatic.translation.administration.changeApiKey.confirm');
          }
          type='success';
        } else {
          type='error';
          message = this.$t('automatic.translation.administration.changeApiKey.error');
        }
        this.$root.$emit('alert-message', message, type);
        this.getConfiguration();
      });
    },
    updateFeaturesOptions() {
      updateFeaturesOptions(this.featuresOptions).then(resp => {
        let message='';
        let type='';
        if (resp?.ok) {
          message = this.$t('automatic.translation.administration.changeFeatureOption.confirm');
          type='success';
        } else {
          type='error';
          message = this.$t('automatic.translation.administration.changeFeatureOption.error');
          this.getConfiguration();
        }
        this.$root.$emit('alert-message', message, type);
        
      });
    }
  }
};
</script>
