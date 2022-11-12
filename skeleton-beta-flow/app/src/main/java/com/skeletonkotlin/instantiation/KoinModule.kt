package com.skeletonkotlin.instantiation

import android.content.Context
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.skeletonkotlin.AppConstants
import com.skeletonkotlin.BuildConfig
import com.skeletonkotlin.Strings
import com.skeletonkotlin.api.HeaderHttpInterceptor
import com.skeletonkotlin.api.service.EntryApiModule
import com.skeletonkotlin.data.room.AppDatabase
import com.skeletonkotlin.helper.util.NetworkUtil
import com.skeletonkotlin.helper.util.PrefUtil
import com.skeletonkotlin.main.entrymodule.model.MainActVM
import com.skeletonkotlin.main.entrymodule.repo.MainActRepo
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.random.Random

object KoinModule {

    val utilModule = module {
        single { PrefUtil(get()) }
        single { NetworkUtil(get()) }
        single {
            get<PrefUtil>().run {
                if (!hasKey(AppConstants.Prefs.ROOM_KEY))
                    roomKey = Random.nextDouble().toString()

                Room.databaseBuilder(
                    get(),
                    AppDatabase::class.java, "room-db"
                ).openHelperFactory(
                    SupportFactory(
                        SQLiteDatabase.getBytes(
                            roomKey.toCharArray()
                        )
                    )
                ).build()
            }
        }
    }

    val apiModule = module {
        factory {
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(
                        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
                    )
                )
                .addInterceptor(HeaderHttpInterceptor(get()))
                .build()
        }

        single {
            Retrofit.Builder().baseUrl(AppConstants.Api.BASE_URL)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().setPrettyPrinting().create()
                    )
                )
                .client(get()).build()
        }

        single { get<Retrofit>().create(EntryApiModule::class.java) }
    }

    val repoModule = module {
        factory { MainActRepo(get(), get()) }
    }

    val vmModule = module {
        viewModel { MainActVM(get()) }
    }
}