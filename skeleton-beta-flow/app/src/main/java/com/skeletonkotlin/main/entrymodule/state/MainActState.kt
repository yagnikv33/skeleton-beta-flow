package com.skeletonkotlin.main.entrymodule.state

import com.skeletonkotlin.data.model.response.LoginResModel

sealed class MainActState {
    object Idle : MainActState()
    object Loading : MainActState()
    object ValidationError : MainActState()
    data class ApiSuccess(val loginResModel: LoginResModel) : MainActState()
}