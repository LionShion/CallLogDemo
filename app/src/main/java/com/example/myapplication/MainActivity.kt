package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class MainActivity : AppCompatActivity() {
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0

    private val mPermissionList = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.WRITE_CALL_LOG
    )

    private val callUri = CallLog.Calls.CONTENT_URI;
    private val columns = arrayOf(
        CallLog.Calls.CACHED_NAME// 通话记录的联系人
        , CallLog.Calls.NUMBER// 通话记录的电话号码
        , CallLog.Calls.DATE// 通话记录的日期
        , CallLog.Calls.DURATION// 通话时长
        , CallLog.Calls.TYPE
    )// 通话类型}

    //通话记录的集合
    private val mList = mutableListOf<CallLogBean>()

    private var callLogAdapter: CallLogAdapter? = CallLogAdapter(mList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //初始化适配器
        rv_view.layoutManager = LinearLayoutManager(this)


        requestPermission()

        /**
         * 点击按钮请求权限
         */
        btn.setOnClickListener {
            //设置数据
            rv_view.adapter = callLogAdapter
        }

    }

    /**
     * 请求权限
     */
    private fun requestPermission() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            //权限申请
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CALL_LOG) &&
                ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_CALL_LOG)
            ) {
                Log.d("WelcomeActivity", "shouldShowRequestPermissionRationale")
                ActivityCompat.requestPermissions(
                    this,
                    mPermissionList,
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS
                )
            } else {
                Log.d("WelcomeActivity", "requestPermissions")
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this,
                    mPermissionList,
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS
                )
            }
        } else {
            //有权限直接跳转且登录状态，不需要调用此方法
            getContentCallLog()
        }
    }

    /**
     * 权限请求返回结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("WelcomeActivity", "onRequestPermissionsResult granted")
                    //同意权限跳转登录界面
                } else {
                    Log.d("WelcomeActivity", "onRequestPermissionsResult denied")
                    showWaringDialog()
                }
                return
            }
        }
        // other 'case' lines to check for other
        // permissions this app might request
    }

    /**
     * 拒绝权限的提示
     */
    private fun showWaringDialog() {
        AlertDialog.Builder(this)
            .setTitle("警告！")
            .setMessage("请前往设置->应用->PermissionDemo->权限中打开相关权限，否则功能无法正常运行！")
            .setPositiveButton("确定") { _, _ ->
                // 一般情况下如果用户不授权的话，功能是无法运行的，做退出处理
                finish()
            }.show()
    }

    //获取通话记录
    @SuppressLint("SimpleDateFormat", "Recycle", "SetTextI18n")
    fun getContentCallLog() {
        val cursor = contentResolver.query(
            callUri, // 查询通话记录的URI
            columns
            , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))  //姓名
            val number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)) //号码
            val dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)) //获取通话日期
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(dateLong))
            val time = SimpleDateFormat("HH:mm").format(Date(dateLong))
            val duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))//获取通话时长，值为多少秒
            val type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
            val dayCurrent = SimpleDateFormat("dd").format(Date())
            val dayRecord = SimpleDateFormat("dd").format(Date(dateLong))

            val callLogBean = CallLogBean()
            if (callLogBean.name != null) {
                callLogBean.name = name
            }else{
                callLogBean.name="null"
            }

            callLogBean.number = number
            callLogBean.dateLong = dateLong.toString()
            callLogBean.date = date

            mList.add(callLogBean)
        }
    }
}
