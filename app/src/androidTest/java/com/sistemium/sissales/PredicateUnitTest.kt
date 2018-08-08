package com.sistemium.sissales

import com.sistemium.sissales.persisting.STMPredicate
import org.junit.Assert.assertEquals
import org.junit.Test

class PredicateUnitTest:BaseInstrumentaltTest() {

    fun testWhereFilter(entityName:String, data:Map<*,*>, expect:String?){

        val predicate = STMPredicate.filterPredicate(null, data, entityName)

        val predicateString = predicate?.toString()

        assertEquals(expect, predicateString)
    }

    @Test
    fun predicateTest(){

        testWhereFilter("STMSaleOrder",
                HashMap<Any,Any>(),
                null)

        testWhereFilter("STMOutlet",
                hashMapOf<Any,Any>("name" to
                        hashMapOf("==" to "test")),
                "name = 'test'")

        testWhereFilter("STMVisit",
                hashMapOf(
                        "date" to hashMapOf("==" to "2018-05-16"),
                        "finished" to hashMapOf("==" to 0),
                        "processing" to hashMapOf("==" to "draft")
                ),
                "date = '2018-05-16'")

        testWhereFilter("STMOutlet",
                hashMapOf("ANY outletSalesmanContracts" to
                        hashMapOf("salesmanId" to
                                hashMapOf("==" to "xid"))
                ),
                "exists ( select * from OutletSalesmanContract where salesmanId = 'xid' " +
                        "and outletId = Outlet.id)"
        )

        testWhereFilter("STMStock",
                hashMapOf("volume" to
                        hashMapOf(">" to 0)
                ),
                "volume > 0")

        testWhereFilter("STMContractArticle",
                hashMapOf("discount" to
                        hashMapOf("!=" to 0)
                ),
                "discount <> 0")

        testWhereFilter("STMDebt",
                hashMapOf("responsibility" to
                        hashMapOf("==" to arrayListOf("op", "mvz", "etp"))
                ),
                "responsibility IN ('op', 'mvz', 'etp')")

        testWhereFilter("STMCashing",
                hashMapOf("uncashingId" to
                        hashMapOf("!=" to null)
                ),
                "uncashingId IS NOT NULL")

        testWhereFilter("STMCashing",
                hashMapOf("uncashingId" to
                        hashMapOf("==" to null)
                ),
                "uncashingId IS NULL")

    }

}