package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMSessionStatus
import com.sistemium.sissales.base.session.STMCoreSettingsController

/**
 * Created by edgarjanvuicik on 01/02/2018.
 */
interface STMSession {

    var coreSettingsController: STMCoreSettingsController?
    var uid:String
    var filing:STMFiling
    var persistenceDelegate: STMFullStackPersisting
    var status: STMSessionStatus

}