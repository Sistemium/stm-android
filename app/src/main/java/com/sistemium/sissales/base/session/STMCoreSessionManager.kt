package com.sistemium.sissales.base.session

import android.util.Log
import com.sistemium.sissales.interfaces.STMSession
import com.sistemium.sissales.interfaces.STMSessionManager

/**
 * Created by edgarjanvuicik on 08/02/2018.
 */
class STMCoreSessionManager private constructor():STMSessionManager {

    private object Holder { val INSTANCE = STMCoreSessionManager() }

    companion object {
        val sharedManager: STMCoreSessionManager by lazy { Holder.INSTANCE }
    }

    var currentSession:STMSession? = null
        get() = sessions[currentSessionUID]

    private val sessions = hashMapOf<String, STMCoreSession>()

    private var currentSessionUID:String? = null

    fun startSession(trackers:ArrayList<String>): STMSession?{

        val uid = STMCoreAuthController.userID

        if (uid == null){

            Log.d("STMCoreSessionManager","no uid")

            return null

        }

        var session = sessions[uid]

        if (session != null) {

            session.stopSession()
            session.dismissSession()

        }

        val core = STMCoreSession(trackers)

        session = core

        session.manager = this
        sessions[uid] = session

        currentSessionUID = uid

        return session

    }

}