# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is DeCor

DeCor is a Java Swing desktop app for creating Anki flashcards directly from PDF documents. The user browses a PDF page-by-page, clicks to mark the start and end of a card region (with percentage position within the page), types front/back text, and syncs the cards to Anki via the AnkiConnect plugin.

## Build and Run Commands

```bash
# Run the application
./gradlew run

# Build
./gradlew build

# Run tests
./gradlew test

# Build fat JAR (output: build/libs/DeCor.jar)
./gradlew customFatJar
```

**Important:** The app must be run from the project root directory — `AnkiConnectHandler` reads resource files via relative paths like `./src/main/resources/styling.css`.

## Architecture

All source is in `src/main/java/fso/decor/`. Key classes and their roles:

- **Main** — Entry point; sets up FlatLightLaf theme and creates `MainContainer`.
- **MainContainer** (JFrame) — Root UI. Handles PDF selection, scroll pane setup, keybindings (`j/k` scroll unit, `d/u` scroll page, `Ctrl+S` save, `Ctrl+A` sync to Anki), and lazy page rendering via a viewport `ChangeListener`.
- **Book** (JPanel) — Scrollable container of `Page` objects. Owns the `Deck`. Maps page IDs to `Page` instances.
- **Page** (JPanel) — Represents one PDF page. Images are lazily created in a background thread on first view and unloaded when scrolled far away (`showImage()`/`hideImage()`). Contains a `ScrollBar` on the left.
- **ScrollBar** — Custom component drawn on the left of each page; visually shows where cards are located on that page.
- **PdfManager** — Wraps PDFBox. Converts PDF pages to JPGs on demand, cached on disk. Identifies PDFs by SHA-512 hash.
- **Deck** — In-memory card store. Loaded from and saved to `{pdfHash}.deck` in the images folder. Serialization uses `‽` as a field delimiter.
- **Card** — A flashcard with `front`, `back`, `beginningPage`, `beginningPercentage`, `endPage`, `endPercentage`, and `id`. Cards spanning multiple pages are tracked on each page.
- **AnkiConnectHandler** — HTTP client for the AnkiConnect API at `http://127.0.0.1:8765`. Creates the "DeCor" Anki model (using templates in `src/main/resources/`), creates decks, transfers media files, and adds new cards.
- **BookState** — Tracks whether the user has made the first click (start of card region) and stores its coordinates.
- **PageMouseListener** — Handles mouse clicks on a `Page` to initiate card creation (first click sets start, second click opens `QuestionTextField` dialog).
- **GlobalConfig** — Static config/path resolver. OS-specific data dirs: Linux `~/.local/share/DeCor/`, Windows `%LOCALAPPDATA%/DeCor/`, macOS `~/Library/Application Support/DeCor/`. Contains `pdf-files/` and `images/` subdirectories. Has a `isTest` flag that redirects to isolated test directories.

## Data Flow

1. PDF placed in `DeCor/pdf-files/` (or selected via file chooser and copied there).
2. On load, `PdfManager` computes a SHA-512 hash of the PDF — this hash keys all image files and the `.deck` save file.
3. PDF pages are rendered to `{hash}_{pageNumber:07d}.jpg` in `DeCor/images/` on demand.
4. The `Deck` is loaded from `DeCor/images/{hash}.deck` if it exists.
5. User creates cards; `Ctrl+S` serializes the deck back to that file.
6. `Ctrl+A` saves the deck, then syncs only new cards (`card.isNew() == true`) and their surrounding page images to Anki via AnkiConnect.

## Anki Integration

AnkiConnect must be running (Anki open with the plugin installed) for sync to work. The "DeCor" note type is auto-created on first sync using the HTML/CSS templates in `src/main/resources/`. The deck name equals the PDF filename (without `.pdf`). Image filenames sent to Anki are truncated to the last 50 characters (Anki limitation).
