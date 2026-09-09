package com.dailydivine.app.data.local.db

// No custom column types yet — all entity fields are Room-native primitives
// (String/Int/Long/Boolean/nullable). Kept as an explicit extension point
// since AppDatabase already references @TypeConverters(Converters::class);
// add converters here if a field type needs one later (e.g. List<Int> stored
// as JSON rather than the current comma/JSON-string convention).
class Converters
