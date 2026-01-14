# Automatic Translation connector

This addon is using to provide translation connector in the platform.

Three providers are implemented : 
- Google Translate
- DeepL
- LanguageWeaver (only Edge (on-prem) version)

# Configuration

## API Keys
Api keys must be provided for each provider you want to use. It can be done in the UI, in the Administration portal > Applications > Automatic Translation.

If you want to use LanguageWeaver, only EDGE (on-premise) version is supported.
You have to set one more property in the configuration file `exo.properties` :
```
exo.automatic-translation.language-weaver.url=${yourLWEdgeUrl}
```

# Connector specificities
## Language Weaver
When using LanguageWeaver Edge, the license allows some pairs of languages only.
When a user requires for a translation, the request use "AutoDetect" as source, and user locale as target language.
If the detected pair/user locale is not allowed by the license, the connector will try to fallback using the platform default language as target language.
If this fallback pair is not allowed too, the translation request will fail.


