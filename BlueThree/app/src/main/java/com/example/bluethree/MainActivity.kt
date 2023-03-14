package com.example.bluethree

import android.Manifest
import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.fastjson.JSON
import com.example.bluethree.apis.*
import com.example.bluethree.bluetooth.BlueToothDeviceAdapter
import com.example.bluethree.bluetooth.Prover_data
import com.example.bluethree.bluetooth.Witness_data
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var bTAdatper: BluetoothAdapter? = null
    private var listView: ListView? = null
    private var adapter: BlueToothDeviceAdapter? = null
    private var text_state: TextView? = null
    private var text_msg: TextView? = null
    private val BUFFER_SIZE = 16384   //1024
    private var connectThread: ConnectThread? = null
    private var listenerThread: ListenerThread? = null


    private var temp_data = ""   //临时字符串，用于拼接传过来的包
    private var is_witness = true  //标记是否为见证者
    private var step_prove = 0 //证明步骤
    private var logtext = ""  //显示用
    private var witnumber = 1
//    private var is_wt_prover_id = "0"
//    private var is_wt_witness_public_key = "witness_public_key"
//    private var not_wt_witniess_pub_key1 = "witniess_pub_key1"
//    private var dataEncodep = "dataEncodep"
//    private var dataEncodew = "dataEncodew"
//    private var polResp1Strp = "polResp1Strp"
//    private var polResp1Strw = "polResp1Strw"
    private var end01 = "end01"
    private var proverData = Prover_data()
    private var witnessData = Witness_data()
//    private var locationinfop = Location_Info(180.0,180.0,0.0)


    var communication0 = communication()


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions() //申请权限

        logtext = ""  //显示用
        communication0.initial()

        is_witness = true  //标记是否为见证者
        initValue()  //初始化全局变量

        initView()
        bTAdatper = BluetoothAdapter.getDefaultAdapter()
        initReceiver()
        listenerThread = ListenerThread()
        listenerThread!!.start()
    }

    private fun initValue(){
        temp_data = ""   //临时字符串，用于拼接传过来的包
        step_prove = 0 //证明步骤

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initView() {
        findViewById<View>(R.id.btn_openBT).setOnClickListener(this)
        findViewById<View>(R.id.btn_search).setOnClickListener(this)
        findViewById<View>(R.id.btn_send).setOnClickListener(this)
        text_state = findViewById(R.id.text_state) as TextView?
        text_msg = findViewById(R.id.text_msg) as TextView?
        listView = findViewById(R.id.listView) as ListView?
        adapter =
            BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item)
        listView!!.adapter = adapter
        listView!!.onItemClickListener =
            OnItemClickListener { parent, view, position, id ->
                if (bTAdatper!!.isDiscovering) {
                    bTAdatper!!.cancelDiscovery()
                }
                logInfo("连接设备前：" + getTimeNow())
                val device = adapter!!.getItem(position) as BluetoothDevice
                //连接设备
                connectDevice(device)
            }
    }

    private fun initReceiver() {
        //注册广播
        val filter = IntentFilter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_openBT -> openBlueTooth()
            R.id.btn_search -> {
                searchDevices()
                is_witness = false
                val threeWit = ThreeWit()
                threeWit.start()

            }
            R.id.btn_send -> if (connectThread != null) {
                connectThread!!.sendMsg()
            }
        }
    }

    /**
     * 开启蓝牙
     */
    private fun openBlueTooth() {
        if (bTAdatper == null) {
            Toast.makeText(this, "当前设备不支持蓝牙功能", Toast.LENGTH_SHORT).show()
        }
        if (!bTAdatper!!.isEnabled) {
            /* Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);*/
            bTAdatper!!.enable()
        }
        //开启被其它蓝牙设备发现的功能
        if (bTAdatper!!.scanMode != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            val i = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
            //设置为一直开启
            i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0)
            startActivity(i)
        }
    }

    /**
     * 搜索蓝牙设备
     */
    private fun searchDevices() {
        if (bTAdatper!!.isDiscovering) {
            bTAdatper!!.cancelDiscovery()
        }
        boundedDevices
        bTAdatper!!.startDiscovery()
    }//获取已经配对过的设备
    //将其添加到设备列表中
    /**
     * 获取已经配对过的设备
     */
    private val boundedDevices: Unit
        private get() {
            //获取已经配对过的设备
            val pairedDevices = bTAdatper!!.bondedDevices
            //将其添加到设备列表中
            if (pairedDevices.size > 0) {
                for (device in pairedDevices) {
                    val macadr = device.address
                    if(is_mymac(macadr)){
                        adapter?.add(device)
                    }
//                    adapter?.add(device)
                }
            }
        }

    /**
     * 连接蓝牙设备
     */
    private fun connectDevice(device: BluetoothDevice) {
        text_state?.setText(getResources().getString(R.string.connecting))
        try {
            //创建Socket
            val socket = device.createRfcommSocketToServiceRecord(BT_UUID)
            //启动连接线程
            connectThread = ConnectThread(socket, true)
            connectThread!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //取消搜索
        if (bTAdatper != null && bTAdatper!!.isDiscovering) {
            bTAdatper!!.cancelDiscovery()
        }
        //注销BroadcastReceiver，防止资源泄露
        unregisterReceiver(mReceiver)
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device =
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                //避免重复添加已经绑定过的设备
                if (device!!.bondState != BluetoothDevice.BOND_BONDED && is_mymac(device.address)) {
                    adapter?.add(device)
                    adapter?.notifyDataSetChanged()
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED == action) {
                Toast.makeText(this@MainActivity, "开始搜索", Toast.LENGTH_SHORT).show()
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED == action) {
                Toast.makeText(this@MainActivity, "搜索完毕", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 连接线程
     */
    private inner class ConnectThread(
        private val socket: BluetoothSocket,
        private val activeConnect: Boolean
    ) :
        Thread() {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() {
            try {
                //如果是自动连接 则调用连接方法
                if (activeConnect) {
                    socket.connect()
                }
                text_state!!.post { text_state!!.setText(getResources().getString(R.string.connect_success)) }
                inputStream = socket.inputStream
                outputStream = socket.outputStream

                logInfo("成功连接设备：" + getTimeNow())

                if(!is_witness && proverData.con1){
                    proverData.con1 = false
                    if (connectThread != null) {
                        connectThread!!.sendMsg()
                    }
                }
                else if(!is_witness && proverData.con2){
                    proverData.con2 = false
                    if (connectThread != null) {
                        connectThread!!.sendMsg()
                    }
                }
                else if(!is_witness && proverData.con3){
                    proverData.con3 = false
                    if (connectThread != null) {
                        connectThread!!.sendMsg()
                    }
                }

                val buffer = ByteArray(BUFFER_SIZE)
                var bytes: Int
                while (true) {
                    //读取数据
                    bytes = inputStream!!.read(buffer)
                    if (bytes > 0) {
                        val data = ByteArray(bytes)
                        System.arraycopy(buffer, 0, data, 0, bytes)

//=============================================获取数据======================================================
                        var tmpdata = String(data)
                        val kk = (tmpdata[tmpdata.length-1] == '@')
                        if(kk){
                            tmpdata = tmpdata.dropLast(1)
                        }
                        var hint1 = "赋值失败"
                        temp_data += tmpdata

                        if(kk){
                            var ifSend = false

                            logInfo("已收到数据，数据大小：" + temp_data.length)
                            //从这里读取接收到的数据(temp_data)
                            if(is_witness){  //见证者
                                if(step_prove==0){
                                    logInfo("data = $temp_data")
                                    step_prove += 1
                                    val obj0 = JSONObject(temp_data)
//                                    is_wt_prover_id = obj0.getString("proverID")
                                    witnessData.proverId = obj0.getString("proverID")
                                    val timep0 = obj0.getString("prover_send_time")
                                    val as_witness_1 = Witness_Receive(witnessData.proverId,timep0)
//                                    is_wt_witness_public_key = communication0.receive_prover(as_witness_1)
                                    witnessData.myPublicKey = communication0.receive_prover(as_witness_1)
                                    hint1 = "证明者的id和时间戳，step = $step_prove"
                                }
                                else if(step_prove==2){
                                    step_prove += 1
//                                    dataEncodew = temp_data
                                    witnessData.dataEncodew = temp_data
                                    hint1 = "由本见证者公钥加密过的证明者信息，step = $step_prove"
                                }
                                ifSend = true
                            }
                            else{  //证明者
                                if(step_prove==1){
                                    step_prove += 1
                                    if(witnumber==1) {
//                                        not_wt_witniess_pub_key1 = temp_data
                                        proverData.wit1.pub_keyp = temp_data
                                    }
                                    else if(witnumber==2){
                                        proverData.wit2.pub_keyp = temp_data
                                    }
                                    else if(witnumber==3){
                                        proverData.wit3.pub_keyp = temp_data
                                    }
//                                    val local_date_time1t = getTimeNow()
                                    val locationinfopJSONStr = JSON.toJSONString(proverData.locationinfop)
                                    if(witnumber==1) {
                                        val proverInfop = Prover_Info(
                                            proverData.wit1.pub_keyp,
                                            locationinfopJSONStr,
                                            proverData.myTime
                                        )
//                                        dataEncodep = communication0.encrypt_info(proverInfop)
                                        proverData.wit1.dataEncodep = communication0.encrypt_info(proverInfop)
                                    }
                                    else if(witnumber==2){
                                        val proverInfop = Prover_Info(
                                            proverData.wit2.pub_keyp,
                                            locationinfopJSONStr,
                                            proverData.myTime
                                        )
                                        proverData.wit2.dataEncodep = communication0.encrypt_info(proverInfop)
                                    }
                                    else if(witnumber==3){
                                        val proverInfop = Prover_Info(
                                            proverData.wit3.pub_keyp,
                                            locationinfopJSONStr,
                                            proverData.myTime
                                        )
                                        proverData.wit3.dataEncodep = communication0.encrypt_info(proverInfop)
                                    }
                                    hint1 = "见证者的公钥，step = $step_prove"
                                    ifSend = true
                                }
                                else if(step_prove==3){
                                    step_prove += 1
                                    if(witnumber==1) {
//                                        polResp1Strp = temp_data
                                        proverData.wit1.polResp1Strp = temp_data
                                        proverData.k1 = true
                                    }
                                    else if(witnumber==2){
                                        proverData.wit2.polResp1Strp = temp_data
                                        proverData.k2 = true
                                    }
                                    else if(witnumber==3){
                                        proverData.wit3.polResp1Strp = temp_data
                                        proverData.k3 = true
                                    }
//                                    hint1 = "见证者发来的polResp1Str，step = $step_prove"
//                                    logInfo("收到的数据类型：" + hint1)
                                    if(witnumber==3) {
                                        logInfo("准备向中心服务器发起综合证明")
                                        val three_Cret = proverData.getThree_Cret()


                                        val locationInfopStr = JSON.toJSONString(proverData.locationinfop)
                                        try {
                                            val zkThread =
                                                ZKThread(three_Cret, proverData.myTime, locationInfopStr)
                                            zkThread.start()
                                            var ii0 = 0
                                            while (end01 == "end01") {
                                                sleep(100)
                                                ii0 += 1
                                                if (ii0 > 100) break
                                            }
//                                    zkThread.join()
                                            if (end01.length < 300) {
                                                logInfo("得到的返回值：" + end01)
                                            } else {
                                                logInfo("返回值过长不打印")
                                            }
                                            logInfo("证明完成，time = " + getTimeNow())

                                        } catch (e: Exception) {
                                            logInfo("与中心服务器通信失败：\n" + e.toString())
                                        }
                                    }
                                    if(witnumber==1){
                                        witnumber += 1
                                        initValue() //证明完成，初始化
                                        proverData.complet1 = true
                                    }
                                    else if(witnumber==2){
                                        witnumber += 1
                                        initValue() //证明完成，初始化
                                        proverData.complet2 = true
                                    }
                                    else if(witnumber==3){
                                        witnumber += 1
                                        initValue() //证明完成，初始化
                                        proverData.complet3 = true
                                    }
                                }
                            }

                            temp_data = ""

                            if(ifSend){
                                if(is_witness) {
                                    logInfo("收到的数据类型：" + hint1)
                                }
                                if(connectThread != null) {
                                    connectThread!!.sendMsg()
                                }
                            }
                        }

//                        text_msg!!.post {
//                            text_msg!!.setText(
//                                logtext
//                            )
//                        }

                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                text_state!!.post { text_state!!.setText(getResources().getString(R.string.connect_error)) }
            }
        }

        /**
         * 发送数据
         *
         * @param msg
         */
//=====================================发送数据=================================================================
        @RequiresApi(Build.VERSION_CODES.O)
        fun sendMsg() {
            var msg = "初始数据，如果这句话显示出来，说明要发送的数据没有赋值成功"
            var hint0 = "赋值失败"

            if(is_witness){
                if(step_prove == 1){
                    step_prove += 1
                    msg = witnessData.myPublicKey
                    hint0 = "本见证者的公钥，step = $step_prove"
                }
                else if(step_prove == 3){
                    step_prove += 1
                    logInfo("准备解密该压缩包并与计算服务器通信"+getTimeNow())
//                    val time_now1 = getTimeNow()
//                    val polReq0 = PolReq(time_now1,is_wt_prover_id,dataEncodew)
//                    val polReq0Str = JSON.toJSONString(polReq0)
                    val myLocation1JSONString = JSON.toJSONString(witnessData.locationinfo)
//                    polResp1Strw = "faild"
                    try {
                        val toWitnessWork =
                            ToWitnessWork(witnessData.dataEncodew, myLocation1JSONString, 2)
//                    polResp1Strw = communication0.wiitness_work(dataEncodew,myLocation1JSONString,2)
                        val jsThread = JSThread(toWitnessWork)
                        jsThread.start()
//                    var ii = 0
//                    while(polResp1Strw=="faild"){
//                        sleep(100)
//                        ii += 1
//                        if(ii>100) break
//                    }
                        jsThread.join()
                    }
                    catch (e:Exception){
                        logInfo("$$" + e.toString())
                    }
                    logInfo("与服务器通信完成"+getTimeNow())
                    if(witnessData.polResp1Strw.length<100){
                        logInfo(witnessData.polResp1Strw)
                    }
                    else{
                        logInfo("数据过长不打出来了")
                    }

                    msg = witnessData.polResp1Strw
                    hint0 = "发给证明者的polResp1Str，step = $step_prove"
                    initValue()  //见证者任务完成，初始化
                }
            }
            else{  //证明者
                if(step_prove==0){
                    step_prove += 1
                    val local_date_time = getTimeNow()
                    proverData.myTime = local_date_time
//                    logInfo("点击按键："+local_date_time)
                    val to_witness_1 = Witness_Receive("1",local_date_time)
                    msg = JSON.toJSONString(to_witness_1)
                    hint0 = "证明者id和时间戳，step = $step_prove"
                }
                if(step_prove == 2){
                    step_prove += 1
                    if(witnumber==1) {
//                        msg = dataEncodep
                        msg = proverData.wit1.dataEncodep
                    }
                    else if(witnumber==2){
                        msg = proverData.wit2.dataEncodep
                    }
                    else if(witnumber==3){
                        msg = proverData.wit3.dataEncodep
                    }
                    hint0 = "用见证者公钥加密过的你的信息，step = $step_prove"
                }
                else if(step_prove == 4){
                    step_prove += 1
                    initValue()
                }
            }


            val to_others = msg + "@"
            val bytes = to_others.toByteArray()
            if (outputStream != null) {
                try {
                    //发送数据
                    if(is_witness) {
                        logInfo("发送数据：" + hint0 + "，数据大小：" + msg.length)
                    }
                    outputStream!!.write(bytes)
//                    text_msg!!.post {
//                        text_msg!!.setText(
//                            logtext
//                        )
//                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    logtext += "发送" + hint0 + "失败，数据大小：" + msg.length +"\n"
                    text_msg!!.post {
                        text_msg!!.setText(
                            logtext
                        )
                    }
                }
            }
        }
    }

    /**
     * 监听线程
     */
    private inner class ListenerThread : Thread() {
        private var serverSocket: BluetoothServerSocket? = null
        private var socket: BluetoothSocket? = null
        override fun run() {
            try {
                serverSocket = bTAdatper!!.listenUsingRfcommWithServiceRecord(
                    NAME, BT_UUID
                )
                while (true) {
                    //线程阻塞，等待别的设备连接
                    socket = serverSocket!!.accept()
                    text_state!!.post { text_state!!.setText(getResources().getString(R.string.connecting)) }
                    connectThread = ConnectThread(socket!!, false)
                    connectThread!!.start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private inner class JSThread(val dataInJS:ToWitnessWork):Thread(){
        override fun run(){
            val fir = dataInJS.dataEncodewt
            val sed = dataInJS.myLocation1JSONStringt
            val thr = dataInJS.distancet
            witnessData.polResp1Strw = communication0.wiitness_work(fir,sed,thr)
        }
    }

    private inner class ZKThread(val three_Cret0:Three_Cret,val time2:String,val locationOfProverStr0:String):Thread(){
        override fun run(){
            end01 = communication0.zk_verify(three_Cret0,time2,locationOfProverStr0)
        }
    }

    private inner class ThreeWit():Thread(){
        override fun run() {
            lateinit var device1:BluetoothDevice
            lateinit var device2:BluetoothDevice
            lateinit var device3:BluetoothDevice

            try {
                device1 = adapter!!.getItem(0) as BluetoothDevice
                device2 = adapter!!.getItem(1) as BluetoothDevice
                device3 = adapter!!.getItem(2) as BluetoothDevice
            }
            catch (e:Exception){
                logInfo("##error0:" + e.toString())
            }
            connectDevice(device1)
            while (!proverData.complet1){
                sleep(200)
            }
            try {
                runOnUiThread {
                    connectDevice(device2)
                }
                while (!proverData.complet2) {
                    sleep(200)
                }
            }
            catch(e:Exception){
                logInfo("##error2:" + e.toString())
            }
            try {
                runOnUiThread {
                    connectDevice(device3)
                }
            }
            catch(e:Exception){
                logInfo("##error3:" + e.toString())
            }
        }
    }

    data class ToWitnessWork(var dataEncodewt:String,
                             var myLocation1JSONStringt:String,
                             var distancet:Int)

    private fun logInfo(mmm:String){
        logtext += mmm + "\n"
        runOnUiThread {
            text_msg!!.post {
                text_msg!!.setText(
                    logtext
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getTimeNow(): String {
        var dt: LocalDateTime? = LocalDateTime.now()
        val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS")
        val local_date_time = dt!!.format(fmt)
        return local_date_time
    }

    companion object {
        private const val NAME = "BT_DEMO"
        private val BT_UUID = UUID.fromString("02001101-0001-1000-8080-00805F9BA9BA")
    }


//=======================================<申请权限=========================================================
    private var permissionInfo: String? = null
    private val SDK_PERMISSION_REQUEST = 127

    @TargetApi(23)
    private fun requestPermissions() { //获取包括定位、文件读写之内的各种权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = ArrayList<String>()



            /*
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */

            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            if (checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.FOREGROUND_SERVICE)
            }

            //读写
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            }

            //后台定位
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }

            /*
             * 电话状态权限只会申请一次
             */
//            // 读写权限
//            if (addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
//            }
            // 读取电话状态权限
            if (addPermission(permissions, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n"
            }
            if (permissions.size > 0) {
                requestPermissions(permissions.toTypedArray(), SDK_PERMISSION_REQUEST)
            }
        }
    }


    @TargetApi(23)
    private fun addPermission(permissionsList: ArrayList<String>, permission: String): Boolean {
        return if (checkSelfPermission(permission) !== PackageManager.PERMISSION_GRANTED) { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
            if (shouldShowRequestPermissionRationale(permission)) {
                true
            } else {
                permissionsList.add(permission)
                false
            }
        } else {
            true
        }
    }

    @TargetApi(23)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults!!)
    }
//=======================================申请权限>=========================================================

    fun is_mymac(macadr:String): Boolean {
        var mack = false
        if(macadr == "08:F4:58:80:D8:79"){
            mack = true
        }
        else if(macadr == "6C:06:D6:8B:A2:8D"){
            mack = true
        }
        else if(macadr == "10:B1:F8:B3:D1:6E"){
            mack = true
        }
        return mack
    }

}