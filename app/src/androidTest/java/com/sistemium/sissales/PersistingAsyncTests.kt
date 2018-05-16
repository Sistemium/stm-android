package com.sistemium.sissales

import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

class PersistingAsyncTests:BaseInstrumentaltTest() {

    @Test
    fun testErrors(){

        val entityName = "UnknownEntity"

        expectError(persister!!.findAll(entityName, null, null))

        expectError(persister!!.find(entityName, entityName, null))

        expectError(persister!!.destroy(entityName, entityName, null))

        expectError(persister!!.merge(entityName, hashMapOf<Any,Any>(), null))

        expectError(persister!!.mergeMany(entityName, arrayListOf<Any>(hashMapOf<Any, Any>()), null))

    }

    @Test
    fun testSuccess(){

        val entityName = "STMLogMessage"

        expectSuccess(persister!!.findAll(entityName, null, null))

        expectSuccess(persister!!.find(entityName, entityName, null))

        expectSuccess(persister!!.destroy(entityName, entityName, null))

        expectSuccess(persister!!.merge(entityName, hashMapOf<Any,Any>(), null))

        expectSuccess(persister!!.mergeMany(entityName, arrayListOf<Any>(hashMapOf<Any, Any>()), null))

    }

}