package com.sistemium.sissales

import android.support.test.runner.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistingAsyncTests:BaseInstrumentaltTest() {

    @Test
    fun testErrors(){

        val entityName = "UnknownEntity"

        expectError(persister!!.findAll(entityName, null, null))

        expectError(persister!!.find(entityName, entityName, null))

        expectError(persister!!.destroy(entityName, entityName, null))

        expectError(persister!!.merge(entityName, hashMapOf<Any,Any>(), null))

        expectError(persister!!.mergeMany(entityName, arrayListOf<Any>(""), null))

    }

}