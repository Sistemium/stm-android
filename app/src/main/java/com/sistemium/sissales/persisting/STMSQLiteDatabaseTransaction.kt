package com.sistemium.sissales.persisting

import android.database.Cursor.FIELD_TYPE_FLOAT
import android.database.Cursor.FIELD_TYPE_INTEGER
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 30/11/2017.
 */
class STMSQLiteDatabaseTransaction(private var database: SQLiteDatabase, private var adapter:STMSQLiteDatabaseAdapter):STMPersistingTransaction {

    var operation:STMSQLiteDatabaseOperation? = null

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {

        val pageSize:Int? = (options?.get(STMConstants.STMPersistingOptionPageSize) as? Double)?.toInt()
        var offset:Int? = (options?.get(STMConstants.STMPersistingOptionStartPage) as? Double)?.toInt()
        val groupBy:Array<*>? = (options?.get(STMConstants.STMPersistingOptionGroupBy) as? ArrayList<*>)?.toArray()

        if (offset != null && pageSize != null) {
            offset -= 1
            offset *= pageSize
        }

        var orderBy:String? = options?.get(STMConstants.STMPersistingOptionOrder) as? String

        val direction = options?.get(STMConstants.STMPersistingOptionOrderDirection) as? String

        val asc:Boolean = direction != null &&  direction.toLowerCase() == "asc"

        if (orderBy == null) {
            orderBy = "id"
        }

        return findAllSync(entityName, predicate, orderBy, asc, pageSize, offset, groupBy)
    }

    private fun findAllSync(entityName: String, predicate: STMPredicate?, orderBy:String?, ascending:Boolean,
                            fetchLimit:Int?, fetchOffset:Int?, groupBy:Array<*>?):Array<Map<*, *>> {

        var options = ""
        var columns = ""

        var columnKeys = groupBy

        if (columnKeys?.count() != null && columnKeys.count() != 0) {

            columnKeys = columnKeys.map {
                "[$it]"
            }.toTypedArray()

            options = " GROUP BY " + columnKeys.joinToString(", ")

            columnKeys = columnKeys.plus(sumKeysForEntityName(entityName))

            columnKeys = columnKeys.plus("count(*) [count()]")

            columns = columnKeys.joinToString(", ")

        } else {
            columns = "*"
        }

        if (orderBy != null) {
            val _orderBy = orderBy.split(",").joinToString( if (ascending) " ASC," else " DESC,")
            val order = " ORDER BY $_orderBy ${if (ascending) "ASC" else "DESC"}"
            options += order
        }

        if (fetchLimit != null && fetchLimit > 0) {
            val limit = " LIMIT $fetchLimit"
            options += limit
        }

        if (fetchOffset != null && fetchOffset > 0) {
            val offset = " OFFSET $fetchOffset"
            options += offset
        }

        val tableName = STMFunctions.removePrefixFromEntityName(entityName)

        var where = ""

        if (predicate != null){
            where = predicate.predicateForAdapter(adapter, entityName) ?: ""
            Log.d("DEBUG:EntityName", entityName)
            Log.d("DEBUG:PREDICATE", where)
            Log.d("DEBUG:OPTIONS", options)
            Log.d("DEBUG:COLUMNS", columns)
        }

        return selectFrom(tableName, columns, where, options)

    }

    private fun sumKeysForEntityName(entityName:String):Array<String>{

        val numericTypes = arrayOf("Integer 16", "Integer 32", "Integer 64", "Decimal", "Double", "Float")

        val minMaxTypes = arrayOf("Date")

        val tableColumns = adapter.model.fieldsForEntityName(entityName)

        var sumKeys = arrayOf<String>()

        tableColumns.map {

            if (it.value.attributeName == "id"){

                return@map

            }

            val valueIsNumeric = numericTypes.contains(it.value.attributeType)

            val minMaxField = minMaxTypes.contains(it.value.attributeType)

            if (valueIsNumeric) {
                sumKeys = sumKeys.plus("sum([${(it.value.attributeName)}]) [sum(${(it.value.attributeName)})]")
            } else if (minMaxField || it.value.attributeName == "date") {
                sumKeys = sumKeys.plus("max([${(it.value.attributeName)}]) [max(${(it.value.attributeName)})]")
                sumKeys = sumKeys.plus("min([${(it.value.attributeName)}]) [min(${(it.value.attributeName)})]")
            }

        }

        return sumKeys

    }

    private fun selectFrom(tableName:String, columns:String, where:String, orderBy:String?):Array<Map<*, *>> {

        var rez = arrayOf<Map<*, *>>()

        var _where = where

        var _orderBy = orderBy

        if (_where.isNotEmpty()) {
            _where = " WHERE " + _where
        }

        if (_orderBy == null) _orderBy = ""

        val query = "SELECT $columns FROM [$tableName]$_where $_orderBy"

        Log.d("DEBUG:QUERY", query)

        val c = this.database.rawQuery(query,null)

        if (c.moveToFirst()) {
            do {

                val dict = hashMapOf<String, Any?>()

                for (columnName in c.columnNames){

                    val index = c.getColumnIndex(columnName)

                    val attributeType = adapter.model.entitiesByName.get(STMFunctions.addPrefixToEntityName(tableName))?.attributesByName?.get(columnName)?.attributeType

                    if (c.isNull(index)){

                        dict.put(columnName, null)

                        continue

                    }

                    if (attributeType == "Boolean"){

                        val data = c.getInt(index)

                        if (data == 0){

                            dict.put(columnName, false)

                        }else{

                            dict.put(columnName, true)

                        }

                        continue

                    }

                    if (c.getType(index) == FIELD_TYPE_INTEGER){

                        dict.put(columnName, c.getInt(index))

                        continue

                    }

                    if (c.getType(index) == FIELD_TYPE_FLOAT){

                        dict.put(columnName, c.getDouble(index))

                        continue

                    }

                    dict.put(columnName, c.getString(index))

                }

                rez = rez.plus(dict)

            } while (c.moveToNext())
        }
        c.close()

        return rez

//        FMResultSet *s = [self.database executeQuery:query];
//
//        NSArray *booleanKeys = [self.stmFMDB.columnsByTable[tableName] allKeysForObject:[NSNumber numberWithUnsignedInteger:NSBooleanAttributeType]];
//
//        NSArray *jsonKeys = [self.stmFMDB.columnsByTable[tableName] allKeysForObject:[NSNumber numberWithUnsignedInteger:NSTransformableAttributeType]];
//
//        while ([s next]) {
//
//            NSMutableDictionary *dict = (NSMutableDictionary*)s.resultDictionary;
//
//            for (NSString *key in booleanKeys){
//                if ([STMFunctions isNotNull:[dict valueForKey:key]]){
//                    dict[key] = (__bridge id _Nullable)([dict[key] boolValue] ? kCFBooleanTrue : kCFBooleanFalse);
//                }
//            }
//
//            for (NSString *key in jsonKeys){
//                if ([STMFunctions isNotNull:[dict valueForKey:key]]){
//                    dict[key] = [STMFunctions jsonObjectFromString:dict[key]];
//                }
//            }
//
//            [rez addObject:dict.copy];
//        }
//
//        // there will be memory warnings loading catalogue on an old device if no copy
//        return rez.copy;

    }

}