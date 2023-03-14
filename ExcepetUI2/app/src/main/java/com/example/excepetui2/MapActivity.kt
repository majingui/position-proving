package com.example.excepetui2

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.MyLocationData


class MapActivity : AppCompatActivity() {

    private var mMapView:MapView? = null
    private lateinit var mBaiduMap:BaiduMap
    private lateinit var mLocationClient : LocationClient
    private lateinit var option : LocationClientOption
    private lateinit var myLocationListener : MyLocationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
//
//        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
//        SDKInitializer.initialize(getApplicationContext())
//        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
//        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
//        SDKInitializer.setCoordType(CoordType.BD09LL)

        mMapView = findViewById<View>(R.id.bmapView) as MapView
//        mBaiduMap = mMapView!!.map
//        mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL
//
//        initMap()  //初始化地图

    }

    private fun initMap(){
        mBaiduMap.isTrafficEnabled = true    //显示路况

        mBaiduMap.isMyLocationEnabled = true   //开启地图的定位图层

        mLocationClient = LocationClient(this)
        option = LocationClientOption()
        option.isOpenGps = true; // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocationClient.locOption = option //设置locationClientOption

        myLocationListener = MyLocationListener() //注册LocationListener监听器
        mLocationClient.registerLocationListener(myLocationListener)

        mLocationClient.start()  //开启地图定位图层
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return
            }
            val locData = MyLocationData.Builder()
                .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(location.direction).latitude(location.latitude)
                .longitude(location.longitude).build()
            mBaiduMap.setMyLocationData(locData)
        }
    }



    override fun onResume() {
        super.onResume()
        mMapView?.onResume()
    }

    override fun onPause() {
        mMapView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
//        mLocationClient.stop();
//        mBaiduMap.isMyLocationEnabled = false;
        mMapView?.onDestroy();
        mMapView = null
        super.onDestroy()
    }

}