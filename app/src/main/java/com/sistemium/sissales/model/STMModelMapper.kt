package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 28/11/2017.
 */
class STMModelMapper(var savedModel:STMManagedObjectModel?, var destinationModel:STMManagedObjectModel) {

    var needToMigrate:Boolean

    init {

        if (savedModel == null) {
            needToMigrate = true
        }else{
            if (savedModel == destinationModel){
                needToMigrate = false
            }else{
                needToMigrate = true
                TODO("Not implemented")
            }
        }

    }

}