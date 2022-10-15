package com.example.ktorandroidpc.utills

import com.example.ktorandroidpc.explorer.FileType


data class FileModel(
    val path: String,
    val fileType: FileType,
    val name: String,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0
)

data class FolderDataClass(var name:String, var occupied:Boolean=true)

data class RecyclerAdapterDataclass(val name: String, val detail:String, val image:Int?=null)

data class FileModelList(var fileModelList: List<FileModel>)