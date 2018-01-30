package com.sistemium.sissales.persisting

import android.content.ContentValues
import android.database.Cursor.FIELD_TYPE_FLOAT
import android.database.Cursor.FIELD_TYPE_INTEGER
import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by edgarjanvuicik on 30/11/2017.
 */
class STMSQLiteDatabaseTransaction(private var database: SQLiteDatabase, private var adapter:STMSQLiteDatabaseAdapter):STMPersistingTransaction {

    override var modellingDelegate: STMModelling? = adapter.model

    var operation:STMSQLiteDatabaseOperation? = null

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>> {

        val pageSize = (options?.get(STMConstants.STMPersistingOptionPageSize) as? Double)?.toInt()
        var offset = (options?.get(STMConstants.STMPersistingOptionStartPage) as? Double)?.toInt()
        val groupBy= options?.get(STMConstants.STMPersistingOptionGroupBy) as? ArrayList<*>

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
                            fetchLimit:Int?, fetchOffset:Int?, groupBy:ArrayList<*>?):ArrayList<Map<*, *>> {

        var options = ""
        var columns = ""

        if (groupBy?.count() != null && groupBy.count() != 0) {

            val columnKeys = ArrayList(groupBy.map {
                "[$it]"
            })

            options = " GROUP BY " + columnKeys.joinToString(", ")

            columnKeys += sumKeysForEntityName(entityName)

            columnKeys += "count(*) [count()]"

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
            STMFunctions.debugLog("EntityName", entityName)
            STMFunctions.debugLog("PREDICATE", where)
            STMFunctions.debugLog("OPTIONS", options)
            STMFunctions.debugLog("COLUMNS", columns)
        }

        return selectFrom(tableName, columns, where, options)

    }

    override fun mergeWithoutSave(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *>? {

        val now = STMFunctions.stringFromNow()

        var returnSaved = true

        val savingAttributes = HashMap(attributes)

        if (options?.get(STMConstants.STMPersistingOptionReturnSaved) == false){

            returnSaved = false

        }

        if (options?.get(STMConstants.STMPersistingOptionLts) != null){

            savingAttributes[STMConstants.STMPersistingOptionLts] = options[STMConstants.STMPersistingOptionLts]
            savingAttributes.remove(STMConstants.STMPersistingKeyVersion)

        } else {

            savingAttributes[STMConstants.STMPersistingKeyVersion] = now
            savingAttributes.remove(STMConstants.STMPersistingOptionLts)

        }

        savingAttributes["deviceAts"] = now

        if (savingAttributes[STMConstants.STMPersistingKeyCreationTimestamp] == null){

            savingAttributes[STMConstants.STMPersistingKeyCreationTimestamp] = now

        }

        val tableName = STMFunctions.removePrefixFromEntityName(entityName)

        val pk = mergeInto(tableName, savingAttributes)

        if (pk == null || !returnSaved){

            return null

        }

        return selectFrom(tableName, "id = '$pk'", null).firstOrNull()

    }

    override fun destroyWithoutSave(entityName: String, predicate: STMPredicate?, options: Map<*, *>?):Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun mergeInto(tableName:String, dictionary:Map<*,*>):String?{

        val pk = if (dictionary[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] != null) dictionary[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as String else STMFunctions.uuidString()

        val (keys, values) = updateTablename(tableName, dictionary)

        val cv = ContentValues()

        for (index in 0 until keys.size){

            if (values[index] == "null"){

                cv.putNull(keys[index])

            }else{

                cv.put(keys[index], values[index])

            }

        }

        try {

            val changes = database.update(tableName, cv, "[id] = ?", arrayOf(pk))

            if (changes == 0) {

                cv.put("id", pk)

                cv.put("[isFantom]", 0)

                database.insert(tableName, null, cv)

            }

        } catch (e:Exception) {

            if (e.toString() == "ignored") {

                return pk

            }

            return null

        }

        return pk

    }

    private fun updateTablename(tableName:String, dictionary:Map<*,*>): Pair<ArrayList<String>, ArrayList<String>>{

        val keys = arrayListOf<String>()
        val values = arrayListOf<String>()

        val columns = adapter.columnsByTable!![tableName]!!.keys

        val jsonColumns = adapter.columnsByTable!![tableName]?.filterValues {

            return@filterValues it == "Transformable"

        }?.keys

        for (key in dictionary.keys){

            if (columns.contains(key) && !arrayListOf(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY, STMConstants.STMPersistingKeyPhantom, STMConstants.STMPersistingKeyCreationTimestamp).contains(key)){

                keys.add("[$key]")

                val value = dictionary[key]

                if (jsonColumns?.contains(key) == true){

                    values.add(STMFunctions.jsonStringFromObject(value!!))

                } else {

                    values.add(value.toString())

                }

            }

        }

        return Pair(keys,values)

    }

    private fun sumKeysForEntityName(entityName:String):ArrayList<String>{

        val numericTypes = arrayOf("Integer 16", "Integer 32", "Integer 64", "Decimal", "Double", "Float")

        val minMaxTypes = arrayOf("Date")

        val tableColumns = adapter.model.fieldsForEntityName(entityName)

        val sumKeys = arrayListOf<String>()

        tableColumns.map {

            if (it.value.attributeName == "id"){

                return@map

            }

            val valueIsNumeric = numericTypes.contains(it.value.attributeType)

            val minMaxField = minMaxTypes.contains(it.value.attributeType)

            if (valueIsNumeric) {
                sumKeys += "sum([${(it.value.attributeName)}]) [sum(${(it.value.attributeName)})]"
            } else if (minMaxField || it.value.attributeName == "date") {
                sumKeys += "max([${(it.value.attributeName)}]) [max(${(it.value.attributeName)})]"
                sumKeys += "min([${(it.value.attributeName)}]) [min(${(it.value.attributeName)})]"
            }

        }

        return sumKeys

    }

    private fun selectFrom(tableName:String, where:String, orderBy:String?):ArrayList<Map<*, *>> {

        return selectFrom(tableName, "*", where, orderBy)

    }

    private fun selectFrom(tableName:String, columns:String, where:String, orderBy:String?):ArrayList<Map<*, *>> {

        val rez = arrayListOf<Map<*, *>>()

        var _where = where

        var _orderBy = orderBy

        if (_where.isNotEmpty()) {
            _where = " WHERE " + _where
        }

        if (_orderBy == null) _orderBy = ""

        val query = "SELECT $columns FROM [$tableName]$_where $_orderBy"

        STMFunctions.debugLog("QUERY", query)

        val c = this.database.rawQuery(query,null)

        STMFunctions.debugLog("QUERY", "execute finished")

        if (c.moveToFirst()) {

            val atributeTypes = hashMapOf<String, String?>()

            c.columnNames.forEach {

                atributeTypes[it] = adapter.model.fieldsForEntityName(STMFunctions.addPrefixToEntityName(tableName))[it]?.attributeType

            }

            STMFunctions.debugLog("QUERY", "result reading preparations finished")

            do {

                val dict = hashMapOf<String, Any?>()

                for (columnName in c.columnNames){

                    val index = c.getColumnIndex(columnName)

                    val attributeType = atributeTypes[columnName]

                    if (c.isNull(index)){

                        dict[columnName] = null

                        continue

                    }

                    if (attributeType == "Boolean"){

                        val data = c.getInt(index)

                        dict[columnName] = data != 0

                        continue

                    }

                    if (c.getType(index) == FIELD_TYPE_INTEGER){

                        dict[columnName] = c.getInt(index)

                        continue

                    }

                    if (c.getType(index) == FIELD_TYPE_FLOAT){

                        dict[columnName] = c.getDouble(index)

                        continue

                    }

                    dict[columnName] = c.getString(index)

                }

                rez += dict

            } while (c.moveToNext())

            STMFunctions.debugLog("QUERY", "result reading finished")
        }
        c.close()

        return rez

    }

}