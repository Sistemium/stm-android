package com.sistemium.sissales.interfaces

import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.enums.STMSessionStatus

/**
 * Created by edgarjanvuicik on 01/02/2018.
 */
interface STMSession {

    var settingsController: STMSettingsController?
    var uid:String
    var filing:STMFiling
    var persistenceDelegate: STMFullStackPersisting
    var status: STMSessionStatus
    var logger: STMLogger?

}