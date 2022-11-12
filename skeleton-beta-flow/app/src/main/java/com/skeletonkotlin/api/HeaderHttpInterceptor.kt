package com.skeletonkotlin.api

import com.skeletonkotlin.helper.util.PrefUtil
import okhttp3.Interceptor
import okhttp3.Response

class HeaderHttpInterceptor(private var prefUtil: PrefUtil) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request().run {
            newBuilder()
                .header("Accept", "application/json").apply {
                    prefUtil.authToken?.let {
                        header("Authorization", "Bearer $it")
                    }
                }.method(method, body).build()

        })
    }
}