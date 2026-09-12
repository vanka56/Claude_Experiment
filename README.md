# Swing Cricket 🏏

A native Android cricket game where **bowling and batting are driven by physically swinging
your phone** — gyroscope + accelerometer fusion turns a bowling action into a delivery (speed,
line, length, swing) and a bat swing into a shot (power, direction, timing). Two players connect
their phones directly (Bluetooth / Wi-Fi, no internet, no server) and play a full match: one bats,
one bowls, then they swap for the second innings.

## Tech stack

- **Kotlin + Jetpack Compose (Material 3)** — chosen over Flutter/React Native for the lowest-latency
  path from raw sensor data to gameplay, and for full control over the spring/motion system used
  everywhere for the "fluid" feel.
- **Google Play Services Nearby Connections** (`P2P_POINT_TO_POINT` strategy) for phone-to-phone
  pairing — automatically negotiates Bluetooth / Wi-Fi Direct / local Wi-Fi, no pairing codes,
  no internet connection required.
- **kotlinx.serialization** for the small JSON wire protocol between the two phones.
- **Coroutines/StateFlow** throughout for reactive state.

## Project layout

```
app/src/main/java/com/swingcricket/game/
  sensors/      SwingDetector — turns raw accelerometer/gyroscope/rotation-vector samples into a
                single SwingEvent (peak force, yaw/pitch angle, wrist rotation, precise impact time).
  game/         Pure game logic: SwingInterpreter (sensor -> delivery/shot), OutcomeCalculator
                (delivery + shot -> runs/wicket, deterministic/skill-based, no RNG), MatchEngine
                (innings/overs/score state machine), Models.
  connection/   NearbyConnectionManager — thin wrapper around Nearby Connections.
  protocol/     GameMessage (wire protocol) + MessageCodec (JSON encode/decode).
  viewmodel/    GameViewModel — orchestrates sensors + connection + match engine + navigation.
  ui/           Compose screens, reusable components (coin flip, scoreboard, swing meter, ball
                approach visualization, confetti, etc.), and the night-stadium theme.
```

### How a ball works, end to end

1. **Bowler's phone**: player swings the phone like a bowling action. `SwingDetector` detects the
   gesture (backswing → forward swing → impact) and reports peak acceleration, yaw/pitch angle
   change, and wrist rotation rate at the moment of peak effort. `SwingInterpreter` maps that to a
   `DeliveryData` (speed in km/h, line, length, swing type). It's sent to the batter's phone over
   Nearby Connections.
2. **Batter's phone**: on receiving the delivery, the UI animates a ball approaching over the
   delivery's calculated flight time. The batter swings the phone like a bat; the swing's impact
   instant is compared (all on the *batter's own device clock*, so no cross-device clock sync is
   needed) against the ideal arrival instant to compute timing accuracy, plus power and shot
   direction. `OutcomeCalculator` deterministically computes the ball's outcome (skill-based, not
   random) and it's sent back to the bowler.
3. Both phones run an identical `MatchEngine` and apply the same sequence of ball outcomes, so
   score, overs, innings transitions, and the match result stay in lockstep without extra network
   messages.

## Building it

This was written entirely by hand in a sandbox **without the Android SDK installed**, so it has
not been compiled or run here — there was no `ANDROID_HOME`, and network access to
`dl.google.com` was blocked, so I couldn't fetch SDK platforms to self-check the build. It's been
carefully reviewed for Kotlin/Compose/API correctness, but treat the first build as the real
smoke test:

1. Open the project root in **Android Studio (Ladybird/2024.2 or newer)** — it uses Kotlin 2.0.21,
   AGP 8.6.0, and the version catalog at `gradle/libs.versions.toml`. Let Gradle sync; Android
   Studio will offer to download any missing SDK platforms/build-tools.
2. Build & run (`Shift+F10`) on two physical Android phones (**API 26+**) — an emulator can't swing
   a bat. Physical devices are required for sensors and Nearby Connections to work.
3. On phone A tap **Host Game**, on phone B tap **Join Game**. Grant the Bluetooth/Wi-Fi/nearby
   permission prompts on both. Once connected, a coin flip decides who picks bat or bowl first.

If Gradle sync reports a dependency version that's since been superseded (Play Services Nearby,
Compose BOM, etc. move fast), bump the relevant version in `gradle/libs.versions.toml` — nothing
in the code is pinned to a specific patch behavior.

## Tuning the feel

All the "physics" is intentionally isolated and easy to retune:

- `sensors/SwingDetector.kt` — gesture thresholds (`BACKSWING_THRESHOLD`, `IMPACT_MIN_THRESHOLD`,
  timing windows).
- `game/SwingInterpreter.kt` — how raw sensor values map to km/h, line/length buckets, flight time.
- `game/OutcomeCalculator.kt` — how timing accuracy + power + delivery type combine into runs or a
  dismissal. It's a pure function, easy to unit test or rebalance without touching sensors or UI.
