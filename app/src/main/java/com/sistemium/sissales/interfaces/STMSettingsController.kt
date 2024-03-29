package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 01/02/2018.
 */
interface STMSettingsController {

    fun stringValueForSettings(settingsName: String, group: String): String?
    fun currentSettingsForGroup(group: String): Map<*, *>?
    var persistenceDelegate: STMFullStackPersisting?

}