package com.sistemium.sissales.base.session

import com.sistemium.sissales.interfaces.*

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncerHelper: STMDefantomizing, STMDataDownloading {

    override var persistenceFantomsDelegate: STMPersistingFantoms? = null
    override var dataDownloadingOwner: STMDataDownloadingOwner? = null
    override var defantomizingOwner:STMDefantomizingOwner? = null

}