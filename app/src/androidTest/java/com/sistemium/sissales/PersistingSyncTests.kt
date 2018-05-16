package com.sistemium.sissales

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionGroupBy
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.persisting.STMPredicate
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class PersistingSyncTests:BaseInstrumentaltTest() {

    fun createTestDataOwnerXid(xid:String, type:String): ArrayList<Map<*,*>>{

        val entityName = "STMLogMessage"

        val testDataA = hashMapOf(
                "type" to type,
                "ownerXid" to xid,
                "text" to "a"
        )

        val testDataZ = hashMapOf(
                "type" to type,
                "ownerXid" to xid,
                "text" to "a"
        )

        return persister!!.mergeManySync(entityName, arrayListOf(testDataA, testDataZ), null)

    }

    fun destroyTestDataOwnerXid(xid:String):Int{

        val entityName = "STMLogMessage"

        val predicate = STMPredicate("ownerXid = '$xid'")

        return persister!!.destroyAllSync(entityName, predicate,
                hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))

    }

    @Test
    fun testOrderBy(){

        val entityName = "STMLogMessage"

        val xid = "xid"

        val predicate = STMPredicate("ownerXid = '$xid'")

        val testData = createTestDataOwnerXid(xid, "debug")
        val testDataA = testData.first()
        val testDataZ = testData.last()

        val key = "text"

        var result = persister!!.findAllSync(entityName, predicate, hashMapOf
                (STMConstants.STMPersistingOptionOrderDirection to
                STMConstants.STMPersistingOptionOrderDirectionAscValue,
                        STMConstants. STMPersistingOptionOrder to "type,text"))

        assertEquals(2, result.size)

        assertEquals(testDataA[key], result.first()[key])

        result = persister!!.findAllSync(entityName, predicate, hashMapOf
        (STMConstants.STMPersistingOptionOrderDirection to
                STMConstants.STMPersistingOptionOrderDirectionDescValue,
                STMConstants. STMPersistingOptionOrder to "type,text"))

        assertEquals(2, result.size)

        assertEquals(testDataZ[key], result.first()[key])

        result = persister!!.findAllSync(entityName, predicate, hashMapOf
        (STMConstants.STMPersistingOptionOrderDirection to
                STMConstants.STMPersistingOptionOrderDirectionAscValue,
                STMConstants. STMPersistingOptionOrder to "text"))

        assertEquals(2, result.size)

        assertEquals(testDataA[key], result.first()[key])

        result = persister!!.findAllSync(entityName, predicate, hashMapOf
        (STMConstants.STMPersistingOptionOrderDirection to
                STMConstants.STMPersistingOptionOrderDirectionDescValue,
                STMConstants. STMPersistingOptionOrder to "type,text"))

        assertEquals(2, result.size)

        assertEquals(testDataZ[key], result.first()[key])

        assertEquals(2, destroyTestDataOwnerXid(xid))

    }

    @Test
    fun testGroupBy(){

        val entityName = "STMVisit"

        val today = STMFunctions.stringFrom(Date())

        val cal = Calendar.getInstance()

        cal.add(Calendar.DATE, -1)

        val yesterday = STMFunctions.stringFrom(cal.time)

        val sample = sampleDataOf(entityName, 10){

            hashMapOf("date" to if (it % 2 == 0) today else yesterday)

        }

        persister!!.mergeManySync(entityName, sample, null)

        val predicate = STMPredicate("ownerXid = '${sample.first()["ownerXid"]}'")

        val options = hashMapOf(STMPersistingOptionGroupBy to arrayListOf("date", "ownerXid"))

        val result = persister!!.findAllSync(entityName, predicate, options)

        assertEquals(2, result.size)
        assertEquals(5, result.first()["count()"])
        assertEquals(5, result.last()["count()"])

    }

}