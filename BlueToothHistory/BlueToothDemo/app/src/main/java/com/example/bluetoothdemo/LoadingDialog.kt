package com.example.bluetoothdemo

import android.app.Dialog
import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * 类说明：连接加载框
 */
class LoadingDialog {

    companion object {

        private lateinit var dialog: Dialog
        private lateinit var timer: CountDownTimer

        /**
         * 倒计时弹框
         */
        fun showTimerDialog(activity: AppCompatActivity) {
            showProgressDialog(activity)
            //计时10秒；每隔一秒执行
            timer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    Toast.makeText(activity, "蓝牙连接连接超时", Toast.LENGTH_SHORT).show()
                    closeTimerDialog()
                }
            }.start()
        }

        /**
         * 关闭计时器
         */
        fun closeTimerDialog() {
            closeDialog()
            timer.cancel()
        }

        /**
         * 显示弹框
         */
        private fun showProgressDialog(context: Context) {
            dialog = Dialog(context, R.style.dialog_style)
            dialog.setContentView(R.layout.loading_dialog)
            dialog.setCanceledOnTouchOutside(false)//点击外部不关闭
            dialog.setCancelable(false)//点击返回键不关闭
            dialog.show()//显示弹框
        }

        /**
         * 关闭弹框
         */
        private fun closeDialog() {
            if (null != dialog) {
                dialog.dismiss()
            }
        }
    }
}
