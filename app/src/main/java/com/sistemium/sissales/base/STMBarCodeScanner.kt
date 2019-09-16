package com.sistemium.sissales.base

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.sistemium.sissales.R
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.SDKHandler
import java.util.*
import kotlin.collections.ArrayList
import kotlin.experimental.and



class STMBarCodeScanner {

    val api = SDKHandler(MyApplication.appContext)

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

    var isDeviceConnected = false

    fun startBarcodeScanning(activity:Activity){

        if (isDeviceConnected) return

        showPairingAlertInViewController(activity)

    }

    private fun showPairingAlertInViewController(activity:Activity){

        val title = R.string.zebra_pairing
        val description = R.string.zebra_pairing_description
        val btAddress = randomBTAddress()

        STMFunctions.debugLog("STMBarCodeScanner", "Connection using BTAddress: $btAddress")
        api.dcssdkSetBTAddress(btAddress)
        val activeList:List<DCSScannerInfo> = ArrayList()
        api.dcssdkGetActiveScannersList(activeList)

        STMFunctions.debugLog("STMBarCodeScanner", activeList.toString())

        for (active in activeList){

            api.dcssdkTerminateCommunicationSession(active.scannerID)

        }

//        val barcode = api.dcssdkGetPairingBarcode(DCSSDKDefs.DCSSDK_BT_PROTOCOL.CRD_BT, DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT)

        activity.runOnUiThread {
            AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(description)
                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setView(barcode)
                    .setNegativeButton(android.R.string.no, null)
                    .show().window?.setLayout(600, 400)
        }
//        PMAlertController *alert = [[PMAlertController alloc] initWithTitle:title
//                description:description
//        image:barcode
//        style:PMAlertControllerStyleAlert];
//        [alert addAction:[[PMAlertAction alloc] initWithTitle:NSLocalizedString(@"CANCEL", nil)
//        style:PMAlertActionStyleCancel
//        action:^() {
//            NSLog(@"OK action");
//            [self.api sbtEnableAvailableScannersDetection:NO];
//        }]];
//        self.pairingAlert = alert;
//        [viewController presentViewController:alert animated:NO completion:^{
//            NSLog(@"Presented");
//            [self.api sbtEnableAvailableScannersDetection:YES];
//        }];

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

}