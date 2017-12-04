package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 30/11/2017.
 */
class STMSQLiteDatabaseTransaction(database: SQLiteDatabase, private var adapter:STMSQLiteDatabaseAdapter):STMPersistingTransaction {

    var operation:STMSQLiteDatabaseOperation? = null

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {

        val pageSize:Int? = options?.get(STMConstants.STMPersistingOptionPageSize) as? Int
        var offset:Int? = options?.get(STMConstants.STMPersistingOptionStartPage) as? Int
        val groupBy:Array<*>? = options?.get(STMConstants.STMPersistingOptionGroupBy) as? Array<*>?

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
        var _orderBy = orderBy

        var columnKeys = groupBy

        if (columnKeys?.count() != null && columnKeys.count() != 0) {

            columnKeys = columnKeys.map {
                "[$it]"
            }.toTypedArray()

            options = "GROUP BY " + columnKeys.joinToString(", ")

            columnKeys = columnKeys.plus(sumKeysForEntityName(entityName))

            columnKeys = columnKeys.plus("count(*) [count()]")

            columns = columnKeys.joinToString(", ")

        } else {
            columns = "*"
        }

        if (orderBy != null) {
            _orderBy = orderBy.split(",").joinToString( if (ascending) " ASC," else " DESC,")
            val order = " ORDER BY $_orderBy ${if (ascending) " ASC," else " DESC,"}"
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
            where = predicate.predicateForAdapter(adapter) ?: throw Exception("wrong predicate")
            where = where
                    .replace(" AND ()", "")
                    .replace("?uncapitalizedTableName?", tableName.toLowerCase())
                    .replace("?capitalizedTableName?", tableName.capitalize())
        }

        return selectFrom(entityName, columns, where, _orderBy)

    }

    private fun sumKeysForEntityName(entityName:String):Array<String>{

        val numericTypes = arrayOf("Integer 16", "Integer 32", "Integer 64", "Decimal", "Double", "Float")

        val tableColumns = adapter.model.fieldsForEntityName(entityName)

        var sumKeys = arrayOf<String>()

        tableColumns.map {

            val valueIsNumeric = numericTypes.contains(it.value.attributeType)

            val keyIsIgnored = adapter.ignoredAttributeNames.contains(it.key)

            if (valueIsNumeric && !keyIsIgnored) sumKeys = sumKeys.plus("sum([$sumKeys.plusAssign(it.value.attributeName)]) [sum($sumKeys.plusAssign(it.value.attributeName))]")

        }

        return sumKeys

    }

    private fun selectFrom(entityName:String, columns:String, where:String, _orderBy:String?):Array<Map<*, *>> {

        TODO("Not implemented")

    }

}