package com.sistemium.sissales.activities
import androidx.annotation.NonNull
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePicturesController
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel


class AuthActivity : FlutterActivity() {
    private val CHANNEL = "com.sistemium.flutterchanel"

    override fun getCachedEngineId(): String {
        return "my flutter engine"
    }

    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(MyApplication.flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler {
            call, result ->
            if (call.method == "startSyncer"){
                STMSession.sharedSession!!.setupSyncer()
            }
            if (call.method == "completeAuth"){
                val arguments = call.arguments as HashMap<*, *>
                STMCoreAuthController.userName = arguments["userName"] as? String
                STMCoreAuthController.phoneNumber = arguments["phoneNumber"] as? String
                STMCoreAuthController.accessToken = arguments["accessToken"] as? String
                STMCoreAuthController.userID = arguments["userID"] as? String
                STMCoreAuthController.entityResource = arguments["redirectUri"] as? String
                STMCoreAuthController.socketURL = arguments["socketURL"] as? String
                STMCoreAuthController.accountOrg = arguments["accountOrg"] as? String
                STMCoreAuthController.iSisDB = arguments["iSisDB"] as? String
                STMCoreAuthController.stcTabs = arguments["stcTabs"] as? ArrayList<*>
                STMCoreAuthController.rolesResponse = arguments["rolesResponse"] as? Map<*,*>
            }
            if (call.method == "logout"){
                STMCoreAuthController.logout()
            }
            if (call.method == "syncData"){
                STMSession.sharedSession?.syncer?.receiveData()
            }
            if (call.method == "startImageDownload"){
                STMCorePicturesController.sharedInstance?.checkNotUploadedPhotos()
            }
            if (call.method == "stopImageDownload"){
            }

//            if ([call.method isEqual: @"loadURL"]){
//            NSDictionary * arguments = [call arguments];
//            STMStoryboard *storyboard = [STMStoryboard storyboardWithName:@"STMWKWebView" bundle:nil];
//            storyboard.parameters = arguments;
//            self.window.rootViewController = [storyboard instantiateInitialViewController];
//        }
        }
    }

}
