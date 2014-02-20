package me.adobegfx.warps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class WarpsVersionCheck {
	public String getLatestVersion() throws MalformedURLException {
		String version = null;
		try {
			URL url = new URL(
					"https://raw2.github.com/AdobeGFX/PluginVerisons/master/VERSIONS.md");
			URLConnection urlconnect = url.openConnection();
			BufferedReader bufferedreader = new BufferedReader(
					new InputStreamReader(urlconnect.getInputStream()));

			boolean gotver = false;
			String l;
			while (((l = bufferedreader.readLine()) != null) && (!gotver)) {

				String[] tokens = l.split("[:]");
				if (tokens.length == 2) {
					if (tokens[0].equalsIgnoreCase("Plugin_Warps_Version")) {
						version = tokens[1].trim();
						gotver = true;
					}
				}
			}
			bufferedreader.close();
		} catch (IOException e) {
			return null;
		}
		return version;
	}

	public boolean compareVersion(String newversion, String oldversion) {
		if (newversion.equals(oldversion)) {
			return false;
		}
		return true;
	}
}