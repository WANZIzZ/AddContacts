package com.wanzi.addcontacts

import android.os.Environment
import java.io.File

/**
 * Created by WZ on 2018-03-21.
 */
object FileUtils {

    private val list = ArrayList<File>()

    /**
     * 递归搜索根目录下指定后缀名的文件
     *
     * @param suffix 文件后缀
     * @return 符合条件的文件列表
     */
    fun recursiveSearchFileFromRoot(suffix: String): List<File> {
        list.clear()
        val root = Environment.getExternalStorageDirectory()
        recursiveSearchFile(root, suffix)
        return list
    }

    /**
     * 递归搜索指定后缀名的文件
     *
     * @param file    要搜索的目录
     * @param suffix  文件后缀
     */
    private fun recursiveSearchFile(file: File, suffix: String) {
        if (file.isDirectory) {
            val files = file.listFiles()
            if (files.isNotEmpty()) {
                for (childFile in files) {
                    recursiveSearchFile(childFile, suffix)
                }
            }
        } else {
            if (file.name.endsWith(suffix)) {
                list.add(file)
            }
        }
    }
}