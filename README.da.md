# Maxing tracker 0.8.0 — RuneLite-plugin

Et sidepanel, der viser aktuel XP, XP til 99 og resterende træningstid for hver skill. Indtast din forventede XP/hr; øverst vises summen af effektive timer til max. Panelets tekster er på engelsk, så projektet kan videreudvikles til Plugin Hub.

## Opdater fra en tidligere version

Luk udviklingsklienten. Pak den nye ZIP ud i en ny mappe, åbn den i VS Code/IntelliJ, og kør `./gradlew run` fra den nye mappe. Dine gemte XP/hr ligger i RuneLite-konfigurationen og bevares som **Custom XP/hr**. Eksisterende Jagex-loginopsætning på samme Mac kan fortsat bruges. Skills på 99 skjules automatisk, også hvis den gamle `hideCompleted`-indstilling var slået fra.

## Nyt design i 0.7.0

Mørkegrå top med max-cape-ikon til højre for titlen, grøn samlet progressbar og gylden trackingbar. Hver skill vises som et kompakt kort med et skill-ikon, XP til 99, dropdown og XP/hr/tid nederst. Custom-feltet vises kun, når Custom er valgt. Søgefeltet filtrerer skillnavne uden at ændre totaler eller progress. Skills på 99 forbliver skjult ved søgning. Metodekrav vises som tooltip; benchmark over dit level vises med gylden tekst.

Agility, Mining, Smithing og Sailing har nye tegnede ikoner som i designforslaget. Øvrige skill-ikoner leveres af `SkillIconManager`, og max-capen hentes asynkront via `ItemManager` fra spillets cache. Max-capen vises derfor først, når spillets ikon er indlæst. Layoutet er tilpasset RuneLites normale sidebarbredde. Det tidligere godkendte billede var en forstørret designskitse.

## To progressbarer

- **Overall · 0 XP to max** viser hele vejen fra 0 XP til level 99 i alle skills. Beregningen er `100 × (1 − sum(XP remaining til 99) / (antal skills × 13.034.431))`. XP over 99 tæller aldrig med. De 24 nuværende skills giver et samlet mål på 312.826.344 XP fordelt på skills; spillets total-XP bruges ikke.
- Tryk **Track my maxing progress** for at gemme det antal XP, du mangler nu. **Since tracking started** viser derefter `100 × (startens XP remaining − nuværende XP remaining) / startens XP remaining`. Eksempel: start ved 1.000.000 tilbage og optjen 100.000 mod 99 → 10 %. XP i skills, der allerede er 99, flytter ingen af barerne.

Tracking gemmes pr. RuneScape-karakter og spiltilstand i RuneLites karakterprofil og fortsætter efter genstart. Knappen er kun aktiv, når du er logget ind, karakterprofilen er klar, og tracking endnu ikke er startet. Når tracking er startet, kan du bruge **Reset tracking** til at gemme et nyt startpunkt ved din aktuelle XP. Det starter trackingbaren fra 0 % igen; den samlede bar og dine rater påvirkes ikke. Reset gemmes for den aktuelle karakter og spiltilstand. Hvis du allerede er maxed, forbliver baren 100 %. En karakter, som allerede er maxed ved start, viser 100 %. Logout skjuler procenterne indtil næste login. Hvis RuneLite får en anden skill-liste, skal tracking startes igen, så startpunkt og mål matcher.

Procenter vises med to decimaler, afkortet så 100 % først vises, når ingen XP mangler. XP/hr og træningsmetodevalg påvirker kun tidsestimaterne; de påvirker ikke XP-progressbarerne.

Visningsnavnet er ændret til Maxing tracker. Java-klassenavne og konfigurationsgruppen `maxingtime` bevares for kompatibilitet med eksisterende indstillinger.

## Metoder i dropdownmenuen

Hver skill har **Custom XP/hr** og Wiki-forslag. Brug dropdownmenuen: første valg er Custom, og de øvrige metoder er sorteret efter XP/hr. Valget gemmes med det samme. Dropdownen har afrundede kanter, tegnet gul pil og ombrydning af lange navne. Slideren er fjernet. Valgte Wiki-rater er faste; XP/hr-feltet bliver skrivebeskyttet indtil du vælger Custom igen. Din seneste Custom-rate bevares separat.

Der følger **179 forslag fordelt på alle 24 skills** med. Agility har fx **Sepulchre F5 · Grand Coffin — 98.500 XP/hr**. Benchmark-level vises, og hvis dit level er lavere, markeres det. Forslagene kan vælges til planlægning uanset unlocks; pluginet kontrollerer ikke quests, udstyr eller andre krav. Hold musen over dropdownmenuen for metodens fulde navn og forudsætninger; højreklik på dropdownmenuen for at åbne Wiki-kilden.

Nyheder i 0.4.0: Shooting Stars, Motherlode Mine, amethyst, flere shipwreck-metoder (inklusive crew-AFK), Giants’ Foundry med flere legeringer og UIM-shopping, karambwans, anglerfish, glassblowing, charter glass, Mastering Mixology, Tithe Farm, flere Mahogany Homes-varianter, Vale Totems og flere low-effort-metoder. Se navn og forudsætninger for forskellen på AFK, lav indsats og aktive ironman-metoder. Ironman-rater inkluderer kun materialefremskaffelse, når det står udtrykkeligt.

Kataloget er et fast research-snapshot, ikke en live scraper. Se [RESEARCH-NOTES.md](RESEARCH-NOTES.md) for supplerende kilder, Wiki-gengivelser og kildebegrænsninger. Se [WIKI-RATES.md](WIKI-RATES.md) for rater, kilder og forudsætninger. Wikiens indekserede indhold kan være ældre end kontroldatoen; især Sailing-guiden markerer usikre/opdateringskrævende rater. Hitpoints-forslaget er en tydeligt markeret afledt beregning. Slayer-forslag er specifikke task-rater, ikke en samlet task-listes gennemsnit.

## Kør lokalt

1. Installer en **JDK 11** (fx Eclipse Temurin), og pak projektet ud.
2. Åbn mappen i IntelliJ IDEA som et Gradle-projekt. Vælg JDK 11 som projektets SDK og Gradle JVM.
3. Kør Gradle-opgaven `run`, eller brug terminalen i projektmappen:

   ```sh
   ./gradlew run
   ```

   Windows: `gradlew.bat run`. Hvis macOS/Linux melder manglende kørselsrettigheder: `chmod +x gradlew`.
4. Aktivér **Maxing tracker** i udviklingsklientens pluginliste. Åbn søjlediagram-ikonet i højre side og log ind.
5. Indtast fx `65000` i en skills XP/hr-felt. Tryk Enter eller forlad feltet for at gemme.

Gradle-wrapperen medfølger og henter selv Gradle og afhængigheder ved første kørsel. Der kræves internet. Projektet bruger `latest.release`, som RuneLites officielle skabelon, og Java 11 bytecode. Ved en forældet klient: `./gradlew --refresh-dependencies run`.

**Jagex-konto:** Udviklingsklienten kræver den særlige opsætning fra [RuneLites officielle vejledning](https://github.com/runelite/runelite/wiki/Using-Jagex-Accounts). Almindelig Gradle-start er ikke nødvendigvis nok til login.

XP/hr er altid din egen planlægningsantagelse, valgt manuelt eller via en metode. Pluginet måler aldrig din faktiske XP/hr og ændrer ikke raten, når du får XP eller levels. Eksempel: Sailing med 1.000.000 XP tilbage og manuelt indtastet 50000 XP/hr viser **20h 0m**.

## Adfærd og beregning

- `XP remaining = max(0, XP til level 99 − aktuel XP)`.
- `Timer = XP remaining / XP/hr`. Totalen summerer de uafrundede timer; visningen rundes op til nærmeste minut.
- Tomt felt eller `0` betyder ukendt rate. Manglende rater for ufærdige skills giver **Known subtotal** og et antal manglende rater, aldrig en misvisende fuld total.
- En skill på 99 eller derover bidrager med nul timer og behøver ingen rate.
- Rater er hele tal uden separatorer, fra 0 til 1.000.000.000. Ugyldig tekst gemmes ikke; beregningen beholder seneste gyldige rate.
- Rater gemmes via `ConfigManager`, separat pr. skill, i den valgte **RuneLite-konfigurationsprofil**. De deles mellem karakterer i samme profil og bevares ved genstart. Brug separate RuneLite-profiler for forskellige træningsplaner.
- XP læses fra den aktuelt indloggede karakter på klienttråden; Swing opdateres på UI-tråden. XP skjules, når klienten ikke er logget ind, og indlæses igen efter login/hop.
- Skills hentes fra `Skill.values()`; en eventuel `OVERALL` udelades. Sailing medtages, når den findes i API'et.
- Skills på 99 eller derover skjules altid automatisk, også i det øjeblik du opnår 99. Totalen omfatter fortsat alle skills.

Summen er en enkel træningsplan: skills regnes som separate aktiviteter. Samtidig XP i fx combat, Hitpoints og Slayer kan derfor tælles flere gange. Passive aktiviteter, banktid, pauser og økonomi modelleres ikke. Tallet er ikke en kalenderprognose eller en optimeret rute til max.

## Projektstruktur

```text
src/main/java/com/maxingtime/
  MaxingTimePlugin.java      Lifecycle, navigation og snapshots af spillets XP
  MaxingTimePanel.java       Scrollbart Swing-sidepanel og inputvalidering
  MaxingTimeConfig.java      RuneLite-konfigurationsgruppe
  XpRateStore.java           Gemte Custom-rater, metodevalg og parsing
  TrainingMethod.java       En træningsmetode med rate, level og kilde
  TrainingMethods.java      Indlæsning og validering af metodekatalog
  MaxingCalculator.java     XP-progress, beregninger og tidsformatering
  ProgressStore.java        Startpunkt pr. karakter og spiltilstand
src/test/java/com/maxingtime/
  MaxingTimePluginTest.java  Lokal RuneLite-launcher
  MaxingCalculatorTest.java  Beregnings- og inputtests
  MaxingTimePanelTest.java   Custom/metodeskift, skjulte 99-skills og logout
  TrainingMethodsTest.java  Katalogdækning og konkrete rateeksempler
  MaxingProgressTest.java   Progressformler, startknap, logout og karaktervisning
src/main/resources/com/maxingtime/
  training-methods.tsv      Kataloget med kilde og forudsætninger pr. metode
build.gradle                Java 11, RuneLite og lokal run-opgave
runelite-plugin.properties  Plugin Hub-metadata
```

## Kontrollér projektet

```sh
./gradlew clean test jar
```

Dette bygger plugin-JAR'en i `build/libs/`. Lokal afprøvning sker med `run`; JAR'en skal ikke manuelt kopieres ind i den normale klients Plugin Hub-cache.

Verificeret: Java 11-kompilering mod RuneLite **1.12.38**, samt **21 beståede automatiske tests** (beregning/input, Swing-panel og metodekatalog). Testene kører uden grafisk desktop. Login og brug inde i spillet er ikke afprøvet her.

Manuel afprøvning med en konto: kontrollér XP mod Skills-panelet; indtast en rate og optjen XP; genstart og kontrollér den gemte rate; prøv tomt/ugyldigt input; log ud og skift karakter; afprøv skjulte 99-skills, resetknap, metodevalg, tilbagevenden til Custom og scrollbar.

## Videre til Plugin Hub

Projektet bruger `build=standard`, ingen ekstra runtime-afhængigheder og normal RuneLite-konfiguration. Det er en basis, ikke et godkendt eller publiceret Plugin Hub-plugin. Tilpas forfatterfeltet, læg projektet i et offentligt GitHub-repository, afprøv det i spillet, og følg [Plugin Hub-guiden](https://github.com/runelite/plugin-hub#creating-new-plugins) for indsendelse og review.

API og struktur kontrolleret mod [officiel skabelon](https://github.com/runelite/example-plugin), [Client API](https://static.runelite.net/runelite-api/apidocs/net/runelite/api/Client.html), [ConfigManager](https://static.runelite.net/runelite-client/apidocs/net/runelite/client/config/ConfigManager.html) og [aktuel Skill-definition](https://github.com/runelite/runelite/blob/master/runelite-api/src/main/java/net/runelite/api/Skill.java).

## Opdatering 0.7.0

68 nye metodevalg: alle rooftops, Werewolf/Prifddinas, cooking-fisk og normal/1-tick karambwan, maple longbows, golems, potions, rumours/goats/chinchompas/Tecu, soul/lava runes, thieving, ironwood/bloodwood og leechfin. Sepulchre F4/F5 er rettet til 77/87; Grand Coffin har særskilte krav i tooltip.

Luk den gamle udviklingsklient, og kør `./gradlew run` fra denne mappe. Eksisterende gemte rater og tracking bevares.

## Passive / ignore time

Markér en skill som **Passive / ignore time**, når du forventer at træne den samtidig med en anden skill. Dens timer udelades fra summen og fra advarsler om manglende XP/hr. XP remaining, begge progressbarer og skjulning ved level 99 ændres ikke. Rater og metodevalg bevares, så du kan fjerne markeringen igen. Valget gemmes i samme RuneLite-konfigurationsprofil som dine rater; det deles mellem karakterer på denne konfigurationsprofil. Reset-knappen nulstiller kun tracking-starten, ikke disse valg.

Eksempel: Slayer estimeres til 20 timer og Magic til 10 timer. Når Magic markeres passive, viser summen 20 timer. Pluginet lover **ikke**, at de 20 Slayer-timer faktisk giver nok Magic XP. Når du stopper Slayer, skal du vurdere resterende combat XP og fjerne passive-markeringen efter behov. Hvis alle ufærdige skills er passive, vises “No active skills selected”.

Ved drift net fishing kan den skill, der kræver længst tid med metodens rate, bruges som aktiv, og den anden markeres passive. Det forudsætter, at du fortsætter drift net hele perioden. En fremtidig automatisk model bør i stedet beregne timer pr. aktivitet og fratrække aktivitetens XP i alle berørte skills, før resttiden beregnes. Den model er ikke implementeret i denne version.

Firemaking har nu fire lines med bl.a. maple, yew, magic og redwood samt separate langsommere campfire/AFK-valg. Wintertodt bevares. Hunter har Stymphikes ved level 82/85/86 og drift net; Fishing har også drift net.

## Opdatering 0.8.0

Giants' Foundry har rune/adamant 14:14, 253.110 XP/hr ved level 85 uden uniform. Guardians of the Rift har valg ved level 40, 50, 75 og 85, både med og uden combo-runes. Raterne er foreløbige fælles benchmarks: kilderne giver ikke en verificeret separat combo/non-combo XP-tabel. Se tooltip og RESEARCH-NOTES.md for forudsætninger.

Leechfin bruger nu level 90: ca. 120.900 XP/hr ved at droppe og 72.540 ved at skære fiskene. Begge er beregnede estimater, ikke direkte målte level-90 rater. De gamle metode-ID'er bevares, så allerede valgte Leechfin-metoder automatisk får det opdaterede benchmark. Custom-rater ændres ikke.
