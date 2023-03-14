package com.example.bluethree.apis

import java.io.IOException
import java.io.PrintStream
import java.lang.Exception
import java.net.Socket
import java.util.*

class connectSocket(ip: String,port: Int):socket_api {
    var sc: Socket = Socket(ip,port)
    var sendToServer = PrintStream(sc.getOutputStream())
    var receiveFromServer = Scanner(sc.getInputStream())
    var isConnect = sc.isConnected()

    override fun sendMessage(message: String?):Boolean {
        val messageToServer = message
        var flag = true
        try {
            if (this.isConnect) {
                if (messageToServer!= null) {
                    sendToServer.println(messageToServer)
                    sendToServer.flush()
                } else {
                    //System.out.println("The message to be sent is empty or have no connect")
                    flag = false
                }

            } else {
                flag = false
                //System.out.println("no connect to send message")
            }
        } catch (e: IOException) {
            flag = false
            //System.out.println("send message to cilent failed")
            e.printStackTrace()
        }catch (e:Exception){
            flag = false
            e.printStackTrace()
        }
        return flag
    }

    override fun receiveMessage(): String {
        var messageFromServer: String = ""
        try {
            if (this.isConnect==true) {
                //System.out.println("开始接收服务端信息")
                while (true) {
                    if (receiveFromServer.hasNext()) {
                        messageFromServer = receiveFromServer.next()
                        break
                    }
                }
            } else {
                //System.out.println("no connect to receive message")
            }
        } catch (e: IOException) {
            //System.out.println("receive message failed")
            e.printStackTrace()
        }catch (e:Exception){
            e.printStackTrace()
        }
        return messageFromServer
    }

    override fun closeSocket() {
        try {
                receiveFromServer.close()
                sendToServer.close()
                sc.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        isConnect = false
        //System.out.println("关闭连接")
    }

    companion object {
        //以后可放静态变量
    }
}
