package com.example.exceptui5.apis


import com.alibaba.fastjson.JSON
import com.example.exceptui5.backend.*
import java.io.IOException
import java.io.PrintStream
import java.net.Socket
import java.util.*
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;



class communication:main_thread_api,wiitness_thread_api {

    val ID: String = "1"//该用户的id
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
        val flag = 1
        return flag//没写完
    }

    override fun initial() {//产生用户的公私钥
        val KeyPair: Map<String, String> = RSA.generateKeyPair()//创建用户公私钥对
        this.user_PrivateKey = KeyPair["privateKey"]!!//这里会不会有问题，原来是toString（）
        this.user_PublicKey = KeyPair["publicKey"]!!
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

    override fun giveOffCa1(ca1Json:Array<String>):Boolean{//上传ca1证书,如果zkverify中返回的信息带有上传ca1标志或者请求对比后进行ca1对比再上传标志，则在外面再调用api
        //参数为ca1证书JSON的列表,将其转格式后发给服务器，返回值为成功与否
        var flag  = true
        val ca1StringToServer = JSON.toJSONString(ca1Json)
        try{
            val mySocket = Socket(
                this.centerserverIpAddress,this.centerport
            )
            val sendTocCenterServer = PrintStream(mySocket.getOutputStream())
            sendTocCenterServer.println(ca1StringToServer)
        }catch (e:Exception){
            flag = false
        }
        return flag
    }

    override fun compareAndGiveOffCa1(ca1StringFromServerToBeCompare:String,myCa1Json:Array<String>):Int{
        //该方法参数为服务器发过来的要对比的阳性病例轨迹数据JSON，以及我的ca1证书JSON列表，返回值说明如下：
        //0：我的轨迹中没有与该轨迹重合的，不用上传
        //1：我的轨迹中有与该轨迹重合，已上传
        //2：我的轨迹中有与该轨迹重合的，但上传失败了
        val ca1StringFromServerToBeCompareObject = JSONObject(ca1StringFromServerToBeCompare) as Array<String>//解出JSON列表
        //如果用两层循环的话14天轨迹每分钟一次的话要进行4亿次运算，
        // 现设计ca1在存储时按照时间先后顺序进行存储和读取，然后在比对检索时可以利用类似于merge合并算法来进行比对
        var point1:Int = 0
        var point2:Int = 0
        var flag : Boolean = false//有没有密接关系
        var flag1: Boolean = true//有没有上传成功
        while (point1 < ca1StringFromServerToBeCompareObject.size && point2 < myCa1Json.size){
            var content1 = JSONObject(ca1StringFromServerToBeCompareObject[point1]) as CA1
            var content2 = JSONObject(myCa1Json[point2]) as CA1
            break
        //if ()
        }
        //flag
        return point1//没写完
    }

    override fun encrypt_info(proverInfo: Prover_Info): String {//证明者用见证者的私钥加密，并返回证明者要给见证者的信息，里面有时间，证明者id，加密后的位置数据
        val witnessPublicKey = proverInfo.witness_public_key
        var data: String = proverInfo.prover_location
        var dataEncode: String = RSA.encrypt(data, witnessPublicKey)
        var myPolReq:PolReq = PolReq(proverInfo.prover_time,this.ID,dataEncode)
        var myPolReqJSON  =  JSON.toJSONString(myPolReq)
        return myPolReqJSON
    }

    override fun zk_verify(threeCret: Three_Cret, time: String,locationOfProver:String): String {//证明者向中心服务器请求零知验证并返回结果证书1和码色
        //返回结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康码的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构，里面有时间戳和位置信息
        var a = geohash()
        var b = JSONObject(locationOfProver)
        var geohashOfProver_Long = a.encode(b.getDouble("longitude"),b.getDouble("latitude"))
        var ca1: CA1 = CA1(time,locationOfProver,geohashOfProver_Long)
        var geohashOfProver_Short  = geohashOfProver_Long//以后要改
        var ca2: CA2 = CA2("","","","","","","",geohashOfProver_Short,"12345678","12345678","12345678")

        var cert1 = JSONObject(threeCret.crat1)
        var cert2 = JSONObject(threeCret.crat2)
        var cert3 = JSONObject(threeCret.crat3)//本质是Pol_Resp1
        var geohashOfWitness1 = cert1.getString("geohashOfWitness_Short")
        var geohashOfWitness2 = cert2.getString("geohashOfWitness_Short")
        var geohashOfWitness3 = cert3.getString("geohashOfWitness_Short")
        ca2.geohashOfProver_Short = geohashOfProver_Long//暂用
        ca2.geohashOfWitness1_Short = geohashOfWitness1
        ca2.geohashOfWitness2_Short = geohashOfWitness2
        ca2.geohashOfWitness3_Short = geohashOfWitness3

        ca2.cert1 = threeCret.crat1//解析见证者的zk1
        ca2.cert2 = threeCret.crat2
        ca2.cert3 = threeCret.crat3
        ca2.idOfCA1 = "001"
        ca2.hashOfCA1 = Digest.signMD5(JSON.toJSONString(ca1),"UTF-8")//摘要统一使用md5
        ca2.time = time
        ca2.idOfProver = this.ID
        val ca2String = JSON.toJSONString(ca2)
        val ca2StringEncoded = AES.encryptToBase64(ca2String, this.aesKey)
        var threadResult:String = ""
            try {
                val connect = Socket(centerserverIpAddress, centerport)
                val sendToServer = PrintStream(connect.getOutputStream())
                val receiveFromServer = Scanner(connect.getInputStream())
                //请求验证之前要先发送用户信息请求中心服务器的公钥
                val myDataToServerForPublicKey = UserData(this.ID,this.user_PublicKey)//我的公钥没用的，主要是要请求服务器的公钥
                val myDataToServerForPublicKeyJSON = JSON.toJSONString(myDataToServerForPublicKey)
                sendToServer.println(myDataToServerForPublicKeyJSON)
                while (true) {
                    //sendToServer.println(userInfo)
                    if (receiveFromServer.hasNext()) {//监听公钥并发送要验证的包
                        centerserverPublicKey = receiveFromServer.next()
                        val aesKeyEncoded = RSA.encrypt(this.aesKey, centerserverPublicKey)
                        var verifyReq: CA2package = CA2package(ca2StringEncoded,aesKeyEncoded)
//                        verifyReq.EncodedCa2 = ca2StringEncoded
//                        verifyReq.EncodedKey = aesKeyEncoded
                        val str2: String = JSON.toJSONString(verifyReq)
                        sendToServer.println(str2)
                        break
                    }
                }

                val verifyResFromServerJSON: String
                var finalResult:String= ""
                while (true) { //等待服务端发来返回结果
                    //证明生成不用身份验证//SendToServer.println(str1) //向将uesr信息的串传给服务器
                    if (receiveFromServer.hasNext()) {
                        verifyResFromServerJSON = receiveFromServer.next()//接收结果
                        finalResult = verifyResFromServerJSON + "@" + JSON.toJSONString(ca1)
                        break//跳出
                    }
                }
                threadResult = finalResult//这个结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康吗的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构
            } catch (e: IOException) {
                //logInfo("failed")
                e.printStackTrace()
                threadResult = ""
            } catch (e: Exception) {
                //logInfo("failed")
                e.printStackTrace()
                threadResult = ""
            }

        //thread1.join()
        return threadResult
    }


    override fun receive_prover(witnessReceive: Witness_Receive): String {//见证者接受证明者的id和时间戳，返回见证者公钥
        if (checkOfWitness(witnessReceive) == true) {
            return this.user_PublicKey
        } else {
            return ""//传入参数有问题，返回空串
        }
    }

    override fun wiitness_work(
        packageFromProver: String,//直接把证明者通过蓝牙发过来的包传进来
        mylocation: String,//见证者自己的位置信息JSON，对象是一个Location_Info
        distance: Int//蓝牙rssi读到的，截取到整数即可
    ): String {
        //这个函数干几个活，第一是请求计算服务器得到零知证明，第二是将计算服务器的结果拆开，一个给证明者，一个给中心服务器，最后的返回值是要给证明者的JSON证书
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
                            //println(R_ca2)
                            //切割
                            val zks: List<String> = zk.split("}")
                            val zk1 = zks[0] + "}"
                            val zk2 = zks[1] + "}"
                            //封装zk2
                            var Pol_Resp22: Pol_Resp2 = Pol_Resp2("","","","","")

                            Pol_Resp22.time = time1
                            Pol_Resp22.idOfWitness = idOfProver
                            Pol_Resp22.zk2 = zk2
                            var hashOfzk2 = Digest.signMD5(zk2,"UTF-8")
                            Pol_Resp22.SignofZk = RSA.sign(hashOfzk2, this.user_PrivateKey)//有一个问题，服务器怎么知道见证者公钥的，以及app一启动就生成公私钥，也就是公私钥是动态的，会不会有问题
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
                            //packageFromProver.time.also { Pol_Resp1.time = it }
                            Pol_Resp11.idOfWitness = idOfProver
                            Pol_Resp11.zk1 = zk1
                            var hashOfzk1 = Digest.signMD5(zk1,"UTF-8")
                            Pol_Resp11.SignofZk = RSA.sign(hashOfzk1, this.user_PrivateKey)
                            Pol_Resp11.geohashOfWitness_Short = "12345678"
                            //返回zk1给证明者
                            val toProver: String =
                                JSON.toJSONString(Pol_Resp11)//统一所有的JSON用JSON.toJSONString和JSONObject
                            threadResult = toProver
                            //break//跳出
                        }
                        break
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
