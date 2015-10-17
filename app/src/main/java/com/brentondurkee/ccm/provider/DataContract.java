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

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Field and table name constants for data provider
 */
public class DataContract {
    private DataContract() {
    }



    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "com.brentondurkee.ccm";

    /**
     * Base URI. (content://com.brentondurkee.ccm)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "event"-type resources..
     */
    private static final String PATH_EVENTS = "events";

    private static final String PATH_TALKS = "talks";


    private static final String PATH_LOCATIONS = "locations";

    private static final String PATH_GROUPS = "groups";

    private static final String PATH_SIGNUPS = "signups";

    private static final String PATH_TOPICS = "topics";

    private static final String PATH_CONVO = "conversations";

    private static final String PATH_BC = "broadcasts";
    /**
     * Columns supported by "events" records.
     */
    public static class Event implements BaseColumns {
        /**
         * MIME type for lists of events.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.events";
        /**
         * MIME type for individual event.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.event";

        /**
         * Fully qualified URI for "event" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_EVENTS).build();

        /**
         * Table name where records are stored for "event" resources.
         */
        public static final String TABLE_NAME = "event";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "event_id";
        /**
         * Event title
         */
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Talk implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.talks";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.talk";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TALKS).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "talk";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "talk_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_AUTHOR = "author";
        public static final String COLUMN_NAME_SUBJECT = "subject";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_REFERENCE = "reference";
        public static final String COLUMN_NAME_OUTLINE = "outline";
        public static final String COLUMN_NAME_VERSE = "verse";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Convo implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.conversations";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.conversation";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONVO).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "conversation";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "conversation_id";
        /**
         * talk title
         */

        public static final String COLUMN_NAME_SUBJECT = "subject";
        public static final String COLUMN_NAME_TOPIC = "topic";
        public static final String COLUMN_NAME_FROM = "from_who";
        public static final String COLUMN_NAME_USER = "participant";
        public static final String COLUMN_NAME_SINGLETON = "singleton";
        public static final String COLUMN_NAME_MINMESSAGES = "minmessages";
        public static final String COLUMN_NAME_MESSAGES = "messages";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Broadcast implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.broadcasts";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.broadcast";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BC).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "broadcast";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "broadcast_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_MSG = "message";
        public static final String COLUMN_NAME_VERSION = "version";
        public static final String COLUMN_NAME_DATE = "date";
    }

    public static class Location implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.locations";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.location";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "location";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "location_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_LAT = "lat";
        public static final String COLUMN_NAME_LNG = "lng";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Group implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.groups";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.group";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_GROUPS).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "user_group";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "group_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_WRITETALKS = "write_talks";
        public static final String COLUMN_NAME_WRITESIGNUPS = "write_signups";
        public static final String COLUMN_NAME_WRITEEVENTS = "write_events";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Signup implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.signups";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.signup";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SIGNUPS).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "user_signup";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "signup_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_MEMBER_OF = "memberOf";
        public static final String COLUMN_NAME_MEMBER_COUNT = "memberCount";
        public static final String COLUMN_NAME_LOCATION = "location";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_DATE_INFO = "dateInfo";
        public static final String COLUMN_NAME_ADDRESS = "address";
        public static final String COLUMN_NAME_VERSION = "version";
    }

    public static class Topic implements BaseColumns {
        /**
         * MIME type for lists of talks.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ccm.topics";
        /**
         * MIME type for individual talk.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ccm.topic";

        /**
         * Fully qualified URI for "talk" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TOPICS).build();

        /**
         * Table name where records are stored for "talk" resources.
         */
        public static final String TABLE_NAME = "user_topic";
        /**
         * MongoDB ID. (Note: Not to be confused with the database primary key, which is _ID.
         */
        public static final String COLUMN_NAME_ENTRY_ID = "topic_id";
        /**
         * talk title
         */
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_IS_ANON = "isAnon";
        public static final String COLUMN_NAME_VERSION = "version";
    }
}