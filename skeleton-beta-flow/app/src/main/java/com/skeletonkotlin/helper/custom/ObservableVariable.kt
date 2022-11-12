package com.skeletonkotlin.helper.custom

import java.util.*

class ObservableVariable<T>(initialValue: T?) : Observable() {
    var value: T? = initialValue
        get() = field
        set(value) {
            field = value
            notifyListeners()
        }

    fun notifyListeners() {
        setChanged()
        notifyObservers()
    }
}