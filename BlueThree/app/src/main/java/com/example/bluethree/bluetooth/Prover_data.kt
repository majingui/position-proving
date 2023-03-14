package com.example.bluethree.bluetooth

import com.example.bluethree.apis.Location_Info
import com.example.bluethree.apis.Three_Cret

class Prover_data {

    var Id = "0"
    var locationinfop = Location_Info(180.0,180.0,0.0)
    var myTime = "myTime"

    var con1 = true
    var con2 = true
    var con3 = true

    var complet1 = false
    var complet2 = false
    var complet3 = false

    var k1 = false
    var k2 = false
    var k3 = false

    var wit1 = Single_wit("pub_keyp","dataEncodep","polResp1Strp")
    var wit2 = Single_wit("pub_keyp","dataEncodep","polResp1Strp")
    var wit3 = Single_wit("pub_keyp","dataEncodep","polResp1Strp")

    fun init(){
        k1 = false
        k2 = false
        k3 = false
//        con1 = false
//        con2 = false
//        con3 = false
    }

    fun getThree_Cret(): Three_Cret {
        var po1 = "po"
        var po2 = "po"
        var po3 = "po"
        if(k1) po1 = wit1.polResp1Strp
        if(k2) po2 = wit2.polResp1Strp
        if(k3) po3 = wit3.polResp1Strp
        if(k1==false){
            if(k2) po1 = po2
            else po1 = po3
        }
        if(k2==false){
            if(k1) po2 = po1
            else po2 = po3
        }
        if(k3==false){
            if(k1) po3 = po1
            else po3 = po2
        }
        val threeCret = Three_Cret(po1,po2,po3)
        return threeCret
    }

    data class Single_wit(var pub_keyp:String,
                          var dataEncodep:String,
                          var polResp1Strp:String)
}

/***
 E0:CC:F8:62:A9:3B
 6C:06:D6:8B:A2:8D
 10:B1:F8:B3:D1:6E
 ***/
