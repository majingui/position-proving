package com.example.excepetui2.apis

import com.alibaba.fastjson.JSON
import com.example.excepetui2.backend.*
import java.io.IOException
import java.io.PrintStream
import java.net.Socket
import java.util.*


class communication:main_thread_api,wiitness_thread_api {

    val ID: String = "1"//该用户的id
    val name: String = "xinandasai"//用户名
    lateinit var user_PublicKey: String//用户公钥
    lateinit var user_PrivateKey: String//用户私钥
    lateinit var centerserverPublicKey: String//中心服务器的公钥
    var aesKey: String = "0123456789abcdef"//aes密钥
    val serverIpAddress = "39.108.177.132" // 计算服务器ip
    val port = 8848  // 计算服务器端口号
    val centerserverIpAddress = "103.46.128.53"//中心服务器ip
    val centerport = 48819//中心服务器端口号
    val secondServerIpAddress = "103.46.128.53"
    val secondport = 16513
    lateinit var ca1: CA1//证书1

    /*fun register(){
        //向中心服务器发起注册
        val myregister = UserData(this.ID,this.user_PublicKey)
        val client = Socket(this.centerserverIpAddress,this.centerport)
        val sentToServer = PrintStream(client.getOutputStream())
    }*/

    fun initial() {//产生用户的公私钥
        val KeyPair: Map<String, String> = RSA.generateKeyPair()//创建用户公私钥对
        this.user_PrivateKey = KeyPair["privateKey"].toString()
        this.user_PublicKey = KeyPair["publicKey"].toString()
    }

    fun checkOfWitness(witnessReceive: Witness_Receive): Boolean {
        var flag: Boolean = true
        if (witnessReceive.proverID == "") {
            flag = false
        }
        if (false) {//以后根据需要检查时间会不会符合
            flag = false
        }
        return flag
    }

    override fun encrypt_info(proverInfo: Prover_Info): String {//证明者用见证者的私钥加密，并返回证明者的绝对位置等数据
        var data: String = JSON.toJSONString(proverInfo)//直接转json加密
        var dataEncode: String = RSA.encrypt(data, proverInfo.witness_public_key)
        return dataEncode
    }

    override fun zk_verify(threeCret: Three_Cret, time: String): String {//证明者向中心服务器请求零知验证并返回结果证书1和码色
        //返回结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康码的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构，里面有时间戳和位置信息
        lateinit var ca2: CA2
        ca2.cert1 = JSON.parseObject(threeCret.crat1) as Pol_Resp1//解析见证者的zk1
        ca2.cert2 = JSON.parseObject(threeCret.crat2) as Pol_Resp1
        ca2.cert3 = JSON.parseObject(threeCret.crat3) as Pol_Resp1
        ca2.idOfCA1 = "001"
        ca2.hashOfCA1 = Digest.digest(JSON.toJSONString(this.ca1))//先把ca1转json再哈希一下
        ca2.time = time
        ca2.idOfProver = this.ID
        val ca2String = JSON.toJSONString(ca2)
        val ca2StringEncoded = AES.encryptToBase64(ca2String, this.aesKey)

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
                    lateinit var verifyReq: CA2package
                    verifyReq.EncodedCa2 = ca2StringEncoded
                    verifyReq.EncodedKey = aesKeyEncoded
                    val str2: String = JSON.toJSONString(verifyReq)
                    sendToServer.println(str2)
                    break
                }
            }

            val verifyResFromServerJSON: String
            lateinit var finalResult:String
            while (true) { //等待服务端发来返回结果
                //证明生成不用身份验证//SendToServer.println(str1) //向将uesr信息的串传给服务器
                if (receiveFromServer.hasNext()) {
                    verifyResFromServerJSON = receiveFromServer.next()//接收结果
                    finalResult = verifyResFromServerJSON + "@" + JSON.toJSONString(this.ca1)
                    break//跳出
                }
            }
            return finalResult//这个结果是一个串，以@为分界线，你把它切成两半，左边是一个JSON，解析后里面有时间戳和健康吗的颜色，右边是证书1，用于本地存储,也是一个JSON，解析后可以看到ca1的结构
        } catch (e: IOException) {
            //logInfo("failed")
            e.printStackTrace()
            return ""
        } catch (e: Exception) {
            //logInfo("failed")
            e.printStackTrace()
            return ""
        }

    }


    override fun receive_prover(witnessReceive: Witness_Receive): String {//见证者接受证明者的id和时间戳，返回见证者公钥
        if (checkOfWitness(witnessReceive) == true) {
            return this.user_PublicKey
        } else {
            return ""//传入参数有问题，返回空串
        }
    }

    override fun wiitness_work(
        packageFromProver: PolReq,
        mylocation: Location_Info,
        distance: Int
    ): String {//
        //这个函数干几个活，第一是请求计算服务器得到零知证明，第二是将计算服务器的结果拆开，一个给证明者，一个给中心服务器，最后的返回值是要给证明者的JSON证书
        if (true) {//先解密位置信息
            var locationDataOfProver: String =
                RSA.decrypt(packageFromProver.locationOfproverEncode, this.user_PrivateKey)//解密位置信息
            val locationDataOfPolReq = JSON.parseObject(locationDataOfProver) as Location_Info//恢复位置信息对象
            var px: Int =
                Math.abs(mylocation.longitude - locationDataOfPolReq.latitude)//你按你的数据类型换一下吧，大概意思就是我的坐标减你的坐标
            var py: Int = Math.abs(mylocation.latitude - locationDataOfPolReq.longitude)
            var pz: Int = Math.abs(mylocation.altitude - locationDataOfPolReq.altitude)
            //封装
            lateinit var PackageToServer:ZeroKReq
            PackageToServer.delta_x = px
            PackageToServer.delta_y = py
            PackageToServer.delta_z = pz
            PackageToServer.r = distance
            //下面开始发包
            try {
                val client = Socket(serverIpAddress, port) // 连接计算服务器
                val SendToServer =
                    PrintStream(client.getOutputStream())//创建一个向服务器传送数据的printstream类的实例叫SendtoServer，里面是流的参数
                val RecieveFromServer = Scanner(client.getInputStream())//接受服务器数据的流
                while (true) { //这里方案改了
                    //计算服务器不用身份信息，也不用加密，所以不用请求公钥
                    /*if (RecieveFromServer.hasNext()) {//如果能接收到数据（有下一条消息），该方法返回真
                        val ServerPublicKey = RecieveFromServer.next() //服务端发来的公钥信息
                        val P_Data: String =
                            RSA.encrypt(JSON.toJSONString(PackageToServer), centerserverPublicKey)*/
                        val P_Data = JSON.toJSONString(PackageToServer)
                        SendToServer.println(P_Data)
                        break//跳出

                }
                while (true) { // 等待计算服务器传来的zk
                    if (RecieveFromServer.hasNext()) {
                        val zk: String = RecieveFromServer.next()
                        //println(R_ca2)
                        //切割
                        val zks: List<String> = zk.split("}")
                        val zk1 = zks[0] + "}"
                        val zk2 = zks[1] + "}"
                        //封装zk2
                        lateinit var Pol_Resp2: Pol_Resp2

                        packageFromProver.time.also { Pol_Resp2.time = it }
                        Pol_Resp2.idOfWitness = packageFromProver.idOfprover
                        Pol_Resp2.zk2 = zk2
                        Pol_Resp2.SignofZk = RSA.encrypt(zk1, this.user_PrivateKey)
                        val str1 = JSON.toJSONString(Pol_Resp2)

                        //zk2要发给二级服务器，再用一个和连接的socket
                        val client2 = Socket(this.secondServerIpAddress,this.secondport)
                        val sendToSecondServer = PrintStream(client2.getOutputStream())

                        while (true) {
                            //换方案了，直接发就行
                            //证明生成不用身份验证//SendToServer.println(str1) //向将uesr信息的串传给服务器
                            //if (RecieveFromServer.hasNext()) {//如果能接收到数据（有下一条消息），该方法返回真
                            //val ServerPublicKey = RecieveFromServer.next() //服务端发来的公钥信息
                            //println(ServerPublicKey)
                            // 用公钥加密AES密钥，得到加密过的AES密钥，赋值给CA2package类里面的EnCodedAESkey
                            //此时是相对位置，不加密
                            //val AESKey = "1234567812345678"
                            //val P_PrString: String =
                            //  RSA.encrypt(AESKey, ServerPublicKey) // RSA 使用服务端公钥加密后的AES密钥
                            //val P_Data1: String =
                            //RSA.encrypt(str1, serverPublicKey) // AES密钥来加密一个名为CA2的字符串得到正文密文
                            //ca2.setAESkey_Encode_ByRSA(P_PrString) // 将加密后的AES密钥赋值给AESkey_Encode_ByRSA
                            //ca2.setCA2(P_Data) // 将正文密文赋值给CA2package类里面的CA2
                            //val str2: String = JSON.toJSONString(ca2) // 将类ca2转化为JSON
                            sendToSecondServer.println(str1)//zk2发给中心服务器
                            break//跳出
                        }

                        //封装zk1//zk1要发给证明者
                        lateinit var Pol_Resp1: Pol_Resp1
                        packageFromProver.time.also { Pol_Resp1.time = it }
                        Pol_Resp1.idOfWitness = packageFromProver.idOfprover
                        Pol_Resp1.zk1 = zk1
                        Pol_Resp1.SignofZk = RSA.encrypt(zk1, this.user_PrivateKey)
                        //返回zk1给证明者
                        val toProver: String =
                            JSON.toJSONString(Pol_Resp1)//统一所有的JSON用JSONString和parseObject
                        return toProver
                        //break//跳出
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            return ""//返回空串
            //目前还没有查错条件，以后需要时用
        }
        return ""//这里一般不会到，除非通信出错加上异常没有预判到
    }
}
