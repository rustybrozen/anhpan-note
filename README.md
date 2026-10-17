# AnhPan Note

A lightweight, offline-first Android note-taking application built with Jetpack Compose and Supabase.

## Features

- Offline-first note management with Room SQLite.
- Cloud synchronization with Supabase Auth and Postgrest.
- Search and multi-select batch deletion.
- Sharing notes via Android system intent.
- Minimalist adaptive app icon.
- Dual language support (English and Vietnamese).

## Tech Stack

- Kotlin & Jetpack Compose
- Room Database
- Supabase (Auth, Postgrest)
- Android Architecture Components (ViewModel, StateFlow)
- DataStore Preferences

## Setup

1. Add your Supabase credentials to `local.properties`:
   ```properties
   SUPABASE_URL="https://your-project.supabase.co"
   SUPABASE_ANON_KEY="your-anon-key"
   ```
2. Build and run the project using Android Studio or Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
