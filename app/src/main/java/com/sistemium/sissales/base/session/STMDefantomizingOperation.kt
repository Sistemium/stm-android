package com.sistemium.sissales.base.session
import com.sistemium.sissales.interfaces.STMDefantomizingOwner

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
class STMDefantomizingOperation(var entityName:String, var identifier: String, var defantomizingOwner:STMDefantomizingOwner):Runnable {

    private val lock = Object()

    override fun run() {

        defantomizingOwner.defantomizeEntityName(entityName, identifier)

        synchronized(lock){

            lock.wait()

        }
    }

    fun finish(){

        synchronized(lock){

            lock.notify()

        }

    }

}