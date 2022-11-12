package ng.ohis.ktorandroidpc.utills


import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import ng.ohis.ktorandroidpc.utills.FileModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONArray

object DataManager {
    var sharedPreferences: SharedPreferences? = null

    fun setString(key: String, data: String?){
        sharedPreferences!!.edit().putString(key, data).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences!!.getString(key, null)
    }

    fun with(activity: Activity): DataManager {
        sharedPreferences = activity.getSharedPreferences(
            Const.PREFERENCES_FILE_NAME, Context.MODE_PRIVATE
        )
//        sharedPreferences = activity.getPreferences(Activity.MODE_MULTI_PROCESS)
        return this
    }

    fun <T> putPreferenceData(`object`: T, key: String) {
        //Convert object to JSON String.
        val jsonString = GsonBuilder().create().toJson(`object`)
        //Save that String in SharedPreferences
        sharedPreferences!!.edit().putString(key, jsonString).apply()
    }

    fun clearAllSharedPreference(){
        sharedPreferences!!.edit().clear().apply()
    }

    fun clearSharedPreferenceKey(key: String){
        sharedPreferences!!.edit().remove(key).apply()
    }

    inline fun <reified T> getPreferenceData(key: String): T? {
        //We read JSON String which was saved.
        val value = sharedPreferences!!.getString(key, null)
        //JSON String was found which means object can be read.
        //We convert this JSON String to model object. Parameter "c" (of
        //type Class < T >" is used to cast.
        return GsonBuilder().create().fromJson(value, T::class.java)
    }

    fun <T> savePreferenceData(dataclass: T, key: String) {
        if (sharedPreferences == null) return
        // make the sharedPreferences database editable
        val prefEdit = sharedPreferences!!.edit()
        // dataclass holds type of data and gson returns the data been held
        val data = Gson().toJson(dataclass)
        // store the data and assign a kry to it
        prefEdit.putString(key, data)
        //save the data
        prefEdit.apply()
    }

    fun retrievePreferenceData(key: String): ArrayList<FileModel> {
        if (sharedPreferences == null) return ArrayList()
        // get the jsonfyied data from sharedPreferences
        val data = sharedPreferences!!.getString(key, null)
        // because the json data is stored in list format
        // convert it to an array of json data
        val jsonArray = JSONArray(data)
        val arrayList = ArrayList<FileModel>()
        // loop through all the jsonArray
        for (i in 0 until jsonArray.length()) {
            // get each json and store it inside the arrayList
            val d = jsonArray.getJSONObject(i)
            // serialize the json data into the dataclass
            // (the arg of both json and dataclass are the same)
            arrayList.add(Gson().fromJson(d.toString(), FileModel::class.java))
        }
        return arrayList
    }
}
