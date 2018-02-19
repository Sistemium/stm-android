package com.sistemium.sissales.base.session

import com.sistemium.sissales.interfaces.STMDataSyncing
import com.sistemium.sissales.interfaces.STMDataSyncingSubscriber
import com.sistemium.sissales.interfaces.STMSession

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMUnsyncedDataHelper: STMDataSyncing {

    override var subscriberDelegate: STMDataSyncingSubscriber? = null
    var session:STMSession? = null

}