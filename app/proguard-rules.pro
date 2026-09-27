# Feature-specific keep rules belong beside the feature that needs them.

# Ads: play-services-ads pulls WorkManager 2.7 / Room 2.2, which instantiates WorkDatabase_Impl
# reflectively. R8 full mode drops the unused no-arg constructor, crashing the release build at startup.
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
