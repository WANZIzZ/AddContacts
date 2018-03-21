package com.wanzi.addcontacts

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.dfsc.logistics_kotlin.adapter.GeneralAdapter
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jxl.Workbook
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.content.ContentUris
import android.provider.ContactsContract
import android.provider.ContactsContract.RawContacts
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

class MainActivity : AppCompatActivity() {

    private var adapter: GeneralAdapter<File>
    val data = ArrayList<File>()

    private var dialog: ProgressDialog? = null

    init {
        adapter = GeneralAdapter(R.layout.item_excel, data, BR.bean)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 检查权限
        val rxPermissions = RxPermissions(this)
        rxPermissions
                .request(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_CONTACTS
                )
                .subscribe {
                    if (!it) {
                        toast("请打开相关权限")
                        // 如果权限申请失败，则退出
                        android.os.Process.killProcess(android.os.Process.myPid())
                    }
                }

        rv.adapter = adapter

        if (dialog == null) {
            dialog = ProgressDialog(this)
            dialog!!.setMessage("添加中...")
            dialog!!.setCancelable(false)
        }

        // 下拉刷新
        swipe_layout.setOnRefreshListener {
            Observable
                    .create<List<File>> {
                        it.onNext(FileUtils.recursiveSearchFileFromRoot(".xls"))
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        swipe_layout.isRefreshing = false
                        data.clear()
                        data.addAll(it)
                        adapter.notifyDataSetChanged()
                    }
        }

        adapter.setOnItemClickListener { adapter, _, position ->
            Observable
                    .create<File> {
                        it.onNext(adapter.data[position] as File)
                        it.onComplete()
                    }
                    .flatMap {
                        val workBook = Workbook.getWorkbook(it)
                        val sheet = workBook.getSheet(0) // 获取第一张表格中的数据
                        val list = ArrayList<Contacts>()
                        // 行数
                        for (row in 0 until sheet.rows) {
                            list.add(Contacts(
                                    sheet.getCell(0, row).contents, // 第一列是姓名
                                    sheet.getCell(1, row).contents  // 第二列是号码
                            ))
                        }
                        workBook.close()
                        return@flatMap Observable.fromIterable(list)
                    }
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe {
                        if (!dialog!!.isShowing && dialog != null) {
                            dialog!!.show()
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(object : Observer<Contacts> {
                        override fun onComplete() {
                            if (dialog!!.isShowing && dialog != null) {
                                dialog!!.dismiss()
                            }
                            toast("添加完毕")
                        }

                        override fun onSubscribe(d: Disposable) {
                        }

                        override fun onNext(t: Contacts) {
                            addContact(t)
                        }

                        override fun onError(e: Throwable) {
                            toast("错误:${e.message}")
                        }

                    })
        }
    }

    fun addContact(contacts: Contacts) {
        // 联系人号码可能不止一个，例如 12345678901;12345678901
        val phone = contacts.phone.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // 创建一个空的ContentValues
        val values = ContentValues()
        // 向RawContacts.CONTENT_URI空值插入，
        // 先获取Android系统返回的rawContactId
        // 后面要基于此id插入值
        val rawContactUri = contentResolver.insert(RawContacts.CONTENT_URI, values)
        val rawContactId = ContentUris.parseId(rawContactUri)
        values.clear()

        values.put(Data.RAW_CONTACT_ID, rawContactId)
        // 内容类型
        values.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
        // 联系人名字
        values.put(StructuredName.GIVEN_NAME, contacts.name)
        // 向联系人URI添加联系人名字
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values)
        values.clear()

        values.put(Data.RAW_CONTACT_ID, rawContactId)
        values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
        // 联系人的电话号码
        values.put(Phone.NUMBER, phone[0])
        // 电话类型
        values.put(Phone.TYPE, Phone.TYPE_MOBILE)
        // 向联系人电话号码URI添加电话号码
        contentResolver.insert(Data.CONTENT_URI, values)
        values.clear()

        // 当联系人存在多个号码，第二个号码存在工作电话
        if (phone.size > 1) {
            values.put(Data.RAW_CONTACT_ID, rawContactId)
            values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE)
            // 联系人的工作电话号码
            values.put(Phone.NUMBER, phone[1])
            // 电话类型
            values.put(Phone.TYPE, Phone.TYPE_WORK_MOBILE)
            // 向联系人电话号码URI添加电话号码
            contentResolver.insert(Data.CONTENT_URI, values)
            values.clear()
        }
    }

    fun log(msg: String) {
        LogUtils.i(this, msg)
    }

    private fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, text, duration).show()
    }


}
