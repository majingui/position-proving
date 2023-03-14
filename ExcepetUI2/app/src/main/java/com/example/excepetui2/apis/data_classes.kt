package com.example.excepetui2.apis

import com.alibaba.fastjson.JSON
import java.time.LocalDate
data class PolReq(
    var time:String,//时间戳
    var idOfprover:String,//证明者的id
    var locationOfproverEncode:String//加密后的位置信息，解密后转对象得到的是location_Info位置信息
)
data class Location_Info(val longitude:Int/*经度*/,
                         val latitude:Int/*纬度*/,
                         val altitude:Int/*高度*/)//位置信息对象

data class Prover_Info(val witness_public_key:String,
                       val prover_location:Location_Info,
                       val prover_time:String)//证明者收到的见证者的信息，见证者公钥，证明者位置信息，时间戳

data class Three_Cret(val crat1:String,
                      val crat2:String,
                      val crat3:String)//三份证书，都是JSON，只和服务器有关，不用知道对象

data class Witness_Receive(val proverID:String,
                           val prover_send_time: String)//见证者收到证明者的id和时间戳

data class Pol_Resp2(//从计算服务器中收到零知包的右边部分，是见证者1发给中心服务器的
    var time:String,
    var idOfWitness:String,
    var idOfprover: String,
    var zk2:String,
    var SignofZk:String)//见证者的签名

data class Pol_Resp1(//从计算服务器中收到零知包的左边部分，是见证者要返回给证明者的
    var time:String,
    var idOfWitness:String,
    var zk1:String,
    var SignofZk:String)//见证者的签名


data class UserData (//请求服务器公钥之前发的一个用户信息，也可以用于见证者发给证明者公钥（这个暂时没用）
    var id: String,
    var publicKey: String
)

data class ZeroKReq (//见证者给计算服务器的位置信息
    var delta_x: Int,
    var delta_y: Int,
    var delta_z: Int,
    var r: Int
)
data class CA1(//证书1
    val time: String,//时间戳
    val locationData:Location_Info//位置信息
)
data class CA2(//证书2
    var cert1:Pol_Resp1,//证明者收到见证者的三个包
    var cert2:Pol_Resp1,
    var cert3:Pol_Resp1,
    var hashOfCA1:String,
    var idOfCA1:String,
    var time:String,
    var idOfProver:String
)
data class CA2package(//发给中心服务器请求零知验证的
    var EncodedCa2:String,
    var EncodedKey:String//加了密的aes
)
data class VerifyRes(//中心服务器返回的信息
    val time:String,
    val color:String
)
