/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brentondurkee.ccm.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.brentondurkee.ccm.common.SelectionBuilder;

public class DataProvider extends ContentProvider {
    DataDatabase mDatabaseHelper;
    /**
     * Content authority for this provider.
     */
    private static final String AUTHORITY = DataContract.CONTENT_AUTHORITY;
    private static final String TAG = "DataProvider";

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /events
     */
    public static final int ROUTE_EVENTS = 1;

    /**
     * URI ID for route: /entries/{ID}
     */
    public static final int ROUTE_EVENTS_ID = 2;

    public static final int ROUTE_TALKS = 3;

    public static final int ROUTE_TALKS_ID = 4;

    public static final int ROUTE_CONVO = 5;

    public static final int ROUTE_CONVO_ID = 6;

    public static final int ROUTE_LOCATIONS = 7;

    public static final int ROUTE_LOCATIONS_ID = 8;

    public static final int ROUTE_GROUPS = 9;

    public static final int ROUTE_GROUPS_ID = 10;

    public static final int ROUTE_SIGNUPS = 11;
    public static final int ROUTE_SIGNUPS_ID = 12;

    public static final int ROUTE_TOPICS = 13;
    public static final int ROUTE_TOPICS_ID = 14;

    public static final int ROUTE_BC = 15;

    public static final int ROUTE_BC_ID = 16;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "events", ROUTE_EVENTS);
        sUriMatcher.addURI(AUTHORITY, "events/*", ROUTE_EVENTS_ID);
        sUriMatcher.addURI(AUTHORITY, "talks", ROUTE_TALKS);
        sUriMatcher.addURI(AUTHORITY, "talks/*", ROUTE_TALKS_ID);
        sUriMatcher.addURI(AUTHORITY, "conversations", ROUTE_CONVO);
        sUriMatcher.addURI(AUTHORITY, "conversations/*", ROUTE_CONVO_ID);
        sUriMatcher.addURI(AUTHORITY, "groups", ROUTE_GROUPS);
        sUriMatcher.addURI(AUTHORITY, "groups/*", ROUTE_GROUPS_ID);
        sUriMatcher.addURI(AUTHORITY, "locations", ROUTE_LOCATIONS);
        sUriMatcher.addURI(AUTHORITY, "locations/*", ROUTE_LOCATIONS_ID);
        sUriMatcher.addURI(AUTHORITY, "signups", ROUTE_SIGNUPS);
        sUriMatcher.addURI(AUTHORITY, "signups/*", ROUTE_SIGNUPS_ID);
        sUriMatcher.addURI(AUTHORITY, "topics", ROUTE_TOPICS);
        sUriMatcher.addURI(AUTHORITY, "topics/*", ROUTE_TOPICS_ID);
        sUriMatcher.addURI(AUTHORITY, "broadcasts", ROUTE_BC);
        sUriMatcher.addURI(AUTHORITY, "broadcasts/*", ROUTE_BC_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DataDatabase(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_EVENTS:
                return DataContract.Event.CONTENT_TYPE;
            case ROUTE_EVENTS_ID:
                return DataContract.Event.CONTENT_ITEM_TYPE;
            case ROUTE_TALKS:
                return DataContract.Talk.CONTENT_TYPE;
            case ROUTE_TALKS_ID:
                return DataContract.Talk.CONTENT_ITEM_TYPE;
            case ROUTE_CONVO:
                return DataContract.Convo.CONTENT_TYPE;
            case ROUTE_CONVO_ID:
                return DataContract.Convo.CONTENT_ITEM_TYPE;
            case ROUTE_GROUPS:
                return DataContract.Group.CONTENT_TYPE;
            case ROUTE_GROUPS_ID:
                return DataContract.Group.CONTENT_ITEM_TYPE;
            case ROUTE_LOCATIONS:
                return DataContract.Location.CONTENT_TYPE;
            case ROUTE_LOCATIONS_ID:
                return DataContract.Location.CONTENT_ITEM_TYPE;
            case ROUTE_SIGNUPS:
                return DataContract.Signup.CONTENT_TYPE;
            case ROUTE_SIGNUPS_ID:
                return DataContract.Signup.CONTENT_ITEM_TYPE;
            case ROUTE_TOPICS:
                return DataContract.Topic.CONTENT_TYPE;
            case ROUTE_TOPICS_ID:
                return DataContract.Topic.CONTENT_ITEM_TYPE;
            case ROUTE_BC:
                return DataContract.Broadcast.CONTENT_TYPE;
            case ROUTE_BC_ID:
                return DataContract.Broadcast.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Perform a database query by URI.
     *
     * <p>Currently supports returning all events (/events) and individual entries by ID
     * (/events/{ID}).
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        int uriMatch = sUriMatcher.match(uri);
        String id;
        Cursor c;
        Context ctx;
        switch (uriMatch) {
            case ROUTE_EVENTS_ID:
                // Return a single entry, by ID.
                id = uri.getLastPathSegment();
                builder.where(DataContract.Event._ID + "=?", id);
            case ROUTE_EVENTS:
                // Return all known entries.
                builder.table(DataContract.Event.TABLE_NAME)
                       .where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_TALKS_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Talk._ID + "=?", id);
            case ROUTE_TALKS:
                builder.table(DataContract.Talk.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_CONVO_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Convo._ID + "=?", id);
            case ROUTE_CONVO:
                builder.table(DataContract.Convo.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_GROUPS_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Group._ID + "=?", id);
            case ROUTE_GROUPS:
                builder.table(DataContract.Group.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_LOCATIONS_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Location._ID + "=?", id);
            case ROUTE_LOCATIONS:
                builder.table(DataContract.Location.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_SIGNUPS_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Signup._ID + "=?", id);
            case ROUTE_SIGNUPS:
                builder.table(DataContract.Signup.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_TOPICS_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Topic._ID + "=?", id);
            case ROUTE_TOPICS:
                builder.table(DataContract.Topic.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            case ROUTE_BC_ID:
                id = uri.getLastPathSegment();
                builder.where(DataContract.Broadcast._ID + "=?", id);
            case ROUTE_BC:
                builder.table(DataContract.Broadcast.TABLE_NAME).where(selection, selectionArgs);
                c = builder.query(db, projection, sortOrder);
                ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Insert a new entry into the database.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        long id;
        switch (match) {
            case ROUTE_EVENTS:
                id = db.insertOrThrow(DataContract.Event.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Event.CONTENT_URI + "/" + id);
                break;
            case ROUTE_EVENTS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_TALKS:
                id = db.insertOrThrow(DataContract.Talk.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Talk.CONTENT_URI + "/" + id);
                break;
            case ROUTE_TALKS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_CONVO:
                id = db.insertOrThrow(DataContract.Convo.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Convo.CONTENT_URI + "/" + id);
                break;
            case ROUTE_CONVO_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_GROUPS:
                id = db.insertOrThrow(DataContract.Group.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Group.CONTENT_URI + "/" + id);
                break;
            case ROUTE_GROUPS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_LOCATIONS:
                id = db.insertOrThrow(DataContract.Location.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Location.CONTENT_URI + "/" + id);
                break;
            case ROUTE_LOCATIONS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_SIGNUPS:
                id = db.insertOrThrow(DataContract.Signup.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Signup.CONTENT_URI + "/" + id);
                break;
            case ROUTE_SIGNUPS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_TOPICS:
                id = db.insertOrThrow(DataContract.Topic.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Topic.CONTENT_URI + "/" + id);
                break;
            case ROUTE_TOPICS_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            case ROUTE_BC:
                id = db.insertOrThrow(DataContract.Broadcast.TABLE_NAME, null, values);
                result = Uri.parse(DataContract.Broadcast.CONTENT_URI + "/" + id);
                break;
            case ROUTE_BC_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    /**
     * Delete an entry by database by URI.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case ROUTE_EVENTS:
                count = builder.table(DataContract.Event.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_EVENTS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Event.TABLE_NAME)
                       .where(DataContract.Event._ID + "=?", id)
                       .where(selection, selectionArgs)
                       .delete(db);
                break;
            case ROUTE_TALKS:
                count = builder.table(DataContract.Talk.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_TALKS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Talk.TABLE_NAME)
                        .where(DataContract.Talk._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_CONVO:
                count = builder.table(DataContract.Convo.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_CONVO_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Convo.TABLE_NAME)
                        .where(DataContract.Convo._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_GROUPS:
                count = builder.table(DataContract.Group.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_GROUPS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Group.TABLE_NAME)
                        .where(DataContract.Group._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_LOCATIONS:
                count = builder.table(DataContract.Location.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_LOCATIONS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Location.TABLE_NAME)
                        .where(DataContract.Location._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_SIGNUPS:
                count = builder.table(DataContract.Signup.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_SIGNUPS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Signup.TABLE_NAME)
                        .where(DataContract.Signup._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_TOPICS:
                count = builder.table(DataContract.Topic.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_TOPICS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Topic.TABLE_NAME)
                        .where(DataContract.Topic._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_BC:
                count = builder.table(DataContract.Broadcast.TABLE_NAME).where(selection, selectionArgs).delete(db);
                break;
            case ROUTE_BC_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Broadcast.TABLE_NAME)
                        .where(DataContract.Broadcast._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * Update an entry in the database by URI.
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id;
        switch (match) {
            case ROUTE_EVENTS:
                count = builder.table(DataContract.Event.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_EVENTS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Event.TABLE_NAME)
                        .where(DataContract.Event._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_TALKS:
                count = builder.table(DataContract.Talk.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_TALKS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Talk.TABLE_NAME)
                        .where(DataContract.Talk._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_CONVO:
                count = builder.table(DataContract.Convo.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_CONVO_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Convo.TABLE_NAME)
                        .where(DataContract.Convo._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_GROUPS:
                count = builder.table(DataContract.Group.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_GROUPS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Group.TABLE_NAME)
                        .where(DataContract.Group._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_LOCATIONS:
                count = builder.table(DataContract.Location.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_LOCATIONS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Location.TABLE_NAME)
                        .where(DataContract.Location._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_SIGNUPS:
                count = builder.table(DataContract.Signup.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_SIGNUPS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Signup.TABLE_NAME)
                        .where(DataContract.Signup._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_TOPICS:
                count = builder.table(DataContract.Topic.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_TOPICS_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Topic.TABLE_NAME)
                        .where(DataContract.Topic._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_BC:
                count = builder.table(DataContract.Broadcast.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_BC_ID:
                id = uri.getLastPathSegment();
                count = builder.table(DataContract.Broadcast.TABLE_NAME)
                        .where(DataContract.Broadcast._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    /**
     * SQLite backend for @{link FeedProvider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by FeedProvider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class DataDatabase extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 12;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "ccmdata.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String TYPE_REAL=" REAL";
        private static final String COMMA_SEP = ",";
        /** SQL statement to create "event" table. */
        private static final String SQL_CREATE_EVENTS =
                "CREATE TABLE " + DataContract.Event.TABLE_NAME + " (" +
                        DataContract.Event._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Event.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_TITLE    + TYPE_TEXT + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_LOCATION + TYPE_TEXT + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_LAT + TYPE_REAL + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_LNG + TYPE_REAL + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_VERSION + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Event.COLUMN_NAME_DATE + TYPE_TEXT + ")";
        private static final String SQL_CREATE_TALKS =
                "CREATE TABLE " + DataContract.Talk.TABLE_NAME + " (" +
                        DataContract.Talk._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Talk.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_AUTHOR    + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_SUBJECT + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_REFERENCE + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_OUTLINE + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_VERSION + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_VERSE + TYPE_TEXT + COMMA_SEP +
                        DataContract.Talk.COLUMN_NAME_DATE + TYPE_TEXT + ")";
        private static final String SQL_CREATE_BCS =
                "CREATE TABLE " + DataContract.Broadcast.TABLE_NAME + " (" +
                        DataContract.Broadcast._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Broadcast.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Broadcast.COLUMN_NAME_TITLE + TYPE_TEXT + COMMA_SEP +
                        DataContract.Broadcast.COLUMN_NAME_MSG + TYPE_TEXT + COMMA_SEP +
                        //TODO add Date
                        DataContract.Broadcast.COLUMN_NAME_VERSION + TYPE_INTEGER + ")";
        private static final String SQL_CREATE_CONVO =
                "CREATE TABLE " + DataContract.Convo.TABLE_NAME + " (" +
                        DataContract.Convo._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Convo.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_SUBJECT + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_TOPIC + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_USER + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_FROM + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_SINGLETON + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_VERSION + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_MINMESSAGES + TYPE_TEXT + COMMA_SEP +
                        DataContract.Convo.COLUMN_NAME_MESSAGES + TYPE_TEXT + ")";
        private static final String SQL_CREATE_GROUPS =
                "CREATE TABLE " + DataContract.Group.TABLE_NAME + " (" +
                        DataContract.Group._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Group.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Group.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        DataContract.Group.COLUMN_NAME_WRITEEVENTS + TYPE_TEXT + COMMA_SEP +
                        DataContract.Group.COLUMN_NAME_WRITESIGNUPS + TYPE_TEXT + COMMA_SEP +
                        DataContract.Group.COLUMN_NAME_WRITETALKS + TYPE_TEXT + COMMA_SEP +
                        DataContract.Group.COLUMN_NAME_VERSION + TYPE_INTEGER + ")";
        private static final String SQL_CREATE_LOCATIONS =
                "CREATE TABLE " + DataContract.Location.TABLE_NAME + " (" +
                        DataContract.Location._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Location.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Location.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        DataContract.Location.COLUMN_NAME_ADDRESS + TYPE_TEXT + COMMA_SEP +
                        DataContract.Location.COLUMN_NAME_LAT + TYPE_REAL + COMMA_SEP +
                        DataContract.Location.COLUMN_NAME_LNG + TYPE_REAL + COMMA_SEP +
                        DataContract.Location.COLUMN_NAME_VERSION + TYPE_INTEGER + ")";
        private static final String SQL_CREATE_SIGNUPS =
                "CREATE TABLE " + DataContract.Signup.TABLE_NAME + " (" +
                        DataContract.Signup._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Signup.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_MEMBER_OF + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_MEMBER_COUNT + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_LOCATION + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_DATE_INFO + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_ADDRESS + TYPE_TEXT + COMMA_SEP +
                        DataContract.Signup.COLUMN_NAME_VERSION + TYPE_INTEGER + ")";
        private static final String SQL_CREATE_TOPICS =
                "CREATE TABLE " + DataContract.Topic.TABLE_NAME + " (" +
                        DataContract.Topic._ID + " INTEGER PRIMARY KEY," +
                        DataContract.Topic.COLUMN_NAME_ENTRY_ID + TYPE_TEXT + COMMA_SEP +
                        DataContract.Topic.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        DataContract.Topic.COLUMN_NAME_IS_ANON + TYPE_INTEGER + COMMA_SEP +
                        DataContract.Topic.COLUMN_NAME_VERSION + TYPE_INTEGER + ")";

        /** SQL statement to drop "event" table. */
        private static final String SQL_DELETE_EVENTS =
                "DROP TABLE IF EXISTS " + DataContract.Event.TABLE_NAME;
        private static final String SQL_DELETE_TALKS =
                "DROP TABLE IF EXISTS " + DataContract.Talk.TABLE_NAME;
        private static final String SQL_DELETE_BCS =
                "DROP TABLE IF EXISTS " + DataContract.Broadcast.TABLE_NAME;
        private static final String SQL_DELETE_CONVO =
                "DROP TABLE IF EXISTS " + DataContract.Convo.TABLE_NAME;
        private static final String SQL_DELETE_GROUPS =
                "DROP TABLE IF EXISTS " + DataContract.Group.TABLE_NAME;
        private static final String SQL_DELETE_LOCATIONS =
                "DROP TABLE IF EXISTS " + DataContract.Location.TABLE_NAME;
        private static final String SQL_DELETE_SIGNUPS =
                "DROP TABLE IF EXISTS " + DataContract.Signup.TABLE_NAME;
        private static final String SQL_DELETE_TOPICS =
                "DROP TABLE IF EXISTS " + DataContract.Topic.TABLE_NAME;

        public DataDatabase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_EVENTS);
            db.execSQL(SQL_CREATE_TALKS);
            db.execSQL(SQL_CREATE_BCS);
            db.execSQL(SQL_CREATE_CONVO);
            db.execSQL(SQL_CREATE_GROUPS);
            db.execSQL(SQL_CREATE_LOCATIONS);
            db.execSQL(SQL_CREATE_SIGNUPS);
            db.execSQL(SQL_CREATE_TOPICS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_EVENTS);
            db.execSQL(SQL_DELETE_TALKS);
            db.execSQL(SQL_DELETE_CONVO);
            db.execSQL(SQL_DELETE_BCS);
            db.execSQL(SQL_DELETE_LOCATIONS);
            db.execSQL(SQL_DELETE_GROUPS);
            db.execSQL(SQL_DELETE_SIGNUPS);
            db.execSQL(SQL_DELETE_TOPICS);
            onCreate(db);
        }
    }
}
