package com.skeletonkotlin.main.entrymodule.model

import androidx.databinding.ObservableField
import com.skeletonkotlin.Strings
import com.skeletonkotlin.main.base.BaseVM
import com.skeletonkotlin.main.common.ApiRenderState
import com.skeletonkotlin.main.entrymodule.repo.MainActRepo

class MainActVM(private val repo: MainActRepo) : BaseVM() {

    val emailData = ObservableField("abc")

    fun login(email: String, password: String) {
        scope {
            if (email.isEmpty()) {
                state.emit(ApiRenderState.ValidationError(Strings.app_name))
                return@scope
            }
            state.emit(ApiRenderState.Loading)

            repo.login(email, password, onApiError)?.let {
                state.emit(ApiRenderState.ApiSuccess(it))
                return@scope
            }
        }
    }

    fun insertData() {
        scope {
            repo.insertData()
        }
    }
}