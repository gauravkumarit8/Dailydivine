# DailyDivine release (R8) rules. R8 runs on every release build.

# Room entities are read by generated code and by name.
-keep class com.dailydivine.app.data.local.entity.** { *; }

# REQUIRED: Gson fills ContentFile / CategoryJson / VerseJson by REFLECTION using the JSON field
# names. Without this R8 renames or strips those fields and every verse file would silently
# parse as empty, so the app would ship with no content.
-keep class com.dailydivine.app.data.content.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Readable stack traces in Play Console (the mapping file is embedded in the AAB).
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Hilt, Room, WorkManager, Compose and DataStore ship their own consumer rules.
