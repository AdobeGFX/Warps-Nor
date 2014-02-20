package me.adobegfx.warps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Warps extends JavaPlugin {
	public Logger logger = Logger.getLogger("Minecraft");
	public WarpsVersionCheck vm = new WarpsVersionCheck();
	public String latestversion;
	private FileConfiguration warpConfig = null;
	private File warpConfigFile = null;

	public void onDisable() {
	}

	public void onEnable() {
		boolean vcOn = getConfig().getBoolean("CheckForLatestVersion");
		getConfig().options().copyDefaults(true);
		saveConfig();
		saveWarpFile();
		this.logger.info("****************** Warps ******************");
		if (vcOn) {
			VCThread check = new VCThread(this);
			check.start();
		} else {
			this.logger.info("[Warps] Version checker is disabled!");
		}
	}

	private class VC implements Runnable {
		private String message;

		private VC(String message) {
			this.message = message;
		}

		public void run() {
			Warps.this.logger.info("[Warps] " + this.message);
		}
	}

	private class VCThread extends Thread {
		private Warps wrp;

		private VCThread(Warps w) {
			this.wrp = w;
		}

		@SuppressWarnings("deprecation")
		public void run() {
			try {
				Warps.this.latestversion = Warps.this.vm.getLatestVersion();
				if (Warps.this.latestversion == null) {
					Warps.this.logger
							.info("[Warps] Could not find a newer Version than "
									+ Warps.this.getDescription().getVersion()
									+ ".");
				} else if (Warps.this.vm.compareVersion(
						Warps.this.latestversion, this.wrp.getDescription()
								.getVersion())) {
					this.wrp.getServer()
							.getScheduler()
							.scheduleSyncDelayedTask(
									this.wrp,
									new Warps.VC(
											"New version of Warps available: "
													+ Warps.this.latestversion
													+ ". You have version "
													+ Warps.this
															.getDescription()
															.getVersion()), 0L);
				}
			} catch (MalformedURLException mue) {
				Warps.this.logger
						.info("[Warps] Warps could not find a newer Version!");
			}
			stop();
		}
	}

	public void reloadWarpFile() {
		if (this.warpConfigFile == null) {
			this.warpConfigFile = new File(getDataFolder(), "warps.yml");
		}
		this.warpConfig = YamlConfiguration
				.loadConfiguration(this.warpConfigFile);

		InputStream defConfigStream = getResource("warps.yml");
		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.warpConfig.setDefaults(defConfig);
		}
	}

	public FileConfiguration getWarpFile() {
		if (this.warpConfig == null) {
			reloadWarpFile();
		}
		return this.warpConfig;
	}

	public void saveWarpFile() {
		if ((this.warpConfig == null) || (this.warpConfigFile == null)) {
			return;
		}
		try {
			getWarpFile().save(this.warpConfigFile);
		} catch (IOException ex) {
			getLogger()
					.log(Level.SEVERE,
							"Could not save warps.yml config to "
									+ this.warpConfigFile, ex);
		}
	}

	public boolean onCommand(CommandSender sender, Command command, String cmd,
			String[] args) {

		// Strings ***
		Player p = (Player) sender;

		String permission = ChatColor.RED
				+ "Du har ikke tilgang til denne kommandoen!";

		String usageWarp = ChatColor.RED + "Riktig bruk: /warp <warp-navn>";

		String usageWarps = ChatColor.RED + "Riktig bruk: /warps";

		String usageSetwarp = ChatColor.RED
				+ "Riktig bruk: /setwarp <warp-navn>";

		String usageDelwarp = ChatColor.RED
				+ "Riktig bruk: /delwarp <warp-navn>";

		String warpTeleportTo = ChatColor.GREEN + "Du ble teleportert til "
				+ ChatColor.RESET + "%warpnavn%";

		String warpDontExist = ChatColor.RED
				+ "Warpen du skrev inn eksisterer ikke!";

		String noWarpExist = ChatColor.RED + "Det eksisterer ingen warps!";
		String warpExist = ChatColor.RED + "Denne warpen eksisterer allerede!";

		String availableWarps = ChatColor.GREEN + "Tilgjengelige warps: "
				+ ChatColor.RESET;

		String warpsLength = ChatColor.GREEN + "Antall warps: "
				+ ChatColor.RESET;

		String newWarp = ChatColor.GREEN + "Du lagde ny warp på værden: "
				+ ChatColor.RESET + p.getWorld().getName();

		String delWarp = ChatColor.GREEN + "Du har fjernet warp "
				+ ChatColor.RESET + "%warpnavn%";

		if (!(sender instanceof Player)) {
			sender.sendMessage("Bare en spille kan bruke denne kommandoen!");
		} else {

			if (!p.hasPermission("warps.*")) {
			} else {

				if (cmd.equalsIgnoreCase("warp")) {

					if (!p.hasPermission("warps.warp")) {
						p.sendMessage(permission);

					} else if (args.length > 1 || args.length == 0) {
						p.sendMessage(usageWarp);

					} else if (!this.getWarpFile()
							.getConfigurationSection("Warps").getKeys(false)
							.contains(args[0].toLowerCase())) {
						p.sendMessage(warpDontExist);

					} else {

						String cworld = this.getWarpFile().getString(
								"Warps." + args[0].toLowerCase() + ".World");

						int x = this.getWarpFile().getInt(
								"Warps." + args[0].toLowerCase() + ".x"), y = this
								.getWarpFile()
								.getInt("Warps." + args[0].toLowerCase() + ".y"), z = this
								.getWarpFile()
								.getInt("Warps." + args[0].toLowerCase() + ".z");
						World world = Bukkit.getWorld(cworld);

						p.teleport(new Location(world, x, y, z));

						p.sendMessage(warpTeleportTo.replace("%warpnavn%",
								args[0].toLowerCase()));

					}
				} else if (cmd.equalsIgnoreCase("warps")) {
					if (!p.hasPermission("warps.warps")) {
						p.sendMessage(permission);

					} else if (args.length > 0) {
						p.sendMessage(usageWarps);

					} else if (this.getWarpFile().getString("Warps") != null) {
						p.sendMessage(availableWarps
								+ this.getWarpFile()
										.getConfigurationSection("Warps")
										.getKeys(false).toString()
										.replace("[", "").replace("]", ""));
						p.sendMessage(warpsLength
								+ this.getWarpFile()
										.getConfigurationSection("Warps")
										.getKeys(false).size());
					} else {
						p.sendMessage(noWarpExist);
					}
				} else if (cmd.equalsIgnoreCase("setwarp")) {
					if (!p.hasPermission("warps.setwarp")) {
						p.sendMessage(permission);

					} else if (args.length > 1 || 0 == args.length) {
						p.sendMessage(usageSetwarp);

					} else if (this.getWarpFile()
							.getConfigurationSection("Warps").getKeys(false)
							.contains(args[0].toLowerCase())) {
						p.sendMessage(warpExist);

					} else {
						this.getWarpFile().set(
								"Warps." + args[0].toLowerCase() + ".World",
								p.getWorld().getName());
						this.getWarpFile().set(
								"Warps." + args[0].toLowerCase() + ".x",
								p.getLocation().getBlockX());
						this.getWarpFile().set(
								"Warps." + args[0].toLowerCase() + ".y",
								p.getLocation().getBlockY());
						this.getWarpFile().set(
								"Warps." + args[0].toLowerCase() + ".z",
								p.getLocation().getBlockZ());

						p.sendMessage(newWarp);
						this.saveWarpFile();

					}
				} else if (cmd.equalsIgnoreCase("delwarp")) {
					if (!p.hasPermission("warps.delwarp")) {
						p.sendMessage(permission);

					} else if (args.length > 1 || 0 == args.length) {
						p.sendMessage(usageDelwarp);

					} else if (this.getWarpFile()
							.getConfigurationSection("Warps").getKeys(false)
							.contains(args[0].toLowerCase())) {
						p.sendMessage(delWarp.replace("%warpnavn%",
								args[0].toLowerCase()));
						this.getWarpFile().set(
								"Warps." + args[0].toLowerCase(), null);
						this.saveWarpFile();

					} else {
						p.sendMessage(warpDontExist);
					}
				}
			}
		}
		return false;
	}
}