package com.example.addui1.apis


import com.alibaba.fastjson.JSON
import com.example.addui1.backend.*
import java.io.IOException
import java.io.PrintStream
import java.net.Socket
import java.util.*
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import java.time.chrono.MinguoChronology
import kotlin.collections.HashMap

//最新的完备系【4.26改】

class communication:main_thread_api,wiitness_thread_api {

    var ID: String = "1"//该用户的id
    val name: String = "xinandasai"//用户名
    var user_PublicKey: String = ""//用户公钥
    var user_PrivateKey: String = ""//用户私钥
    var centerserverPublicKey: String = ""//中心服务器的公钥
    var aesKey: String = "0123456789abcdef"//aes密钥
    val serverIpAddress = "39.108.177.132" // 计算服务器ip
    val port = 8848  // 计算服务器端口号
    val centerserverIpAddress = "103.46.128.53"//中心服务器ip
    val centerport = 48819//中心服务器端口号
    val secondServerIpAddress = "103.46.128.53"
    val secondport = 16513


    fun locationChangeToInt(locationInfo: JSONObject):Location_Info_Int{
        var location_Info_Int:Location_Info_Int = Location_Info_Int(1,2,3)
        location_Info_Int.latitude = (locationInfo.getDouble("latitude")/0.00001141).toInt()
        location_Info_Int.longitude = (locationInfo.getDouble("longitude")/0.00000899).toInt()
        location_Info_Int.altitude =  (locationInfo.getDouble("altitude")).toInt()
        return location_Info_Int
    }


    override fun compareTime(time1:String,time2:String):Int{
        var flag = 65535
        if (time1.substring(0,14).equals(time2.substring(0,14))){
            var delta = Math.abs(time1.substring(14,16).toInt() - time2.substring(14,16).toInt())//左闭右开
            flag = delta
        }
        return flag
    }


    override fun initial() {//产生用户的公私钥//测试成功
        val KeyPair: Map<String, String> = RSA.generateKeyPair()//创建用户公私钥对
        var keys = Key(KeyPair["publicKey"]!!,KeyPair["privateKey"]!!)
        var keyText  = JSON.toJSONString(keys)
        var a = appio()
        var have = a.readID()
        if(have.equals("readERROR")){
            a.writeID(this.ID)
        }
        else{
            this.ID = a.readID()
        }
        a.writeRSAKey(keyText)//尝试写
        var str = a.readRSAKey()
        var b = JSONObject(str)
        this.user_PublicKey = b.getString("publicKey")
        this.user_PrivateKey = b.getString("privateKey")
    }

    override fun checkOfWitness(witnessReceive: Witness_Receive): Boolean {
        var flag: Boolean = true
        if (witnessReceive.proverID == "") {
            flag = false
        }
        if (false) {//以后根据需要检查时间会不会符合
            flag = false
        }
        return flag
    }

    override fun giveOffCa1(time:String,ca1ItemJson:List<String>):String{
        //参数为ca1证书item的列表,从文件中读取到一个串，用$切割成数组后传入,将其封装打包返回
        var flag  = true
        var mmap :MutableMap<String,String> = mutableMapOf()
        for(i in ca1ItemJson){
            if(i.equals("")){
                continue
            }
            var ca1ItemObject = JSONObject(i)
            var id = ca1ItemObject.getString("ca1Id")
            var ca1Json = ca1ItemObject.getString("ca1Json")
            mmap[id] = ca1Json
        }
        var ca1package = CA1package(time,mmap)
        var str  = JSON.toJSONString(ca1package)
        return str
    }

    override fun compareAndGiveOffCa1(ca1StringFromServerToBeCompare:String,ca1ItemJson:List<String>):String{
        //该方法参数为服务器发过来的要对比的阳性病例轨迹数据JSON，以及我的ca1证书JSON列表，返回值说明如下：
        //0：我的轨迹中没有与该轨迹重合的
        //1：我的轨迹中有与该轨迹重
        var returnValue:Int = 0
        var debug:String = "aaa"
        lateinit var ca1HashMap : HashMap<String,String>

        lateinit var obj:JSONObject
        lateinit var geo:String
        lateinit var time:String
        var deltaMin1 = 65535
        lateinit var t:changejson
        lateinit var ca1json:String
        lateinit var ca1:JSONObject
//现在锁定问题在这个函数，其他地方都是对的
        try {
            t = changejson()
        }catch (e:Exception){
            debug = debug + "bbb"
        }
        try {
            ca1HashMap= t.jsonToHashMap(ca1StringFromServerToBeCompare) //解出JSON列表
        }catch (e:Exception){
            debug = debug + "ccc"+ca1HashMap.toString()//看一下服务器的数据结构对不对
        }
        try {
            for(i in ca1ItemJson) {
                if (i.equals("")){
                    continue
                }//防止空串
                obj = JSONObject(i)
                ca1json = obj.getString("ca1Json")
                ca1 = JSONObject(ca1json)
                geo = ca1.getString("geohashOfProver_Long_hash")
                time = ca1.getString("time")
                if (ca1HashMap.containsValue(geo)) {
                    for (j in ca1HashMap.keys) {
                        if (ca1HashMap.getValue(j).equals(geo)) {
                            deltaMin1 = this.compareTime(j, time)
                            if (deltaMin1 < 5) {
                                returnValue = 1
                                return returnValue.toString()//+debug
                            }
                        }
                    }
                }
            }
        }catch (e:Exception){
            debug = debug + "ddd"
        }
        try {

        }catch (e:Exception){
            debug = debug + "eee"
        }
        try {

        }catch (e:Exception){
            debug = debug + "fff"
        }




        return returnValue.toString()//+debug
    }


    override fun encrypt_info(proverInfo: Prover_Info): String {
        //证明者用见证者的私钥加密，并返回证明者要给见证者的信息，里面有时间，证明者id，加密后的位置数据
        val witnessPublicKey = proverInfo.witness_public_key
        var data: String = proverInfo.prover_location
        var dataEncode: String = RSA.encrypt(data, witnessPublicKey)
        var myPolReq:PolReq = PolReq(proverInfo.prover_time,this.ID,dataEncode)
        var myPolReqJSON  =  JSON.toJSONString(myPolReq)
        return myPolReqJSON
    }

    override fun zk_verify(threeCret: Three_Cret, time: String,locationOfProver:String): String {
        //返回结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康码的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构，里面有时间戳和位置信息
        //如果该函数返回一个空串，说明见证者当中有一个的验证是无效的（可能是虚拟定位）
        var a = geohash()//统一长的取10位，短的取5位
        var b = JSONObject(locationOfProver)
        var geohashOfProver_Raw: String =
            a.encode(b.getDouble("latitude"), b.getDouble("longitude"))
        var geohashOfProver_Long = geohashOfProver_Raw.substring(0, 8)//取八位（暂时）

        var ca1: CA1 = CA1(time, locationOfProver, Digest.signMD5(geohashOfProver_Long, "UTF-8"))
        var geohashOfProver_Short = geohashOfProver_Raw.substring(0, 5)
        var ca2: CA2 = CA2("", "", "", "", "", "", "", geohashOfProver_Short)

        var cert1 = JSONObject(threeCret.crat1)
        var cert2 = JSONObject(threeCret.crat2)
        var cert3 = JSONObject(threeCret.crat3)//本质是Pol_Resp1
        var geohashOfWitness1 = cert1.getString("geohashOfWitness_Short")
        var geohashOfWitness2 = cert2.getString("geohashOfWitness_Short")
        var geohashOfWitness3 = cert3.getString("geohashOfWitness_Short")
        ca2.geohashOfProver_Short = geohashOfProver_Short

        var flag = false
        if (cert1.getString("zk1") == "") {
            flag = true
        }
        if (cert2.getString("zk1") == "") {
            flag = true
        }
        if (cert3.getString("zk1") == "") {
            flag = true
        }
        if (flag == true) {
            return ""
        }

        ca2.cert1 = threeCret.crat1//解析见证者的zk1
        ca2.cert2 = threeCret.crat2
        ca2.cert3 = threeCret.crat3
        ca2.idOfCA1 = time + this.ID
        ca2.hashOfCA1 = Digest.signMD5(JSON.toJSONString(ca1), "UTF-8")//摘要统一使用md5
        ca2.time = time
        ca2.idOfProver = this.ID

        val ca2String = JSON.toJSONString(ca2)
        val ca2StringEncoded = AES.encryptToBase64(ca2String, this.aesKey)
        //var threadResult: String = ""
        var debug = "hhh"

        val connect = Socket(centerserverIpAddress, centerport)
        val sendToServer = PrintStream(connect.getOutputStream())
        val receiveFromServer = Scanner(connect.getInputStream())

        //请求验证之前要先发送用户信息请求中心服务器的公钥
        val myDataToServerForPublicKey =
            UserData(this.ID, this.user_PublicKey)//我的公钥没用的，主要是要请求服务器的公钥
        val myDataToServerForPublicKeyJSON = JSON.toJSONString(myDataToServerForPublicKey)
        sendToServer.println(myDataToServerForPublicKeyJSON)
        while (true) {
            if (receiveFromServer.hasNext()) {//监听公钥并发送要验证的包
                this.centerserverPublicKey = receiveFromServer.next()
                val aesKeyEncoded = RSA.encrypt(this.aesKey, centerserverPublicKey)
                var verifyReq: CA2package = CA2package(ca2StringEncoded, aesKeyEncoded)
                val str2: String = JSON.toJSONString(verifyReq)
                sendToServer.println(str2)
                break
            }
        }

        var verifyResFromServerJSON: String
        var finalResult: String = ""
        lateinit var color: String

        while(true){
            if (receiveFromServer.hasNext()) {
                verifyResFromServerJSON = receiveFromServer.next()//接收结果
                lateinit var ca1Json: String
                lateinit var mca1Item: CA1Item
                lateinit var d: appio
                lateinit var ver: JSONObject

                try {
                    ca1Json = JSON.toJSONString(ca1)
                    mca1Item = CA1Item(ca1Json, ca2.idOfCA1)
                    finalResult = verifyResFromServerJSON
                    d = appio()
                    d.writeCA1(JSON.toJSONString(mca1Item))//写入文件ca1item
                    ver = JSONObject(verifyResFromServerJSON)
                    color = ver.getString("color")
                } catch (e: Exception) {
                    return "1 " + e.toString()
                }
                break
            }
        }

        if (color.equals("RED")) {
            lateinit var tmp: appio
            lateinit var str1: String
            lateinit var ca1ItemJsonArray: List<String>
            lateinit var packageUpLoading: String
            lateinit var packageUpLoadingEncoded: String
            try {
                tmp = appio()
                str1 = tmp.readCA1()
            } catch (e: Exception) {
                return "2 " + e.toString()
            }

            ca1ItemJsonArray = str1.split("$")
            try {
                packageUpLoading = this.giveOffCa1(time, ca1ItemJsonArray)
            } catch (e: Exception) {
                return "3.1 " + e.toString() + "\n"
            }
            try {
                packageUpLoadingEncoded = packageUpLoading
                //RSA.encrypt(packageUpLoading, this.centerserverPublicKey)
            } catch (e: Exception) {
                return "3.2 " + e.toString() + "\n" + str1
            }
            try {
                sendToServer.println(packageUpLoadingEncoded)
            } catch (e: Exception) {
                return "4 " + e.toString()
            }
        }

        if (color.equals("YELLOW")) {
            lateinit var toCheckJSON:String
            lateinit var tmp:appio
            lateinit var str1:String
            lateinit var ca1ItemJsonArray1:List<String>
            var danger:Int
            var dangerdebug = ""
            lateinit var packageUpLoading1:String
            lateinit var packageUpLoadingEncoded1:String

            try {
                if (receiveFromServer.hasNext()) {
                    debug = debug + "yyy"
                    toCheckJSON = receiveFromServer.next()
                    debug = debug + "kkk"
                }
            }catch(e:Exception){
                return debug+finalResult
            }

            try {
                tmp = appio()
                str1 = tmp.readCA1()
                debug = debug + "ttt"
                ca1ItemJsonArray1 = str1.split("$")
                debug = debug + "rrr"
            }catch (e:Exception) {
                return debug+finalResult
            }

            try {
                debug = debug + "vvv"
                dangerdebug =
                    this.compareAndGiveOffCa1(toCheckJSON, ca1ItemJsonArray1)
                debug = debug + "danger = " + dangerdebug + "sss"
            }catch (e:Exception) {
                return debug+finalResult
            }

            try {
                if (dangerdebug.equals("1")) {
                    debug = debug + "ccc"
                    packageUpLoading1 =
                        this.giveOffCa1(time, ca1ItemJsonArray1)//和上面是一样的，但是重名会不会有问题
                    packageUpLoadingEncoded1 = packageUpLoading1
                    debug = debug + "ppp"
                    sendToServer.println(packageUpLoadingEncoded1)
                    debug = debug + "uuu"
                } else {
                    debug = debug + "ooo"
                    sendToServer.println("__")
                }
            }catch (e:Exception) {
                return debug+finalResult
            }

//            try {
//
//            }catch (e:Exception) {
//                return debug+threadResult
//            }
//
//            try {
//
//            }catch (e:Exception) {
//                return debug+threadResult
//            }
//
//            try {
//
//            }catch (e:Exception) {
//                return debug+threadResult
//            }

        }

        return debug+finalResult+"Yes"
    }


    override fun receive_prover(witnessReceive: Witness_Receive): String {//见证者接受证明者的id和时间戳，返回见证者公钥
        if (checkOfWitness(witnessReceive) == true) {
            return this.user_PublicKey
        } else {
            return ""
        }
    }


    override fun wiitness_work(
        packageFromProver: String,//直接把证明者通过蓝牙发过来的包传进来
        mylocation: String,//见证者自己的位置信息JSON，对象是一个Location_Info
        distance: Int//蓝牙rssi读到的，截取到整数即可
    ): String {
        val a = geohash()
        //这个函数干几个活，第一是请求计算服务器得到零知证明，第二是将计算服务器的结果拆开，一个给证明者，一个给中心服务器，最后的返回值是要给证明者的JSON证书
        //返回的对象中如果zk1成员为空串，则说明零知生成失败（位置过远）
        if (true) {//先解密位置信息
            var errordata = "no_error"
            var ke = false

            var tmpMyLocation = JSONObject(mylocation)
            val mylocationOfInt = locationChangeToInt(tmpMyLocation)

            var tmpPackageFromProver = JSONObject(packageFromProver)
            var time1 = tmpPackageFromProver.getString("time")
            var idOfProver = tmpPackageFromProver.getString("idOfprover")
            var location1 = tmpPackageFromProver.getString("locationOfproverEncode")
            var locationDataOfProver: String =
                RSA.decrypt(location1, this.user_PrivateKey)//解密位置信息,得到JSON
            val locationDataOfPolReq = JSONObject(locationDataOfProver)//恢复位置信息对象
            val locationDataOfPolReqOfInt = locationChangeToInt(locationDataOfPolReq)

            var px: Int =
                Math.abs(mylocationOfInt.longitude - locationDataOfPolReqOfInt.longitude)
            var py: Int = Math.abs(mylocationOfInt.latitude - locationDataOfPolReqOfInt.latitude)
            var pz: Int = Math.abs(mylocationOfInt.altitude - locationDataOfPolReqOfInt.altitude)
            //封装
            var PackageToServer:ZeroKReq = ZeroKReq(1,2,3,4)
            PackageToServer.delta_x = px
            PackageToServer.delta_y = py
            PackageToServer.delta_z = pz
            PackageToServer.r = distance
            //下面开始发包
            var threadResult:String = ""//存放线程的返回结果
            try {
                val client = Socket(serverIpAddress, port) // 连接计算服务器
                val SendToServer =
                    PrintStream(client.getOutputStream())//创建一个向服务器传送数据的printstream类的实例叫SendtoServer，里面是流的参数
                val RecieveFromServer = Scanner(client.getInputStream())//接受服务器数据的流

                val P_Data = JSON.toJSONString(PackageToServer)
                SendToServer.println(P_Data)

                while (true) { // 等待计算服务器传来的zk
                    if (RecieveFromServer.hasNext()) {
                        val zk: String = RecieveFromServer.next()
                        val zks: List<String> = zk.split("}")
                        val zk1 = zks[0] + "}"
                        val zk2 = zks[1] + "}"

                        //封装zk2
                        var Pol_Resp22: Pol_Resp2 = Pol_Resp2("","","","","")
                        Pol_Resp22.time = time1
                        Pol_Resp22.idOfWitness = idOfProver
                        Pol_Resp22.zk2 = zk2
                        Pol_Resp22.SignofZk = RSA.sign(zk2, this.user_PrivateKey)//有一个问题，服务器怎么知道见证者公钥的，以及app一启动就生成公私钥，也就是公私钥是动态的，会不会有问题
                        val str1 = JSON.toJSONString(Pol_Resp22)
                        //zk2要发给二级服务器，再用一个和连接的socket
                        val client2 = Socket(this.secondServerIpAddress, this.secondport)
                        val sendToSecondServer = PrintStream(client2.getOutputStream())

                        while (true) {
                            sendToSecondServer.println(str1)//zk2发给中心服务器
                            break//跳出
                        }
                        //封装zk1//zk1要发给证明者
                        var Pol_Resp11: Pol_Resp1 = Pol_Resp1("","","","","")
                        Pol_Resp11.time = time1
                        Pol_Resp11.idOfWitness = idOfProver
                        Pol_Resp11.zk1 = zk1
                        Pol_Resp11.SignofZk = RSA.sign(zk1, this.user_PrivateKey)
                        Pol_Resp11.geohashOfWitness_Short = a.encode(
                            tmpMyLocation.getDouble("latitude"),
                            tmpMyLocation.getDouble("longitude")).substring(0,5)//这里传的是长的geohash，看情况截取
                            //返回zk1给证明者
                        val toProver: String =
                            JSON.toJSONString(Pol_Resp11)//统一所有的JSON用JSON.toJSONString和JSONObject
                        threadResult = toProver
                            break
                        }
                    }
                } catch (e: IOException) {
                    errordata = "错误位置2：\n" + e.toString()
                    ke = true
                } catch (e: Exception) {
                    errordata = "错误位置3：\n" + e.toString()
                    ke = true
                }
            if(ke){
                return errordata
            }
            //返回结果
            return threadResult
        }//if的
        return ""//这句永远不会执行 只是为了适应返回值要求
    }
}
