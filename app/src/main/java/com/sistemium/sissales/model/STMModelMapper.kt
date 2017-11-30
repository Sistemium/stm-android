package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 28/11/2017.
 */
class STMModelMapper(var savedModel:STMManagedObjectModel?, var destinationModel:STMManagedObjectModel) {

    var needToMigrate:Boolean

    init {

        // ugly pyramidal code, but it seems that kotlin doesn't allow use return in init
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