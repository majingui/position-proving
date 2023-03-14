package com.example.bluetoothdemo

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import no.nordicsemi.android.ble.observer.ConnectionObserver

/**
 * 类说明：蓝牙连接类
 */
class ConnectUtils private constructor() : ConnectionObserver {

    private val TAG = "ConnectUtils"

    //设备管理类
    private var bleManager: MyBleManager? = null

    //dfu升级【预留】
    var isDfuUpdate = false

    /**
     * 单例
     * Lazy是接受一个 lambda 并返回一个 Lazy 实例的函数，返回的实例可以作为实现延迟属性的委托：
     * 第一次调用 get() 会执行已传递给 lazy() 的 lambda 表达式并记录结果， 后续调用 get() 只是返回记录的结果
     */
    companion object {
        val getInstance: ConnectUtils by lazy {
            ConnectUtils()
        }
    }

    /**
     * 开始连接设备
     */
    fun startConnect(context: Context, device: BluetoothDevice) {
        Log.e(TAG, "startConnect: 连接的设备名：" + device.name)
        bleManager = MyBleManager(context)
        //设置连接观察器
        bleManager!!.setConnectionObserver(this)
        //数据回调
        bleManager!!.setOnListener { device, byteArray ->
            //从结构上来看apply函数和run函数很像，唯一不同点就是它们各自返回的值不一样，
            //run函数是以闭包形式返回最后一行代码的值，而apply函数的返回的是传入对象的本身。
            byteArray.apply {
                //返回基础字节数组。
                Log.e(TAG, "响应的数据ByteArray: " + ByteUtils.bytesToString(byteArray!!))
            }
        }
        //开始连接
        bleManager!!.connect(device)
            //是否断连自动重连
            .useAutoConnect(true)
            //连接超时10秒
            .timeout(10000)
            //重连的次数，每次重连延时100
            .retry(3, 100)
            //设置完成回调
            .done {
                //连接完成，关闭加载框
                LoadingDialog.closeTimerDialog()
                Log.e(TAG, "startConnect: 初始化连接成功")
            }
            //将请求排队以进行异步执行。
            .enqueue()
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        if (null != bleManager) {
            bleManager!!.disconnect()
            bleManager!!.close()
        }
    }

    override fun onDeviceDisconnecting(device: BluetoothDevice) {
        Log.e(TAG, "onDeviceDisconnecting: 设备断开连接中...")
        listener.invoke(device, "设备断开连接中")
    }

    override fun onDeviceDisconnected(device: BluetoothDevice, reason: Int) {
        Log.e(TAG, "onDeviceDisconnected: 设备断开连接" + device.name)
        Log.e(TAG, "onDeviceDisconnected: 设备断开连接" + device.address)
        listener.invoke(device, "设备断开连接")
    }

    override fun onDeviceReady(device: BluetoothDevice) {
        //方法在所有初始化请求都已完成时调用
        Log.e(TAG, "onDeviceReady: " + device.name)
        Log.e(TAG, "onDeviceReady: " + device.address)
    }

    override fun onDeviceConnected(device: BluetoothDevice) {
        Log.e(TAG, "onDeviceConnected: 设备已连接 ")
        listener.invoke(device, "设备已连接")
    }

    override fun onDeviceFailedToConnect(device: BluetoothDevice, reason: Int) {
        Log.e(TAG, "onDeviceFailedToConnect: 设备连接失败超时$reason")
        listener.invoke(device, "设备连接超时")
    }

    override fun onDeviceConnecting(device: BluetoothDevice) {
        Log.e(TAG, "onDeviceConnecting: 设备连接中...")
        listener.invoke(device, "设备连接中")
    }

    /**
     * 连接状态回调
     */
    private lateinit var listener: (BluetoothDevice, String) -> Unit

    fun setOnListener(deviceData: (device: BluetoothDevice, state: String) -> Unit) {
        listener = deviceData
    }
}
