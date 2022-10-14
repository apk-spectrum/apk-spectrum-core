package com.apkspectrum.resource;

import java.awt.Image;
import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

public enum _RImg implements ResImage<Image>
{
	PERM_GROUP_ACCESSIBILITY_FEATURES	("perm_group/perm_group_accessibility_features.png"),
	PERM_GROUP_ACCOUNTS					("perm_group/perm_group_accounts.png"),
	PERM_GROUP_AFFECTS_BATTERY			("perm_group/perm_group_affects_battery.png"),
	PERM_GROUP_APP_INFO					("perm_group/perm_group_app_info.png"),
	PERM_GROUP_AUDIO_SETTINGS			("perm_group/perm_group_audio_settings.png"),
	PERM_GROUP_BLUETOOTH				("perm_group/perm_group_bluetooth.png"),
	PERM_GROUP_BOOKMARKS				("perm_group/perm_group_bookmarks.png"),
	PERM_GROUP_CALENDAR					("perm_group/perm_group_calendar.png"),
	PERM_GROUP_CALL_LOG					("perm_group/perm_group_call_log.png"),
	PERM_GROUP_CAMERA					("perm_group/perm_group_camera.png"),
	PERM_GROUP_CONTACTS					("perm_group/perm_group_contacts.png"),
	PERM_GROUP_COST_MONEY				("perm_group/perm_group_cost_money.png"),
	PERM_GROUP_DECLARED					("perm_group/perm_group_declared.png"),
	PERM_GROUP_DEVELOPMENT_TOOLS		("perm_group/perm_group_development_tools.png"),
	PERM_GROUP_DEVICE_ALARMS			("perm_group/perm_group_device_alarms.png"),
	PERM_GROUP_DISPLAY					("perm_group/perm_group_display.png"),
	PERM_GROUP_HARDWARE_CONTROLS		("perm_group/perm_group_hardware_controls.png"),
	PERM_GROUP_LOCATION					("perm_group/perm_group_location.png"),
	PERM_GROUP_MESSAGES					("perm_group/perm_group_messages.png"),
	PERM_GROUP_MICROPHONE				("perm_group/perm_group_microphone.png"),
	PERM_GROUP_NETWORK					("perm_group/perm_group_network.png"),
	PERM_GROUP_PERSONAL_INFO			("perm_group/perm_group_personal_info.png"),
	PERM_GROUP_PHONE_CALLS				("perm_group/perm_group_phone_calls.png"),
	PERM_GROUP_REVOKED					("perm_group/perm_group_revoked.png"),
	PERM_GROUP_SCREENLOCK				("perm_group/perm_group_screenlock.png"),
	PERM_GROUP_SENSORS					("perm_group/perm_group_sensors.png"),
	PERM_GROUP_SHORTRANGE_NETWORK		("perm_group/perm_group_shortrange_network.png"),
	PERM_GROUP_SMS						("perm_group/perm_group_sms.png"),
	PERM_GROUP_SOCIAL_INFO				("perm_group/perm_group_social_info.png"),
	PERM_GROUP_STATUS_BAR				("perm_group/perm_group_status_bar.png"),
	PERM_GROUP_STORAGE					("perm_group/perm_group_storage.png"),
	PERM_GROUP_SYNC_SETTINGS			("perm_group/perm_group_sync_settings.png"),
	PERM_GROUP_SYSTEM_CLOCK				("perm_group/perm_group_system_clock.png"),
	PERM_GROUP_SYSTEM_TOOLS				("perm_group/perm_group_system_tools.png"),
	PERM_GROUP_UNKNOWN					("perm_group/perm_group_unknown.png"),
	PERM_GROUP_USER_DICTIONARY			("perm_group/perm_group_user_dictionary.png"),
	PERM_GROUP_USER_DICTIONARY_WRITE	("perm_group/perm_group_user_dictionary_write.png"),
	PERM_GROUP_VOICEMAIL				("perm_group/perm_group_voicemail.png"),
	PERM_GROUP_WALLPAPER				("perm_group/perm_group_wallpaper.png"),

	RESOURCE_BACKGROUND			("resource_tap_image_background.jpg"),
	RESOURCE_BACKGROUND_DARK	("resource_tap_image_background_dark.jpg"),

	TREE_GLOBAL_SETTING			("configure-2.png"),
	TREE_NETWORK_SETTING		("internet-connection_manager.png"),
	TREE_CONFIG_SETTING			("kservices.png"),

	; // ENUM END

	private DefaultResImage res;

	private _RImg(String value) {
		res = new DefaultResImage(value);
	}

	@Override
	public String getValue() {
		return res.getValue();
	}

	@Override
	public String getConfiguration() {
		return res.getConfiguration();
	}

	@Override
	public String toString() {
		return res.toString();
	}

	@Override
	public String getPath() {
		return res.getPath();
	}

	@Override
	public URL getURL() {
		return res.getURL();
	}

	@Override
	public Image get() {
		return res.get();
	}

	@Override
	public Image getImage() {
		return res.getImage();
	}

	@Override
	public Image getImage(int w, int h) {
		return res.getImage(w, h);
	}

	@Override
	public ImageIcon getImageIcon() {
		return res.getImageIcon();
	}

	@Override
	public ImageIcon getImageIcon(int w, int h) {
		return res.getImageIcon(w, h);
	}

	@Override
	public URL getResource() {
		return res.getResource();
	}

	@Override
	public InputStream getResourceAsStream() {
		return res.getResourceAsStream();
	}
}
