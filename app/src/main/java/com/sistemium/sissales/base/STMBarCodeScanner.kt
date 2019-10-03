package com.sistemium.sissales.base

import android.app.Activity
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and
import android.app.Dialog
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import android.graphics.Point
import android.widget.LinearLayout
import com.sistemium.sissales.R
import com.zebra.scannercontrol.*
import com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_BEEPER_VOLUME
import com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_BEEPER_VOLUME_LOW
import android.speech.tts.TextToSpeech
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.webInterface.WebAppInterface
import android.os.Looper
import android.os.Handler
import android.support.v4.os.ConfigurationCompat
import com.sistemium.sissales.base.session.STMSession

enum class STMBarCodeScannedType(val type:String) {
    STMBarCodeTypeUnknow(""),
    STMBarCodeTypeArticle("Article"),
    STMBarCodeTypeExciseStamp("ExciseStamp"),
    STMBarCodeTypeStockBatch("StockBatch")
}

class STMBarCodeScanner:IDcsSdkApiDelegate {

    val api = SDKHandler(MyApplication.appContext)

    private var connectedId = 0
    private var dialogFwReconnectScanner: Dialog? = null
    private val barCodeTypes by lazy {
        STMSession.sharedSession!!.persistenceDelegate.findAllSync("STMBarCodeType", null, null)
    }

    companion object {
        private var INSTANCE:STMBarCodeScanner? = null

        var sharedScanner: STMBarCodeScanner?
            get() {

                if (INSTANCE == null){

                    INSTANCE = STMBarCodeScanner()

                }

                return INSTANCE!!

            }
            set(value) {

                INSTANCE = value

            }
    }

    init {

        api.dcssdkSetDelegate(this)
        api.dcssdkSubsribeForEvents(
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
                        or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value
                        or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
                        or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value
                        or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
        )

        api.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)

        api.dcssdkEnableAvailableScannersDetection(true)
    }

    var isDeviceConnected = false

    fun startBarcodeScanning(activity:Activity){

        if (isDeviceConnected) return

        showPairingAlertInViewController(activity)

    }

    private fun showPairingAlertInViewController(activity:Activity){

        val btAddress = randomBTAddress()

        STMFunctions.debugLog("STMBarCodeScanner", "Connection using BTAddress: $btAddress")
        api.dcssdkSetBTAddress(btAddress)
        val activeList:List<DCSScannerInfo> = ArrayList()
        api.dcssdkGetActiveScannersList(activeList)

        STMFunctions.debugLog("STMBarCodeScanner", activeList.toString())

        for (active in activeList){

            api.dcssdkTerminateCommunicationSession(active.scannerID)

        }

        val barcode = api.dcssdkGetPairingBarcode(DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE, DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT)

        dialogFwReconnectScanner = Dialog(activity)
        dialogFwReconnectScanner?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogFwReconnectScanner?.setContentView(R.layout.dialog_fw_reconnect_scanner)

        val cancelButton = dialogFwReconnectScanner?.findViewById(R.id.btn_cancel) as TextView

        cancelButton.setOnClickListener {

            dialogFwReconnectScanner?.dismiss()


            dialogFwReconnectScanner = null
        }

        val llBarcode: FrameLayout = dialogFwReconnectScanner?.findViewById(R.id.scan_to_connect_barcode) as FrameLayout

        val layoutParams = LinearLayout.LayoutParams(-1, -1)
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val x = width * 9 / 10
        val y = x / 3
        barcode.setSize(x, y)

        llBarcode.addView(barcode, layoutParams)

        dialogFwReconnectScanner?.setCancelable(false)
        dialogFwReconnectScanner?.setCanceledOnTouchOutside(false)
        dialogFwReconnectScanner?.setOnCancelListener {
            api.dcssdkEnableAvailableScannersDetection(false)
        }
        dialogFwReconnectScanner?.show()
        api.dcssdkEnableAvailableScannersDetection(true)
        val window = dialogFwReconnectScanner?.window
        val scale = activity.resources.displayMetrics.density
        window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, (300 * scale + 0.5f).toInt())


    }

    fun randomBTAddress():String{

        val rand = Random()
        val macAddr = ByteArray(6)
        rand.nextBytes(macAddr)

        macAddr[0] = (macAddr[0] and 254.toByte())  //zeroing last 2 bytes to make it unicast and locally adminstrated

        val sb = StringBuilder(18)
        for (b in macAddr) {

            if (sb.isNotEmpty())
                sb.append(":")

            sb.append(String.format("%02x", b))
        }


        return sb.toString()

    }

    fun disconnect(){

        api.dcssdkEnableAutomaticSessionReestablishment(false, connectedId)
        api.dcssdkTerminateCommunicationSession(connectedId)

    }

    private fun barcodeTypeFromTypesDics(types:ArrayList<Map<*,*>>, barcode:String):STMBarCodeScannedType{

        var matchedType:STMBarCodeScannedType = STMBarCodeScannedType.STMBarCodeTypeUnknow

        main@ for (barCodeType in types) {

            val mask = barCodeType["mask"] as? String ?: continue

            val numberOfMatches = mask.toRegex(RegexOption.IGNORE_CASE).findAll(barcode).count()

            if (numberOfMatches > 0){

                val type = barCodeType["type"] as? String

                for (barcodeType in STMBarCodeScannedType.values()){

                    if (barcodeType.type == type){

                        matchedType = barcodeType

                        break@main

                    }

                }

            }

        }

        return matchedType

    }

    private fun applySettingsToScanner(scannerId:Int){

        val xmlInput = "<inArgs><scannerID>$scannerId</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>$RMD_ATTR_BEEPER_VOLUME</id><datatype>B</datatype><value>$RMD_ATTR_VALUE_BEEPER_VOLUME_LOW</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>"

        STMFunctions.debugLog("STMBarCodeScanner", "Sending beeper command to scannerId: $scannerId")

        val out = java.lang.StringBuilder()

        val result = api.dcssdkExecuteCommandOpCodeInXMLForScanner(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, xmlInput, out, scannerId)

        if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS){

            STMFunctions.debugLog("STMBarCodeScanner", "Successfully updated beeper settings for scanner ID $scannerId")

        }else{

            STMFunctions.debugLog("STMBarCodeScanner", "Failed to update beeper settings from scanner ID $scannerId")

        }

    }

    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dcssdkEventCommunicationSessionTerminated(p0: Int) {

        STMFunctions.debugLog("STMBarCodeScanner", "dcssdkEventCommunicationSessionTerminated scannerId: $p0")

        isDeviceConnected = false

        var tts:TextToSpeech? = null

        tts = TextToSpeech(MyApplication.appContext){

            if (it == TextToSpeech.SUCCESS) {

                tts!!.language = ConfigurationCompat.getLocales(MyApplication.appContext!!.resources.configuration)[0]

                tts!!.speak(MyApplication.appContext!!.getString(R.string.scanner_removal), TextToSpeech.QUEUE_ADD, null, null)

            }
        }

        WebViewActivity.webInterface!!.scannerDisconnected()

        api.dcssdkEnableAvailableScannersDetection(true)

    }

    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) {

        val status = if (p0!!.isActive) {"active"} else {"available"}

        val scannerId = p0.scannerID

        STMFunctions.debugLog("STMBarCodeScanner", "Scanner is $status: scannerId: $scannerId name: ${p0.scannerName}")

    }

    override fun dcssdkEventCommunicationSessionEstablished(p0: DCSScannerInfo?) {

        val scannerId = p0!!.scannerID

        connectedId = scannerId

        isDeviceConnected = true

        val result = api.dcssdkEnableAutomaticSessionReestablishment(true, scannerId)

        if (result != DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {

            STMFunctions.debugLog("STMBarCodeScanner", "Automatic Session Reestablishment for scannerId %$scannerId could not be set")
            return

        }

        STMFunctions.debugLog("STMBarCodeScanner", "Automatic Session Reestablishment for scannerId $scannerId has been set successfully")

        api.dcssdkEnableAvailableScannersDetection(false)

        dialogFwReconnectScanner!!.dismiss()

        applySettingsToScanner(scannerId)

        var tts:TextToSpeech? = null

        tts = TextToSpeech(MyApplication.appContext){

            if (it == TextToSpeech.SUCCESS) {

                tts!!.language = ConfigurationCompat.getLocales(MyApplication.appContext!!.resources.configuration)[0]

                tts!!.speak(MyApplication.appContext!!.getString(R.string.scanner_arrival), TextToSpeech.QUEUE_ADD, null, null)

            }
        }

        WebViewActivity.webInterface!!.scannerConnected()

    }

    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun dcssdkEventBarcode(p0: ByteArray?, p1: Int, p2: Int) {

        val barcode = String(p0!!)

        STMFunctions.debugLog("STMBarCodeScanner", "Got barcode: '$barcode' from scannerId: $p2")

        val type = barcodeTypeFromTypesDics(barCodeTypes, barcode)

        WebViewActivity.webInterface!!.receiveBarCode(barcode, type)

        //        STMBarCodeScan *barCodeScan = [[STMBarCodeScan alloc] init];
//        barCodeScan.code = barcode;
//        [self.delegate barCodeScanner:self receiveBarCodeScan:barCodeScan withType:type];


    }

    override fun dcssdkEventScannerDisappeared(p0: Int) {
        STMFunctions.debugLog("STMBarCodeScanner","dcssdkEventScannerDisappeared scannerId: $p0")
    }

}