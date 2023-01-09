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
                    Toast.makeText(this@MainActivity, "请先开启定位权限", Toast.LENGTH_LONG).show()
                }
            }

        //initWifiManager
        wifiManagerWrapper = WifiManagerWrapper().wifiManagerInti(this)
        //开始扫描
        binding.startScanButton.setOnClickListener {
            val hasLocationPermission = ActivityCompat.checkSelfPermission(this, locationPermission) == PackageManager.PERMISSION_GRANTED
            if (hasLocationPermission) {
                startScan()
            } else {
                locationPermissionRequester.launch(locationPermission)
            }
        }
        //导出数据
        binding.copyScanResult.setOnClickListener {
            if (data.isNotEmpty()) {
                val sb = StringBuffer()
                val list = arrayListOf<Map<String, String>>()
                data.forEach {
                    list.add(
                        mapOf(
                            "wifi名称" to it.SSID,
                            "wifiMac地址" to it.BSSID
                        )
                    )
                }
                val json = Gson().toJson(list)
                copyClipBoard(json)

            } else {
                Toast.makeText(this, "列表为空", Toast.LENGTH_SHORT).show()
            }
        }

        binding.stopScanButton.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("注意")
                .setMessage("退出后数据将不会被保留")
                .setPositiveButton("知道了") { _, _ -> finish() }
                .setNegativeButton("取消", null)
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
        //保持在最后一个
        val size = data.size - 1
        if (size > 0)
            binding.record.smoothScrollToPosition(size)
        Toast.makeText(this, "扫描到${itemCount}个新wifi信号", Toast.LENGTH_SHORT).show()
    }

    /**
     * 复制到剪切板
     * @param content
     */
    private fun copyClipBoard(content: CharSequence?) {
        try {
            if (TextUtils.isEmpty(content)) return
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
            if (cm != null) {
                val mClipData = ClipData.newPlainText("Label", content)
                cm.setPrimaryClip(mClipData)
                Toast.makeText(this, "列表数据已经复制到剪切板", Toast.LENGTH_LONG).show()
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