package com.example.ktorandroidpc.utills


import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object DataManager {
    lateinit var sharedPreferences: SharedPreferences

    fun with(activity: Application): DataManager {
        sharedPreferences = activity.getSharedPreferences(
            Const.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
        )
//        sharedPreferences = activity.getPreferences(Activity.MODE_MULTI_PROCESS)
        return this
    }

    fun <T> put(`object`: T, key: String) {
        //Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(`object`)
        //Save that String in SharedPreferences
        sharedPreferences.edit().putString(key, jsonString).apply()
    }

    inline fun <reified T> get(key: String): T? {
        //We read JSON String which was saved.
        val value = sharedPreferences.getString(key, null)
        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of
        //type Class < T >" is used to cast.
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    fun <T> savePreferenceData(dataclass: T, key: String) {
        val prefEdit = sharedPreferences.edit()
        val data = Gson().toJson(dataclass)
        prefEdit.putString(key, data)
        prefEdit.apply()
    }

    fun retrievePreferenceData(key: String): ArrayList<FileModel> {
        val gs = Gson()
        val data = sharedPreferences.getString(key, null)
        val jobj1 = JSONArray(data)
        val type = ArrayList<FileModel>()
        for (i in 0 until  jobj1.length()){
            val d = jobj1.getJSONObject(i)
            type.add(gs.fromJson(d.toString(), FileModel::class.java))
            Tools.debugMessage(d.toString(), "CATCH")
        }
        return type
    }
}
