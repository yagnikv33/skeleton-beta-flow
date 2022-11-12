package com.skeletonkotlin.helper.custom.topsnackbar

import android.os.Handler
import android.os.Looper
import android.os.Message

import java.lang.ref.WeakReference

internal class SnackbarManager private constructor() {

    private val mLock: Any
    private val mHandler: Handler

    private var mCurrentSnackbar: SnackbarRecord? = null
    private var mNextSnackbar: SnackbarRecord? = null

    init {
        mLock = Any()
        mHandler = Handler(Looper.getMainLooper(), Handler.Callback { message ->
            when (message.what) {
                MSG_TIMEOUT -> {
                    handleTimeout(message.obj as SnackbarRecord)
                    return@Callback true
                }
            }
            false
        })
    }

    internal interface Callback {
        fun show()

        fun dismiss(event: Int)
    }

    fun show(duration: Int, callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                // Means that the callback is already in the queue. We'll just update the duration
                mCurrentSnackbar!!.duration = duration
                // If this is the TopSnackbar currently being shown, call re-schedule it's
                // timeout
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar)
                scheduleTimeoutLocked(mCurrentSnackbar!!)
                return
            } else if (isNextSnackbar(callback)) {
                // We'll just update the duration
                mNextSnackbar!!.duration = duration
            } else {
                // Else, we need to create a new record and queue it
                mNextSnackbar = SnackbarRecord(duration, callback)
            }

            if (mCurrentSnackbar != null && cancelSnackbarLocked(
                    mCurrentSnackbar!!,
                    TopSnackbar.Callback.DISMISS_EVENT_CONSECUTIVE
                )
            ) {
                // If we currently have a TopSnackbar, try and cancel it and wait in line
                return
            } else {
                // Clear out the current snackbar
                mCurrentSnackbar = null
                // Otherwise, just show it now
                showNextSnackbarLocked()
            }
        }
    }

    fun dismiss(callback: Callback, event: Int) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                cancelSnackbarLocked(mCurrentSnackbar!!, event)
            }
            if (isNextSnackbar(callback)) {
                cancelSnackbarLocked(mNextSnackbar!!, event)
            }
        }
    }

    /**
     * Should be called when a TopSnackbar is no longer displayed. This is after any exit
     * animation has finished.
     */
    fun onDismissed(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                // If the callback is from a TopSnackbar currently show, remove it and show a new one
                mCurrentSnackbar = null
                if (mNextSnackbar != null) {
                    showNextSnackbarLocked()
                }
            }
        }
    }

    /**
     * Should be called when a TopSnackbar is being shown. This is after any entrance animation has
     * finished.
     */
    fun onShown(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar!!)
            }
        }
    }

    fun cancelTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                mHandler.removeCallbacksAndMessages(mCurrentSnackbar)
            }
        }
    }

    fun restoreTimeout(callback: Callback) {
        synchronized(mLock) {
            if (isCurrentSnackbar(callback)) {
                scheduleTimeoutLocked(mCurrentSnackbar!!)
            }
        }
    }

    fun isCurrent(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentSnackbar(callback)
        }
    }

    fun isCurrentOrNext(callback: Callback): Boolean {
        synchronized(mLock) {
            return isCurrentSnackbar(callback) || isNextSnackbar(callback)
        }
    }

    private class SnackbarRecord internal constructor(var duration: Int, callback: Callback) {
        var callback: WeakReference<Callback>

        init {
            this.callback = WeakReference(callback)
        }

        internal fun isSnackbar(callback: Callback?): Boolean {
            return callback != null && this.callback.get() === callback
        }
    }

    private fun showNextSnackbarLocked() {
        if (mNextSnackbar != null) {
            mCurrentSnackbar = mNextSnackbar
            mNextSnackbar = null

            val callback = mCurrentSnackbar!!.callback.get()
            if (callback != null) {
                callback.show()
            } else {
                // The callback doesn't exist any more, clear out the TopSnackbar
                mCurrentSnackbar = null
            }
        }
    }

    private fun cancelSnackbarLocked(record: SnackbarRecord, event: Int): Boolean {
        val callback = record.callback.get()
        if (callback != null) {
            callback.dismiss(event)
            return true
        }
        return false
    }

    private fun isCurrentSnackbar(callback: Callback): Boolean {
        return mCurrentSnackbar != null && mCurrentSnackbar!!.isSnackbar(callback)
    }

    private fun isNextSnackbar(callback: Callback): Boolean {
        return mNextSnackbar != null && mNextSnackbar!!.isSnackbar(callback)
    }

    private fun scheduleTimeoutLocked(r: SnackbarRecord) {
        if (r.duration == TopSnackbar.LENGTH_INDEFINITE) {
            // If we're set to indefinite, we don't want to set a timeout
            return
        }

        var durationMs = LONG_DURATION_MS
        if (r.duration > 0) {
            durationMs = r.duration
        } else if (r.duration == TopSnackbar.LENGTH_SHORT) {
            durationMs = SHORT_DURATION_MS
        }
        mHandler.removeCallbacksAndMessages(r)
        mHandler.sendMessageDelayed(Message.obtain(mHandler, MSG_TIMEOUT, r), durationMs.toLong())
    }

    private fun handleTimeout(record: SnackbarRecord) {
        synchronized(mLock) {
            if (mCurrentSnackbar === record || mNextSnackbar === record) {
                cancelSnackbarLocked(record, TopSnackbar.Callback.DISMISS_EVENT_TIMEOUT)
            }
        }
    }

    companion object {

        private val MSG_TIMEOUT = 0

        private val SHORT_DURATION_MS = 1500
        private val LONG_DURATION_MS = 2750

        private var sSnackbarManager: SnackbarManager? = null

        val instance: SnackbarManager
            get() {
                if (sSnackbarManager == null) {
                    sSnackbarManager = SnackbarManager()
                }
                return sSnackbarManager!!
            }
    }

}