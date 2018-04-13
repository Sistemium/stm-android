package com.sistemium.sissales.base.session

import android.util.Log
import com.sistemium.sissales.enums.STMSessionStatus
import com.sistemium.sissales.interfaces.STMSession
import com.sistemium.sissales.interfaces.STMSessionManager
import java.util.*

/**
 * Created by edgarjanvuicik on 08/02/2018.
 */
class STMCoreSessionManager private constructor() : STMSessionManager {

    private object Holder {
        val INSTANCE = STMCoreSessionManager()
    }

    companion object {
        val sharedManager: STMCoreSessionManager by lazy { Holder.INSTANCE }
    }

    var currentSession: STMSession? = null
        get() = sessions[currentSessionUID]

    private val sessions = hashMapOf<String, STMCoreSession>()

    private var currentSessionUID: String? = null

    override fun sessionStopped(session: STMSession) {

//        if (session.status == STMSessionRemoving || session.status == STMSessionFinishing || session.status == STMSessionStopped) {
//            session.status = STMSessionStopped;
//            [self removeSessionForUID:session.uid];
//        } else {
//            [self removeSessionForUID:session.uid];
//        }

        if (session.status == STMSessionStatus.STMSessionRemoving || session.status == STMSessionStatus.STMSessionFinishing){

            session.status = STMSessionStatus.STMSessionStopped

        }

        removeSessionForUID(session.uid)

    }

    fun startSession(trackers: ArrayList<String>): STMSession? {

        val uid = STMCoreAuthController.userID

        if (uid == null) {

            Log.d("STMCoreSessionManager", "no uid")

            return null

        }

        var session = sessions[uid]

        if (session != null) {

            session.stopSession()

        }

        val core = STMCoreSession(trackers)

        session = core

        session.manager = this
        sessions[uid] = session

        currentSessionUID = uid

        return session

    }

    fun stopSessionForUID(uid:String?){

        if (uid != null){

            val session = sessions[uid]
            if (session?.status == STMSessionStatus.STMSessionRunning || session?.status == STMSessionStatus.STMSessionRemoving) {

                if (currentSessionUID == uid) {

                    currentSessionUID = null

                }

                session.stopSession()

            }

        }

    }

    private fun removeSessionForUID(uid:String){

        val session = sessions[uid]

        if (session?.status == STMSessionStatus.STMSessionStopped) {

            sessions.remove(uid)

        } else {

            session?.status = STMSessionStatus.STMSessionRemoving
            stopSessionForUID(uid)

        }

    }

}