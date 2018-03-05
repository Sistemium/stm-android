package com.sistemium.sissales.interfaces

import java.util.concurrent.ExecutorService

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDataDownloading {

    var dataDownloadingOwner: STMDataDownloadingOwner?
    var downloadingQueue: ExecutorService?

    fun startDownloading(entitiesNames:ArrayList<String>?)
    fun stopDownloading()
    fun dataReceivedSuccessfully(entityName:String, dataRecieved:ArrayList<*>?, offset:String?, pageSize:Int?, error:Exception?)

}