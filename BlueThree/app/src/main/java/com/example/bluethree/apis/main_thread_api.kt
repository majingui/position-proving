package com.example.bluethree.apis

interface main_thread_api {
    //初始化产生用户公私钥方法
    fun initial()

    //时间比对函数，返回两个时间的差值，time1-time2，单位为秒
    fun compareTime(time1:String,time2:String):Int

    //关于轨迹比对以及轨迹上传
    fun compareAndGiveOffCa1(ca1StringFromServerToBeCompare:String,myCa1Json:Array<String>):Int

    //轨迹上传
    fun giveOffCa1(ca1Json:Array<String>):Boolean

    //用见证者公钥加密自己的位置信息和时间戳，返回加密后的串
    fun encrypt_info(proverInfo: Prover_Info): String//我加了一个返回值，返回加密后的串

    //向服务器请求ZK证明验证，返回验证是否成功
    fun zk_verify(threeCret: Three_Cret, time: String,locationOfProver:String): String
//第三个参数传的是证明者的位置信息，是Location_Info转JSON



}