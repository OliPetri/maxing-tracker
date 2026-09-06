# Maxing tracker

A RuneLite side panel for planning the remaining training time to level 99 in every skill.

## Features

- Remaining XP and estimated time to 99 for each unfinished skill. Skills at 99 are automatically hidden.
- Manually enter Custom XP/hr or select from 179 training presets, including AFK and ironman options. Rates never follow your measured XP/hr automatically.
- An overall progress bar caps each skill at the level-99 XP target, so XP above 99 does not inflate progress.
- **Track my maxing progress** saves your current remaining XP as a starting point. **Reset tracking** starts again from your current XP.
- **Passive / ignore time** excludes a skill from the time estimate when you plan to train it alongside another skill. Both XP progress bars still include it.
- Search, skill icons, and saved training choices.

For example, 1,000,000 remaining Sailing XP at a manually selected 50,000 XP/hr gives 20 hours.

## Using the estimates

Time is remaining XP divided by the selected XP/hr. The total adds unrounded times and displays minutes rounded up. Missing rates produce a known subtotal instead of a complete estimate.

Passive mode is a manual planning choice: it does not verify that another activity will supply enough XP to finish the ignored skill. Review ignored skills as your plan changes. Resource gathering, bank time and downtime are excluded unless a preset explicitly includes them.

Presets are a static research snapshot. Their tooltips show assumptions; right-click a method dropdown to open its Wiki source. You may select a method before meeting its requirements for planning purposes. Quest and equipment requirements are not automatically checked.

Some rates are provisional or derived. Guardians of the Rift combo and non-combo entries currently share general benchmarks, because a separate verified rate table was unavailable. Level-90 Leechfin rates are estimates derived from catch chances, rather than measured rates. See [rates and sources](WIKI-RATES.md) and [research notes](RESEARCH-NOTES.md) for details (currently in Danish).

## Saved settings

Custom rates, selected methods and passive choices are saved in the current RuneLite configuration profile and shared by characters using that profile. Tracking baselines are saved separately per RuneScape character and game mode. Resetting tracking does not change your rates or methods.

## Run locally

Install JDK 11 and open this folder in IntelliJ IDEA or VS Code. Set the Gradle JVM to JDK 11, then run:

```sh
./gradlew run
```

On Windows use `gradlew.bat run`. If needed on macOS/Linux, run `chmod +x gradlew` first. The included Gradle wrapper downloads Gradle and dependencies on first use.

Enable **Maxing tracker** in the development client's plugin list, open its sidebar tab, and log in. Jagex accounts require the setup described in the [official RuneLite guide](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts).

## Build and tests

```sh
./gradlew clean test jar
```

The project targets Java 11 and uses RuneLite's standard build conventions. It includes 21 automated tests covering calculations, progress, panel behaviour and the method catalogue. Test compilation has been verified against RuneLite 1.12.38. Test account switching, restart persistence and tracking reset in the game before submitting a release.

## Structure

- `src/main/java/com/maxingtime/`: plugin lifecycle, Swing panel, calculations, persistence and method catalogue loader.
- `src/main/resources/com/maxingtime/training-methods.tsv`: rates, benchmark levels, assumptions and source links.
- `src/test/java/com/maxingtime/`: tests and the local development launcher.
- `runelite-plugin.properties`: Plugin Hub metadata.

The existing Java package and configuration group remain `maxingtime` to preserve saved settings.

## Plugin Hub status

This project has not yet been approved for the Plugin Hub. Submission and review follow the [official Plugin Hub guide](https://github.com/runelite/plugin-hub#submitting-a-plugin).

Maintainer: [OliPetri](https://github.com/OliPetri). Code is licensed under the [BSD 2-Clause License](LICENSE).

[Detailed Danish documentation](README.da.md)
