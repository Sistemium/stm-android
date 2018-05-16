package com.sistemium.sissales

import android.support.test.runner.AndroidJUnit4
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.persisting.STMPredicate
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.runner.RunWith
import java.util.*
import kotlin.collections.ArrayList

class PersistingSpeedTest:BaseInstrumentaltTest() {

    @Test
    fun testMergeSyncSpeed(){

        val entityName = "STMLogMessage"

        val numberOfLogs = 100

        val options = hashMapOf(STMConstants.STMPersistingOptionReturnSaved to false)

        measureBlock("testMergeSyncSpeed"){

            for (i in 1..numberOfLogs) {

                val messageText = "Log message test #$i"

                val logMessage = hashMapOf(
                        "text" to "${STMFunctions.stringFrom(Date())}: $messageText",
                        "type" to "debug",
                        "ownerXid" to ownerXid
                )

                expectSuccess(persister!!.merge(entityName, logMessage, options))

            }

        }

    }

    @Test
    fun testFindFromLargeData(){

        val entityName = "LogMessage"

        val numberOfPages = 10
        val pageSize = 10000

        val totalItems = pageSize * numberOfPages

        val options = hashMapOf(STMConstants.STMPersistingOptionReturnSaved to false)

        val startedAt = Date()

        for (i in 1..numberOfPages){

            expectSuccess(persister!!.mergeMany(entityName, sampleDataOf(entityName, pageSize), options))

            STMFunctions.debugLog("STMPersistingSpeedTest","testFindFromLargeData created page $i of $numberOfPages")

        }

        STMFunctions.debugLog("", "")

        STMFunctions.debugLog("STMPersistingSpeedTest", "testFindFromLargeData created $totalItems of $entityName in ${(Date().time -startedAt.time)/1000.0} seconds")

        val endsWith0 = STMPredicate("text LIKE '%00'")

        measureBlock("testFindFromLargeData"){

            val rez = persister!!.findAllSync(entityName, endsWith0, null)

            assertEquals(totalItems.div(100), rez.size)

        }

    }

}