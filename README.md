# Gaming Vault – Android Tablet

Native Kotlin/Jetpack Compose Android-App, ausschließlich Querformat. Visuelles Referenzdesign: `design-reference.png`.

## Funktionen
- Spiele und Accounts anlegen, bearbeiten und löschen
- Benutzername, Passwort, E-Mail, E-Mail-Passwort, Gamigo-Zugang und Notizen
- Jedes Passwort standardmäßig maskiert, separat per Auge sichtbar
- Offline-Speicherung mit AES-256-GCM und Android Keystore
- Screenshot-Schutz (FLAG_SECURE), Android-Backup deaktiviert
- Dunkles Gold-/Fantasy-Dashboard für Tablets

## APK bauen
1. Projektordner `GamingVault` in Android Studio öffnen.
2. Gradle-Synchronisierung abwarten (Internet für Android-Abhängigkeiten erforderlich).
3. `Build > Build App Bundle(s) / APK(s) > Build APK(s)` wählen.
4. APK liegt unter `app/build/outputs/apk/debug/app-debug.apk`.

Für eine veröffentlichte APK: `Build > Generate Signed App Bundle / APK` und einen eigenen Signaturschlüssel verwenden.

## Hinweise
Die Grafiken wurden aus der Designreferenz zugeschnitten; dynamische Bedienelemente sind nativ implementiert. Das generierte Bild lässt sich nicht vollständig pixelgenau in einzelne UI-Assets zerlegen. Ein tatsächlicher APK-Build konnte hier mangels Android-SDK/Gradle nicht durchgeführt werden. Lokale Android-Keystore-Schlüssel sind gerätegebunden; beim Löschen der App können die Daten unwiederbringlich verloren gehen. Es gibt derzeit keine Export-/Import-Funktion und keine PIN-/Biometrie-Sperre. Verwende die App vor einem eigenen Funktionstest nicht als einzigen Speicherort wichtiger Passwörter.
