package com.sistemium.sissales.base

import java.text.SimpleDateFormat
import java.util.*

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

        fun removePrefixFromEntityName(entityName:String):String {

            var _entityName = entityName

            if (entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = entityName.removePrefix(STMConstants.ISISTEMIUM_PREFIX)
            }
            return _entityName

        }

        fun stringFromNow():String{

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

            sdf.timeZone = TimeZone.getTimeZone("GMT")

            return sdf.format(Date())


        }

        fun uuidString():String{

            return UUID.randomUUID().toString()

        }

        fun jsonStringFromObject(value:Any):String{

            TODO("not implemented")

        }

    }

}