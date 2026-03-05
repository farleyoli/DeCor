# DeCor

A desktop app for creating [Anki](https://apps.ankiweb.net/) flashcards directly from PDF documents.

Browse a PDF page-by-page, click to mark card regions, type your question and answer, and sync everything to Anki — cards are sent immediately when Anki is running, or batched via Ctrl+A.

## Features

- **PDF browsing** — Scroll through PDF pages with vim-style keybindings (j/k for line, d/u for page)
- **Visual card regions** — Click to mark the start and end of a card region on a page; a colored scroll bar shows where cards are located
- **Multi-page cards** — Card regions can span across multiple pages
- **Lazy rendering** — Pages are rendered on demand and cached as JPGs, keeping memory usage low even for large PDFs
- **Direct Anki sync** — Cards are sent to Anki immediately on creation (if connected) via [AnkiConnect](https://ankiweb.net/shared/info/2055492159); also available via Ctrl+A
- **Custom Anki note type** — The "DeCor" model is auto-created on first sync, with a back template that displays the surrounding PDF pages with the card region highlighted
- **Cross-platform** — Works on Linux, Windows, and macOS

## Requirements

- Java 17+
- [Anki](https://apps.ankiweb.net/) with the [AnkiConnect](https://ankiweb.net/shared/info/2055492159) plugin (for syncing cards)

## Installation

### From release

Download the JAR from the [Releases](https://github.com/farleyoli/DeCor/releases) page and run:

```bash
java -jar DeCor-1.0.jar
```

### From source

```bash
git clone https://github.com/farleyoli/DeCor.git
cd DeCor
./gradlew customFatJar
java -jar build/libs/DeCor.jar
```

Or run directly with Gradle:

```bash
./gradlew run
```

**Note:** The app must be run from the project root directory.

## Usage

1. Launch the app and select a PDF (or place PDFs in the `DeCor/pdf-files/` data directory)
2. Browse the PDF using scroll or keyboard shortcuts
3. **Click** on a page to mark the **start** of a card region
4. **Click** again to mark the **end** — a dialog opens to enter the question (front) and extra info (back)
5. Cards sync to Anki automatically if it's running; otherwise press **Ctrl+A** to sync manually
6. Press **Ctrl+S** to save your deck to disk

### Keyboard shortcuts

| Key | Action |
|---|---|
| `j` / `k` | Scroll down / up (small) |
| `d` / `u` | Scroll down / up (large) |
| `Ctrl+S` | Save deck |
| `Ctrl+A` | Save and sync to Anki |
| `Ctrl+Click` | Delete a card at that position |

### Data locations

| OS | Path |
|---|---|
| Linux | `~/.local/share/DeCor/` |
| Windows | `%LOCALAPPDATA%/DeCor/` |
| macOS | `~/Library/Application Support/DeCor/` |

## License

This project is open source.
