package com.mhwarp.vip

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject // JSON Parse လုပ်ရန်
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val VPN_REQUEST_CODE = 100
    private var generatedWarpConfig: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startButton = Button(this).apply {
            text = "Generate WARP & Connect"
            setOnClickListener {
                fetchConfigAndConnect()
            }
        }
        setContentView(startButton)
    }

    private fun fetchConfigAndConnect() {
        Toast.makeText(this, "Generating MHWARP Config...", Toast.LENGTH_SHORT).show()
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://mhwarppro.netlify.app/.netlify/functions/warp-gen")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = connection.inputStream.bufferedReader().readText()
                    
                    // ၁။ JSON မှ ဒေတာများကို ဆွဲထုတ်ခြင်း
                    val jsonResponse = JSONObject(responseText)
                    val privateKey = jsonResponse.getString("key")
                    
                    val configObj = jsonResponse.getJSONObject("config")
                    val interfaceObj = configObj.getJSONObject("interface").getJSONObject("addresses")
                    val v4Address = interfaceObj.getString("v4")
                    val v6Address = interfaceObj.getString("v6")
                    
                    val peerObj = configObj.getJSONArray("peers").getJSONObject(0)
                    val publicKey = peerObj.getString("public_key")
                    val endpoint = peerObj.getJSONObject("endpoint").getString("host")

                    // ၂။ ရလာသော ဒေတာများဖြင့် WireGuard INI Config တည်ဆောက်ခြင်း
                    generatedWarpConfig = """
                        [Interface]
                        PrivateKey = $privateKey
                        Address = $v4Address/32, $v6Address/128
                        DNS = 1.1.1.1, 1.0.0.1, 2606:4700:4700::1111
                        MTU = 1280
                        
                        [Peer]
                        PublicKey = $publicKey
                        Endpoint = $endpoint
                        AllowedIPs = 0.0.0.0/0, ::/0
                    """.trimIndent()
                    
                    withContext(Dispatchers.Main) {
                        checkVpnPermission()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Failed to get config. Code: $responseCode", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MHWARPvpn", "API Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun checkVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE)
        } else {
            startWarpVpnService()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            startWarpVpnService()
        } else {
            Toast.makeText(this, "VPN Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startWarpVpnService() {
        if (generatedWarpConfig.isNotEmpty()) {
            val serviceIntent = Intent(this, WarpVpnService::class.java)
            serviceIntent.putExtra("WARP_CONFIG", generatedWarpConfig)
            startService(serviceIntent)
            Toast.makeText(this, "Connecting MHWARPvpn...", Toast.LENGTH_SHORT).show()
        }
    }
}
