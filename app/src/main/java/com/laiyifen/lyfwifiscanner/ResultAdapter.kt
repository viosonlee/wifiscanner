package com.laiyifen.lyfwifiscanner

import android.content.Context
import android.net.wifi.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.laiyifen.lyfwifiscanner.databinding.ItemScanResultBinding

/**
 *Author:viosonlee
 *Date:2023/1/6
 *DESCRIPTION:
 */
class ResultAdapter(private val context: Context, private val data: List<ScanResult>) : RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemScanResultBinding.inflate(LayoutInflater.from(context), parent, false))
    }


    class ViewHolder(private val binding: ItemScanResultBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: ScanResult) {
            binding.name.text = item.SSID
            binding.macAddress.text = item.BSSID
        }
    }

}