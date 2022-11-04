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
    override fun getCachedEngineId(): String {
        return "my flutter engine"
    }
}
