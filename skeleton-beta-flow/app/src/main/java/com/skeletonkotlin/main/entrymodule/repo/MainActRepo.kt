package com.skeletonkotlin.main.entrymodule.repo

import com.skeletonkotlin.api.service.EntryApiModule
import com.skeletonkotlin.data.model.response.LoginResModel
import com.skeletonkotlin.data.room.AppDatabase
import com.skeletonkotlin.data.room.entity.DoorEntity
import com.skeletonkotlin.main.base.ApiResult
import com.skeletonkotlin.main.base.BaseRepo

class MainActRepo(private val apiCall: EntryApiModule, private val db: AppDatabase) : BaseRepo() {

    suspend fun login(
        email: String,
        password: String,
        onError: ((ApiResult<Any>) -> Unit)?
    ): LoginResModel? {
        return with(apiCall(executable = { apiCall.login(email, password) })) {
            if (data == null)
                onError?.invoke(ApiResult(null, resultType, error, resCode = resCode))
            data
        }
    }

    fun insertData() {
        db.doorDao().insertAll(DoorEntity(0, 0, "s"))
    }
}