# SketchID

**SketchID** is an Android app for collecting drawing data to train AI models to identify users
based on their drawing styles. The app supports two modalities:

1. Drawing images from visual prompts.
2. Drawing based on text prompts.

## Table of Contents

- [SketchID](#sketchid)
    - [Table of Contents](#table-of-contents)
    - [Installation](#installation)
    - [Features](#features)
    - [Usage](#usage)

## Installation

1. Clone the repository.
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.

## Features

- **Square Drawing Canvas**: Collects normalized (0.0 to 1.0) stroke data.
- **User Profiles**: Associate drawings with users.
- **Database**: Stores `users`, `images`, `words`, and `drawing_data`.
- **Export**: Save collected data as CSV.

## Usage

1. Draw in the **DrawingActivity**.
2. Manage users and export data via **SettingsActivity**.
