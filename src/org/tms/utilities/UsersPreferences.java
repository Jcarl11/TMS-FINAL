package org.tms.utilities;

import java.util.prefs.Preferences;
import org.json.JSONObject;

public class UsersPreferences {
	private UsersPreferences() {
	}

	private static UsersPreferences instance = null;

	public static UsersPreferences getInstance() {
		if (instance == null)
			instance = new UsersPreferences();
		return instance;
	}

	private Preferences preference = Preferences.userRoot().node("Utilities.UsersPreferences");

	public void userData(JSONObject data) {
		preference.put("objectId", data.getString("objectId"));
		preference.put("sessionToken", data.getString("sessionToken"));
		preference.put("username", data.getString("username"));
		preference.putBoolean("rememberpassword", data.getBoolean("rememberpassword"));
	}

	public void clearPreference() {
		preference.remove("objectId");
		preference.remove("sessionToken");
		preference.remove("username");
	}

	public Preferences getPreference() {
		return preference;
	}
}
