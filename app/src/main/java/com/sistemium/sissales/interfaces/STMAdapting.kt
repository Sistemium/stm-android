package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMStorageType

/**
 * Created by edgarjanvuicik on 15/11/2017.
 */

interface STMAdapting {

    var model:STMModelling
    var storageType:STMStorageType
    var ignoredAttributeNames:Array<String>
    var builtInAttributeNames:Array<String>

    fun beginTransaction(readOnly:Boolean = false):STMPersistingTransaction

}