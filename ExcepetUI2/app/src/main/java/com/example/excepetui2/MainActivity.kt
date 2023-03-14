package com.example.excepetui2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.excepetui2.apis.Location_Info
import com.example.excepetui2.apis.Witness_Receive
import com.example.excepetui2.ble.BleBlueImpl
import com.example.excepetui2.ble.BleClientActivity
import com.zhengsr.zplib.ZPermission
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.location.Criteria

import android.location.GpsStatus

import android.location.GpsSatellite

import android.location.LocationProvider
import android.provider.Settings
import android.view.View

import android.widget.EditText
import com.alibaba.fastjson.JSON
import com.example.excepetui2.apis.PolReq
import com.example.excepetui2.apis.communication


class MainActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName




    private lateinit var textView: TextView
    private val mSb = StringBuilder()
    private var mBluetoothGattServer: BluetoothGattServer? = null
    private var bluetoothAdapter: BluetoothAdapter? = null


    private val start_type = 0   //是否显示textview
    private var to_type = 0  //用于标识证明过程的第几步
    private var from_type = 0  //用于标识证明过程的第几步
    var communication0 = communication()
    private var witness_public_key = "witness_public_key"
    private var prover_id = "prover_id"
    private var polResp1Str = "polResp1Str"



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        to_type = 0
        from_type = 0


        requestPermission()


//        默认是谷歌GPS定位，信号不好的时候getLastKnownLocation经常返回空指针导致闪退
//        考虑用gps和北斗融合定位的百度地图lbs替代
//        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
//
//        lateinit var location :Location
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//           ) {
//            location == locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
//        }
//        else{
//            logInfo("缺少权限",1)
//        }





        val bluetooth = BluetoothAdapter.getDefaultAdapter()
        if (bluetooth == null) {
            Toast.makeText(this, "您的设备未找到蓝牙驱动！!", Toast.LENGTH_SHORT).show()
            finish()
        }else {
            if (!bluetooth.isEnabled) {
                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),1)
            }
        }



        textView = findViewById(R.id.info)
        initBleServer()

        button1.setOnClickListener {
            val intent = Intent(this, BleClientActivity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        button3.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }




        val local_date_time0t = getTimeNow()
        logInfo(local_date_time0t,1)
//        if(location == null){
//            logInfo("位置空指针",1)
//        }
//        else{
//            val locationInfo = getLocationInfo(location)
//            logInfo("x = ${locationInfo.longitude}, y = ${locationInfo.latitude} z = ${locationInfo.altitude}",1)
//        }

    }

    fun getLocationInfo(location:Location): Location_Info {   //获得位置信息
        val x = location.longitude
        val y = location.latitude
        val z = location.altitude

        val locationInfo0 = Location_Info(x.toInt(),y.toInt(),z.toInt())
        return locationInfo0
    }




    override fun onResume() {
        super.onResume()

        to_type = 0
        from_type = 0

        initBleServer()
        button1.setOnClickListener {
            val intent = Intent(this, BleClientActivity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }
    }




    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1){
            if (resultCode == Activity.RESULT_CANCELED){
                Toast.makeText(this, "请您不要拒绝开启蓝牙，否则应用无法运行", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun requestPermission(){

        ZPermission.with(this)
            .permissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN
            ).request { isAllGranted, deniedLists ->
                if (!isAllGranted){
                    Toast.makeText(this, "需要开启权限才能运行应用", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }


        //在 Android 10 还需要开启 gps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val lm: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                Toast.makeText(this@MainActivity, "请您先开启gps,否则蓝牙不可用", Toast.LENGTH_SHORT).show()
            }
        }

    }



    private fun initBleServer() {
        val blueManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = blueManager.adapter
        bluetoothAdapter?.name = "hw0"

        /**
         * GAP广播数据最长只能31个字节，包含两中： 广播数据和扫描回复
         * - 广播数据是必须的，外设需要不断发送广播，让中心设备知道
         * - 扫描回复是可选的，当中心设备扫描到才会扫描回复
         * 广播间隔越长，越省电
         */

        //广播设置
        val advSetting = AdvertiseSettings.Builder()
            //低延时，高功率，不使用后台
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            // 高的发送功率
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            // 可连接
            .setConnectable(true)
            //广播时限。最多180000毫秒。值为0将禁用时间限制。（不设置则为无限广播时长）
            .setTimeout(0)
            .build()
        //设置广播包，这个是必须要设置的
        val advData = AdvertiseData.Builder()
            .setIncludeDeviceName(true) //显示名字
            .setIncludeTxPowerLevel(true)//设置功率
            .addServiceUuid(ParcelUuid(BleBlueImpl.UUID_SERVICE)) //设置 UUID 服务的 uuid
            .build()



        //测试 31bit
        val byteData = byteArrayOf(-65, 2, 3, 6, 4, 23, 23, 9, 9,
            9,1, 2, 3, 6, 4, 23, 23, 9, 9, 8,23,23,23)

        //扫描广播数据（可不写，客户端扫描才发送）
        val scanResponse = AdvertiseData.Builder()
            //设置厂商数据
            .addManufacturerData(0x19, byteData)
            .build()


        /**
         * GATT 使用了 ATT 协议，ATT 把 service 和 characteristic 对应的数据保存在一个查询表中，
         * 依次查找每一项的索引
         * BLE 设备通过 Service 和 Characteristic 进行通信
         * 外设只能被一个中心设备连接，一旦连接，就会停止广播，断开又会重新发送
         * 但中心设备同时可以和多个外设连接
         * 他们之间需要双向通信的话，唯一的方式就是建立 GATT 连接
         * 外设作为 GATT(server)，它维持了 ATT 的查找表以及service 和 charateristic 的定义
         */

        val bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
        //开启广播,这个外设就开始发送广播了
        bluetoothLeAdvertiser?.startAdvertising(
            advSetting,
            advData,
            scanResponse,
            advertiseCallback
        )


        /**
         * characteristic 是最小的逻辑单元
         * 一个 characteristic 包含一个单一 value 变量 和 0-n个用来描述 characteristic 变量的
         * Descriptor。与 service 相似，每个 characteristic 用 16bit或者32bit的uuid作为标识
         * 实际的通信中，也是通过 Characteristic 进行读写通信的
         */
        //添加读+通知的 GattCharacteristic
        val readCharacteristic = BluetoothGattCharacteristic(
            BleBlueImpl.UUID_READ_NOTIFY,
            BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )


        //添加写的 GattCharacteristic
        val writeCharacteristic = BluetoothGattCharacteristic(
            BleBlueImpl.UUID_WRITE,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        //添加 Descriptor 描述符
        val descriptor =
            BluetoothGattDescriptor(
                BleBlueImpl.UUID_DESCRIBE,
                BluetoothGattDescriptor.PERMISSION_WRITE
            )

        //为特征值添加描述
        writeCharacteristic.addDescriptor(descriptor)


        /**
         * 添加 Gatt service 用来通信
         */

        //开启广播service，这样才能通信，包含一个或多个 characteristic ，每个service 都有一个 uuid
        val gattService =
            BluetoothGattService(
                BleBlueImpl.UUID_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
        gattService.addCharacteristic(readCharacteristic)
        gattService.addCharacteristic(writeCharacteristic)


        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        //打开 GATT 服务，方便客户端连接
        mBluetoothGattServer = bluetoothManager.openGattServer(this, gattServiceCallbak)
        mBluetoothGattServer?.addService(gattService)


    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeNow(): String {
        var dt: LocalDateTime? = LocalDateTime.now()
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        val local_date_time = dt!!.format(fmt)
        return local_date_time
    }

    private val gattServiceCallbak = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            device ?: return
            Log.d(TAG, "zsr onConnectionStateChange: ")
            if (status == BluetoothGatt.GATT_SUCCESS && newState == 2) {
                logInfo("连接到中心设备: ${device?.name}",1)
            } else {
                logInfo("与: ${device?.name} 断开连接失败！",1)
            }
        }


        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            /**
             * 中心设备read时，回调
             */

//=================================================要传输的数据=======================================

            var data = "this is a test from ble server"

            if(to_type==0 && from_type==1){
                to_type += 1
                data = witness_public_key
                logInfo("已发送公钥,t=$to_type,f=$from_type",1)
            }
            else if(to_type==1 && from_type==2){
                to_type += 1
                data = polResp1Str
                logInfo("已向证明者发送polResp1,t=$to_type,f=$from_type",1)
            }


            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, data.toByteArray()
            )

            logInfo("客户端读取 [characteristic ${characteristic?.uuid}] $data",start_type)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, value
            )
            value?.let {

//==============================接收到的数据================================================================
                if(from_type ==0){

                    communication0.initial()
                    from_type += 1
                    logInfo("${String(it)}",1)
                    var to_witness_1 = JSON.parseObject(String(it)) as Witness_Receive
                    logInfo("接受到证明者id和时间戳,f=$from_type,t=$to_type",1)
                    prover_id = to_witness_1.proverID
                    witness_public_key = communication0.receive_prover(to_witness_1)
                    logInfo("接受证明者ID并发放公钥",1)
                }
                else if(from_type==1){
                    from_type += 1
                    val dataEncode = String(it)
                    logInfo("接受证明者的加密包,f=$from_type,t=$to_type",1)
                    val time_now1 = getTimeNow()
                    val polReq = PolReq(time_now1,prover_id,dataEncode)
                    val myLocation = Location_Info(502,500,0)
                    polResp1Str = communication0.wiitness_work(polReq,myLocation,2)
                    logInfo("向计算服务器发送证明请求",1)
                }


                logInfo("客户端写入 [characteristic ${characteristic?.uuid}] ${String(it)}",start_type)
            }
        }

        override fun onDescriptorReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            descriptor: BluetoothGattDescriptor?
        ) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)

            val data = "this is a test"


            mBluetoothGattServer?.sendResponse(
                device, requestId, BluetoothGatt.GATT_SUCCESS,
                offset, data.toByteArray()
            )
            logInfo("客户端读取 [descriptor ${descriptor?.uuid}] $data",start_type)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(
                device,
                requestId,
                descriptor,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )

            value?.let {
                logInfo("客户端写入 [descriptor ${descriptor?.uuid}] ${String(it)}",start_type)
                // 简单模拟通知客户端Characteristic变化
                Log.d(TAG, "zsr onDescriptorWriteRequest: $value")
            }


        }

        override fun onExecuteWrite(device: BluetoothDevice?, requestId: Int, execute: Boolean) {
            super.onExecuteWrite(device, requestId, execute)
            Log.d(TAG, "zsr onExecuteWrite: ")
        }

        override fun onNotificationSent(device: BluetoothDevice?, status: Int) {
            super.onNotificationSent(device, status)
            Log.d(TAG, "zsr onNotificationSent: ")
        }

        override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
            super.onMtuChanged(device, mtu)
            Log.d(TAG, "zsr onMtuChanged: ")
        }
    }


    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            logInfo("服务准备就绪，请搜索广播",start_type)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
                logInfo("广播数据超过31个字节了 !",start_type)
            } else {
                logInfo("服务启动失败: $errorCode",start_type)
            }
        }
    }

    private fun logInfo(msg: String, start_type: Int) {

        if(start_type==0){
//            mSb.apply {
//                append(msg).append("\n")
//            }
        }
        else {
            runOnUiThread {
                mSb.apply {
                    append(msg).append("\n")
                    textView.text = toString()
                }
            }
        }
    }


    override fun onPause() {
        super.onPause()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        mBluetoothGattServer?.close()
    }


    override fun onDestroy() {
        super.onDestroy()
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        mBluetoothGattServer?.close()
    }



}