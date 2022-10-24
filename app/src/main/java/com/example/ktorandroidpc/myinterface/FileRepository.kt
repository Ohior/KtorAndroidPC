package com.example.ktorandroidpc.myinterface

import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class FileRepository {
    suspend fun uploadFile(file: File): Boolean{
        return try {
            FileApiInterface.instance.uploadFile(
                file = MultipartBody.Part
                    .createFormData(
                        name = "myFile",
                        filename = file.name,
                        body = file.asRequestBody()
                    )
            )
            true
        }catch (ioe: IOException){
            ioe.printStackTrace()
            false
        }catch (he: HttpException){
            he.printStackTrace()
            false
        }
    }
}