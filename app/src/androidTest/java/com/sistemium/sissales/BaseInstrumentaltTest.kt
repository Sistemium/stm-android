package com.sistemium.sissales

import android.support.test.InstrumentationRegistry
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMFiling
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.persisting.STMPersister
import com.sistemium.sissales.persisting.STMPersisterRunner
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import org.junit.After
import org.junit.Assert
import org.junit.Before
import java.util.*

open class BaseInstrumentaltTest {

    var filing: STMFiling? = null
    var persister: STMPersister? = null
    var ownerXid:String? = null
    var modeler:STMModeller? = null

    @Before
    fun setUp(){

        ownerXid = STMFunctions.uuidString()

        val context = InstrumentationRegistry.getTargetContext()

        filing = STMCoreSessionFiler("test", "test", context)

        val dataModelName = STMCoreAuthController.dataModelName

        val databaseFile = "$dataModelName.db"

        val databasePath = filing!!.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

        modeler = STMModeller(filing!!.bundledModelJSON(dataModelName))

        val adapter = STMSQLiteDatabaseAdapter(modeler!!, databasePath)

        val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

        persister = STMPersister(runner)

    }

    @After
    fun finish(){

        filing?.removeOrgDirectory()

    }

    fun expectSuccess(promise: Promise<Any?, Exception>){

        var error = true
        val lock = Object()

        promise.then {

            synchronized(lock) {

                error = false

                lock.notify()

            }

        }.fail {

            synchronized(lock) {
                lock.notify()

            }

        }


        synchronized (lock) {
            lock.wait()
        }

        Assert.assertFalse(error)

    }

    fun measureBlock(name:String, block:()->Unit){

        var summ = 0.0

        for (i in 1..10){

            val startedAt = Date()

            block()

            val end = (Date().time -startedAt.time)/1000.0

            summ += end

            STMFunctions.debugLog("Performance", "$name finished in $end seconds")

        }

        STMFunctions.debugLog("", "")

        STMFunctions.debugLog("Performance", "Average: ${summ/10.0} seconds")

    }

    fun expectError(promise: Promise<Any?,Exception>){

        var error = false
        val lock = Object()

        promise.then {

            synchronized(lock) {

                lock.notify()

            }

        }.fail {

            synchronized(lock) {

                error = true
                lock.notify()

            }

        }


        synchronized (lock) {
            lock.wait()
        }

        Assert.assertTrue(error)


    }

    fun sampleDataOf(entityName:String, count:Int, addArguments:((i:Int)->(Map<String,String>))? = null):ArrayList<Map<*,*>>{

        val result = arrayListOf<Map<*,*>>()

        val now = STMFunctions.stringFrom(Date())

        for (i in 1..count){

            val name = "$entityName at $now - $i"

            val item = hashMapOf(
                    "ownerXid" to ownerXid,
                    "name" to name,
                    "text" to name,
                    "type" to "debug"
            )

            val arg = addArguments?.invoke(i)

            if (arg != null){

                item.putAll(arg)
            }

            result.add(item)

        }

        return result

    }

}