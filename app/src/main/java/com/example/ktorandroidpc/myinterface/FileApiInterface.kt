package com.example.ktorandroidpc.myinterface

import com.example.ktorandroidpc.utills.Const
import okhttp3.MultipartBody
import retrofit2.Retrofit
import retrofit2.http.Multipart
import retrofit2.http.Part

interface FileApiInterface {
    @Multipart
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    )

    companion object{
        val instance by lazy {
            Retrofit.Builder()
                .baseUrl("http://${Const.ADDRESS}")
                .build()
                .create(FileApiInterface::class.java)
        }
    }
}