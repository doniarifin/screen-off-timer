# Sleep Timer for Android
Android application designed to automatically turn off your screen or pause media playback after a set duration.

I'm still learning Android development, so the code may not follow best practices in some areas. Feedback and suggestions are welcome!

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/packages/com.inod.screenofftimer)

## Screenshots
### Dark theme

<img src="./screenshot/ss_dark_home.png" width="240"> <img src="./screenshot/ss_dark_setting.png" width="240">

### Light theme

<img src="./screenshot/ss_light_home.png" width="240"> <img src="./screenshot/ss_light_setting.png" width="240">

## Key Features
- **Precision Timer** — Set countdown up to 60 minutes using an intuitive circular drag slider.
- **Auto Lock Screen** — Lock screen automatically when timer ends via Accessibility Service or Device Admin.
- **Auto Stop Media** — Pause all media playback when the timer runs out.
- **Background Reliable** — Timer keeps running even after swiping the app away.
- **Persistent Notification** — Live countdown shown in notification bar with quick controls.
- **Material 3 Design** — Dynamic colors, dark/light theme support, clean modern UI.
- **No Ads, No Tracking** — Fully open source, no data collection.

## Tech Stack
- **Language** — Kotlin
- **UI Framework** — Jetpack Compose
- **Design System** — Material Design 3
- **Concurrency** — Kotlin Coroutines & Flow
- **Background** — Foreground Service

## How to Use
1. Launch the Screen Off Timer app.
2. Drag the circular slider or tap a preset to set duration.
3. Tap the play button to start the timer.
4. The app counts down in the background. Once it reaches zero, it will lock the screen or stop media playback.

## Contributing
Contributions are welcome! Here's how to get started:

1. Fork this repository
2. Create a new branch
```bash
   git checkout -b feature/your-feature-name
```
3. Make your changes and commit
```bash
   git commit -m "Add: your feature description"
```
4. Push to your fork
```bash
   git push origin feature/your-feature-name
```
5. Open a Pull Request

## License
This project is licensed under the [GPL-3.0 License](LICENSE).