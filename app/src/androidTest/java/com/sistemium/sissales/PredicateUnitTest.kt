package com.sistemium.sissales

import android.support.test.runner.AndroidJUnit4
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.persisting.STMPredicate
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

class PredicateUnitTest:BaseInstrumentaltTest() {

    @Test
    fun testWhereFilter(){

        val data = hashMapOf("name" to hashMapOf("==" to "test"))

        val predicate = STMPredicate.filterPredicate(null, data)

        val predicateString = predicate!!.predicateForModel(modeler!!, "STMOutlet")

        assertEquals("name = 'test'", predicateString)

    }

    @Test
    fun testWhereFilterANY(){

        val entityName = "STMOutlet"

        assertTrue(modeler!!.isConcreteEntityName(entityName))

        val xid = "xid"

        val data = hashMapOf("ANY outletSalesmanContracts"
                to hashMapOf("salesmanId"
                to hashMapOf("=="
                to xid)))

        val predicate = STMPredicate.filterPredicate(null, data)

        val predicateString = predicate!!.predicateForModel(modeler!!, entityName)

        assertEquals("(exists ( select * from OutletSalesmanContract where " +
                "salesmanId = 'xid' " +
                "and outletId = Outlet.id ))", predicateString)

    }

}