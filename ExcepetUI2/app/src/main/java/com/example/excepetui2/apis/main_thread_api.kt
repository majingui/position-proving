package com.example.excepetui2.apis

interface main_thread_api {
    //用见证者公钥加密自己的位置信息和时间戳，返回加密后的串
    fun encrypt_info(proverInfo: Prover_Info): String//我加了一个返回值，返回加密后的串

    //向服务器请求ZK证明验证，返回验证是否成功
    fun zk_verify(threeCret: Three_Cret, time: String): String
}