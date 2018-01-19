package com.sistemium.sissales.model

import android.database.sqlite.SQLiteDatabase

/**
 * Created by edgarjanvuicik on 29/11/2017.
 */
class STMSQLIteSchema(val database: SQLiteDatabase) {

    fun createTablesWithModelMapping(modelMapper:STMModelMapper): Map<String, ArrayList<String>>{

        TODO("not implemented")

    }

    fun currentDBScheme(): Map<String, ArrayList<String>>{

        val result = hashMapOf<String, ArrayList<String>>()

        val c = database.rawQuery("SELECT * FROM sqlite_master WHERE type='table' ORDER BY name", null)

        if (c.moveToFirst()) {
            do {

                val nameIndex = c.getColumnIndex("name")

                val tableName = c.getString(nameIndex)

                val cc = database.rawQuery("PRAGMA table_info('$tableName')", null)

                val columns = arrayListOf<String>()

                if (cc.moveToFirst()) {
                    do {

                        val columnNameIndex = cc.getColumnIndex("name")

                        val columnName = cc.getString(columnNameIndex)

                        columns.add(columnName)

                    } while (cc.moveToNext())
                }

                cc.close()

                result[tableName] = columns

            }while (c.moveToNext())
        }

        c.close()

        return result

    }

}