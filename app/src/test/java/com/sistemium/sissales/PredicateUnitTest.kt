package com.sistemium.sissales

import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.persisting.STMPredicate
import org.junit.Assert.assertEquals
import org.junit.BeforeClass
import org.junit.Test

class PredicateUnitTest {

    companion object {

        private var testModeler:STMModelling? = null

        @BeforeClass
        @JvmStatic
        fun setUp(){

            val modelJSOM = "{ \"model\": {\"entity\":[" +
                    "{\"name\": \"STMOutlet\", " +
                    "\"attribute\": [{\"name\": \"name\", \"attributeType\": \"String\"}, " +
                    "{\"name\": \"ts\", \"attributeType\": \"Date\"}, {\"name\": \"size\", " +
                    "\"attributeType\": \"Integer 64\"}]," +
                    " \"relationship\": [{\"name\": \"partner\", " +
                    "\"destinationEntity\": \"STMPartner\", \"inverseName\": \"outlets\"," +
                    "\"inverseEntity\": \"STMPartner\"}]}, " +
                    "{\"name\": \"STMPartner\", " +
                    "\"attribute\": [{\"name\": \"name\", \"attributeType\": \"String\"}, " +
                    "{\"name\": \"ts\", \"attributeType\": \"Date\"}, " +
                    "{\"name\": \"size\", \"attributeType\": \"Integer 64\"}], " +
                    "\"relationship\": [{\"name\": \"outlets\", \"toMany\": \"YES\", " +
                    "\"destinationEntity\": \"STMOutlet\", \"inverseName\": \"partner\", " +
                    "\"inverseEntity\": \"STMOutlet\"}]}]} }"

            testModeler = STMModeller(modelJSOM)

        }
    }

    @Test
    fun testSQLFiltersSubqueries(){

//        //outlet.partnerId == xid
//        var data = hashMapOf("partnerId" to hashMapOf("==" to "xid"))
//
//        var predicate = STMPredicate.filterPredicate(null, data)
//
//        var predicateString = predicate!!.predicateForModel(testModeler!!, "STMOutlet")
//
//        assertEquals("(exists ( select * from Outlet where [partnerId] = 'xid' and id = outletId ))", predicateString)
//
//        //ANY outlets.partner.id == xid
//
//        data = hashMapOf("ANY outlets.partner.id" to hashMapOf("==" to "xid"))
//
//        predicate = STMPredicate.filterPredicate(null, data)
//
//        predicateString = predicate!!.predicateForModel(testModeler!!, "STMOutlet")
//
//        assertEquals("(exists ( select * from Outlet where partnerId = 'xid' and ?uncapitalizedTableName?Id = ?capitalizedTableName?.id ))", predicateString)

    }
}