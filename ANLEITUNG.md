# Chatfilter

Sortiert deinen Chat: Nachrichten, die du auswählst, landen in eigenen kleinen Fenstern
irgendwo auf dem Bildschirm – oder werden ganz ausgeblendet. So gehen wichtige Nachrichten
nicht mehr im Spam unter.

Der Mod läuft nur bei dir. Der Server merkt nichts davon, du kannst ihn überall benutzen.

## Installieren

1. Fabric für **Minecraft 26.3** installieren.
2. **Fabric API** und **chatfilter-1.0.0.jar** in deinen `mods`-Ordner legen.
3. Minecraft starten – fertig.

## Loslegen

Drei Fenster sind schon fertig eingerichtet:

* **MSG** (oben links) – private Nachrichten
* **TEAM** (oben rechts) – Teamchat
* **Jobs** (unten rechts) – Job-Meldungen

Bleiben die Fenster leer? Dann schreibt dein Server die Nachrichten anders. Passe einfach den
Suchtext der passenden Regel an (siehe unten).

## Bedienung

| | |
| --- | --- |
| **K** | Einstellungen öffnen |
| **Mausrad** | im Fenster hoch- und runterscrollen (Chat muss offen sein) |
| **Maus draufhalten** | zeigt Infos an, z. B. Items, die jemand in den Chat schickt |
| `/chatfilter toggle` | Mod kurz aus- und wieder anschalten |
| `/chatfilter defaults` | alles auf Anfang zurücksetzen |

## Eigenes Fenster anlegen

**K** → *Chat-Fenster verwalten* → *Neues Fenster*

Dort gibst du ihm einen Namen und stellst Größe, Position und Aussehen ein.
Am einfachsten geht das über *Mit Maus anordnen*: Fenster einfach dahin ziehen, wo du es haben
willst, und an der Ecke unten rechts größer oder kleiner ziehen.

## Nachrichten in ein Fenster schicken

**K** → *Filterregeln verwalten* → *Neue Regel*

Du brauchst nur drei Dinge:

1. **Muster** – der Text, nach dem gesucht wird, z. B. `TEAM »`
2. **Aktion** – *In Fenster umleiten* (oder *Verstecken*, wenn du die Nachricht loswerden willst)
3. **Ziel** – in welches Fenster die Nachricht soll

Unten im Editor kannst du eine Testnachricht eintippen. Dann siehst du sofort, ob deine Regel
greift – ohne warten zu müssen.

**Tipp:** Stelle eine neue Regel erst auf *Behalten* und teste sie. Erst wenn sie das Richtige
findet, stellst du auf *Verstecken*. Sonst verschwindet vielleicht mehr, als dir lieb ist.

## Wenn etwas nicht klappt

* **Fenster bleibt leer** → Der Suchtext passt nicht. Schau dir eine echte Nachricht an und
  nimm ein Stück daraus, das nur dort vorkommt.
* **Nachricht ist ganz weg** → Bei der Regel ist kein Ziel-Fenster ausgewählt.
* **Fenster nicht zu sehen** → Unter *Fenster anordnen* werden alle angezeigt, auch leere.
* **Scrollen geht nicht** → Der Chat muss geöffnet sein.
* **Alles durcheinander** → `/chatfilter defaults` setzt alles zurück.

## Einstellungen

Alles wird in `config/chatfilter.json` gespeichert und bleibt bei einem Update erhalten.
Diese Datei kannst du auch an Freunde weitergeben, damit sie deine Einrichtung bekommen.
