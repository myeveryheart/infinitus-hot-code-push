package com.infinitus.hcp.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.infinitus.hcp.config.HCPInternalPreferences;

/**
 * Created by M on 16/9/9.
 * <p/>
 * 保存 shared preferences
 *
 * @see HCPInternalPreferences
 * @see IObjectPreferenceStorage
 * @see SharedPreferences
 */
public class HCPInternalPreferencesStorage implements IObjectPreferenceStorage<HCPInternalPreferences> {

    private static final String PREF_FILE_NAME = "hcp_hcp_config_pref";
    private static final String PREF_KEY = "config_json";

    private SharedPreferences preferences;

    /**
     * Class constructor
     *
     * @param context application context
     */
    public HCPInternalPreferencesStorage(Context context) {
        preferences = context.getSharedPreferences(PREF_FILE_NAME, 0);
    }

    @Override
    public boolean storeInPreference(HCPInternalPreferences config) {
        if (config == null) {
            return false;
        }

        preferences.edit().putString(PREF_KEY, config.toString()).apply();

        return true;
    }

    @Override
    public HCPInternalPreferences loadFromPreference() {
        final String configJson = preferences.getString(PREF_KEY, null);
        if (configJson == null) {
            return null;
        }

        return HCPInternalPreferences.fromJson(configJson);
    }
}