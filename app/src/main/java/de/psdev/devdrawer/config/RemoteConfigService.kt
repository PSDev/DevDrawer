package de.psdev.devdrawer.config

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import de.psdev.devdrawer.BuildConfig
import de.psdev.devdrawer.R
import de.psdev.devdrawer.utils.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigService @Inject constructor() {

    private val remoteConfig by lazy { Firebase.remoteConfig }

    init {
        val settings = remoteConfigSettings {
            if (BuildConfig.DEBUG) {
                minimumFetchIntervalInSeconds = 5
            }
        }
        remoteConfig.setConfigSettingsAsync(settings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
    }

    suspend fun getBoolean(key: String): Boolean {
        remoteConfig.fetchAndActivate().await()
        return remoteConfig[key].asBoolean()
    }

    suspend fun getInteger(key: String): Int {
        remoteConfig.fetchAndActivate().await()
        return remoteConfig[key].asLong().toInt()
    }

    suspend fun getLong(key: String): Long {
        remoteConfig.fetchAndActivate().await()
        return remoteConfig[key].asLong()
    }

}