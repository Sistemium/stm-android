package com.sistemium.sissales.base.session

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
class STMDefantomizingOperation(var entityName:String, identifier: String):Runnable {

    private val lock = Object()

    override fun run() {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

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