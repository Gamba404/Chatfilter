# Chatfilter

Ein clientseitiger Fabric-Mod für **Minecraft 26.2**, der den Chat filtert und Nachrichten in eigene,
frei platzierbare Chat-Fenster umleitet – vergleichbar mit der Second-Chat-/Chat-Tab-Funktion von LabyMod,
aber mit vollem Regex-Support.

> **Für Spieler:** Eine kurze Bedienungsanleitung steht in [ANLEITUNG.md](ANLEITUNG.md).

## Funktionen

* **Eigene Chat-Fenster** ("zweiter Chat"): beliebig viele, jeweils mit eigener Position, Größe,
  Skalierung, Hintergrund-Deckkraft, Ausblendzeit, Zeitstempeln und Titelzeile.
* **Ausrichtung pro Fenster**: `Oben` – Nachrichten beginnen oben im Fenster und laufen nach unten
  (Standard) – oder `Unten` – am unteren Rand verankert wie der Vanilla-Chat. Die neueste Nachricht
  steht in beiden Fällen unten.
* **Filterregeln**, die von oben nach unten abgearbeitet werden. Pro Regel:
  * Muster-Typ: `Enthält`, `Beginnt mit`, `Endet mit`, `Exakt`, `Platzhalter (*)` oder **`Regex`**
  * Groß-/Kleinschreibung ignorieren, Bedingung umkehren
  * Aktion: `Verstecken`, `In Fenster umleiten`, `Hervorheben` (Farbe frei wählbar) oder `Behalten`
  * optional zusätzlich im Hauptchat lassen
  * optionaler Sound bei Treffer (beliebige Sound-ID, Lautstärke, Tonhöhe)
  * "Danach stoppen", um Regelketten zu bauen (z. B. eine `Behalten`-Ausnahme vor einer `Verstecken`-Regel)
* **Anordnen mit der Maus**: Fenster ziehen, an der Ecke unten rechts skalieren, Einrasten an den Bildschirmrändern.
* **Scrollen** in jedem eigenen Fenster mit dem Mausrad, solange der Chat geöffnet ist (Shift = zeilenweise).
* **Hover & Klick wie im Hauptchat**: Bei geöffnetem Chat zeigen die eigenen Fenster Tooltips für
  Hover-Events – also auch den vollständigen Item-Tooltip, wenn jemand per Befehl sein Item in den Chat
  schickt – und Klick-Events (Befehl ausführen, Link öffnen, Text einfügen) funktionieren ebenfalls.
* **Mitgelieferte Standardkonfiguration**: In einer frischen Instanz startet der Mod direkt mit der
  vorkonfigurierten Aufteilung (MSG / TEAM / Jobs), statt bei null anzufangen.
* **Live-Test** im Regel-Editor: Testnachricht eintippen und sofort sehen, ob die Regel greift.

## Bedienung

| Aktion | Weg |
| --- | --- |
| Konfiguration öffnen | Taste **K** (in den Steuerungs-Einstellungen änderbar) |
| Konfiguration öffnen | `/chatfilter` |
| Filter an/aus | `/chatfilter toggle` |
| Config neu laden | `/chatfilter reload` |
| Fenster leeren | `/chatfilter clear` |
| Auf mitgelieferte Standardkonfiguration zurücksetzen | `/chatfilter defaults` (sichert die alte Datei als `chatfilter.json.bak`) |
| Regeln gegen einen Text testen | `/chatfilter test <Nachricht>` |

Die Konfiguration liegt in `config/chatfilter.json` und kann auch von Hand bearbeitet werden
(danach `/chatfilter reload`). Existiert die Datei noch nicht, wird die im JAR mitgelieferte
`default_config.json` verwendet – eine vorhandene Konfiguration wird dabei **nie** überschrieben.
Wer die eigenen Einstellungen zum neuen Standard machen will, kopiert seine `config/chatfilter.json`
nach `src/main/resources/default_config.json` und baut neu.

## Beispiele für Regeln

| Ziel | Typ | Muster |
| --- | --- | --- |
| Private Nachrichten in ein eigenes Fenster | Regex | `^(?:\[.*\] )?(?:Von\|To\|From\|An) ` |
| Join-/Leave-Meldungen ausblenden | Regex | ` (joined\|left) the game$` |
| Alles von einem Spieler ausblenden | Regex | `^<Spielername>` |
| Werbung ausblenden | Enthält | `.gg/` |
| Erwähnungen des eigenen Namens hervorheben | Enthält | dein Name, Aktion `Hervorheben` |

Bei `Hervorheben` färbt der Mod nur die Textteile ein, die serverseitig noch keine eigene Farbe haben –
die Nachricht wird dafür durch eine eingefärbte Kopie ersetzt.

## Bauen

```bash
./gradlew build
```

Das fertige JAR liegt danach in `build/libs/chatfilter-1.0.0.jar`.

Zum Testen im Entwicklungs-Client:

```bash
./gradlew runClient
```

## Technik

| Baustein | Version |
| --- | --- |
| Minecraft | 26.2 |
| Fabric Loader | 0.19.3 |
| Fabric API | 0.156.0+26.2 |
| Fabric Loom | 1.17 |
| Gradle | 9.5.1 |
| Java | 25 (wird von Gradle automatisch geladen) |

Seit Minecraft 26.x gibt es **kein Yarn mehr** – Loom benutzt die offiziellen Mojang-Mappings.
Das HUD-Rendering läuft über die neue Render-State-Extraktion (`GuiGraphicsExtractor`), die
eigenen Fenster hängen per `HudElementRegistry` direkt hinter dem Vanilla-Chat.

Nachrichten werden über `ClientReceiveMessageEvents` (Fabric API) abgefangen – nicht per Mixin in den
Chat-Renderer. Dadurch bleibt der Mod kompatibel mit anderen Chat-Mods. Der einzige Mixin
(`ChatScreenMixin`) leitet nur das Mausrad um, wenn der Zeiger über einem eigenen Fenster steht.
