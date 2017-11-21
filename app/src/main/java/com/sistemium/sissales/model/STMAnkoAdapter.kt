package com.sistemium.sissales.model

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling

/**
 * Created by edgarjanvuicik on 15/11/2017.
 */
class STMAnkoAdapter(model:STMModelling) :STMAdapting {

    override var storageType = STMStorageType.STMStorageTypeAnko

    override var model = model

}