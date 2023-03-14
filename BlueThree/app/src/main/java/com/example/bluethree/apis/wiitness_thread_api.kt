package com.example.bluethree.apis
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import android.location.Location


interface wiitness_thread_api {
    //见证者检查证明者的包是否合法（这个不用主动调用）
    fun checkOfWitness(witnessReceive: Witness_Receive): Boolean

    //调用该函数，传入证明者ID和时间戳，返回见证者公钥
    fun receive_prover(witnessReceive: Witness_Receive): String

    //接收证明者的数据包，并发起zK生成请求，收到服务器的ZK包后，并签名并返回包。
    fun wiitness_work(packageFromProver: String, mylocation: String, distance: Int):String//
}
