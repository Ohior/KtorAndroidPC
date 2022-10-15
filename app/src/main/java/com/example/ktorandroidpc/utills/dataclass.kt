package com.example.ktorandroidpc.utills

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.example.ktorandroidpc.R
import com.example.ktorandroidpc.explorer.FileType
import com.squareup.picasso.Picasso
import java.net.URI


data class FileModel(
    val path: String,
    val fileType: FileType,
    val name: String,
    val sizeInMB: Double,
    val extension: String = "",
    val subFiles: Int = 0
)

data class FolderDataClass(var name: String, var occupied: Boolean = true)

data class RecyclerAdapterDataclass(
    val name: String,
    val detail: String,
    val drawable: Int? = null,
)

data class FileModelList(var fileModelList: List<FileModel>)