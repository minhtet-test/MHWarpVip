package com.mhwarp.vip

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import wgwrapper.Wgwrapper // သင့် Go AAR package 

class WarpVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val configStr = intent?.getStringExtra("WARP_CONFIG")
        if (!configStr.isNullOrEmpty()) {
            startVpn(configStr)
        } else {
            Log.e("MHWARPvpn", "Config string is empty!")
            stopSelf()
        }
        return START_STICKY
    }

    private fun startVpn(configStr: String) {
        val parsedConfig = ConfigParser.parse(configStr)
        val builder = Builder()

        builder.setMtu(parsedConfig.mtu)

        for (addr in parsedConfig.addresses) {
            val parts = addr.split("/")
            if (parts.size == 2) {
                builder.addAddress(parts[0], parts[1].toInt())
            }
        }

        for (dns in parsedConfig.dnsServers) {
            builder.addDnsServer(dns)
        }

        builder.addRoute("0.0.0.0", 0)
        builder.addRoute("::", 0)

        try {
            vpnInterface = builder.establish()
            val fd = vpnInterface?.fd ?: -1

            if (fd != -1) {
                // Go function သို့ လှမ်းပို့ခြင်း
                val status = Wgwrapper.startVPN(fd.toLong(), configStr)
                Log.d("MHWARPvpn", "Go Status: $status")
            }
        } catch (e: Exception) {
            Log.e("MHWARPvpn", "Start Error: ${e.message}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        val status = Wgwrapper.stopVPN()
        Log.d("MHWARPvpn", "Go Stopped: $status")
        vpnInterface?.close()
        vpnInterface = null
    }
}
