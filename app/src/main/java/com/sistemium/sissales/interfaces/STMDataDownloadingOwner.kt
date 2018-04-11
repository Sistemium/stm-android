package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDataDownloadingOwner {

    fun receiveData(entityName: String, offset: String)
    fun entitiesChanged()
    fun dataDownloadingFinished()

}