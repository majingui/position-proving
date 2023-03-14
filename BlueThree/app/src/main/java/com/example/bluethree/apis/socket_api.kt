package com.example.bluethree.apis

interface  socket_api{
    fun sendMessage(message: String?):Boolean//该方法向服务器发送信息，返回发送是否成功

    fun receiveMessage(): String//该方法从服务器获取信息，返回获取的信息

    fun closeSocket()//该方法关闭该socket

}