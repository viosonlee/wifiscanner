package com.laiyifen.lyfwifiscanner

import android.Manifest
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.ScanResult
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.DialogCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.laiyifen.lyfwifiscanner.databinding.ActivityMainBinding
import com.wifimanagerwrapper.WifiManagerWrapper
import com.wifimanagerwrapper.WifiScanCallbackResult

/**
 *Author:viosonlee
 *Date:2023/1/6
 *DESCRIPTION:
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var wifiManagerWrapper: WifiManagerWrapper

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION

    private lateinit var locationPermissionRequester: ActivityResultLauncher<String>

    private val data = arrayListOf<ScanResult>()

    private val resultAdapter by lazy { ResultAdapter(this, data) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationPermissionRequester =
            activityResultRegistry.register("requestLocationPermission", this, ActivityResultContracts.RequestPermission()) {
                if (it) {
                    startScan()
                } else {
                    Toast.makeText(this@MainActivity, "????????????????????????", Toast.LENGTH_LONG).show()
                }
            }

        //initWifiManager
        wifiManagerWrapper = WifiManagerWrapper().wifiManagerInti(this)
        //????????????
        binding.startScanButton.setOnClickListener {
            val hasLocationPermission = ActivityCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED
            if (hasLocationPermission) {
                startScan()
            } else {
                locationPermissionRequester.launch(locationPermission)
            }
        }
        //????????????
        binding.copyScanResult.setOnClickListener {
            if (data.isNotEmpty()) {
                val sb = StringBuffer()
                val list = arrayListOf<Map<String, String>>()
                data.forEach {
                    list.add(
                        mapOf(
                            "wifi??????" to it.SSID,
                            "wifiMac??????" to it.BSSID
                        )
                    )
                }
                val json = Gson().toJson(list)
                copyClipBoard(json)

            } else {
                Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stopScanButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("??????")
                .setMessage("?????????????????????????????????")
                .setPositiveButton("?????????") { _, _ -> finish() }
                .setNegativeButton("??????", null)
                .show()
        }

        //init list
        with(binding.record) {
            layoutManager = LinearLayoutManager(this@MainActivity)
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
            adapter = resultAdapter
        }

    }

    /**
     * start Scan
     */
    private fun startScan() {
        wifiManagerWrapper.autoWifiScanner(object : WifiScanCallbackResult {
            override fun wifiFailureResult(results: MutableList<ScanResult>) {
                addNewResult(results)
            }

            override fun wifiSuccessResult(results: List<ScanResult>) {
                addNewResult(results)
            }
        })
    }

    private fun addNewResult(results: List<ScanResult>) {
        val newResult = results.filter { res -> data.find { it.BSSID == res.BSSID } == null }
        val startIndex = if (data.isEmpty()) 0 else data.size - 1
        val itemCount = newResult.size
        data.addAll(newResult)
        resultAdapter.notifyItemRangeChanged(startIndex, itemCount)
        //?????????????????????
        val size = data.size - 1
        if (size > 0)
            binding.record.smoothScrollToPosition(size)
        Toast.makeText(this, "?????????${itemCount}??????wifi??????", Toast.LENGTH_SHORT).show()
    }

    /**
     * ??????????????????
     * @param content
     */
    private fun copyClipBoard(content: CharSequence?) {
        try {
            if (TextUtils.isEmpty(content)) return
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            if (cm != null) {
                val mClipData = ClipData.newPlainText("Label", content)
                cm.setPrimaryClip(mClipData)
                Toast.makeText(this, "????????????????????????????????????", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}