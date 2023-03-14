package com.example.bluetoothdemo

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * 类说明：扫描+连接
 */
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var bluetoothDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        BleScanUtils.getInstance.startScanBle(this)
        BleScanUtils.getInstance.setOnListener {
            for (i in it) {
                if (i.name == "kaixinyujue") {
                    bluetoothDevice = i
                    //扫描到关闭扫描
                    BleScanUtils.getInstance.stopScanBle()
                }
                Log.e(TAG, "onCreate: " + i.address)
                Log.e(TAG, "onCreate: " + i.name)
            }
        }
        startConnectBtn.setOnClickListener {
            if (null != bluetoothDevice) {
                if (startConnectBtn.text.toString() == "断开连接") {
                    //断开连接【手动断连不走回调】
                    ConnectUtils.getInstance.disconnect()
                    startConnectBtn.text = "开始连接"
                    connectStateTv.text = "设备已断连"
                } else {
                    //开始连接
                    LoadingDialog.showTimerDialog(this)
                    ConnectUtils.getInstance.startConnect(this@MainActivity, bluetoothDevice!!)
                }
            }
        }
        //连接状态
        ConnectUtils.getInstance.setOnListener { device, state ->
            connectStateTv.text = state
            startConnectBtn.text = "断开连接"
        }
    }

    /**
     * 关闭扫描
     */
    override fun onDestroy() {
        super.onDestroy()
        BleScanUtils.getInstance.stopScanBle()
    }
}
