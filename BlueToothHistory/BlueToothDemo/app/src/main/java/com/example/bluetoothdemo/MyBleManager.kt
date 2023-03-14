package com.example.bluetoothdemo

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.data.Data
import java.util.*

/**
 * 蓝牙UUID介绍：https://blog.csdn.net/jiangchao3392/article/details/90213465
 * 基本的UUID：0000xxxx-0000-1000-8000-00805F9B34FB【设备可自定义各种格式】
 * 类说明：蓝牙管理类
 */
class MyBleManager(context: Context) : BleManager(context) {

    private val TAG = "MyBleManager"

    //服务UUID
    private val SERVICE_UUID: UUID = UUID.fromString("0000001-0000-1000-8000-00805F9B34FB")

    //写入UUID
    private val WRITE_UUID = UUID.fromString("00000002-0000-1000-8000-00805F9B34FB")

    //特征码UUID【监听】
    private val NOTIFY_UUID = UUID.fromString("00000003-0000-1000-8000-00805F9B34FB")

    //蓝牙GATT特性
    private var writeChar: BluetoothGattCharacteristic? = null
    private var notifyChar: BluetoothGattCharacteristic? = null

    //设备是否需要Dfu升级【后续预留DFU设备升级】
    var isDeviceUpdater = false

    override fun getGattCallback(): BleManagerGattCallback {
        return MyBleManagerGattCallback()
    }

    /**
     * Gatt回调类，蓝牙连接过程中的操作一般都是通过此类来完成
     */
    private inner class MyBleManagerGattCallback : BleManagerGattCallback() {

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            //校验设备是否拥有我们所需的服务与特征
            val service = gatt.getService(SERVICE_UUID)
            //是否需要更新
            if (isDeviceUpdater && null == service) {
                return true
            }
            if (service != null) {
                //读写特征
                writeChar = service.getCharacteristic(WRITE_UUID)
                //通知特征
                notifyChar = service.getCharacteristic(NOTIFY_UUID)
            }
            //校验读写特征是否拥有写入数据的权限
            var notify = false
            if (notifyChar != null) {
                val properties = notifyChar!!.properties
                notify = properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0
            }
            var writeRequest = false
            if (writeChar != null) {
                val properties = writeChar!!.properties
                writeRequest =
                    properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0
                writeChar!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            }
            //如果找到了所有需要的服务，则返回true
            return writeChar != null && notifyChar != null && notify && writeRequest
        }

        //在这里初始化设备。通常需要启用通知和设置required
        override fun initialize() {
            //创建一个原子请求队列。来自队列的请求将按顺序执行。
            beginAtomicRequestQueue()
                //最大20字节，GATT需要额外3字节。这将允许数据包大小为244字节
                .add(requestMtu(23)
                    .with { device: BluetoothDevice?, mtu: Int ->
                        Log.e(TAG, "with: $mtu")
                    }
                    .fail { device: BluetoothDevice?, status: Int ->
                        Log.e(TAG, "fail: $status")
                    })
                .add(enableNotifications(notifyChar))
                .done { device: BluetoothDevice? ->
                    Log.e(TAG, "done: " + device!!.name)
                    Log.e(TAG, "done: " + device.address)
                }
                .enqueue()
            //通过notificationDataCallback进行通知监听
            setNotificationCallback(notifyChar)
                .with { device: BluetoothDevice?, data: Data? ->
                    listener.invoke(device, data!!.value)
                }
            //enableNotifications(notifyChar).enqueue();
        }

        /**
         * 断开连接
         */
        override fun onDeviceDisconnected() {
            //设备断开连接。置null
            writeChar = null
            notifyChar = null
        }
    }

    /**
     * 通知数据回调
     */
    private lateinit var listener: (BluetoothDevice?, ByteArray?) -> Unit

    fun setOnListener(deviceData: (device: BluetoothDevice?, byteArray: ByteArray?) -> Unit) {
        listener = deviceData
    }
}
