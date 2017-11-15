package com.sistemium.sissales.base

/**
 * Created by edgarjanvuicik on 14/11/2017.
 */

class STMFunctions{

    companion object {
        fun addPrefixToEntityName(entityName:String):String {

            var _entityName = entityName

            if (!entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = STMConstants.ISISTEMIUM_PREFIX + entityName
            }
            return _entityName

        }
    }

}