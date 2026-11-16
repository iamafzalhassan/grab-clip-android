# GrabClip

![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?logo=jetpackcompose&logoColor=white)
![yt-dlp](https://img.shields.io/badge/engine-yt--dlp_%2B_FFmpeg-FF0000)
![Platform](https://img.shields.io/badge/platform-Android_8.0%2B_(arm64)-3DDC84?logo=android&logoColor=white)

A native Android app, built with Kotlin and Jetpack Compose, that turns a copied video link into a file ready to share. Copy a YouTube, Instagram or Facebook link, open GrabClip, and it picks the link up on its own: the title, duration and available qualities appear, one tap downloads the one you want, and the file goes straight to the Android share sheet.

There is no text field, no account, no sign-in and no history. GrabClip handles public videos only and keeps one video at a time. It's a personal project: it's sideloaded and never published.

## Features

- **Reads the clipboard on its own**: open the app with a link copied and it is detected at once. A link that was already handled is never fetched twice, and the clipboard is only read when it has changed, so Android's "pasted" toast doesn't appear on every visit. **Paste Link** reads it on demand.
- **Three sites, one flow**: YouTube videos, Shorts, live and Music links; Instagram reels and posts; Facebook watch, reel, share and `fb.watch` links. A link is found even inside surrounding text, and pages that aren't videos are rejected.
- **Every resolution on offer**, highest first, then **Audio only**. Streams of the same resolution are one choice, and a vertical video is named by its short side, so 1080 × 1920 is "1080p".
- **Preferred quality**: choose 2160, 1440, 1080, 720, 480, 360 or Audio in Settings, and the closest match at or below it is selected for you.
- **Size estimates** next to every quality, and no number at all when the size is unknown, rather than a guess.
- **Files that play everywhere**: always MP4 with H.264 video and AAC audio, chosen over higher-bitrate VP9 or AV1 because the point of the file is to be shared.
- **Downloads that survive leaving the app**: a foreground service keeps the download alive in the background, with the percentage in a notification. A download can be cancelled at any time, and nothing is left behind.
- **Saved to Downloads**, in `Download/GrabClip`, with a clean file name taken from the video's title.
- **Delete After Sharing**, on by default: once a video has been shared, it is removed from Downloads when you come back to the app. With the setting off, every video stays.
- **A downloader that keeps up**: yt-dlp updates itself once a day at launch, and on demand from Settings, because an old yt-dlp is the usual reason a site stops working.
- **Plain-language errors** for no connection, no storage space, a removed video, a video that needs a sign-in, and a link that isn't a video.
- **No images, ever.** No thumbnails are loaded or cached; every thumbnail the extractor returns is ignored.

## Architecture

- **MVVM with unidirectional data flow.** Each screen has a ViewModel exposing one immutable `UiState` as a `StateFlow` and accepting a sealed `Event` through a single `onEvent`.
- **Packages named for their role.** `domain` holds the pure rules (link parsing, the quality catalogue, file names), `data` holds the extractor (`remote`), settings (`local`), the repository and file storage (`storage`), `ui` holds the theme, components and formatters, `platform` the Android bridges, and `feature` one package per screen. Only `app` sees everything, to wire the graph and run the download service.
- **One owner for each risky job.** `VideoExtractor` is the only class that talks to yt-dlp, and `VideoFiles` is the only class that creates, moves or deletes a video file.
- **Layering by convention.** Composables never touch the repository, the extractor or the file store; ViewModels never import Compose UI or a `Context`.
- **Manual dependency graph.** `AppGraph` builds the extractor, the repository, the preferences and `VideoFiles` once per process. No DI framework and no code generation.

## How a grab works

1. **Finding the link.** When the window gains focus, `ClipboardReader` compares the clip's timestamp with the last one it saw, and only reads a clip that is new. `VideoLinkParser` takes the first supported video link in the text and names its source.
2. **Reading the video.** yt-dlp fetches the title, duration and every stream the site offers. `QualityCatalog` folds those streams into one choice per resolution, sizes each by its H.264 stream plus the best M4A audio, and adds Audio only.
3. **Downloading.** `FormatSelector` turns the chosen quality into a yt-dlp format string that prefers H.264 and M4A and caps every fallback at the chosen height. YouTube serves anything above 720p as separate video and audio streams, which FFmpeg merges into one MP4.
4. **Reporting progress.** A merged download reports 0 to 100 twice, video then audio. `DownloadProgress` gives the first stream the first nine tenths and the rest the last tenth, and never moves backwards.
5. **Saving.** yt-dlp works in `cacheDir/downloads`, and the finished file is copied into `Download/GrabClip` through `MediaStore`, so no storage permission is needed. The cache is emptied at launch, before each download and after a failure or a cancel.
6. **Sharing and cleaning up.** The share sheet reports the chosen app to `ShareResultReceiver`. Android never says when that app has finished reading the file, so the video is deleted the next time GrabClip comes to the foreground, or at the next launch if it was closed. A video that was never shared is never deleted automatically.

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.2, Coroutines, Flow, JDK 17 |
| UI | Jetpack Compose, Material 3, Core SplashScreen |
| Lifecycle | AndroidX Lifecycle, ViewModel Compose |
| Engine | `youtubedl-android` 0.18.1 (yt-dlp) with its FFmpeg module |
| Background | Foreground service (`dataSync`) |
| Files | MediaStore, FileProvider |
| Settings | SharedPreferences, `org.json` |
| Platform | Android 8.0 (API 26) and later, `arm64-v8a` only, compile SDK 37 |

## Design system

GrabClip has its own look, separate from my other apps, inspired by the visual language of the "Dafi" case study by Mambo Mambo. It uses a warm off-white ground, white cards in soft greige trays, near-black ink and black buttons. The only colour is an iridescent pastel gradient, and it appears in exactly three places: the glow behind the heading, the download ring and the status strip. Each one means the app is working or has something ready. Depth comes from layering rather than shadow. Every gradient is a drawn Compose `Brush`, never an image.

Colours, spacing and typography are tokens in `ui/theme`, with SF Pro Display for all text. Shared components (`AppButtons`, `AppIconButton`, `AppSwitch`, `AppChip`, `QualityOption`, `AppTopBar`, `AppCard`, `AppTag`, `TwoToneHeading`, `IridescentGlow`, `ProgressRing`, `GlowStrip`, `AppSnackbar`, `AppModalSheet` and the iOS-style `ActivityIndicator`) live in `ui/components`. Screens never use a raw colour or a bare measurement. The launcher icon and splash screen are one vector mark, a split gradient ring, generated by `tools/mark.js`.

## Code conventions

- A strict member ordering convention for every Kotlin file: properties sorted by type tier, then type, then name; functions ordered by call order.
- No comments anywhere in the project. Names, types and ordering carry the meaning.
- All user-facing text lives in `strings.xml`.
- `.editorconfig` configures ktlint's `intellij_idea` style at a 240-column line width for the IDE; no ktlint plugin runs in the build.

## Project structure

```
app/src/main/java/com/example/grabclip/
    MainActivity.kt, GrabClipApplication.kt
    app/                    App, AppGraph, DownloadService, ShareResultReceiver
    domain/link/            VideoLink, VideoSource, VideoLinkParser
    domain/video/           VideoModels, QualityCatalog, FileNames
    domain/download/        DownloadState
    data/local/             SettingsPreferences
    data/remote/            VideoExtractor, FormatSelector, DownloadProgress, ExtractorErrors
    data/repository/        VideoRepository
    data/storage/           VideoFiles
    platform/               ClipboardReader, VideoSharer, NotificationPermission
    ui/theme/               AppColors, AppSpacing, AppTextStyles, GrabClipTheme
    ui/components/          Shared components: control/, display/, feedback/, sheet/
    ui/format/              DurationFormat, SizeFormat, QualityLabel
    common/                 StateSharing
    feature/grab/           The grab screen: link, qualities, download, share
    feature/settings/       Settings sheet: quality, delete after sharing, downloader update
tools/mark.js               Generates the launcher and splash vector mark
```

## Building

**Requirements:** Android Studio with the Android SDK (compile SDK 37), JDK 17, and an `arm64-v8a` device.

- Open the project in Android Studio and run the `app` configuration, or run `./gradlew :app:assembleDebug`.
- Only `arm64-v8a` is packaged, to keep the APK from carrying Python and FFmpeg four times over, so the app will not run on an x86 emulator.
- Release builds are shrunk with R8 and signed with the debug key, since the app is sideloaded; run `./gradlew :app:assembleRelease`.
- To change the launcher mark, edit `tools/mark.js` and run `node tools/mark.js <launcher.xml> <splash.xml>`.

## Testing

JUnit 4 tests live in `app/src/test/java`, mirroring the package of the code they cover:

- **`domain/link/VideoLinkParserTest`**: YouTube, Instagram and Facebook video links are recognised in every form, a link is found inside surrounding text, the first supported link wins, and channels, profiles, pages and other sites are rejected.
- **`domain/video/QualityCatalogTest`**: every resolution is offered once and highest first, sizes come from the H.264 stream plus audio, a vertical video is named by its short side, unknown sizes stay unknown, and the preferred quality picks the highest at or below the preference.
- **`domain/video/FileNamesTest`**: characters a file name cannot hold are removed, letters from other scripts are kept, long titles are shortened, and an empty result falls back to `video`.
- **`data/remote/FormatSelectorTest`**: a known height caps every alternative, an unknown height asks for the best, and Audio only prefers M4A.
- **`data/remote/DownloadProgressTest`**: the first stream fills nine tenths, a second stream fills the last tenth, and progress never moves backwards.
- **`data/remote/ExtractorErrorsTest`**: yt-dlp's messages are classified as offline, no space, unavailable, sign-in required or unsupported, and anything else falls back to a general failure.
- **`ui/format/DurationFormatTest`**: lengths read as `12:48` or `1:02:03`, and a negative length reads as `0:00`.
- **`ui/format/SizeFormatTest`**: sizes read in whole megabytes, gigabytes with one decimal, and never zero kilobytes, and an unknown size is left blank.

Run them with `./gradlew :app:testDebugUnitTest`.

## Roadmap

- GitHub Actions CI for tests and builds
- Support for more sites that yt-dlp already handles
