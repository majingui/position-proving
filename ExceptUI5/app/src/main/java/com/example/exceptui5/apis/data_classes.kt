package com.example.exceptui5.apis

import com.alibaba.fastjson.JSON
import java.time.LocalDate

data class PolReq(//证明者要通过蓝牙发给见证者的加密位置
    var time:String,//时间戳
    var idOfprover:String,//证明者的id
    var locationOfproverEncode:String//加密后的位置信息，解密后转对象得到的是location_Info位置信息，里面是三个double
)

data class Location_Info(val longitude:Double/*经度*/,
                         val latitude:Double/*纬度*/,
                         val altitude:Double/*高度*/)//位置信息对象

data class Location_Info_Int(var longitude:Int/*经度*/,
                         var latitude:Int/*纬度*/,
                         var altitude:Int/*高度*/)//位置信息对象

data class Prover_Info(//这个只是一个中转类，它作为一个参数，传入encrypt_info函数，方便进行信息打包
    var witness_public_key:String,
    val prover_location:String,//Location_Info转成的JSON
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
    var SignofZk:String,//见证者的签名
    var geohashOfWitness_Short:String//见证者模糊的geohash
    )



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
data class CA1(//证书1,现在有三项，时间戳，经纬度，经纬度拼起来的hash值
    val time: String,//时间戳
    val locationData:String,//Location_Info//位置信息,都是Double
    val geohashOfProver_Long:String//证明者自己精确的geohash
)

data class CA2(//证书2
    var cert1:String,//Pol_Resp1,//证明者收到见证者的三个包
    var cert2:String,//Pol_Resp1,
    var cert3:String,//Pol_Resp1,
    var hashOfCA1:String,
    var idOfCA1:String,
    var time:String,
    var idOfProver:String,
    var geohashOfProver_Short:String,
    var geohashOfWitness1_Short: String,
    var geohashOfWitness2_Short: String,
    var geohashOfWitness3_Short: String
)

data class CA2package(//发给中心服务器请求零知验证的
    var EncodedCa2:String,
    var EncodedKey:String//加了密的aes
)

data class VerifyRes(//中心服务器返回的信息
    val time:String,
    val color:String
)
//用户把CA1（时间加位置）存本地，发过去，服务器收
