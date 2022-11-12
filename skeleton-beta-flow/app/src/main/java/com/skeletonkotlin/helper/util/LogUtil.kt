package com.skeletonkotlin.helper.util

import android.text.TextUtils
import android.util.Log
import com.skeletonkotlin.BuildConfig

object LogUtil {

    const val VERBOSE = Log.VERBOSE
    const val DEBUG = Log.DEBUG
    const val INFO = Log.INFO
    const val WARN = Log.WARN
    const val ERROR = Log.ERROR
    const val WTF = Log.ASSERT

    private const val TAG = "LogHelper"

    fun log(level: Int, msg: String?, throwable: Throwable?) {
        if (BuildConfig.DEBUG) {
            val elements = Throwable().stackTrace
            var callerClassName = "?"
            var callerMethodName = "?"
            var callerLineNumber = "?"
            if (elements.size >= 4) {
                callerClassName = elements[3].className
                callerClassName = callerClassName.substring(callerClassName.lastIndexOf('.') + 1)
                if (callerClassName.indexOf("$") > 0) {
                    callerClassName = callerClassName.substring(0, callerClassName.indexOf("$"))
                }
                callerMethodName = elements[3].methodName
                callerMethodName = callerMethodName.substring(callerMethodName.lastIndexOf('_') + 1)
                if (callerMethodName == "<init>")
                    callerMethodName = callerClassName
                callerLineNumber = elements[3].lineNumber.toString()
            }

            val stack =
                "[$callerClassName.$callerMethodName():$callerLineNumber]" + if (TextUtils.isEmpty(
                        msg
                    )
                ) "" else " "

            when (level) {
                VERBOSE -> Log.v(TAG, stack + msg!!, throwable)
                DEBUG -> Log.d(TAG, stack + msg!!, throwable)
                INFO -> Log.i(TAG, stack + msg!!, throwable)
                WARN -> Log.w(TAG, stack + msg!!, throwable)
                ERROR -> Log.e(TAG, stack + msg!!, throwable)
                WTF -> Log.wtf(TAG, stack + msg!!, throwable)
            }
        }
    }

}

fun String.logD(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.DEBUG, this, throwable)
}

fun String.logV(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.VERBOSE, this, throwable)
}

fun String.logI(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.INFO, this, throwable)
}

fun String.logW(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.WARN, this, throwable)
}

fun String.logE(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.ERROR, this, throwable)
}

fun String.logWTF(throwable: Throwable? = null) {
    LogUtil.log(LogUtil.WTF, this, throwable)
}