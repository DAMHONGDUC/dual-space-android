package com.duplicateapp.theuniverse.media;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.duplicateapp.theuniverse.core.env.BEnvironment;
import com.duplicateapp.theuniverse.utils.BzFileUtils;
import com.duplicateapp.theuniverse.utils.Slog;

public final class TheUniverseMediaStore {
    private static final String TAG = "TheUniverseMediaStore";
    private static final String DATABASE_NAME = "media-index.db";
    private static final int DATABASE_VERSION = 1;

    private static final Object DB_LOCK = new Object();
    private static SQLiteDatabase sDatabase;

    private TheUniverseMediaStore() {
    }

    public static File getImagesRoot(int userId) {
        return new File(BEnvironment.getExternalUserDir(userId), TheUniverseMediaContract.ROOT_IMAGES);
    }

    public static File getVideosRoot(int userId) {
        return new File(BEnvironment.getExternalUserDir(userId), TheUniverseMediaContract.ROOT_VIDEOS);
    }

    public static List<TheUniverseMediaEntry> listMediaEntries(int userId) {
        ensureFresh(userId, MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);
        synchronized (DB_LOCK) {
            return queryEntriesLocked(getDatabaseLocked(),
                    TheUniverseMediaContract.COL_USER_ID + "=?",
                    new String[]{String.valueOf(userId)},
                    TheUniverseMediaContract.COL_DATE_MODIFIED + " DESC, " + TheUniverseMediaContract.COL_ID + " DESC");
        }
    }

    public static TheUniverseMediaEntry getEntry(int userId, long id) {
        ensureFresh(userId, MediaStore.Files.FileColumns.MEDIA_TYPE_NONE);
        synchronized (DB_LOCK) {
            List<TheUniverseMediaEntry> entries = queryEntriesLocked(
                    getDatabaseLocked(),
                    TheUniverseMediaContract.COL_USER_ID + "=? AND " + TheUniverseMediaContract.COL_ID + "=?",
                    new String[]{String.valueOf(userId), String.valueOf(id)},
                    null
            );
            return entries.isEmpty() ? null : entries.get(0);
        }
    }

    public static TheUniverseMediaEntry importMedia(Context context, int userId, Uri sourceUri, String mimeHint) throws IOException {
        if (sourceUri == null) {
            throw new IOException("Missing source URI");
        }
        ensureRoots(userId);
        String resolvedMime = context.getContentResolver().getType(sourceUri);
        if (TextUtils.isEmpty(resolvedMime)) {
            resolvedMime = mimeHint;
        }
        String displayName = queryDisplayName(context, sourceUri);
        if (TextUtils.isEmpty(displayName)) {
            displayName = "shared_" + System.currentTimeMillis();
        }
        if (TextUtils.isEmpty(resolvedMime)) {
            resolvedMime = guessMimeFromName(displayName);
        }
        int mediaType = TheUniverseMediaContract.mediaTypeForMime(resolvedMime);
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
            throw new IOException("Only images and videos are supported");
        }

        File targetDir = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                ? getVideosRoot(userId)
                : getImagesRoot(userId);
        if (!targetDir.exists()) {
            BzFileUtils.mkdirs(targetDir);
        }

        String safeName = buildSafeDisplayName(displayName, resolvedMime, mediaType);
        File target = uniqueFile(targetDir, safeName);
        InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
        if (inputStream == null) {
            throw new IOException("Unable to open source URI");
        }
        BzFileUtils.copyFile(inputStream, target);
        TheUniverseMediaEntry entry = upsertFile(userId, target, mediaType, resolvedMime);
        refreshStoredState(userId, entry.getMediaType());
        return entry;
    }

    public static void rescanUser(int userId) {
        ensureRoots(userId);
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            scanRootLocked(database, userId, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, getImagesRoot(userId));
            scanRootLocked(database, userId, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, getVideosRoot(userId));
        }
    }

    public static Cursor query(int userId, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int mediaType = resolveMediaType(uri);
        ensureFresh(userId, mediaType);
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            String finalSelection = buildSelection(uri, userId, mediaType, selection);
            String[] finalSelectionArgs = buildSelectionArgs(uri, userId, selectionArgs);
            String orderBy = TextUtils.isEmpty(sortOrder)
                    ? TheUniverseMediaContract.COL_DATE_MODIFIED + " DESC, " + TheUniverseMediaContract.COL_ID + " DESC"
                    : sortOrder;
            try {
                return database.query(
                        TheUniverseMediaContract.TABLE_MEDIA,
                        normalizeProjection(projection),
                        finalSelection,
                        finalSelectionArgs,
                        null,
                        null,
                        orderBy
                );
            } catch (Throwable firstError) {
                Slog.w(TAG, "Media query fallback for projection/selection: " + firstError.getMessage());
                try {
                    List<TheUniverseMediaEntry> entries = queryEntriesLocked(database, finalSelection, finalSelectionArgs, null);
                    return buildCompatCursor(projection, entries);
                } catch (Throwable secondError) {
                    Slog.w(TAG, "Media query broad fallback: " + secondError.getMessage());
                    String broadSelection = buildSelection(uri, userId, mediaType, null);
                    List<TheUniverseMediaEntry> entries = queryEntriesLocked(database, broadSelection, null, null);
                    return buildCompatCursor(projection, entries);
                }
            }
        }
    }

    private static String[] normalizeProjection(String[] projection) {
        if (projection == null || projection.length == 0) {
            return null;
        }
        ArrayList<String> supported = new ArrayList<>();
        for (String column : projection) {
            if (isStoredColumn(column)) {
                supported.add(column);
            }
        }
        if (supported.size() == projection.length) {
            return projection;
        }
        return null;
    }

    private static boolean isStoredColumn(String column) {
        if (TextUtils.isEmpty(column)) {
            return false;
        }
        return TheUniverseMediaContract.COL_ID.equals(column)
                || TheUniverseMediaContract.COL_USER_ID.equals(column)
                || TheUniverseMediaContract.COL_MEDIA_TYPE.equals(column)
                || TheUniverseMediaContract.COL_DISPLAY_NAME.equals(column)
                || TheUniverseMediaContract.COL_TITLE.equals(column)
                || TheUniverseMediaContract.COL_MIME_TYPE.equals(column)
                || TheUniverseMediaContract.COL_SIZE.equals(column)
                || TheUniverseMediaContract.COL_DATE_ADDED.equals(column)
                || TheUniverseMediaContract.COL_DATE_MODIFIED.equals(column)
                || TheUniverseMediaContract.COL_DATE_TAKEN.equals(column)
                || TheUniverseMediaContract.COL_RELATIVE_PATH.equals(column)
                || TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME.equals(column)
                || TheUniverseMediaContract.COL_BUCKET_ID.equals(column)
                || TheUniverseMediaContract.COL_WIDTH.equals(column)
                || TheUniverseMediaContract.COL_HEIGHT.equals(column)
                || TheUniverseMediaContract.COL_DURATION.equals(column)
                || TheUniverseMediaContract.COL_IS_PENDING.equals(column)
                || TheUniverseMediaContract.COL_DATA.equals(column)
                || TheUniverseMediaContract.COL_CANONICAL_PATH.equals(column)
                || TheUniverseMediaContract.COL_ROOT_PATH.equals(column);
    }

    private static Cursor buildCompatCursor(String[] projection, List<TheUniverseMediaEntry> entries) {
        String[] columns = projection != null && projection.length > 0 ? projection : defaultProjection();
        MatrixCursor cursor = new MatrixCursor(columns);
        if (entries == null) {
            return cursor;
        }
        for (TheUniverseMediaEntry entry : entries) {
            Object[] row = new Object[columns.length];
            for (int i = 0; i < columns.length; i++) {
                row[i] = valueForColumn(entry, columns[i]);
            }
            cursor.addRow(row);
        }
        return cursor;
    }

    private static String[] defaultProjection() {
        return new String[]{
                TheUniverseMediaContract.COL_ID,
                TheUniverseMediaContract.COL_MEDIA_TYPE,
                TheUniverseMediaContract.COL_DISPLAY_NAME,
                TheUniverseMediaContract.COL_MIME_TYPE,
                TheUniverseMediaContract.COL_SIZE,
                TheUniverseMediaContract.COL_DATE_MODIFIED,
                TheUniverseMediaContract.COL_DATA
        };
    }

    private static Object valueForColumn(TheUniverseMediaEntry entry, String column) {
        if (entry == null || column == null) return null;
        switch (column) {
            case TheUniverseMediaContract.COL_ID:
                return entry.getId();
            case TheUniverseMediaContract.COL_USER_ID:
                return entry.getUserId();
            case TheUniverseMediaContract.COL_MEDIA_TYPE:
                return entry.getMediaType();
            case TheUniverseMediaContract.COL_DISPLAY_NAME:
                return entry.getDisplayName();
            case TheUniverseMediaContract.COL_TITLE:
                return entry.getTitle();
            case TheUniverseMediaContract.COL_MIME_TYPE:
                return entry.getMimeType();
            case TheUniverseMediaContract.COL_SIZE:
                return entry.getSize();
            case TheUniverseMediaContract.COL_DATE_ADDED:
                return entry.getDateAdded();
            case TheUniverseMediaContract.COL_DATE_MODIFIED:
                return entry.getDateModified();
            case TheUniverseMediaContract.COL_DATE_TAKEN:
                return entry.getDateTaken();
            case TheUniverseMediaContract.COL_RELATIVE_PATH:
                return entry.getRelativePath();
            case TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME:
                return entry.getBucketDisplayName();
            case TheUniverseMediaContract.COL_BUCKET_ID:
                return entry.getBucketId();
            case TheUniverseMediaContract.COL_WIDTH:
                return entry.getWidth();
            case TheUniverseMediaContract.COL_HEIGHT:
                return entry.getHeight();
            case TheUniverseMediaContract.COL_DURATION:
                return entry.getDuration();
            case TheUniverseMediaContract.COL_IS_PENDING:
                return entry.getIsPending();
            case TheUniverseMediaContract.COL_DATA:
                return entry.getFilePath();
            case TheUniverseMediaContract.COL_CANONICAL_PATH:
                return entry.getCanonicalPath();
            case TheUniverseMediaContract.COL_ROOT_PATH:
                return entry.getRootPath();
            case "orientation":
            case "is_favorite":
            case "is_trashed":
                return 0;
            case "volume_name":
                return TheUniverseMediaContract.EXTERNAL_VOLUME;
            case "owner_package_name":
                return "";
            default:
                return null;
        }
    }

    public static Uri insert(int userId, Uri uri, ContentValues values) throws IOException {
        int mediaType = resolveMediaType(uri);
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
            mediaType = values.getAsInteger(TheUniverseMediaContract.COL_MEDIA_TYPE) != null
                    ? values.getAsInteger(TheUniverseMediaContract.COL_MEDIA_TYPE)
                    : TheUniverseMediaContract.mediaTypeForMime(values.getAsString(TheUniverseMediaContract.COL_MIME_TYPE));
        }
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
            throw new IOException("Unsupported media type");
        }
        ensureRoots(userId);

        String displayName = values.getAsString(TheUniverseMediaContract.COL_DISPLAY_NAME);
        if (TextUtils.isEmpty(displayName)) {
            displayName = "media_" + System.currentTimeMillis();
        }
        String mimeType = values.getAsString(TheUniverseMediaContract.COL_MIME_TYPE);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = guessMimeFromName(displayName);
        }
        String safeName = buildSafeDisplayName(displayName, mimeType, mediaType);
        String relativePath = normalizeRelativePath(values.getAsString(TheUniverseMediaContract.COL_RELATIVE_PATH), mediaType);
        File targetDir = resolveTargetDir(userId, relativePath, mediaType);
        BzFileUtils.mkdirs(targetDir);
        File targetFile = uniqueFile(targetDir, safeName);
        if (!targetFile.exists() && !targetFile.createNewFile()) {
            throw new IOException("Failed to create media file");
        }

        long nowSeconds = System.currentTimeMillis() / 1000L;
        ContentValues rowValues = new ContentValues();
        rowValues.put(TheUniverseMediaContract.COL_USER_ID, userId);
        rowValues.put(TheUniverseMediaContract.COL_MEDIA_TYPE, mediaType);
        rowValues.put(TheUniverseMediaContract.COL_DISPLAY_NAME, targetFile.getName());
        rowValues.put(TheUniverseMediaContract.COL_TITLE, titleFromDisplayName(targetFile.getName()));
        rowValues.put(TheUniverseMediaContract.COL_MIME_TYPE, mimeType);
        rowValues.put(TheUniverseMediaContract.COL_SIZE, targetFile.length());
        rowValues.put(TheUniverseMediaContract.COL_DATE_ADDED, values.getAsLong(TheUniverseMediaContract.COL_DATE_ADDED) != null
                ? values.getAsLong(TheUniverseMediaContract.COL_DATE_ADDED)
                : nowSeconds);
        rowValues.put(TheUniverseMediaContract.COL_DATE_MODIFIED, values.getAsLong(TheUniverseMediaContract.COL_DATE_MODIFIED) != null
                ? values.getAsLong(TheUniverseMediaContract.COL_DATE_MODIFIED)
                : nowSeconds);
        rowValues.put(TheUniverseMediaContract.COL_DATE_TAKEN, values.getAsLong(TheUniverseMediaContract.COL_DATE_TAKEN) != null
                ? values.getAsLong(TheUniverseMediaContract.COL_DATE_TAKEN)
                : targetFile.lastModified());
        rowValues.put(TheUniverseMediaContract.COL_RELATIVE_PATH, relativePath);
        rowValues.put(TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME, bucketDisplayName(relativePath));
        rowValues.put(TheUniverseMediaContract.COL_BUCKET_ID, bucketId(relativePath));
        rowValues.put(TheUniverseMediaContract.COL_WIDTH, values.getAsInteger(TheUniverseMediaContract.COL_WIDTH) != null
                ? values.getAsInteger(TheUniverseMediaContract.COL_WIDTH)
                : 0);
        rowValues.put(TheUniverseMediaContract.COL_HEIGHT, values.getAsInteger(TheUniverseMediaContract.COL_HEIGHT) != null
                ? values.getAsInteger(TheUniverseMediaContract.COL_HEIGHT)
                : 0);
        rowValues.put(TheUniverseMediaContract.COL_DURATION, values.getAsLong(TheUniverseMediaContract.COL_DURATION) != null
                ? values.getAsLong(TheUniverseMediaContract.COL_DURATION)
                : 0L);
        rowValues.put(TheUniverseMediaContract.COL_IS_PENDING, values.getAsInteger(TheUniverseMediaContract.COL_IS_PENDING) != null
                ? values.getAsInteger(TheUniverseMediaContract.COL_IS_PENDING)
                : 0);
        rowValues.put(TheUniverseMediaContract.COL_DATA, targetFile.getAbsolutePath());
        rowValues.put(TheUniverseMediaContract.COL_CANONICAL_PATH, canonicalPath(targetFile));
        rowValues.put(TheUniverseMediaContract.COL_ROOT_PATH, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                ? getVideosRoot(userId).getAbsolutePath()
                : getImagesRoot(userId).getAbsolutePath());

        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            long rowId = database.insertOrThrow(TheUniverseMediaContract.TABLE_MEDIA, null, rowValues);
            refreshStoredState(userId, mediaType);
            return TheUniverseMediaContract.buildPrivateItemUri(mediaType, rowId, userId);
        }
    }

    public static int update(int userId, Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int mediaType = resolveMediaType(uri);
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            String finalSelection = buildSelection(uri, userId, mediaType, selection);
            String[] finalSelectionArgs = buildSelectionArgs(uri, userId, selectionArgs);
            int updated = database.update(TheUniverseMediaContract.TABLE_MEDIA, values, finalSelection, finalSelectionArgs);
            if (updated > 0) {
                refreshStoredState(userId, mediaType);
            }
            return updated;
        }
    }

    public static int delete(int userId, Uri uri, String selection, String[] selectionArgs) {
        int mediaType = resolveMediaType(uri);
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            String finalSelection = buildSelection(uri, userId, mediaType, selection);
            String[] finalSelectionArgs = buildSelectionArgs(uri, userId, selectionArgs);
            List<TheUniverseMediaEntry> entries = queryEntriesLocked(database, finalSelection, finalSelectionArgs, null);
            for (TheUniverseMediaEntry entry : entries) {
                BzFileUtils.deleteDir(entry.getFilePath());
            }
            int deleted = database.delete(TheUniverseMediaContract.TABLE_MEDIA, finalSelection, finalSelectionArgs);
            if (deleted > 0) {
                refreshStoredState(userId, mediaType);
            }
            return deleted;
        }
    }

    public static ParcelFileDescriptor openFile(int userId, Uri uri, String mode) throws IOException {
        TheUniverseMediaEntry entry = getEntry(userId, TheUniverseMediaContract.getIdFromUri(uri));
        if (entry == null) {
            throw new IOException("Media entry not found");
        }
        File file = new File(entry.getFilePath());
        if (!file.exists()) {
            throw new IOException("Media file is missing");
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode));
    }

    public static String getType(int userId, Uri uri) {
        TheUniverseMediaEntry entry = getEntry(userId, TheUniverseMediaContract.getIdFromUri(uri));
        if (entry == null) {
            int mediaType = resolveMediaType(uri);
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                return "vnd.android.cursor.dir/image";
            }
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                return "vnd.android.cursor.dir/video";
            }
            return "vnd.android.cursor.dir/file";
        }
        return entry.getMimeType();
    }

    private static void ensureFresh(int userId, int mediaType) {
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE || mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
                maybeRescanRootLocked(database, userId, MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, getImagesRoot(userId));
            }
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO || mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
                maybeRescanRootLocked(database, userId, MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, getVideosRoot(userId));
            }
        }
    }

    private static void maybeRescanRootLocked(SQLiteDatabase database, int userId, int mediaType, File root) {
        ensureRootExists(root);
        RootState currentState = RootState.compute(root);
        RootState storedState = loadStoredStateLocked(database, userId, root.getAbsolutePath());
        if (!currentState.sameAs(storedState)) {
            scanRootLocked(database, userId, mediaType, root);
        }
    }

    private static void scanRootLocked(SQLiteDatabase database, int userId, int mediaType, File root) {
        ensureRootExists(root);
        List<File> files = collectMediaFiles(root, mediaType);
        Set<String> seenCanonicalPaths = new HashSet<>();
        database.beginTransaction();
        try {
            for (File file : files) {
                TheUniverseMediaEntry entry = upsertFileLocked(database, userId, file, mediaType, null);
                seenCanonicalPaths.add(entry.getCanonicalPath());
            }
            cleanupMissingRowsLocked(database, userId, root.getAbsolutePath(), seenCanonicalPaths);
            saveRootStateLocked(database, userId, root.getAbsolutePath(), RootState.compute(root));
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }
    }

    private static void cleanupMissingRowsLocked(SQLiteDatabase database, int userId, String rootPath, Set<String> seenCanonicalPaths) {
        try (Cursor cursor = database.query(
                TheUniverseMediaContract.TABLE_MEDIA,
                new String[]{TheUniverseMediaContract.COL_ID, TheUniverseMediaContract.COL_CANONICAL_PATH},
                TheUniverseMediaContract.COL_USER_ID + "=? AND " + TheUniverseMediaContract.COL_ROOT_PATH + "=?",
                new String[]{String.valueOf(userId), rootPath},
                null,
                null,
                null
        )) {
            while (cursor.moveToNext()) {
                long rowId = cursor.getLong(0);
                String canonicalPath = cursor.getString(1);
                if (!seenCanonicalPaths.contains(canonicalPath)) {
                    database.delete(
                            TheUniverseMediaContract.TABLE_MEDIA,
                            TheUniverseMediaContract.COL_ID + "=?",
                            new String[]{String.valueOf(rowId)}
                    );
                }
            }
        }
    }

    private static TheUniverseMediaEntry upsertFile(int userId, File file, int mediaType, String forcedMimeType) {
        synchronized (DB_LOCK) {
            return upsertFileLocked(getDatabaseLocked(), userId, file, mediaType, forcedMimeType);
        }
    }

    private static TheUniverseMediaEntry upsertFileLocked(SQLiteDatabase database, int userId, File file, int mediaType, String forcedMimeType) {
        ensureRootExists(file.getParentFile());
        String canonicalPath = canonicalPath(file);
        String relativePath = buildRelativePath(userId, file, mediaType);
        MediaMetadata metadata = readMetadata(file, mediaType, forcedMimeType);
        long nowSeconds = Math.max(1L, file.lastModified() / 1000L);

        ContentValues values = new ContentValues();
        values.put(TheUniverseMediaContract.COL_USER_ID, userId);
        values.put(TheUniverseMediaContract.COL_MEDIA_TYPE, mediaType);
        values.put(TheUniverseMediaContract.COL_DISPLAY_NAME, file.getName());
        values.put(TheUniverseMediaContract.COL_TITLE, titleFromDisplayName(file.getName()));
        values.put(TheUniverseMediaContract.COL_MIME_TYPE, metadata.mimeType);
        values.put(TheUniverseMediaContract.COL_SIZE, file.length());
        values.put(TheUniverseMediaContract.COL_DATE_ADDED, nowSeconds);
        values.put(TheUniverseMediaContract.COL_DATE_MODIFIED, nowSeconds);
        values.put(TheUniverseMediaContract.COL_DATE_TAKEN, file.lastModified());
        values.put(TheUniverseMediaContract.COL_RELATIVE_PATH, relativePath);
        values.put(TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME, bucketDisplayName(relativePath));
        values.put(TheUniverseMediaContract.COL_BUCKET_ID, bucketId(relativePath));
        values.put(TheUniverseMediaContract.COL_WIDTH, metadata.width);
        values.put(TheUniverseMediaContract.COL_HEIGHT, metadata.height);
        values.put(TheUniverseMediaContract.COL_DURATION, metadata.duration);
        values.put(TheUniverseMediaContract.COL_IS_PENDING, 0);
        values.put(TheUniverseMediaContract.COL_DATA, file.getAbsolutePath());
        values.put(TheUniverseMediaContract.COL_CANONICAL_PATH, canonicalPath);
        values.put(TheUniverseMediaContract.COL_ROOT_PATH, mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                ? getVideosRoot(userId).getAbsolutePath()
                : getImagesRoot(userId).getAbsolutePath());

        Long existingId = findRowIdByCanonicalPathLocked(database, userId, canonicalPath);
        long rowId;
        if (existingId != null) {
            database.update(
                    TheUniverseMediaContract.TABLE_MEDIA,
                    values,
                    TheUniverseMediaContract.COL_ID + "=?",
                    new String[]{String.valueOf(existingId)}
            );
            rowId = existingId;
        } else {
            rowId = database.insertOrThrow(TheUniverseMediaContract.TABLE_MEDIA, null, values);
        }
        return new TheUniverseMediaEntry(
                rowId,
                userId,
                mediaType,
                file.getName(),
                titleFromDisplayName(file.getName()),
                metadata.mimeType,
                file.length(),
                nowSeconds,
                nowSeconds,
                file.lastModified(),
                relativePath,
                bucketDisplayName(relativePath),
                bucketId(relativePath),
                metadata.width,
                metadata.height,
                metadata.duration,
                0,
                file.getAbsolutePath(),
                canonicalPath,
                values.getAsString(TheUniverseMediaContract.COL_ROOT_PATH)
        );
    }

    private static List<TheUniverseMediaEntry> queryEntriesLocked(SQLiteDatabase database, String selection, String[] selectionArgs, String sortOrder) {
        List<TheUniverseMediaEntry> entries = new ArrayList<>();
        try (Cursor cursor = database.query(
                TheUniverseMediaContract.TABLE_MEDIA,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        )) {
            while (cursor.moveToNext()) {
                entries.add(readEntry(cursor));
            }
        }
        return entries;
    }

    private static TheUniverseMediaEntry readEntry(Cursor cursor) {
        return new TheUniverseMediaEntry(
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_USER_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_MEDIA_TYPE)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DISPLAY_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_TITLE)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_MIME_TYPE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_SIZE)),
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DATE_ADDED)),
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DATE_MODIFIED)),
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DATE_TAKEN)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_RELATIVE_PATH)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_BUCKET_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_WIDTH)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_HEIGHT)),
                cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DURATION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_IS_PENDING)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_DATA)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_CANONICAL_PATH)),
                cursor.getString(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.COL_ROOT_PATH))
        );
    }

    private static String buildSelection(Uri uri, int userId, int mediaType, String selection) {
        StringBuilder builder = new StringBuilder();
        builder.append(TheUniverseMediaContract.COL_USER_ID).append("=").append(userId);
        if (mediaType != MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
            builder.append(" AND ").append(TheUniverseMediaContract.COL_MEDIA_TYPE).append("=").append(mediaType);
        }
        long rowId = TheUniverseMediaContract.getIdFromUri(uri);
        if (rowId > 0) {
            builder.append(" AND ").append(TheUniverseMediaContract.COL_ID).append("=").append(rowId);
        }
        if (!TextUtils.isEmpty(selection)) {
            builder.append(" AND (").append(selection).append(")");
        }
        return builder.toString();
    }

    private static String[] buildSelectionArgs(Uri uri, int userId, String[] selectionArgs) {
        return selectionArgs;
    }

    private static int resolveMediaType(Uri uri) {
        if (TheUniverseMediaContract.isImagesUri(uri)) {
            return MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
        }
        if (TheUniverseMediaContract.isVideosUri(uri)) {
            return MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
        }
        return MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
    }

    private static Long findRowIdByCanonicalPathLocked(SQLiteDatabase database, int userId, String canonicalPath) {
        try (Cursor cursor = database.query(
                TheUniverseMediaContract.TABLE_MEDIA,
                new String[]{TheUniverseMediaContract.COL_ID},
                TheUniverseMediaContract.COL_USER_ID + "=? AND " + TheUniverseMediaContract.COL_CANONICAL_PATH + "=?",
                new String[]{String.valueOf(userId), canonicalPath},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }
        return null;
    }

    private static RootState loadStoredStateLocked(SQLiteDatabase database, int userId, String rootPath) {
        try (Cursor cursor = database.query(
                TheUniverseMediaContract.TABLE_SCAN_STATE,
                null,
                TheUniverseMediaContract.STATE_COL_USER_ID + "=? AND " + TheUniverseMediaContract.STATE_COL_ROOT_PATH + "=?",
                new String[]{String.valueOf(userId), rootPath},
                null,
                null,
                null
        )) {
            if (cursor.moveToFirst()) {
                return new RootState(
                        cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.STATE_COL_ROOT_LAST_MODIFIED)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.STATE_COL_FILE_COUNT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(TheUniverseMediaContract.STATE_COL_MAX_MODIFIED))
                );
            }
        }
        return RootState.EMPTY;
    }

    private static void saveRootStateLocked(SQLiteDatabase database, int userId, String rootPath, RootState state) {
        ContentValues values = new ContentValues();
        values.put(TheUniverseMediaContract.STATE_COL_USER_ID, userId);
        values.put(TheUniverseMediaContract.STATE_COL_ROOT_PATH, rootPath);
        values.put(TheUniverseMediaContract.STATE_COL_ROOT_LAST_MODIFIED, state.rootLastModified);
        values.put(TheUniverseMediaContract.STATE_COL_FILE_COUNT, state.fileCount);
        values.put(TheUniverseMediaContract.STATE_COL_MAX_MODIFIED, state.maxModified);
        int updated = database.update(
                TheUniverseMediaContract.TABLE_SCAN_STATE,
                values,
                TheUniverseMediaContract.STATE_COL_USER_ID + "=? AND " + TheUniverseMediaContract.STATE_COL_ROOT_PATH + "=?",
                new String[]{String.valueOf(userId), rootPath}
        );
        if (updated == 0) {
            database.insert(TheUniverseMediaContract.TABLE_SCAN_STATE, null, values);
        }
    }

    private static void refreshStoredState(int userId, int mediaType) {
        synchronized (DB_LOCK) {
            SQLiteDatabase database = getDatabaseLocked();
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE || mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
                saveRootStateLocked(database, userId, getImagesRoot(userId).getAbsolutePath(), RootState.compute(getImagesRoot(userId)));
            }
            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO || mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_NONE) {
                saveRootStateLocked(database, userId, getVideosRoot(userId).getAbsolutePath(), RootState.compute(getVideosRoot(userId)));
            }
        }
    }

    private static SQLiteDatabase getDatabaseLocked() {
        if (sDatabase != null && sDatabase.isOpen()) {
            return sDatabase;
        }
        BEnvironment.load();
        File databaseFile = new File(BEnvironment.getSystemDir(), DATABASE_NAME);
        sDatabase = SQLiteDatabase.openOrCreateDatabase(databaseFile, null);
        ensureSchemaLocked(sDatabase);
        return sDatabase;
    }

    private static void ensureSchemaLocked(SQLiteDatabase database) {
        if (database.getVersion() == DATABASE_VERSION) {
            return;
        }
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TheUniverseMediaContract.TABLE_MEDIA + " ("
                        + TheUniverseMediaContract.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + TheUniverseMediaContract.COL_USER_ID + " INTEGER NOT NULL, "
                        + TheUniverseMediaContract.COL_MEDIA_TYPE + " INTEGER NOT NULL, "
                        + TheUniverseMediaContract.COL_DISPLAY_NAME + " TEXT NOT NULL, "
                        + TheUniverseMediaContract.COL_TITLE + " TEXT, "
                        + TheUniverseMediaContract.COL_MIME_TYPE + " TEXT, "
                        + TheUniverseMediaContract.COL_SIZE + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_DATE_ADDED + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_DATE_MODIFIED + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_DATE_TAKEN + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_RELATIVE_PATH + " TEXT NOT NULL, "
                        + TheUniverseMediaContract.COL_BUCKET_DISPLAY_NAME + " TEXT, "
                        + TheUniverseMediaContract.COL_BUCKET_ID + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_WIDTH + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_HEIGHT + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_DURATION + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_IS_PENDING + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.COL_DATA + " TEXT NOT NULL, "
                        + TheUniverseMediaContract.COL_CANONICAL_PATH + " TEXT NOT NULL, "
                        + TheUniverseMediaContract.COL_ROOT_PATH + " TEXT NOT NULL"
                        + ")"
        );
        database.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_media_user_path ON "
                        + TheUniverseMediaContract.TABLE_MEDIA + "("
                        + TheUniverseMediaContract.COL_USER_ID + ", "
                        + TheUniverseMediaContract.COL_CANONICAL_PATH + ")"
        );
        database.execSQL(
                "CREATE INDEX IF NOT EXISTS idx_media_user_type ON "
                        + TheUniverseMediaContract.TABLE_MEDIA + "("
                        + TheUniverseMediaContract.COL_USER_ID + ", "
                        + TheUniverseMediaContract.COL_MEDIA_TYPE + ")"
        );
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS " + TheUniverseMediaContract.TABLE_SCAN_STATE + " ("
                        + TheUniverseMediaContract.STATE_COL_USER_ID + " INTEGER NOT NULL, "
                        + TheUniverseMediaContract.STATE_COL_ROOT_PATH + " TEXT NOT NULL, "
                        + TheUniverseMediaContract.STATE_COL_ROOT_LAST_MODIFIED + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.STATE_COL_FILE_COUNT + " INTEGER NOT NULL DEFAULT 0, "
                        + TheUniverseMediaContract.STATE_COL_MAX_MODIFIED + " INTEGER NOT NULL DEFAULT 0, "
                        + "PRIMARY KEY (" + TheUniverseMediaContract.STATE_COL_USER_ID + ", " + TheUniverseMediaContract.STATE_COL_ROOT_PATH + ")"
                        + ")"
        );
        database.setVersion(DATABASE_VERSION);
    }

    private static void ensureRoots(int userId) {
        ensureRootExists(getImagesRoot(userId));
        ensureRootExists(getVideosRoot(userId));
    }

    private static void ensureRootExists(File root) {
        if (root != null && !root.exists()) {
            BzFileUtils.mkdirs(root);
        }
    }

    private static String canonicalPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    private static List<File> collectMediaFiles(File root, int mediaType) {
        List<File> files = new ArrayList<>();
        if (root == null || !root.exists()) {
            return files;
        }
        ArrayDeque<File> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            File current = stack.pop();
            File[] children = current.listFiles();
            if (children == null) {
                continue;
            }
            for (File child : children) {
                if (child.isDirectory()) {
                    stack.push(child);
                } else if (child.isFile()) {
                    String mimeType = TheUniverseMediaContract.inferMimeType(child);
                    if (TheUniverseMediaContract.mediaTypeForMime(mimeType) == mediaType) {
                        files.add(child);
                    }
                }
            }
        }
        return files;
    }

    private static String queryDisplayName(Context context, Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri,
                new String[]{MediaStore.MediaColumns.DISPLAY_NAME},
                null,
                null,
                null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Slog.w(TAG, "Failed to query display name: " + e.getMessage());
        }
        String lastSegment = uri.getLastPathSegment();
        if (TextUtils.isEmpty(lastSegment)) {
            return null;
        }
        int slash = lastSegment.lastIndexOf('/');
        return slash >= 0 ? lastSegment.substring(slash + 1) : lastSegment;
    }

    private static String guessMimeFromName(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return null;
        }
        String extension = MimeTypeMap.getFileExtensionFromUrl(displayName);
        if (TextUtils.isEmpty(extension)) {
            int dotIndex = displayName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < displayName.length() - 1) {
                extension = displayName.substring(dotIndex + 1);
            }
        }
        if (TextUtils.isEmpty(extension)) {
            return null;
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase(Locale.ROOT));
    }

    private static String buildSafeDisplayName(String displayName, String mimeType, int mediaType) {
        String cleaned = TextUtils.isEmpty(displayName) ? "media_" + System.currentTimeMillis() : displayName.trim();
        cleaned = cleaned.replaceAll("[\\\\/:*?\"<>|]+", "_");
        if (TextUtils.isEmpty(cleaned)) {
            cleaned = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                    ? "video_" + System.currentTimeMillis()
                    : "image_" + System.currentTimeMillis();
        }
        if (!cleaned.contains(".") && !TextUtils.isEmpty(mimeType)) {
            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (!TextUtils.isEmpty(extension)) {
                cleaned = cleaned + "." + extension;
            }
        }
        return cleaned;
    }

    private static File uniqueFile(File directory, String displayName) {
        File candidate = new File(directory, displayName);
        if (!candidate.exists()) {
            return candidate;
        }
        String baseName = displayName;
        String extension = "";
        int dotIndex = displayName.lastIndexOf('.');
        if (dotIndex > 0) {
            baseName = displayName.substring(0, dotIndex);
            extension = displayName.substring(dotIndex);
        }
        int suffix = 1;
        while (candidate.exists()) {
            candidate = new File(directory, baseName + "_" + suffix + extension);
            suffix++;
        }
        return candidate;
    }

    private static String titleFromDisplayName(String displayName) {
        if (TextUtils.isEmpty(displayName)) {
            return "";
        }
        int dotIndex = displayName.lastIndexOf('.');
        return dotIndex > 0 ? displayName.substring(0, dotIndex) : displayName;
    }

    private static String normalizeRelativePath(String relativePath, int mediaType) {
        String fallback = TheUniverseMediaContract.defaultRelativePathForMediaType(mediaType);
        if (TextUtils.isEmpty(relativePath)) {
            return fallback;
        }
        String normalized = relativePath.replace('\\', '/');
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        if (!normalized.startsWith(TheUniverseMediaContract.ROOT_IMAGES)
                && !normalized.startsWith(TheUniverseMediaContract.ROOT_VIDEOS)) {
            return fallback;
        }
        return normalized;
    }

    private static File resolveTargetDir(int userId, String relativePath, int mediaType) {
        String normalized = normalizeRelativePath(relativePath, mediaType);
        return new File(BEnvironment.getExternalUserDir(userId), normalized);
    }

    private static String buildRelativePath(int userId, File file, int mediaType) {
        File root = mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ? getVideosRoot(userId) : getImagesRoot(userId);
        String rootPath = root.getAbsolutePath();
        String filePath = file.getParentFile() != null ? file.getParentFile().getAbsolutePath() : rootPath;
        String suffix = filePath.startsWith(rootPath) ? filePath.substring(rootPath.length()) : "";
        if (suffix.startsWith(File.separator)) {
            suffix = suffix.substring(1);
        }
        String base = TheUniverseMediaContract.defaultRelativePathForMediaType(mediaType);
        if (TextUtils.isEmpty(suffix)) {
            return base;
        }
        return base + suffix.replace(File.separatorChar, '/') + "/";
    }

    private static String bucketDisplayName(String relativePath) {
        if (TextUtils.isEmpty(relativePath)) {
            return "TheUniverseGallery";
        }
        String normalized = relativePath;
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        int slash = normalized.lastIndexOf('/');
        return slash >= 0 ? normalized.substring(slash + 1) : normalized;
    }

    private static int bucketId(String relativePath) {
        return bucketDisplayName(relativePath).toLowerCase(Locale.ROOT).hashCode();
    }

    private static MediaMetadata readMetadata(File file, int mediaType, String forcedMimeType) {
        MediaMetadata metadata = new MediaMetadata();
        metadata.mimeType = !TextUtils.isEmpty(forcedMimeType) ? forcedMimeType : TheUniverseMediaContract.inferMimeType(file);
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            metadata.width = Math.max(options.outWidth, 0);
            metadata.height = Math.max(options.outHeight, 0);
            return metadata;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file.getAbsolutePath());
            metadata.width = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            metadata.height = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            metadata.duration = parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            Slog.w(TAG, "Failed to read video metadata for " + file.getName() + ": " + e.getMessage());
        } finally {
            try {
                retriever.release();
            } catch (Exception ignored) {
            }
        }
        return metadata;
    }

    private static int parseInt(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private static long parseLong(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private static final class MediaMetadata {
        private String mimeType;
        private int width;
        private int height;
        private long duration;
    }

    private static final class RootState {
        private static final RootState EMPTY = new RootState(0L, 0, 0L);

        private final long rootLastModified;
        private final int fileCount;
        private final long maxModified;

        private RootState(long rootLastModified, int fileCount, long maxModified) {
            this.rootLastModified = rootLastModified;
            this.fileCount = fileCount;
            this.maxModified = maxModified;
        }

        private boolean sameAs(RootState other) {
            return other != null
                    && rootLastModified == other.rootLastModified
                    && fileCount == other.fileCount
                    && maxModified == other.maxModified;
        }

        private static RootState compute(File root) {
            if (root == null || !root.exists()) {
                return EMPTY;
            }
            long maxModified = root.lastModified();
            int fileCount = 0;
            ArrayDeque<File> stack = new ArrayDeque<>();
            stack.push(root);
            while (!stack.isEmpty()) {
                File current = stack.pop();
                File[] children = current.listFiles();
                if (children == null) {
                    continue;
                }
                for (File child : children) {
                    if (child.isDirectory()) {
                        stack.push(child);
                    } else if (child.isFile()) {
                        String mimeType = TheUniverseMediaContract.inferMimeType(child);
                        if (TheUniverseMediaContract.isSupportedMime(mimeType)) {
                            fileCount++;
                            maxModified = Math.max(maxModified, child.lastModified());
                        }
                    }
                }
            }
            return new RootState(root.lastModified(), fileCount, maxModified);
        }
    }
}
