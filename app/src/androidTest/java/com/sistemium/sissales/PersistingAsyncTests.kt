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

        val lock = Object()
        var error:Boolean? = null

        val entityName = "UnknownEntity"
        persister!!.findAll(entityName, null, null).then {

            synchronized(lock) {

                error = false
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

        assertTrue(error!!)


//        completionHandler:PATExpectArrayError(findAllAsync)];
//        PATExpectation(destroyAllAsync)
//        [self.persister destroyAllAsync:entityName
//                predicate:nil
//        options:nil
//        completionHandler:PATExpectIntegerError(destroyAllAsync)];
//        PATExpectation(findAsync)
//        [self.persister findAsync:entityName
//                identifier:entityName
//        options:nil
//        completionHandler:PATExpectDictionaryError(findAsync)];
//        PATExpectation(destroyAsync)
//        [self.persister destroyAsync:entityName
//                identifier:entityName
//        options:nil
//        completionHandler:PATExpectError(destroyAsync)];
//        PATExpectation(mergeAsync)
//        [self.persister mergeAsync:entityName
//                attributes:@{}
//        options:nil
//        completionHandler:PATExpectDictionaryError(mergeAsync)];
//        PATExpectation(mergeManyAsync)
//        [self.persister mergeManyAsync:entityName
//                attributeArray:@[@{}]
//        options:nil
//        completionHandler:PATExpectArrayError(mergeManyAsync)];
//        PATExpectation(updateAsync)
//        [self.persister updateAsync:entityName
//                attributes:@{}
//        options:nil
//        completionHandler:PATExpectDictionaryError(updateAsync)];
//        PATExpectation(updateAsyncNoCallback)
//        [self.persister updateAsync:entityName
//                attributes:@{}
//        options:nil
//        completionHandler:nil];
//        [updateAsyncNoCallback fulfill];
//        if (self.fakePersistingOptions) {
//            PATExpectation(updateAsyncNoName)
//            [self.persister updateAsync:nil
//                    attributes:@{}
//            options:nil
//            completionHandler:^(STMP_ASYNC_DICTIONARY_RESULT_CALLBACK_ARGS) {
//                PATExpectErrorBody(updateAsyncNoName)
//                XCTAssertEqualObjects(error.localizedDescription, @"Entity name can not be null");
//            }];
//        }
//        [self waitForExpectationsWithTimeout:1 handler:nil];

    }

}