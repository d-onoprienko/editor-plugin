# Inspection plugin

## Installation

- Using the IDE built-in plugin system:
  - <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Manage Plugin Repositories</kbd> > <kbd>Add</kbd> > <kbd>Enter http://localhost:8081/repository/idea-plugin-repo/updatePlugins.xml</kbd>
  - <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "inspection-plugin"</kbd> >
  <kbd>Install</kbd>
- Manually:

  Download the [latest release](http://localhost:8081/repository/idea-plugin-repo/inspection-plugin/) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

<!-- Plugin description -->
This plugin provides specific inspections that must be used in KamaGames projects

This specific section is a source for the [plugin.xml](/src/main/resources/META-INF/plugin.xml) file which will be extracted by the [Gradle](/build.gradle.kts) during the build process.

<!-- Plugin description end -->
---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
