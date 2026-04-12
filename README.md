# BodyCheck

A lightweight Android app for scanning and visualizing body composition data from gym QR codes.

## Features

- **QR Code Scanning** -- Scan body composition QR codes via camera or import from gallery images
- **Visualization** -- Circular gauges, BMI bar chart, segmental lean/fat mass body figure
- **History** -- Track your body composition over time with stored scan history
- **Export/Import** -- Transfer your data between devices via JSON backup
- **Offline** -- No internet connection required, all data stays on your device
- **Small** -- ~7 MB APK (arm64-v8a) or ~5.5 MB (armeabi-v7a); per-ABI APK splits are enabled

## Supported Measurements

The app displays the following metrics from each scan:

| Metric | Unit |
|--------|------|
| Weight | kg |
| Body Mass Index (BMI) | - |
| Body Fat | % |
| Skeletal Muscle Mass | kg |
| Total Body Water | L |
| Protein Mass | kg |
| Basal Metabolic Rate | kcal/day |
| Biological Age | years |
| Health Score | 0-100 |
| Segmental Lean Mass | kg (per limb + trunk) |
| Segmental Fat Mass | kg (per limb + trunk) |
| Waist-Hip Ratio | - |
| Bone Mineral Content | kg |
| And more... | 64 fields total |

## QR Code Format

The app reads QR codes containing a JSON array of floating-point numbers:

```
[<weight>,<free_fat_mass>,<fat_mass>,<body_fat_pct>,...]
```

Valid array lengths are 63, 64, or 95 elements. The 95-element format includes extended assessment data. Basic sanity checks validate that key values (weight, height, BMI, body fat %, health score) fall within physiologically plausible ranges.

## Building

Requirements:
- JDK 21+
- Android SDK (platform 35, build-tools 35.0.0)

```bash
# Using the Nix flake (recommended):
nix develop
gradle assembleRelease

# Or with a local Android SDK:
export ANDROID_HOME=/path/to/sdk
gradle assembleRelease
```

The release APK will be at `app/build/outputs/apk/release/app-release-unsigned.apk`.

## Tech Stack

- **Kotlin** with XML layouts (no Jetpack Compose)
- **ML Kit** for barcode scanning
- **CameraX** for camera integration
- **SQLite** for local scan history
- **Custom Canvas views** for all visualizations
- **AGP 8.7.3**, **Kotlin 2.1.10**, **Gradle 8.12.1**
- Target SDK 35 (Android 15), min SDK 24 (Android 7.0)

## Privacy

No internet permission. No analytics or tracking. All scan data stays on your device. Export/import via local JSON files.

## License

This project is licensed under the GNU General Public License v3.0 -- see the [LICENSE](LICENSE) file for details.
