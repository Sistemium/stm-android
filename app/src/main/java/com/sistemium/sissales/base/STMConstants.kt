package com.sistemium.sissales.base

/**
 * Created by edgarjanvuicik on 14/11/2017.
 */

class STMConstants {

    companion object {

        const val ISISTEMIUM_PREFIX = "STM"
        const val DEFAULT_PERSISTING_PRIMARY_KEY = "id"
        const val STMPersistingOptionFantoms = "fantoms"
        const val STMPersistingOptionForceStorage = "forceStorage"
        const val POOL_SIZE = 4
        const val SQL_LITE_PATH = "sqllite"
        const val STMPersistingOptionPageSize = "pageSize"
        const val STMPersistingOptionStartPage = "startPage"
        const val STMPersistingOptionGroupBy = "groupBy"
        const val STMPersistingOptionOrder = "sortBy"
        const val STMPersistingOptionOrderDirection = "direction"
        const val STMPersistingKeyCreationTimestamp = "deviceCts"
        const val STMPersistingKeyVersion = "deviceTs"
        const val STMPersistingOptionLts = "lts"
        const val STMPersistingKeyPhantom = "isFantom"
        const val RELATIONSHIP_SUFFIX = "Id"
        const val STMPersistingOptionReturnSaved = "returnSaved"
        const val STM_ENTITY_NAME = "STMEntity"
        const val STM_SETTING_NAME = "STMSetting"
        const val STM_RECORDSTATUS_NAME = "STMRecordStatus"
        const val STMPersistingOptionRecordstatuses = "createRecordStatuses"
        const val STMPersistingOptionOrderDirectionDescValue = "DESC"
        const val STMPersistingOptionWhere = "where"
        const val PERSISTENCE_PATH = "persistence"
        const val AUTH_DELAY = 25
        const val BACKGROUND_DELAY = 300
        const val syncInterval = 600
        const val LOGMESSAGE_MAX_TIME_INTERVAL_TO_UPLOAD = 1000 * 60 * 60 * 24
        const val STMPersistingOptionOrderDirectionAscValue = "ASC"
        const val kSocketUpdateMethod = "update"
        const val kSocketDestroyMethod = "destroy"
        const val STMPersistingOptionOffset = "offset"
        const val kSocketFindMethod = "find"
        const val kSocketFindAllMethod = "findAll"
        const val SQLiteText = "TEXT"
        const val SQLiteInt = "INTEGER"
        const val SQLiteNumber = "NUMERIC"
        const val CASCADE_TRIGGER_PREFIX = "cascade_"
        const val SQLiteBeforeDelete = "BEFORE DELETE"
        const val SQLiteBeforeInsert = "BEFORE INSERT"
        const val SQLiteDefaultNow = "DEFAULT(STRFTIME('%Y-%m-%d %H:%M:%f', 'NOW'))"
        const val fetchLimit = 500
        const val NOTIFICATION_SYNCER_HAVE_UNSYNCED_OBJECTS = "haveUnsyncedObjects"
        const val NOTIFICATION_SYNCER_HAVE_NO_UNSYNCED_OBJECTS = "haveNoUnsyncedObjects"
        const val NOTIFICATION_SYNCER_SENDIG_DATA = "syncerIsSendingData"
        const val THUMBNAIL_SIZE = 150

    }

}