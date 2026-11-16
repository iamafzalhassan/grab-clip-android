# GrabClip

Android app that turns a copied video link into a file ready to share. Open the
app with a YouTube, Instagram or Facebook link on the clipboard and it detects
the link, shows the video's title, duration and available qualities, downloads
the chosen quality, and hands the file to the Android share sheet. After sharing,
the downloaded file is deleted, unless the developer has switched that off.
Native Kotlin, Jetpack Compose (Material 3), `youtubedl-android` (yt-dlp) and
FFmpeg.

GrabClip is a personal project. It is sideloaded, never published and never put
on the Play Store, so the `com.example.grabclip` package and application id stay
as they are. It handles public videos only: no sign-in, no cookies, no account.

The rules in this file are carried over from `Debitter` and `QuietFeed`
(`C:\Users\AfzalHassan\My Projects`). Where the two disagree, `QuietFeed` wins,
because it is the newer and the stricter of the two; each such case is listed
under **Decisions**. Every decision is recorded in this file, not in separate
notes.

The bar is industry grade: one design system applied with total consistency, one
architecture applied the same way in every feature, and every file written to the
member ordering convention below from its first line.

## Hard constraints

- **Claude never builds, runs, tests or installs the app. The developer does.**
  No `gradlew`, no `assembleDebug`, no unit tests, no instrumented tests, no
  `adb`, no emulator, no Android Studio invocation. Not to "verify", not to
  "check it compiles", not once at the end. Verification is static: read the
  code, reason it through, and report which cases were checked by reading and
  which were not.
- **No git actions without explicit permission.** Never commit, push, branch,
  init or run any other git command unless the developer asks. Even when asked,
  confirm a second time before running it.
- **No images, ever.** No video thumbnails, no channel or profile pictures, no
  image loading library, no image caching. Every thumbnail field the extractor
  returns is ignored. Material vector icons in the chrome (top bar, empty
  states) are not images and are allowed.
- **No comments anywhere in the project. None, of any kind.** No `//`, no
  `/* */`, no KDoc `/** */`, no section headers, no TODOs, no commented-out
  code, no `#` comments in `.properties`, `.toml` or `.pro`, no `<!-- -->` in
  XML. This is absolute and it overrides §4 and §4a of the ordering convention
  below. Names, types and ordering carry the meaning; if something needs
  explaining, rename it. Tool-generated files are exempt because their tools
  write and rewrite them: `gradlew`, `gradlew.bat`,
  `gradle/gradle-daemon-jvm.properties`, `gradle/wrapper/gradle-wrapper.properties`
  and `local.properties`. They are never hand-edited.
- **Every Kotlin file follows the member ordering convention below**, without
  exception, tests included. A violation is a regression to fix, not legacy to
  leave alone.
- **Never touch another project.** `Debitter`, `QuietFeed`, `biller` and
  `Evenly` in `C:\Users\AfzalHassan\My Projects` are read-only design
  references. Read them freely; never create, edit, move, format or delete
  anything outside `GrabClip`.
- **Unit tests mirror the source tree.** A test for
  `app/src/main/java/<package path>/X.kt` lives at
  `app/src/test/java/<package path>/XTest.kt`, uses JUnit 4, covers critical
  flows, follows the member ordering and no-comments rules, and is run by the
  developer.
- **No code generation.** No KAPT, no KSP, no annotation processors, no
  serialization plugin. JSON is read by hand with `org.json`, and decoding is
  total: a missing or malformed field falls back to a default rather than
  throwing.
- **Alignment and spacing are strict.** Every measurement comes from
  `AppSpacing`. Never a bare number in a composable.
- **Single-line text never wraps.** `maxLines = 1` with `TextOverflow.Ellipsis`,
  so a long value can never push a row out of alignment. The one exception is a
  video title, see **Decisions**.
- **Loading is always the iOS activity indicator** (`ActivityIndicator`). Never
  a Material progress indicator, never a "Loading…" label. The one measured
  progress display is the download's `ProgressRing`, see **Decisions**.
- **Light theme only.** No dark scheme, no dynamic color, no following the
  system setting.
- **The design system below is the only source of look and feel.** It is
  GrabClip's own and deliberately not the family's; never port a colour, radius
  or component from `QuietFeed`, `Debitter`, `Evenly` or `biller` without
  recording it under **Decisions**.
- **All user-facing text is a string resource** in `res/values/strings.xml`,
  read with `stringResource` in composition and with `LocalResources` inside a
  coroutine (snackbar messages). No string literal reaches the screen.
- **A downloaded file is never left behind by accident.** Every file GrabClip
  writes has exactly one owner (`VideoFiles`) and one recorded reason to still
  exist. Anything else in the download folder is stale and is removed on the
  next launch.

## Architecture

Single `app` module. Each top-level package is named for its role, following
`QuietFeed` and `Evenly`. Update this as files are added or moved.

```
app/src/main/java/com/example/grabclip/
    MainActivity, GrabClipApplication    platform entry points
    app/                         App, AppGraph, DownloadService; the only package that sees every other one
    domain/                      pure Kotlin rules, no IO, no Compose UI
        link/                    VideoLink, VideoSource, VideoLinkParser
        video/                   VideoModels (VideoDetails, VideoQuality, VideoStream, VideoError), QualityCatalog, QualityPreference, FileNames
        download/                DownloadState, DownloadedVideo
    data/
        local/                   SettingsPreferences
        remote/                  VideoExtractor, FormatSelector, DownloadProgress, ExtractorErrors, ExtractionException
        repository/              VideoRepository
        storage/                 VideoFiles
    platform/                    ClipboardReader, VideoSharer, NotificationPermission
    ui/
        theme/                   AppColors, AppSpacing, AppTextStyles, GrabClipTheme
        components/control/      AppButtons, AppIconButton, AppSwitch, AppChip, QualityOption
        components/display/      AppTopBar, AppCard (AppTray, AppCard), AppTag, TwoToneHeading, IridescentGlow, ProgressRing, GlowStrip
        components/feedback/     ActivityIndicator, AppSnackbar
        components/sheet/        AppModalSheet
        format/                  DurationFormat, SizeFormat, QualityLabel
    common/                      StateSharing
    feature/grab/                GrabScreen, GrabViewModel, GrabUiState (GrabPhase, GrabEvent, GrabFeedback)
    feature/settings/            SettingsSheet, SettingsViewModel, SettingsUiState (SettingsEvent, UpdateResult)
app/src/main/res/font/           sf_pro_display_*.otf
app/src/main/res/values/         strings.xml, colors.xml, themes.xml
app/src/main/res/xml/            file_paths.xml
app/src/test/java/               one test file per tested source file, same package path
```

`app/` also holds `DownloadService` and `ShareResultReceiver`, because both
reach the repository through the graph and only `app` may see everything.

## Layering

1. Dependencies point inward. `domain` imports nothing from the app. `data`
   imports `domain` and `common`. `ui` imports `domain` and `common`.
   `platform` and `common` import nothing from the app. `feature/*` may import
   any of those, never `app` and never another feature. Only `app` sees
   everything, to wire the graph, navigation and background work.
2. Composables never touch the repository, the extractor or `VideoFiles`
   directly. Only ViewModels do.
3. ViewModels never import a Compose UI type or a `Context`. `@Immutable` /
   `@Stable` runtime annotations are the one exception.
4. State flows one way: an immutable `UiState` down as `StateFlow`, a sealed
   `<Feature>Event` up through a single `onEvent`. No state is duplicated
   between Compose and the ViewModel.
5. Model classes are `@Immutable` data classes whose lists are only ever rebuilt,
   never mutated in place.
6. A change to shared behaviour lands in one place. If two screens need the same
   widget, it moves to `ui/components/`, into the folder for its kind. Code only
   one feature uses lives in that feature.
7. `VideoExtractor` is the only class that talks to `youtubedl-android`, and
   `VideoFiles` is the only class that creates, moves or deletes a video file.
8. Dependency wiring is manual. `AppGraph` builds the extractor, the
   repository, the preferences and `VideoFiles` once; `GrabClipApplication`
   holds it. No DI framework.

## Decisions

Every deliberate deviation from the family rules or from this file, and every
significant choice, is recorded here as a bullet when it is made, with its
reason.

- **Where `Debitter` and `QuietFeed` disagree, `QuietFeed` wins.** No git
  actions without permission, every string in `strings.xml`, and no comments in
  `.properties`, `.toml` and `.pro` as well as Kotlin, Gradle and XML. The one
  rule taken from `Debitter` instead is **no code generation at all**: GrabClip
  has no Room database, so QuietFeed's KSP exception has nothing to apply to.
- **The look is GrabClip's own, not the family's.** At the developer's request
  the visual theme follows the "Dafi" case study by Mambo Mambo
  (`https://www.behance.net/gallery/253069605/Dafi`): a warm off-white ground,
  white cards in soft greige trays, near-black ink and black buttons, and an
  iridescent pastel gradient that appears only where the app is working or has
  something ready. Only the visual language is taken. No Dafi name, logo, icon,
  artwork, photograph or font is used, and every colour value below was chosen
  by eye from the published images, not copied from a source file. The family's
  structural rules still hold in full: every size from `AppSpacing`, every
  colour from `AppColors`, every style from `AppTextStyles`, the activity
  indicator for loading, and full-sentence snackbars. The family's dotted
  divider, navy and pill buttons are not used.
- **The typeface stays SF Pro Display**, the family's, because it is already on
  hand and sits close to the reference's geometric grotesque. The reference's
  airy feel comes from weight rather than family, so headings are large and
  Regular instead of Bold. The reference also sets some titles in a serif; that
  is not adopted, to keep one family.
- **Left out because they have no subject here:** Debitter's `BigDecimal` money
  rule and all of its PDF rules, and QuietFeed's Room, WorkManager, RSS and
  notification rules.
- **The engine is `youtubedl-android` (yt-dlp) with its FFmpeg module.**
  `QuietFeed`'s NewPipeExtractor handles YouTube only; yt-dlp handles all three
  sites behind one interface. YouTube serves anything above 720p as separate
  video and audio streams, so FFmpeg is needed to merge them. The cost is a
  large APK, accepted for a personal app.
- **Output is always MP4 with H.264 video and AAC audio**, chosen ahead of
  higher-bitrate VP9 or AV1 streams, because the whole point of the file is to
  be shared and that is the combination every receiving app plays.
- **No thumbnail**, at the developer's request, which is the family's
  "no images, ever" rule in any case.
- **Video titles may take three lines**, as in `QuietFeed`, because a single
  line hides most titles.
- **`ProgressRing` shows download progress.** A download is the one place a
  measured amount exists, and the activity indicator cannot show it. It is a
  custom ring drawn with the iridescent sweep, the percentage set in its
  centre, not a Material progress indicator. Fetching the video's details,
  which has no measurable progress, still uses `ActivityIndicator`.
- **Gradients are drawn, never loaded.** `IridescentGlow`, `ProgressRing` and
  `GlowStrip` are Compose `Brush` gradients whose stops fade to transparent, so
  the "no images, ever" rule holds and nothing depends on `Modifier.blur`, which
  only works on API 31 and later.
- **Delete after sharing is a setting, on by default.** With it on, the file
  lives in `cacheDir/downloads` and is shared through `FileProvider`. The share
  sheet reports the chosen app to `ShareResultReceiver`, which only raises a
  flag; the file is deleted the next time the screen starts (`ON_START`), which
  is the developer coming back from the other app. Android reports which app
  was chosen but never when that app has finished reading the file, so deleting
  at the moment of sharing would break the share. If the share sheet is
  dismissed without a choice, nothing is deleted, and the Delete button is
  always there.
- **With the setting off, the video is saved to the gallery the moment the
  download finishes**, not after sharing: `Movies/GrabClip` for video,
  `Music/GrabClip` for audio, and it is then shared from there. Saving at
  completion rather than on return means a kept video can never be lost to the
  app being closed mid-share. The setting is read when the download finishes,
  so changing it does not move a video that is already on screen. The Delete
  button becomes Done, which clears the screen and leaves the file.
- **Anything in `cacheDir/downloads` at launch is stale and is deleted**
  (`VideoRepository.prepare`). The download state lives in memory only, so a
  file found there at start belongs to no one. This is what makes "delete after
  sharing" hold even when the app was closed before the developer came back.
- **No storage permission.** `MediaStore` writes to the gallery on API 29 and
  later with no permission, as `Debitter` does for Downloads. On API 26–28 a
  kept video goes to the app's own external `Movies` or `Music` folder instead
  and is shared through `FileProvider`.
- **The clipboard is read when the window gains focus, and only when it has
  changed.** Android 10 and later refuse the clipboard to an app without focus,
  so `onResume` is too early; `LocalWindowInfo.isWindowFocused` is the signal.
  Android 12 and later show a "pasted" toast on every read, so
  `ClipboardReader.readIfChanged` compares the clip's timestamp, which costs no
  toast, and reads only a clip it has not seen. Paste Link always reads. A link
  that was just handled is therefore never fetched twice, and deleting a video
  does not make the app fetch the same link again.
- **A new link on the clipboard replaces the video on screen**, deleting it if
  it was not kept, except while a download is running, when the clipboard is
  ignored.
- **Downloads run in a foreground service** (`DownloadService`, type
  `dataSync`). Android freezes an app's processes soon after it leaves the
  screen, and yt-dlp is a child process, so without the service a download
  would stall the moment the developer switched apps. The service owns no
  logic: it watches `VideoRepository.download`, shows the percentage and stops
  itself when the download is no longer running. The notification permission is
  asked once, at the first download; the download runs whether or not it is
  granted.
- **One lock guards the engine.** Initialising, updating, reading a link and
  downloading all take `VideoExtractor`'s mutex, so yt-dlp is never replaced
  under a running download. The cost is that reading a link waits for the
  launch-time update to finish.
- **yt-dlp updates itself once a day at launch**, and on demand from Settings.
  The copy bundled in the library is as old as the library, and an old yt-dlp
  is the usual reason a site stops working.
- **Progress is one number across two streams.** A merged download reports 0 to
  100 twice, video then audio. `DownloadProgress` gives the first stream the
  first nine tenths and everything after it the last tenth, and never moves
  backwards. A single-stream download therefore ends at 90% and jumps to done.
- **Only 720p, 480p and Audio only are offered**, at the developer's request:
  the videos are for sharing, and a short list beats a full ladder. For each of
  720 and 480 the catalog offers the highest resolution the video has at or
  below it, so a video with 540 and 360 shows "540p" and "360p", and a video
  that has both targets shows exactly "720p" and "480p". If everything is above
  720, the lowest one is offered so there is always a video choice. The
  Preferred Quality setting has the same three choices and defaults to 720; a
  stored value from the older, longer list reads as 720.
- **A quality is named by the video's short side**, so a vertical 1080 × 1920
  video is "1080p", while the format selector caps on the real height. When a
  site reports no resolution at all, the one choice is "Best available".
- **Sizes are estimates until the download finishes**: the H.264 stream's size
  plus the best M4A audio. An unknown size shows nothing rather than a guess.
- **Only `arm64-v8a` is packaged**, to keep the APK from carrying Python and
  FFmpeg four times over. The app will not run on an x86 emulator.
- **There is no text field, confirm sheet or history.** A link arrives from the
  clipboard, Delete acts at once because the video can be downloaded again, and
  only one video exists at a time.
- **Public videos only.** No sign-in and no cookie import. A video that needs an
  account fails with a message that says so.
- **Release builds are signed with the debug key and shrunk with R8**, as in
  `QuietFeed`: the app is sideloaded and never published, so no release keystore
  is kept.
- **Launcher icon and splash share one vector mark**
  (`drawable/ic_launcher_foreground`): a letter G for GrabClip, drawn as a
  single round-capped stroke, 11 wide — a ring of radius 19 on the 108 canvas, open at
  the upper right, with a bar running in from the right to the centre — and
  stroked with a diagonal linear gradient in the five `iridescent` hues, top left
  to bottom right, on `brand_paper`. The gradient runs from 36.68 to 71.32 on
  both axes, which is exactly where the diagonal enters and leaves the stroked
  ring, and its stops sit at 0.1, 0.3, 0.5, 0.7 and 0.9 with the end colours
  held to the edges, so each of the five colours gets an equal band; fitted to
  the whole canvas, the first and last colours fell outside the letter. If the
  ring's radius or stroke changes, recompute both ends. It is linear on purpose: a sweep gradient
  pinches every colour to a point at the centre, exactly where the G's bar
  ends, and showed as a hard pinwheel there. This was
  the developer's choice over a black ground. The pastel stops themselves
  disappear on paper, so the icon alone uses deeper versions of the same hues
  (butter `#F2C230`, rose `#EE7FA3`, lilac `#A98BE6`, sky `#6FB2EE`, mint
  `#7FCF8A`); they are not `AppColors` tokens and are used nowhere else.
  The developer's reference was Google's G; the mark is GrabClip's own drawing
  with its own geometry, deliberately not a recolour of that logo. It is an
  adaptive icon with the same drawable as its monochrome layer, and the
  template's raster mipmaps were removed because `minSdk` 26 always uses the
  adaptive icon. The splash is `core-splashscreen` (`Theme.GrabClip.Launch`) on
  `brand_paper`, then `Theme.GrabClip` with the same paper window background,
  so the splash hands over to the app without a colour change.
- **No backup.** `allowBackup="false"`, as in `Debitter` and `QuietFeed`.
- **Versions match `QuietFeed`** (AGP 9.1.1 with built-in Kotlin, Kotlin 2.2.10
  for the Compose plugin, Compose BOM 2026.08.00, `compileSdk` 37, `targetSdk`
  36, `minSdk` 26, Java 17), so one machine builds both. `youtubedl-android` is
  0.18.1, the newest on Maven Central when the app was written.
- **Unverified until the developer runs it:** the whole app, since Claude never
  builds. In particular: Gradle sync with `youtubedl-android` 0.18.1 on AGP
  9.1.1; that the library's native binaries load with `targetSdk` 36 and
  `useLegacyPackaging`; the R8 keep rules in `proguard-rules.pro`, which are a
  cautious guess; yt-dlp's real answers for Instagram and Facebook links; and
  how the theme's colours, the glow and the ring look on a phone.

---

# Design system

GrabClip's own, modelled on the "Dafi" case study (see **Decisions**). The idea
in one line: a calm, warm, almost colourless surface, on which a soft
iridescent gradient is the only colour and always means something — the app is
working, or a video is ready.

Three rules keep it honest:

1. **Colour is a signal, not decoration.** The iridescent gradient appears in
   exactly three places: the glow behind the heading, the download ring, and the
   strip that carries the download's status. Nothing else is tinted.
2. **Black is the only action colour.** The primary button, the active icon
   button and the selected option are near-black. There is no brand hue.
3. **Depth comes from layering, not shadow.** Ground, then a greige tray, then
   white cards. No elevation, no drop shadow, and a border only where two
   whites would otherwise meet.

## Colors — `ui/theme/AppColors.kt`

| Token | Hex | Use |
|---|---|---|
| `primary` | `#111111` | ink black — primary button, active icon button, selected option border and check |
| `primaryOn` | `#FFFFFF` | text, icon and spinner on primary |
| `danger` | `#C8402F` | inline errors, danger buttons, failure snackbar |
| `success` | `#2E7D4F` | success snackbar |
| `surfaceBase` | `#F5F3F0` | scaffold ground, window background, sheets |
| `surfaceSunken` | `#ECE8E2` | trays that group cards, disabled fill, ring track, switch track when off |
| `surfaceCard` | `#FFFFFF` | cards, icon buttons, secondary buttons |
| `surfaceInverse` | `#111111` | neutral snackbar |
| `divider` | `#E4DFD8` | sheet drag handle, disabled button border |
| `textPrimary` | `#111111` | values, headings, icons |
| `textSecondary` | `#6F6B66` | labels, captions, meta |
| `textTertiary` | `#8E8983` | the quiet line of a two-tone heading, hints, empty state icon |
| `textDisabled` | `#B9B4AD` | disabled text |
| `tagLime` | `#DFF08A` | the source tag |
| `glowButter` | `#F9E08A` | iridescent stop |
| `glowRose` | `#F7C4D4` | iridescent stop |
| `glowLilac` | `#D8C8F3` | iridescent stop |
| `glowSky` | `#BFDCF5` | iridescent stop |
| `glowMint` | `#CDEBC4` | iridescent stop |

`AppColors` is an `object` of `Color` vals in name order, plus one list,
`iridescent`, holding the five glow stops in the order butter, rose, lilac, sky,
mint. Every gradient is built from that list, so the three gradient components
can never drift apart. `textTertiary` is for large text and icons only; anything
at body size or smaller uses `textSecondary` or darker, so it stays readable on
the ground. The Material `lightColorScheme` in `GrabClipTheme` maps every slot
onto these tokens, so no Material default colour can leak into a component. The
window theme is `android:Theme.Material.Light.NoActionBar` on `surface_base`,
and `MainActivity` uses `SystemBarStyle.light`, so the status and navigation bar
icons are dark. Text selection uses `primary` at `SELECTION_ALPHA` 0.16.

## Spacing and sizing — `ui/theme/AppSpacing.kt`

Scale `xs 4, sm 8, md 12, lg 16, xl 24, xxl 40`.
`screenPadding 16`, `cardPadding 16`, `trayPadding 8`, `controlHeight 52`,
`topBarHeight 56`, `hairline 1`. `buttonHeight` is a computed alias of
`controlHeight`, so a button and a field can never drift apart.
Radii: `radiusTag 6`, `radiusField 12`, `radiusButton 12`, `radiusCard 20`,
`radiusTray 28`. A tray's radius is its card's radius plus `trayPadding`, so the
two corners stay concentric; change one and the other follows.
Icons: `icon 20`, `iconButton 40`, `iconEmptyState 32`, `emptyStateIcon 80`.
Controls: `checkBox 24`, `chipHeight 36`, `switchHeight 28`, `switchWidth 48`.
Indicators: `progressIndicator 20`, `progressRing 200`, `ringStroke 14`,
`glowHeight 320`. Edges: `fadingEdge 32`.

The token set is trimmed to what the app uses, so an unreferenced token is dead
code rather than vocabulary. Add a token here before using a new size, and name
it for its role, not its value.

## Typography — `ui/theme/AppTextStyles.kt`

SF Pro Display for everything, from `res/font/` (`sf_pro_display_regular`,
`sf_pro_display_medium`, `sf_pro_display_bold`). Upright only, no italics, no
other family. Large text is Regular and tight; weight is saved for small text,
where Medium does the work Bold does in the family apps. Every numeric style
carries `fontFeatureSettings = "tnum"`. `AppTextStyles` is an `object`, and
every style is built by one private `style(...)` helper.

| Style | Size / weight | Use |
|---|---|---|
| `metric` | 40 / 400 tnum, -0.5 tracking | the percentage inside the ring |
| `display` | 28 / 400, -0.3 tracking | the two-tone heading |
| `videoTitle` | 22 / 400, -0.2 tracking | the video's title |
| `sectionHeading` | 20 / 400 | sheet titles, card group titles |
| `screenTitle` | 15 / 500 | app bar title |
| `button` | 15 / 500 | all buttons |
| `body` | 15 / 400 | prose, confirm sheet message |
| `listPrimary` | 15 / 500 | quality name, settings row title |
| `snack` | 14 / 500 | snackbar message |
| `listMeta` | 13 / 400 tnum | duration, file size, download progress |
| `listSecondary` | 13 / 400 | helper text, empty state message, settings row subtitle |
| `errorHint` | 12 / 500 | inline errors |
| `overline` | 10 / 600, +0.6 tracking | the source tag |

## Component conventions — `ui/components/`

- **Ground and glow** (`IridescentGlow`) — every screen is `surfaceBase`. Behind
  the top of the main screen sits one glow, `glowHeight` tall: overlapping
  radial gradients in the `iridescent` stops, each fading to transparent, so it
  reads as light on the paper rather than as a coloured panel. A vertical fade
  to `surfaceBase` covers its lower part, and the canvas is clipped to its
  bounds: a Compose `Canvas` does not clip on its own, so without the clip the
  circles carry on below the fade and reappear as a hard line. It is still
  while idle and drifts slowly while the app is fetching or downloading. It is
  never placed behind body text smaller than `videoTitle`.
- **Two-tone heading** (`TwoToneHeading`) — two lines in `display`: the first in
  `textPrimary` states what happened, the second in `textTertiary` says what to
  do next ("Link found," / "pick a quality"). It is the screen's only large
  text and it changes with the screen's state.
- **Tray** (`AppTray`) — `surfaceSunken`, `radiusTray`, `trayPadding` inside,
  holding one or more cards with an `sm` gap. Related cards always share a
  tray; a card never sits directly on the ground.
- **Card** (`AppCard`) — `surfaceCard`, `radiusCard`, `cardPadding` inside, no
  border and no elevation.
- **Tag** (`AppTag`) — `tagLime` fill, `radiusTag`, `overline` text in
  `textPrimary`, `xs` vertical and `sm` horizontal padding. Used for the video's
  source.
- **Quality option** (`QualityOption`) — a card row at least `controlHeight`
  tall: a `listPrimary` name, a trailing `listMeta` size. When selected it takes
  a `hairline` `primary` border and a trailing `primary` square holding a
  `primaryOn` check; unselected reserves the same space, so selecting never
  shifts the layout.
- **Buttons** (`AppButtons`) — `PrimaryButton`: `primary` fill, `primaryOn`
  label, `radiusButton`, 52dp, no elevation; disabled uses `surfaceSunken` and
  `textDisabled`; `isLoading` disables it and swaps the label for the activity
  indicator at the same height. `SecondaryButton`: `surfaceCard` fill with a
  `hairline` `primary` border and `textPrimary` label, same metrics.
  `DangerButton`: the same outline in `danger`.
- **Icon button** (`AppIconButton`) — an `iconButton` square, `radiusField`,
  `surfaceCard` fill with an Outlined icon in `textPrimary`. The active or
  primary one inverts: `primary` fill, `primaryOn` icon.
- **Switch** (`AppSwitch`) — a `switchWidth` by `switchHeight` pill: `primary`
  track with a `primaryOn` thumb when on, `surfaceSunken` track with a
  `surfaceCard` thumb when off.
- **Chip** (`AppChip`) — a `chipHeight` choice, `radiusField`: `primary` fill
  with `primaryOn` text when selected, `surfaceBase` fill with `textPrimary`
  text otherwise. Chips sit in one row that scrolls sideways.
- **App bar** (`AppTopBar`) — flat and transparent over the glow, no elevation,
  no surface tint, title in `screenTitle` left-aligned at `screenPadding`,
  actions as icon buttons.
- **Progress ring** (`ProgressRing`) — a `progressRing` circle with a
  `ringStroke` round-capped stroke: the track in `surfaceSunken`, the progress
  as a sweep gradient through the `iridescent` stops, the percentage centred in
  `metric`, and the quality and its estimated size under it in `listMeta`.
- **Glow strip** (`GlowStrip`) — a `controlHeight` bar, `radiusField`, filled
  with a horizontal gradient of the `iridescent` stops, carrying one line of
  status in `listPrimary` and a trailing icon button. It is the only gradient
  the user can touch, and it appears only while a download is running or a
  video is ready.
- **No divider lines.** Cards and trays are separated by space, never by a
  line. `divider` colours the sheet's drag handle and a disabled button's
  border.
- **Vertical rhythm** — one gap per level, the same on every screen and sheet:
  `xxl 40` above the heading; `xl 24` between the heading and the first block
  and at the foot of a sheet; `lg 16` between every stacked block
  (a tray, a strip, the ring, the action row); `md 12` between elements inside
  a card and between paired buttons; `sm 8` between cards in a tray and between
  chips; `xs 4` between a title and the line of detail under it. A sheet's
  title counts as its heading. Never pick a gap by eye: find the level.
- **Actions are pinned to the foot of the screen**, at the developer's request
  after trying them inline. The action row, or the strip that stands in for it,
  sits under the scrolling content with `lg` above it and `lg` below, above the
  navigation bar. Every block spans the same width, `screenPadding` from each
  edge, so trays, strips, text and buttons share one left and one right edge.
  A sheet is short enough not to scroll, so its button is simply its last
  block.
- **Scroll fog** (`ScrollFog`) — content never meets the pinned actions at a
  hard edge. A `fadingEdge`-tall band at the bottom of the scrolling area fades
  from clear to `surfaceBase`, so content dissolves into the ground as it
  passes under the actions. It is a drawn gradient in the ground colour, never
  a line or a shadow, and the scrolling content ends with a `fadingEdge` spacer
  so its last block can always be scrolled clear of the fog.
- **Sheet** (`AppModalSheet`) — `surfaceBase`, top corners `radiusTray`, drag
  handle in `divider`, title in `sectionHeading`, `xl`, content in trays, then
  an `lg` gap and one full-width primary button, and `xl` under it. Sheet actions hide the sheet
  first and act from `invokeOnCompletion` (`AppSheetState.closeThen`), so
  nothing ever appears over a sheet that is still animating away, and a second
  tap during the animation is ignored. A result that arrives while a sheet is
  open is shown inside the sheet, never as a snackbar over it.
- **Loading** (`ActivityIndicator`) — the iOS activity indicator: eight fading
  spokes on a one-second cycle, `progressIndicator` square, in `textSecondary`
  on a light surface and `primaryOn` inside a primary button. A busy button
  shows it in place of its label. Never a Material progress indicator.
- **Snackbar** (`AppSnackbarHost`, `AppSnackbarState`) — a floating card:
  `radiusField`, `screenPadding` from the screen edges and above the navigation
  bar, `surfaceInverse` ground, `danger` for failure, `success` for a completed
  action, `primaryOn` text, max 2 lines, `SnackbarDuration.Long`. Every message
  is a full explanation: what happened, then what to do or what it means. Never
  a bare "Saved". A snackbar is shown only after any bottom sheet has finished
  animating away, never over one.
- **Empty state** — drawn by the screen itself, not a component: an
  `emptyStateIcon` circle in `surfaceCard` holding an `iconEmptyState` Outlined
  icon in `textTertiary`, sitting on the glow, then `xl`, the two-tone heading,
  a `body` explanation in `textSecondary`, and the primary action at the foot
  of the screen.
- **Motion** — the glow's drift and the ring's fill are the only decorative
  motion, both slow and both skipped when the system's animator duration scale
  is zero. Everything else uses the platform's default transitions.
- **Icons** — Material Icons, Outlined, `textPrimary`.
- **Labels** — button labels and setting titles are Title Case (`Paste Link`); headings,
  messages and hints stay in sentence case.

---

# Kotlin / Jetpack Compose member ordering convention

Apply this to **every** Kotlin/Compose file in this project, without exception,
including tests and Gradle Kotlin scripts where the constructs apply. When
applying it to existing code, reorder and re-space only — never change logic,
names, or behaviour.

Ported from the team's Dart convention. Section numbering matches the Dart and
React Native docs, so all three can be diffed side by side.

## 0. What changes vs. Dart

| Dart | Kotlin |
|---|---|
| Fields declared separately from the constructor | Primary-constructor `val`/`var` params **are** the fields |
| `late final x = ...` | `val x by lazy { }` / `lateinit var x` |
| `static` members inline | `companion object` block |
| `State` class with lifecycle overrides | Composable + `remember` / `LaunchedEffect` / `DisposableEffect`, or a state-holder class |

## 1. Member order within a class

Primary-constructor properties come first — they live in the class header, and
the header itself is sorted by §2. In Kotlin the constructor *is* the field
block, which replaces Dart's "constructor at position 8".

Then, in the class body:

1. `companion object` (see §1a)
2. `val` properties
3. `by lazy` properties
4. `lateinit var` properties
5. `var` properties
6. nullable `var` properties
7. `init { }` block
8. secondary constructors
9. computed properties (`val x get() =`) and custom accessors
10. public functions
11. `internal` functions
12. `private` functions
13. `operator` functions
14. **overrides, last** — overridden properties (sorted by §2), then other
    overridden functions by name, then `equals` and `hashCode` adjacent, then
    `toString`, then framework teardown (`onCleared`, `close`) at the very bottom

**Placement rules:**

- Visibility never moves a property between groups 2–6. A `private var` is
  still a property: it stays in group 5/6, not with the private functions.
- An overridden **property** (`override val items`) leaves the property block
  and joins the overrides tail.
- Abstract and `open` members stay in their groups. Abstract functions lead
  their visibility group — they have no body, so no call order.
- Computed properties and custom accessors (group 9) sort by §2.

### 1a. Inside `companion object`

1. `const val`
2. `val`
3. `var`
4. factory functions

Groups 1–3 sorted by §2; factories by call order (§3).

**Placement:** the official Kotlin style guide puts `companion object` last.
This convention puts it first, mirroring the Dart rule that statics lead. See §8.

### 1b. File-level ordering

1. Imports, in the order ktlint's `import-ordering` rule expects (§9)
2. File-private `const val`, sorted by §2
3. Enums, then value classes, then sealed hierarchies — sealed subtypes in name
   order
4. Classes, data classes, and interfaces — each type before the types that
   reference or extend it
5. Public `@Composable` entry point
6. Its `@Composable` children, in call order (§3)
7. `remember*` factory functions
8. State-holder classes
9. Private helpers
10. Extension properties, then extension functions — each sorted by receiver
    type, then name
11. `object` declarations
12. `@Preview` composables, last

## 2. Property ordering inside each group

Sort by **type tier**, then **type name**, then **property name**.

**Tier order:**

1. **Primitives** — `Boolean`, `Byte`, `Char`, `Double`, `Float`, `Int`,
   `Long`, `Short`, `String`, then unsigned variants by name
2. **Collections** — `Array`, `Iterable`, `List`, `Map`, `MutableList`,
   `MutableMap`, `MutableSet`, `Sequence`, `Set`
3. **Everything else** — function types, `Any`, `BigDecimal`, `Duration`,
   `Instant`, `Regex`, `Throwable`, Compose types (`Color`, `Dp`, `Modifier`,
   `TextStyle`), coroutine types (`Flow`, `Job`, `StateFlow`), enums, value
   classes, and all project model classes

**Within a tier:**

- Type name alphabetical, case-insensitive.
- Then property name alphabetical, case-insensitive.
- A nullable type sits immediately after its non-nullable base
  (`String?` right after `String`, `List<String>?` right after `List<String>`).
- Sort on the **full type string**, so type arguments break ties.
  `List<Int>` before `List<String>`.
  **`Flow<Int>` before `FlowCollector`** — `<` (0x3C) sorts before `C` (0x43).
  Counterintuitive; expect to correct it in review.
- Function types start with `(` — place them first in tier 3, before `Any`.
  `() -> Unit` before `(Int) -> Unit` before `(String) -> Boolean`.
  A nullable function type follows its non-nullable shape: `((Int) -> Unit)?`
  sits right after `(Int) -> Unit`.
- SCREAMING_SNAKE constants get no special tier — sort them case-insensitively
  by type first, like everything else. So `IS_STRICT` (Boolean), `TAX_RATE`
  (Double), `MAX_ITEMS` (Int), `PREFIX` (String).
- `Nothing` and `Unit` cannot be property types.
- `lateinit` cannot apply to primitive types, so the only tier-1 type that can
  appear in group 4 is `String`.

## 3. Function ordering

**Properties sort by type. Functions sort by call order.**

Within each visibility group, place functions in four steps:

1. **Construction** — functions called while the object is constructed
   (property initializers, `init` blocks, constructors), in the order first
   called.
2. **Entry points** (public and `internal` groups only) — functions never called
   inside the class, in name order.
3. **Called functions** — functions called from other members, ordered by their
   last call site, reading the class top to bottom.
4. **Unused** (private group only) — functions never called inside the class, in
   name order.

**Refinements:**

- A function called by another function of its own group goes directly after
  that caller — after the last one, if several call it. Several such helpers
  follow their caller in the order it first calls them.
- Passing a function by reference counts as a call (`state::onTextChanged`).

Computed properties sort by §2, like other properties — they have no call order.

Judgment call, not a mechanical rule: two reviewers may order the same class
differently. Accepted — reading order beats sortability for functions.

## 4. Blank lines

- One blank line between each distinct **type** in the property block. `T` and
  `T?` count as distinct types.
- Properties of the *same* type sit together with **no** break between them.
  `val fingerprint: String` and `val slug: String` are adjacent;
  `val checksum: Int` and `val displayName: String` are separated.
- Single-line computed properties follow the same rule.
- Every other member — `init`, constructors, functions, multi-line properties —
  gets one blank line above and below.
- One blank line always separates two groups, even when the types match.
- **No** blank lines inside parameter lists, map literals, or argument lists —
  including the primary constructor, even where the type changes.
- **No** section header comments. Group boundaries are conveyed by the ordering
  itself.

### 4a. Comments

**None.** See the hard constraint above — this project carries no comments in
any source file, of any kind: no `//`, no `/* */`, no KDoc `/** */`, no section
headers, no TODOs, no commented-out code, in Kotlin, Gradle, XML, TOML and
properties alike. That constraint is absolute and it overrides the upstream
convention, which allows a single-line `/* */` comment when genuinely urgent and
exempts KDoc. The section number is kept only so the document still lines up
with the Dart original.

## 5. Compactness

- Single-expression bodies always use `=`, unless that would change the
  inferred return type — `job?.cancel()` would return `Unit?`, and `TODO()`
  would return `Nothing`. Those keep a block body.
- Multi-statement bodies stay expanded.
- Trailing commas on every multi-line parameter and argument list.
- Introduce no line break that ktlint would not require at 240 columns (§9).

## 6. Compose arguments

### At call sites

Named arguments are alphabetical, with two exceptions:

- `modifier` always first (the `key` analogue).
- `content` / trailing lambda always last, hoisted out of the parens.

Example: `modifier`, then `accent`, `draft`, `enabled`, `gutter`,
`onValueChange`, `style`, `subtitle`, `tags`, then `content`.

This covers every named-argument call — composables, framework functions, and
helpers — except the mirrored lists in §7.

### In declarations

Compose's own convention wins over pure alphabetical:

1. required params, in §2 tier order
2. `modifier: Modifier = Modifier`
3. optional params with defaults, in §2 tier order
4. trailing `content: @Composable () -> Unit`, last

## 7. Mirrored lists stay consistent

Primary-constructor params, `copy()` call sites, secondary-constructor
delegation, `toJson` / snapshot keys, `fromJson` arguments, database column
lists, destructuring order, and state-holder constructor calls follow the
**same flat tier + type + name order** as the properties they mirror.

- Sort on the **mirrored property's type**, not the serialised type —
  `createdAt` sorts as `Instant` in a snapshot even though it becomes a string.
- The sort is flat across groups — `val` and `var` properties interleave by type.
- A parameter with no matching property (e.g. `clearMessages`) sorts into its
  type group by name like any other.

## 8. Known trade-offs (accepted)

- `companion object` first contradicts the official Kotlin style guide.
  Accepted for consistency with the Dart codebase.
- Constant order inside `companion object` reads oddly, because it is driven by
  type rather than name.
- Snapshot / JSON key order is driven by Kotlin types, not readability — `id`
  will not be first.
- Alphabetical ordering splits semantically related properties
  (`marginTop` / `paddingTop`).
- Data classes generate `equals` / `hashCode` / `toString`, so the overrides
  tail is usually empty — but constructor order still drives `copy()` and
  destructuring, so order it carefully.
- With no section header comments, the boundary between two adjacent groups is
  invisible when the last member of one and the first of the next have
  different types — it looks like an ordinary type break.
- `hashCode` is a function in Kotlin, so Dart's "getter living with the
  overrides" trade-off disappears.

## 9. Tooling

- No ktlint or detekt rule enforces property ordering — team convention, upheld
  in review.
- Android Studio "Rearrange Code" uses a different scheme. Turn rearranging off
  in the reformat dialog (uncheck *Rearrange entries*).
- No ktlint Gradle plugin is applied, so nothing enforces formatting in the
  build. `.editorconfig` configures the ktlint style the IDE formats to:
  signature wrapping at 240 columns, trailing commas, and import order. It does
  not fight this convention. The settings match `Debitter`. The trailing-comma
  keys are required, because the `intellij_idea` style removes trailing commas
  by default, and Composables must be exempt from the lowercase function-name
  rule:

```editorconfig
[*.{kt,kts}]
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true
indent_size = 4
insert_final_newline = true
ktlint_code_style = intellij_idea
ktlint_function_naming_ignore_when_annotated_with = Composable
max_line_length = 240

[*.{xml,toml,properties}]
indent_size = 4
insert_final_newline = true
```

## 10. Retrofitting

`app/src/main/java/` and `app/src/test/java/` are written to this convention
from the start. A violation there is a regression to fix, not legacy to leave
alone.

---

## Reference example

Covers every tier, group, and tie-break case in one file.

```kotlin
package com.example.ordering

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import kotlin.time.Duration

private const val DEFAULT_RETRIES: Int = 3

private const val TIMEOUT_LABEL: String = "timeout"

enum class SyncState { FAILED, IDLE, RUNNING, SYNCED }

@JvmInline
value class UserId(val raw: String)

sealed interface SyncEvent {
    data class Failed(val cause: Throwable) : SyncEvent

    data object Started : SyncEvent

    data class Succeeded(val at: Instant) : SyncEvent
}

class KitchenSink(
    val isEnabled: Boolean,
    val isVisible: Boolean,
    val flagOverride: Boolean?,
    val separator: Char,
    val ratio: Double,
    val threshold: Double?,
    val scale: Float,
    val retries: Int,
    val timeoutMs: Long,
    val label: String,
    val name: String,
    val nickname: String?,
    val revision: UInt,
    val rawBytes: Array<Byte>,
    val ids: Iterable<Int>,
    val counts: List<Int>,
    val tags: List<String>,
    val optionalTags: List<String>?,
    val members: List<UserId>,
    val scores: Map<String, Int>,
    val overrides: MutableList<String>,
    val cache: MutableMap<String, ByteArray>,
    val seen: MutableSet<UserId>,
    val stream: Sequence<Int>,
    val unique: Set<String>,
    val onIdle: () -> Unit,
    val onCount: (Int) -> Unit,
    val onRetry: ((Int) -> Unit)?,
    val onValidate: (String) -> Boolean,
    val payload: Any,
    val extra: Any?,
    val amount: BigDecimal,
    val accent: Color,
    val gutter: Dp,
    val ttl: Duration,
    val events: Flow<Int>,
    val collector: FlowCollector<Int>,
    val createdAt: Instant,
    val status: SyncState,
    val bodyStyle: TextStyle,
    val owner: UserId,
) {
    companion object {
        const val IS_STRICT: Boolean = true

        const val TAX_RATE: Double = 0.08

        const val MAX_ITEMS: Int = 50

        const val PREFIX: String = "KS"

        val DEFAULT_TAGS: List<String> = listOf("default")

        val EPOCH: Instant = Instant.EPOCH

        var instanceCount: Int = 0

        fun fromSnapshot(snapshot: Map<String, Any?>): KitchenSink = TODO()

        fun minimal(name: String): KitchenSink = TODO()
    }

    val fingerprint: String = "$PREFIX-$name"
    val slug: String = name.lowercase()

    val defaults: List<String> = DEFAULT_TAGS

    val checksum: Int by lazy { fingerprint.hashCode() }

    val displayName: String by lazy { nickname ?: name }

    val summary: Map<String, String> by lazy { mapOf("name" to name, "slug" to slug) }

    lateinit var sessionId: String

    lateinit var scope: CoroutineScope

    var attempts: Int = 0

    var lastLabel: String = TIMEOUT_LABEL

    var syncState: SyncState = SyncState.IDLE

    var errorText: String? = null

    private var pollJob: Job? = null

    var lastError: Throwable? = null

    init {
        instanceCount++
        seedCache()
    }

    constructor(name: String) : this(
        isEnabled = true,
        isVisible = true,
        flagOverride = null,
        separator = ',',
        ratio = 1.0,
        threshold = null,
        scale = 1f,
        retries = DEFAULT_RETRIES,
        timeoutMs = 30_000L,
        label = name,
        name = name,
        nickname = null,
        revision = 0u,
        rawBytes = emptyArray(),
        ids = emptyList(),
        counts = emptyList(),
        tags = DEFAULT_TAGS,
        optionalTags = null,
        members = emptyList(),
        scores = emptyMap(),
        overrides = mutableListOf(),
        cache = mutableMapOf(),
        seen = mutableSetOf(),
        stream = emptySequence(),
        unique = emptySet(),
        onIdle = {},
        onCount = {},
        onRetry = null,
        onValidate = { it.isNotBlank() },
        payload = Unit,
        extra = null,
        amount = BigDecimal.ZERO,
        accent = Color.Unspecified,
        gutter = 0.dp,
        ttl = Duration.ZERO,
        events = emptyFlow(),
        collector = FlowCollector {},
        createdAt = EPOCH,
        status = SyncState.IDLE,
        bodyStyle = TextStyle.Default,
        owner = UserId(""),
    )

    val canRetry: Boolean get() = attempts < retries && lastError != null
    val isDirty: Boolean get() = overrides.isNotEmpty()

    val itemCount: Int get() = tags.size

    var displayLabel: String
        get() = lastLabel
        set(value) {
            lastLabel = value.trim()
        }

    fun start() {
        syncState = SyncState.RUNNING
        attempts = 0
        poll()
    }

    fun poll() {
        pollJob = scope.launch { drain() }
    }

    fun stop() {
        pollJob?.cancel()
        syncState = SyncState.IDLE
    }

    fun toSnapshot(): Map<String, Any?> = mapOf("isEnabled" to isEnabled, "isVisible" to isVisible, "retries" to retries, "label" to label, "name" to name, "tags" to tags, "createdAt" to createdAt.toString(), "status" to status.name)

    internal fun debugDump(): String = toSnapshot().toString()

    private fun seedCache() = cache.clear()

    private suspend fun drain() = events.collect { value -> record(value) }

    private fun record(value: Int) {
        attempts += value
    }

    private fun formatError(cause: Throwable): String = "${cause::class.simpleName}: ${cause.message}"

    operator fun plus(other: KitchenSink): KitchenSink = TODO()

    override fun equals(other: Any?): Boolean = other is KitchenSink && other.name == name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "KitchenSink($name, $syncState)"
}

data class UserProfile(val isVerified: Boolean, val age: Int, val bio: String, val handle: String, val handleOld: String?, val badges: List<String>, val joinedAt: Instant, val id: UserId) {
    fun redacted(): UserProfile = copy(bio = "", handleOld = null)
}

abstract class Repository<T>(protected val cacheKey: String) {
    abstract val items: StateFlow<List<T>>

    abstract suspend fun refresh()

    open fun invalidate() = Unit
}

class UserRepository(cacheKey: String, private val scope: CoroutineScope) :
    Repository<UserProfile>(cacheKey),
    AutoCloseable {
    private val _items: MutableStateFlow<List<UserProfile>> = MutableStateFlow(emptyList())

    private var refreshJob: Job? = null

    fun prefetch() {
        refreshJob = scope.launch { refresh() }
    }

    private fun merge(incoming: List<UserProfile>): List<UserProfile> = (_items.value + incoming).distinctBy { it.id }

    override val items: StateFlow<List<UserProfile>> = _items

    override fun invalidate() {
        _items.value = emptyList()
    }

    override suspend fun refresh() {
        _items.value = merge(emptyList())
    }

    override fun close() {
        refreshJob?.cancel()
    }
}

@Composable
fun SinkPanel(
    isEnabled: Boolean,
    maxLines: Int,
    title: String,
    tags: List<String>,
    accent: Color,
    gutter: Dp,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onDismiss: (() -> Unit)? = null,
    style: TextStyle = TextStyle.Default,
    content: @Composable () -> Unit = {},
) {
    val state = rememberSinkPanelState(maxLines = maxLines, title = title)

    SinkPanelBody(modifier = modifier, accent = accent, draft = state.draft, enabled = isEnabled, gutter = gutter, onValueChange = state::onTextChanged, style = style, subtitle = subtitle, tags = tags, content = content)
}

@Composable
private fun SinkPanelBody(
    enabled: Boolean,
    draft: String,
    subtitle: String?,
    tags: List<String>,
    onValueChange: (String) -> Unit,
    accent: Color,
    gutter: Dp,
    style: TextStyle,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    TODO()
}

@Composable
fun rememberSinkPanelState(maxLines: Int, title: String): SinkPanelState {
    val scope = rememberCoroutineScope()
    return remember(scope) { SinkPanelState(maxLines = maxLines, title = title, scope = scope) }
}

@Stable
class SinkPanelState(private val maxLines: Int, private val title: String, private val scope: CoroutineScope) {
    var isDirty: Boolean by mutableStateOf(false)
        private set

    var charCount: Int by mutableStateOf(0)
        private set

    var draft: String by mutableStateOf(title)
        private set

    var errorText: String? by mutableStateOf(null)
        private set

    private var debounceJob: Job? = null

    val canSave: Boolean get() = isDirty && errorText == null

    val remaining: Int get() = maxLines * 80 - charCount

    fun cancel() {
        debounceJob?.cancel()
    }

    fun onTextChanged(value: String) {
        draft = value
        errorText = validate(value)
        isDirty = value != title
        charCount = value.length
    }

    private fun validate(value: String): String? = if (value.isBlank()) "Cannot be empty" else null
}

val UserProfile.initials: String get() = handle.take(2).uppercase()

fun UserProfile.matches(query: String): Boolean = handle.contains(query, ignoreCase = true) || bio.contains(query, ignoreCase = true)

object SyncDefaults {
    const val RETRY_LIMIT: Int = 5

    val BACKOFF: Duration = Duration.ZERO
}
```
