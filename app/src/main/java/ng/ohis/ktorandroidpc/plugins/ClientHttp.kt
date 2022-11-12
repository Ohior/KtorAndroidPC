package ng.ohis.ktorandroidpc.plugins


class ClientHttp{
    companion object{
        suspend fun clientHttp(function: ()->Unit){
            function()
        }
    }
}

//import ng.ohis.ktorandroidpc.Const
//import ng.ohis.ktorandroidpc.Tools
//import okhttp3.ResponseBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.http.GET
//import java.io.*
//
//
//interface FileDownloadClient {
//    //    https://upload.wikimedia.org/wikipedia/commons/thumb/7/75/Trifid_Nebula_by_Deddy_Dayag.jpg/330px-Trifid_Nebula_by_Deddy_Dayag.jpg
////    @GET("/upload")
//    @GET("wikipedia/commons/thumb/7/75/Trifid_Nebula_by_Deddy_Dayag.jpg/330px-Trifid_Nebula_by_Deddy_Dayag.jpg")
//    fun downloadFile(): Call<ResponseBody>
//}
//
//class ClientHttp {
//    companion object {
//        fun downloadFile(): FileDownloadClient {
//            val builder = Retrofit.Builder()
//                .baseUrl("https://upload.wikimedia.org/")
//            val retrofit = builder.build()
//            return retrofit.create(FileDownloadClient::class.java)
//        }
//    }
//
//    fun downLoadFile() {
//        val fileDownloadClient = downloadFile()
//        val call = fileDownloadClient.downloadFile()
//        call.enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                writeToRoDisk(response.body()!!)
//                Tools.debugMessage("Call back was successful")
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                Tools.debugMessage("Call back failure")
//            }
//        })
//    }
//
//    private fun writeToRoDisk(body: ResponseBody): Boolean {
//        try {
//            val mFile = File(Const.OH_UPLOAD_PATH)
//            var inputStream: InputStream? = null
//            var outputStream: OutputStream? = null
//            try {
//                val fileReader = ByteArray(4096)
//                val fileSize = body.contentLength()
//                var fileSizeDownload = 0
//                inputStream = body.byteStream()
//                outputStream = FileOutputStream(mFile)
//                while (true) {
//                    val read = inputStream.read(fileReader)
//                    outputStream.write(fileReader, 0, read)
//                    fileSizeDownload += read
//                    Tools.debugMessage("File downloaded $fileSizeDownload", "Size $fileSize")
//                    if (read == -1) break
//                }
//                outputStream.flush()
//                return true
//            } catch (e: IOException) {
//                e.printStackTrace();return false
//            } finally {
//                inputStream?.close()
//                outputStream?.close()
//                Tools.debugMessage("Say my name")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace(); return false
//        }
//    }
//}