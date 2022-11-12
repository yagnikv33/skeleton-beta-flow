package com.skeletonkotlin.api.service

import com.skeletonkotlin.AppConstants.Api.EndUrl.LOGIN
import com.skeletonkotlin.AppConstants.Api.EndUrl.SIGN_UP
import com.skeletonkotlin.data.model.response.LoginResModel
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface EntryApiModule {

    @FormUrlEncoded
    @POST(LOGIN)
    suspend fun login(
        @Field("username") uName: String,
        @Field("password") pass: String
    ): LoginResModel

}



