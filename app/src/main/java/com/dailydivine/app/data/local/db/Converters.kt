package com.dailydivine.app.data.local.db

// Intentionally has no @TypeConverter methods yet, and is therefore NOT
// currently referenced by @TypeConverters on AppDatabase -- Room's KSP
// processor errors ("referenced as a converter but has no converter
// methods") if an empty class like this is registered. All entity fields
// are Room-native primitives right now. When a field needs a converter
// (e.g. a List<Int> stored as JSON), add the @TypeConverter method here
// AND add @TypeConverters(Converters::class) back onto AppDatabase.
class Converters
