# Privacy Policy for hued

**Last updated:** April 5, 2026

## Overview

hued is designed with privacy as a core principle. All image processing happens entirely on your device. Your photos never leave your phone.

## Data Collection

hued does not collect, transmit, or store any personal data on external servers.

Specifically, hued does **not**:
- Upload your photos or any image data to any server
- Use analytics, tracking, or telemetry services
- Require an account or authentication
- Access the internet (no network permissions requested)
- Share any data with third parties
- Use advertising SDKs

## Data Stored on Device

hued stores the following data locally on your device:

- **Color data**: Dominant colors extracted from your photos (hex color codes only, not image data)
- **Palette metadata**: Aggregated color palettes per time period, photo counts, poetic descriptions
- **Processing state**: Checkpoint data to resume scanning after interruption
- **Settings**: Your preferences for palette depth, weighted bands, and folder exclusions
- **Folder paths**: Which folders to include or exclude from scanning

All data is stored in an app-private Room database that other apps cannot access.

## Photo Access

hued requests permission to read your photo gallery in order to extract color information. hued reads image pixels only to determine dominant colors. No image data is stored, cached, or transmitted. Only the extracted hex color codes are saved.

On Android 14+, hued supports partial photo access. You can choose which photos hued can see.

## Share Cards

When you share a palette card, hued generates a PNG image containing only color data and text (no photos). This image is shared via Android's standard share intent. The image is temporarily cached on device and cleaned up on next app launch.

## Data Deletion

Uninstalling hued removes all stored data. You can also reprocess your gallery from Settings, which clears all existing data and starts fresh.

## Children's Privacy

hued does not knowingly collect data from children under 13. The app contains no user-generated content, social features, or data collection.

## Changes to This Policy

Updates to this privacy policy will be reflected in the app and on this page with a new "Last updated" date.

## Contact

If you have questions about this privacy policy, contact: saxena.akhil42@gmail.com
