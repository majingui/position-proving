package com.example.excepetui2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.example.excepetui2.apis.CA1
import java.io.PrintStream
import java.net.Socket
import java.util.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var textView: TextView

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        textView = findViewById(R.id.info6)


        logInfo("视图创建成功",1)

        val client = Socket(serverIpAddress, port) // 连接计算服务器
        val SendToServer =
            PrintStream(client.getOutputStream())//创建一个向服务器传送数据的printstream类的实例叫SendtoServer，里面是流的参数
        val RecieveFromServer = Scanner(client.getInputStream())//接受服务器数据的流
        val P_Data = "aasdasfg"
            SendToServer.println(P_Data)

        logInfo("已传输",1)


    }


    val sb  = StringBuilder()  //存放要显示的字符串
    private fun logInfo(msg:String ,start_type:Int){
        if(start_type == 0){
//            sb.apply {
//                append(msg).append("\n")
//            }
        }
        else {
            runOnUiThread {
                sb.apply {
                    append(msg).append("\n")
                    textView.text = toString()
                }
            }
        }
    }
}