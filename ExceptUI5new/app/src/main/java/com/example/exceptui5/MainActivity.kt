package com.example.exceptui5

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.example.exceptui5.apis.*
import com.example.exceptui5.bluetooth.BlueToothDeviceAdapter
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
    private var is_wt_prover_id = "0"
    private var is_wt_witness_public_key = "witness_public_key"
    private var not_wt_witniess_pub_key1 = "witniess_pub_key1"
    private var dataEncodep = "dataEncodep"
    private var dataEncodew = "dataEncodew"
    private var polResp1Strp = "polResp1Strp"
    private var toWitnessWork = ToWitnessWork("a","b",1) //专门给witnesswork用的
    private var polResp1Strw = "polResp1Strw"
    private var end01 = "end01"

    var communication0 = communication()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initValue()  //初始化全局变量

        initView()
        bTAdatper = BluetoothAdapter.getDefaultAdapter()
        initReceiver()
        listenerThread = ListenerThread()
        listenerThread!!.start()
    }

    private fun initValue(){
        temp_data = ""   //临时字符串，用于拼接传过来的包
        is_witness = true  //标记是否为见证者
        step_prove = 0 //证明步骤
        logtext = ""  //显示用
        communication0.initial()
        //=========================测试部分 可以全删==================================================
//        var a = geohash()
//        var location1 : Location_Info= Location_Info(1.0,1.0,1.0)
//        var locationOfProver = JSON.toJSONString(location1)
//        var b = JSONObject(locationOfProver)
//        var geohashOfProver_Long = a.encode(b.getDouble("longitude"),b.getDouble("latitude"))
//        var ca1: CA1 = CA1("time",locationOfProver,geohashOfProver_Long)
//        var geohashOfProver_Short  = geohashOfProver_Long//以后要改
//        var ca2: CA2 = CA2("","","","","","","",geohashOfProver_Short,"12345678","12345678","12345678")
//
//        var cert11 :Pol_Resp1 = Pol_Resp1("time","id","zk1","sign","geo")
//        var cert12 = JSON.toJSONString(cert11)
//
//        var cert1 = JSONObject(cert12)
//        var cert2 = JSONObject(cert12)
//        var cert3 = JSONObject(cert12)//本质是Pol_Resp1
//        var geohashOfWitness1 = cert1.getString("geohashOfWitness_Short")
//        var geohashOfWitness2 = cert2.getString("geohashOfWitness_Short")
//        var geohashOfWitness3 = cert3.getString("geohashOfWitness_Short")
//        ca2.geohashOfProver_Short = geohashOfProver_Long//暂用
//        ca2.geohashOfWitness1_Short = geohashOfWitness1
//        ca2.geohashOfWitness2_Short = geohashOfWitness2
//        ca2.geohashOfWitness3_Short = geohashOfWitness3
//
//        ca2.cert1 = JSON.toJSONString(cert1)//解析见证者的zk1
//        ca2.cert2 = JSON.toJSONString(cert2)
//        ca2.cert3 = JSON.toJSONString(cert3)
//        ca2.idOfCA1 = "001"
//        ca2.hashOfCA1 = Digest.signMD5(JSON.toJSONString(ca1),"UTF-8")//摘要统一使用md5
//        ca2.time = "time"
//        ca2.idOfProver = "this.ID"
//        val ca2String = JSON.toJSONString(ca2)
//        val ca2StringEncoded = AES.encryptToBase64(ca2String, communication0.aesKey)
//        var threadResult:String = ""
//        var e11:Thread = Thread(Runnable {
//            try {
//                val connect = Socket("103.46.128.53", 48819)
//                val sendToServer = PrintStream(connect.getOutputStream())
//                val receiveFromServer = Scanner(connect.getInputStream())
//                System.out.println("aaaaaa连接上了")
//                //请求验证之前要先发送用户信息请求中心服务器的公钥
//                val myDataToServerForPublicKey = UserData(communication0.ID,communication0.user_PublicKey)//我的公钥没用的，主要是要请求服务器的公钥
//                val myDataToServerForPublicKeyJSON = JSON.toJSONString(myDataToServerForPublicKey)
//                sendToServer.println(myDataToServerForPublicKeyJSON)
//                while (true) {
//                    //sendToServer.println(userInfo)
//                    if (receiveFromServer.hasNext()) {//监听公钥并发送要验证的包
//                        communication0.centerserverPublicKey = receiveFromServer.next()
//                        System.out.println("aaaaaaaPublic====${communication0.centerserverPublicKey}")
//                        val aesKeyEncoded = RSA.encrypt(communication0.aesKey, communication0.centerserverPublicKey)
//                        var verifyReq: CA2package = CA2package(ca2StringEncoded,aesKeyEncoded)
////                        verifyReq.EncodedCa2 = ca2StringEncoded
////                        verifyReq.EncodedKey = aesKeyEncoded
//                        val str2: String = JSON.toJSONString(verifyReq)
//                        sendToServer.println(str2)
//                        System.out.println("aaaaaa发过去了")
//                    }
////                    if(receiveFromServer.hasNext()){
////                        var t = receiveFromServer.next()
////                        System.out.println("aaaaaa服务器发来的${t}")
////                    }
//                    break
//                }
//
//                val verifyResFromServerJSON: String
//                var finalResult:String= ""
//                while (true) { //等待服务端发来返回结果
//                    //证明生成不用身份验证//SendToServer.println(str1) //向将uesr信息的串传给服务器
//                    if (receiveFromServer.hasNext()) {
//                        verifyResFromServerJSON = receiveFromServer.next()//接收结果
//                        finalResult = verifyResFromServerJSON + "@" + JSON.toJSONString(ca1)
//                        System.out.println("aaaaaa服务器发来的${finalResult}")
//                        break//跳出
//                    }
//                }
//                threadResult = finalResult//这个结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康吗的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构
//            } catch (e: IOException) {
//                //logInfo("failed")
//                e.printStackTrace()
//                threadResult = ""
//            } catch (e: Exception) {
//                //logInfo("failed")
//                e.printStackTrace()
//                threadResult = ""
//            }
//        })
//        e11.start()
//证明者与服务器通信成功
    }

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
            R.id.btn_search -> searchDevices()
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
                    adapter?.add(device)
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
                if (device!!.bondState != BluetoothDevice.BOND_BONDED) {
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
                            logInfo("已收到数据，数据大小：" + temp_data.length)
                            //从这里读取接收到的数据(temp_data)
                            if(is_witness){  //见证者
                                if(step_prove==0){
                                    logInfo("data = $temp_data")
                                    step_prove += 1
                                    val obj0 = JSONObject(temp_data)
                                    is_wt_prover_id = obj0.getString("proverID")
                                    val timep0 = obj0.getString("prover_send_time")
                                    val as_witness_1 = Witness_Receive(is_wt_prover_id,timep0)
                                    is_wt_witness_public_key = communication0.receive_prover(as_witness_1)
                                    hint1 = "证明者的id和时间戳，step = $step_prove"
                                }
                                else if(step_prove==2){
                                    step_prove += 1
                                    dataEncodew = temp_data
                                    hint1 = "由本见证者公钥加密过的证明者信息，step = $step_prove"
                                }

                            }
                            else{  //证明者
                                if(step_prove==1){
                                    step_prove += 1
                                    not_wt_witniess_pub_key1 = temp_data
                                    val locationinfop = Location_Info(180.0,180.0,0.0)
                                    val local_date_time1t = getTimeNow()
                                    val locationinfopJSONStr = JSON.toJSONString(locationinfop)
                                    val proverInfop = Prover_Info(not_wt_witniess_pub_key1,locationinfopJSONStr,local_date_time1t)
                                    dataEncodep = communication0.encrypt_info(proverInfop)
                                    hint1 = "见证者的公钥，step = $step_prove"
                                }
                                else if(step_prove==3){
                                    step_prove += 1
                                    polResp1Strp = temp_data
                                    hint1 = "见证者发来的polResp1Str，step = $step_prove"
                                }
                            }

                            logtext += "收到的数据类型：" + hint1 +"\n"
                            temp_data = ""
                        }

                        text_msg!!.post {
                            text_msg!!.setText(
                                logtext
                            )
                        }
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

            if(step_prove==0 && is_witness){
                step_prove += 1
                is_witness = false  //变为证明者
                val local_date_time = getTimeNow()
                val to_witness_1 = Witness_Receive("1",local_date_time)
                msg = JSON.toJSONString(to_witness_1)
                hint0 = "证明者id和时间戳，step = $step_prove"
            }
            if(is_witness){
                if(step_prove == 1){
                    step_prove += 1
                    msg = is_wt_witness_public_key
                    hint0 = "本见证者的公钥，step = $step_prove"
                }
                else if(step_prove == 3){
                    step_prove += 1
                    logInfo("准备解密该压缩包并与计算服务器通信")
//                    val time_now1 = getTimeNow()
//                    val polReq0 = PolReq(time_now1,is_wt_prover_id,dataEncodew)
//                    val polReq0Str = JSON.toJSONString(polReq0)
                    val myLocation1 = Location_Info(180.0,180.000016,0.0)
                    val myLocation1JSONString = JSON.toJSONString(myLocation1)
                    polResp1Strw = "faild"

                    toWitnessWork.dataEncodewt = dataEncodew
                    toWitnessWork.myLocation1JSONStringt = myLocation1JSONString
                    toWitnessWork.distancet = 2
//                    polResp1Strw = communication0.wiitness_work(dataEncodew,myLocation1JSONString,2)
                    val jsThread = JSThread()
                    jsThread.start()
                    var ii = 0
                    while(polResp1Strw=="faild"){
                        sleep(100)
                        ii += 1
                        if(ii>100) break
                    }
                    if(polResp1Strw.length<100){
                        logInfo(polResp1Strw)
                    }
                    else{
                        logInfo("数据过长不打出来了")
                    }

                    msg = polResp1Strw
                    hint0 = "发给证明者的polResp1Str，step = $step_prove"
                }
            }
            else{  //证明者
                if(step_prove == 2){
                    step_prove += 1
                    msg = dataEncodep
                    hint0 = "用见证者公钥加密过的你的信息，step = $step_prove"
                }
                else if(step_prove == 4){
                    step_prove += 1
                    logInfo("准备向中心服务器发起综合证明")
                    val three_Cret = Three_Cret(polResp1Strp,polResp1Strp,polResp1Strp)
                    val time_now2 = getTimeNow()
                    val locationinfop = Location_Info(180.0,180.0,0.0)
                    val locationInfopStr = JSON.toJSONString(locationinfop)

                    val zkThread = ZKThread(three_Cret,time_now2,locationInfopStr)
//                    val end01 = communication0.zk_verify(three_Cret,time_now2,locationInfopStr)
                    zkThread.start()
                    var ii0 = 0
                    while(end01=="end01"){
                        sleep(100)
                        ii0 += 1
                        if(ii0>100) break
                    }
                    if(end01.length < 500){
                        logInfo("得到的返回值："+end01)
                    }
                    else{
                        logInfo("返回值过长不打印 长度为${end01.length}")
                    }
                    hint0 = "忽略本条信息"
                }

            }


            val to_others = msg + "@"
            val bytes = to_others.toByteArray()
            if (outputStream != null) {
                try {
                    //发送数据
                    logtext += "发送数据：" + hint0 + "，数据大小：" + msg.length + "\n"
                    outputStream!!.write(bytes)
                    text_msg!!.post {
                        text_msg!!.setText(
                            logtext
                        )
                    }
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

    private inner class JSThread:Thread(){
        override fun run(){
            val fir = toWitnessWork.dataEncodewt
            val sed = toWitnessWork.myLocation1JSONStringt
            val thr = toWitnessWork.distancet
            polResp1Strw = communication0.wiitness_work(fir,sed,thr)
        }
    }

    private inner class ZKThread(val three_Cret0:Three_Cret,val time2:String,val locationOfProverStr0:String):Thread(){
        override fun run(){
            end01 = communication0.zk_verify(three_Cret0,time2,locationOfProverStr0)
        }
    }

    data class ToWitnessWork(var dataEncodewt:String,
                             var myLocation1JSONStringt:String,
                             var distancet:Int)

    private fun logInfo(mmm:String){
        logtext += mmm + "\n"
        text_msg!!.post {
            text_msg!!.setText(
                logtext
            )
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
}