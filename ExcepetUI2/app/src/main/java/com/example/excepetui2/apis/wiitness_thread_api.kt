package com.example.excepetui2.apis

import android.location.Location
import com.alibaba.fastjson.JSON

interface wiitness_thread_api {
    //调用该函数，传入证明者ID和时间戳，返回见证者公钥
    fun receive_prover(witnessReceive: Witness_Receive): String

    //接收证明者的数据包，并发起zK生成请求，收到服务器的ZK包后，并签名并返回包。
    // 这个包里就是Pol_Resp,第一个串是服务器给的JSON不用管，后面附上见证者的私钥签名即可
    fun wiitness_work(packageFromProver: PolReq, mylocation: Location_Info, distance: Int):String//前面那个串是加了密的JSON
}
//注意wiit