package com.example.demandmanagementsystem.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.demandmanagementsystem.model.User
import com.example.demandmanagementsystem.model.UserData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_data_preferences")
private object PreferencesKeys {
    val TC_IDENTITY_NO = stringPreferencesKey("tcIdentityNo")
    val EMAIL = stringPreferencesKey("email")
    val USERNAME = stringPreferencesKey("name")
    val PASSWORD = stringPreferencesKey("password")
    val TELNO = stringPreferencesKey("telNo")
    val AUTHORITY_TYPE = stringPreferencesKey("authorityType")
    val DEPARTMENT_TYPE = stringPreferencesKey("departmentType")

}
class UserDataPreferences(context: Context) {

    private val dataStore = context.dataStore

    private val _userData = MutableLiveData<UserData>()

    suspend fun saveUserData(user: UserData) {

        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TC_IDENTITY_NO] = user.tcIdentityNo
            preferences[PreferencesKeys.EMAIL] = user.email
            preferences[PreferencesKeys.USERNAME] = user.name
            preferences[PreferencesKeys.PASSWORD] = user.password
            preferences[PreferencesKeys.TELNO] = user.telNo
            preferences[PreferencesKeys.AUTHORITY_TYPE] = user.authorityType
            preferences[PreferencesKeys.DEPARTMENT_TYPE] = user.departmentType

        }
    }
    suspend fun removeUserData(){
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.TC_IDENTITY_NO)
            preferences.remove(PreferencesKeys.EMAIL)
            preferences.remove(PreferencesKeys.USERNAME)
            preferences.remove(PreferencesKeys.PASSWORD)
            preferences.remove(PreferencesKeys.TELNO)
            preferences.remove(PreferencesKeys.AUTHORITY_TYPE)
            preferences.remove(PreferencesKeys.DEPARTMENT_TYPE)
        }

    }

    val userDataFlow = dataStore.data
        .map {preferences ->
            val tcIdentityNo = preferences[PreferencesKeys.TC_IDENTITY_NO] ?: ""
            val email = preferences[PreferencesKeys.EMAIL] ?: ""
            val name = preferences[PreferencesKeys.USERNAME] ?: ""
            val password = preferences[PreferencesKeys.PASSWORD] ?: ""
            val telNo = preferences[PreferencesKeys.TELNO] ?: ""
            val authorityType = preferences[PreferencesKeys.AUTHORITY_TYPE] ?: ""
            val departmentType = preferences[PreferencesKeys.DEPARTMENT_TYPE] ?: ""

            UserData(
                tcIdentityNo,email, name, password, telNo, authorityType, departmentType
            )
        }



}