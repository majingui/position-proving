package com.example.bluetoothdemo

/**
 * 类说明：蓝牙工具类
 */
class ByteUtils {

    companion object {

        private val STRING_ARRAY = "0123456789ABCDEF".toCharArray()

        /**
         * 字节数组转16进制Hex字符串【大写】
         */
        fun bytesToString(bytes: ByteArray): String {
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v: Int = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = STRING_ARRAY[v ushr 4]
                hexChars[j * 2 + 1] = STRING_ARRAY[v and 0x0F]
            }
            return String(hexChars)
        }

        /**
         * 字节数组转16进制Hex字符串【小写】
         */
//        fun bytesToHex(bytes: ByteArray): String? {
//            val sb = StringBuffer()
//            for (i in bytes.indices) {
//                val hex = Integer.toHexString(bytes[i].toInt() and 0xFF)
//                if (hex.length < 2) {
//                    sb.append(0)
//                }
//                sb.append(hex)
//            }
//            return sb.toString()
//        }
    }
}
