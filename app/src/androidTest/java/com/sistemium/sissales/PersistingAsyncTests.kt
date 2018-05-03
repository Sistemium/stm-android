package com.sistemium.sissales

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMFiling
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.persisting.STMPersister
import com.sistemium.sissales.persisting.STMPersisterRunner
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistingAsyncTests {

    companion object {

        var filing: STMFiling? = null
        var persister:STMPersister? = null

        @BeforeClass
        @JvmStatic
        fun setUp(){

            val context = InstrumentationRegistry.getTargetContext()

            filing = STMCoreSessionFiler("test", "test", context)

            val dataModelName = STMCoreAuthController.dataModelName

            val databaseFile = "$dataModelName.db"

            val databasePath = filing!!.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

            val modeler = STMModeller(filing!!.bundledModelJSON(dataModelName))

            val adapter = STMSQLiteDatabaseAdapter(modeler, databasePath)

            val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

            persister = STMPersister(runner)

        }

        @AfterClass
        @JvmStatic
        fun finish(){

            filing?.removeOrgDirectory()

        }

    }

    @Test
    fun testErrors(){

        val entityName = "UnknownEntity"

        expectError(persister!!.findAll(entityName, null, null))

        expectError(persister!!.find(entityName, entityName, null))

        expectError(persister!!.destroy(entityName, entityName, null))

        expectError(persister!!.merge(entityName, hashMapOf<Any,Any>(), null))

        expectError(persister!!.mergeMany(entityName, arrayListOf<Any>(""), null))

    }

    private fun expectError(promise: Promise<Any?,Exception>){

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
            lock.wait(2000000)
        }

        assertTrue(error)


    }

}