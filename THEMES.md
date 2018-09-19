![everestheader](https://user-images.githubusercontent.com/23148259/39124644-c886b47a-4719-11e8-953c-f079b3edb664.png)

# Theme Development Guide
This brief guide gets you started developing themes for Everest. It points you to the default themes in Everest for the UI and syntax. You can refer to these while developing your custom ones. The expected paths to the CSS files is also mentioned along with the property that needs to be defined in the settings file (`EverestInstallationDirectory/config/settings.json`) to activate the theme.

While development, you can edit the CSS file and use the `Shift + T` shortcut within Everest to reload the theme without restarting the application.

## UI Theme
- Default: [Adreana](https://github.com/RohitAwate/RESTaurant/blob/master/src/main/resources/css/Adreana.css)
- Location: `EverestInstallationDirectory/Everest/themes/`
- Property in Settings:
```json
{
    "theme": "NameOfYourThemeWithoutCSSExtension"
}
```

## Syntax Theme
- Default: [Moondust](https://github.com/RohitAwate/Everest/blob/master/src/main/resources/css/syntax/Moondust.css)
- Location: `EverestInstallationDirectory/Everest/themes/syntax/`
- Property in Settings:
```json
{
    "syntaxTheme": "NameOfYourSyntaxThemeWithoutCSSExtension"
}
```

#### Note
A GUI 'Preferences' menu will be added to Everest soon to allow for easily selecting your UI theme.