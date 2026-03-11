package com.mhwarp.vip

object ConfigParser {
    fun parse(configStr: String): VpnConfig {
        val config = VpnConfig()
        val lines = configStr.lines()

        for (line in lines) {
            val cleanLine = line.substringBefore("#").trim()
            if (cleanLine.isEmpty() || cleanLine.startsWith("[")) continue

            val parts = cleanLine.split("=", limit = 2)
            if (parts.size != 2) continue

            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()

            when (key) {
                "address" -> config.addresses.addAll(value.split(",").map { it.trim() })
                "dns" -> config.dnsServers.addAll(value.split(",").map { it.trim() })
                "mtu" -> config.mtu = value.toIntOrNull() ?: 1280
            }
        }
        return config
    }
}

class VpnConfig {
    val addresses = mutableListOf<String>()
    val dnsServers = mutableListOf<String>()
    var mtu: Int = 1280
}
