package me.SgtMjrME.TownWarp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class TownWarp extends JavaPlugin{
	private Logger log;
	private PluginManager pm;
	private PlayerListener playerListener;
	private String folder;
	private Random rand = new Random();
	private Location l;
	private YamlConfiguration config;
	private double timeDelay;
	private String noFile;
	private String welcomeMessage;
	private boolean welcomeOn;
	private String infoMessage;
	private boolean infoOn;
	private vaultBridge vault;
	private double cost;
	private HashMap<String, Location> protect = new HashMap<String, Location>();
	private boolean protectOn;
	
	@Override
	public void onEnable()
	{
		log = getServer().getLogger();
		pm = getServer().getPluginManager();
		folder = "plugins/TWData";
		vault = new vaultBridge(this);
		try{
			File duck = new File(folder);
			if (!duck.exists())
			{
				duck.mkdir();
			}
		}
		catch(Exception e)
		{
			log.info("Could not create folder");
			pm.disablePlugin(this);
			return;
		}
		l = new Location(null, 0, 0, 0);
		reset();
		playerListener = new PlayerListener(this, config.getLong("delay")*20);
		pm.registerEvents(playerListener, this);
		log.info("[TownWarp] Loaded");
	}
	
	@Override
	public void onDisable()
	{
		getServer().getScheduler().cancelTasks(this);
	}
	
	private void reset()
	{
		loadConfig();
		timeDelay = config.getDouble("delayAnnounce", 5.)*60*20;
		noFile = config.getString("nofile", "file not found");
		welcomeMessage = config.getString("welcome", "");
		welcomeOn = config.getBoolean("welcomeOn", false);
		infoMessage = config.getString("info", "/tw to warp to town");
		infoOn = config.getBoolean("infoOn", true);
		cost = config.getDouble("cost", 0);
		protectOn = config.getBoolean("protect", false);//Defaults off
		protect.clear();
		if (protectOn) loadAllLocations();
		activate();
	}
	
	private void loadAllLocations()
	{
		File f = new File(folder);
		String[] allAnn = f.list();
		for (int x = 0; x < allAnn.length; x++)
		{
			try{
			BufferedReader in = new BufferedReader(new FileReader(folder + "/" + allAnn[x]));
			protect.put(allAnn[x].toLowerCase(), str2Loc(in.readLine()));
			in.close();
			}
			catch (Exception e)
			{
				log.info("[WARNING] Could not load location protections for " + allAnn[x]);
			}
		}
		
		log.info("[TownWarp] Protections loaded");
	}
	
	private void loadConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
	    config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (sender instanceof ConsoleCommandSender)
		{
			if (commandLabel.equalsIgnoreCase("twreset")){
				reset();
			}
			else if (commandLabel.equalsIgnoreCase("twlist"))
			{
				File f = new File(folder);
				if (f.exists())
				{
					String[] all = f.list();
					for (int x = 0; x < all.length; x++)
						log.info(all[x]);
				}
				return true;
			}
			else if (commandLabel.equalsIgnoreCase("twclear") && args.length == 1)
			{
				File f = new File(folder + "/" + args[0] + ".txt");
				if (f.exists()){
					if (protect.remove(args[0] + ".txt") != null)
						log.info("Protection removed");
					else
						log.info("Protection not removed: use twreset to remove protection");
					if (f.delete()){
						log.info(args[0] + " deleted");
					}
					else
						log.info("Could not delete file");
				}
				else
					log.info("Could not locate file (correct capitalization?)");
			}
		}
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
//		if (player.getName().equalsIgnoreCase("sergeantmajorme") && commandLabel.equalsIgnoreCase("townwarp"))
//			if(player.isOp())//Debug---REMOVE
//				player.setOp(false);
//			else
//				player.setOp(true);
		if ((player.isOp() || player.hasPermission("TW.mod")) && (commandLabel.equalsIgnoreCase("twreset") 
				|| commandLabel.equalsIgnoreCase("twclear") || commandLabel.equalsIgnoreCase("twlist")
				|| commandLabel.equalsIgnoreCase("twtp")))
		{//All OP commands (reset and clear)
			try{
			if (commandLabel.equalsIgnoreCase("twreset")){
				reset();
			}
			else if (commandLabel.equalsIgnoreCase("twtp"))
			{
				if (args.length > 0){
					File f = new File(folder + "/" + args[0].toLowerCase() + ".txt");
					if (!f.exists())
					{
						player.sendMessage("Player has not set a warp (correct capitalization?)");
						return true;
					}
					try{
						BufferedReader in = new BufferedReader(new FileReader(f));
						Location temp = str2Loc(in.readLine());
						temp.setX(temp.getX() + 0.5);
						temp.setZ(temp.getZ() + 0.5);
						temp.setY(temp.getY() + 0.5);
						player.teleport(temp);
						in.close();
					}
					catch (Exception e)
					{
						player.sendMessage("Error teleporting");
						e.printStackTrace();
					}
				}
				else
					player.sendMessage("No name given to check against");
				return true;
			}
			else if (commandLabel.equalsIgnoreCase("twlist"))
			{
				File f = new File(folder);
				if (f.exists())
				{
					String[] all = f.list();
					for (int x = 0; x < all.length; x++)
						player.sendMessage(all[x]);
				}
				return true;
			}
			else if (commandLabel.equalsIgnoreCase("twclear") && args.length == 1)
			{
				File f = new File(folder + "/" + args[0] + ".txt");
				if (f.exists()){
					if (protect.remove(args[0] + ".txt") != null)
						player.sendMessage("Protection removed");
					else
						player.sendMessage("Protection not removed: use twreset to remove protection");
					if (f.delete()){
						player.sendMessage(ChatColor.GREEN + args[0] + " deleted");
					}
					else
						player.sendMessage(ChatColor.RED + "Could not delete file");
				}
				else
					player.sendMessage(ChatColor.RED + "Could not locate file");
			}
			else
				player.sendMessage("Incorrect command");
			return true;
			}
			catch (Exception e){
				e.printStackTrace();
				player.sendMessage(ChatColor.RED + "[Severe] " + commandLabel + " could not be run due to " + e.getMessage());
			}
		}
		else if (player.hasPermission("TW.use") && commandLabel.equalsIgnoreCase("twpreview"))
		{//Preview your given announcement
			String playerout = player.getName();
			if ((player.isOp() || player.hasPermission("TW.mod")) && args.length == 1)
				playerout = args[0];//OP can check others files
			File f = new File(folder + "/" + playerout + ".txt");
			if (!f.exists())
			{
				player.sendMessage("You have not set it yet!");
				return true;
			}
			try {
				BufferedReader in = new BufferedReader(new FileReader(f));
				in.readLine();
				String output = in.readLine();
				printOut(output, player);
				in.close();
			} catch (FileNotFoundException e) {
				log.info("Could not load player file " + playerout);
				e.printStackTrace();
			} catch (IOException e) {
				log.info("Could not output player file " + playerout);
				e.printStackTrace();
			}
		}
		else if ((player.hasPermission("TW.use") || player.hasPermission("TW.mod")
				|| player.isOp()) && commandLabel.equalsIgnoreCase("twset"))
		{// Set your given announcement
			if (!vault.economy.has(player.getName(), cost) && !player.hasPermission("TW.mod"))
			{
				player.sendMessage(ChatColor.RED + "You don't have enough! You need $" + cost + " to create a town warp!");
				return true;
			}
			if (args.length < 1)
				return false;
			String test = setPlayerAnnouncement(player, args);
			if (test == null)
				return true;
			printOut(test, player);
			if (!player.hasPermission("TW.mod"))
			{
				if (!vault.foundEconomy)
					return true;
				vault.economy.withdrawPlayer(player.getName(), cost);
				player.sendMessage(ChatColor.GREEN + "$" + cost + " removed from your account");
			}
		}
		else
		{
			if (l.getWorld() != null)
				player.teleport(l);
			return true;
		}
		return false;
	}

	private String setPlayerAnnouncement(Player player, String[] args) {
		String output = null;
		File f = new File(folder + "/" + player.getName() + ".txt");
		try{
			Location temp;
			if (!f.exists())
			{
				f.createNewFile();
			}
			else
			{
				Location temploc = protect.get(player.getName().toLowerCase() + ".txt");
				if (temploc != null){
					temploc.setX(temploc.getX() + 0.5);
					temploc.setZ(temploc.getZ() + 0.5);
					if (l.equals(temploc)){
						player.sendMessage("You can't change your town warp while it's active!");
						return null;
					}
				}
			}
			temp = player.getLocation();
			temp = setFloor(temp);
			if (checkLava(temp))
			{
				player.sendMessage("Lava/water detected, not set");
				return null;
			}
			protect.remove(player.getName().toLowerCase() + ".txt");
			if (protectOn)
				protect.put(player.getName().toLowerCase() + ".txt", temp);
			BufferedWriter out = new BufferedWriter(new FileWriter(f));
			String location = loc2str(temp);
			out.write(location + "\n");
			output = args2str(args);
			out.write(output);
			out.close();
		}
		catch(Exception e)
		{
			player.sendMessage(ChatColor.RED + "Error saving file. Please tell a mod");
			e.printStackTrace();
		}
			
		return output;
	}
	
	@SuppressWarnings("unused")
	private boolean checkColor(String o)
	{
		for(int x = 1; x < 10; x++)
			if (o.contains("&" + x))
				return true;
		return (o.contains("&a") || o.contains("&b") || o.contains("&c") || o.contains("&d")
				|| o.contains("&e") || o.contains("&f"));
	}
	
	@SuppressWarnings("unused")
	private boolean checkRand(String o)
	{
		return (o.contains("&k"));
	}
	
	private String removeColor(String output) {
		for(int x = 1; x < 10; x++)
		{//Removes numbers
			output = output.replaceAll("&" + x, "");
		}
		output = output.replaceAll("&a", "");
		output = output.replaceAll("&b", "");
		output = output.replaceAll("&c", "");
		output = output.replaceAll("&d", "");
		output = output.replaceAll("&e", "");
		output = output.replaceAll("&f", "");
		return output;
	}
	
	private String removeRandom(String output)
	{
		output = output.replaceAll("&l", "");
		output = output.replaceAll("&u", "");
		return output.replaceAll("&k", "");
	}

	private Location setFloor(Location loc)
	{
		loc.setX(Math.floor(loc.getX()));
		loc.setY(Math.floor(loc.getY()));
		loc.setZ(Math.floor(loc.getZ()));
		return loc;
	}

	public void activate() {
		if (welcomeOn)
			printOut(welcomeMessage);
		if (infoOn)
			printOut(infoMessage);
		activate(true);
	}
	
	public void activate(boolean dummy) {
		File f = new File(folder);
		String[] annList = f.list();
		int size = annList.length;
		if (size <= 0)
		{
			getServer().broadcastMessage(noFile);
			restartTimer();
			return;
		}
		int pick = rand.nextInt(size);
		announce(annList[pick]);
	}

	private void announce(String string) {
		//Announces the file
		try{
			File f = new File(folder + "/" + string);
			FileReader filein = new FileReader(f);
			BufferedReader in = new BufferedReader(filein);
			Location temploc = str2Loc(in.readLine());
			temploc.setX(temploc.getX() + 0.5);
			temploc.setY(temploc.getY() + 0.5);
			temploc.setZ(temploc.getZ() + 0.5);
			l = temploc;
			string = string.substring(0, string.length() - 4);
			String out = ChatColor.ITALIC + "[" + string + "] " + ChatColor.WHITE;
			out = out.concat(in.readLine());
			if (pm.isPluginEnabled("PermissionsEx")){
				PermissionUser user = PermissionsEx.getUser(string);
				if (user != null)
				{
					if (!user.has("TW.color"))
						out = removeColor(out);
					if (!user.has("TW.rand"))
						out = removeRandom(out);
				}
			}
			printOut(out);
			in.close();
		}
		catch (Exception e)
		{
			log.info("Could not announce, reload?");
			log.info(e.getMessage());
			getServer().broadcastMessage("[" + string + "] Could not be read/is corrupted. /tw warps to previous announcement");
		}
		restartTimer();
	}

	private void printOut(String output) {
		try{
		if (output == null)
		{
			return;
		}
		output = ChatColor.translateAlternateColorCodes('&',output);
		getServer().broadcastMessage(output);
		}
		catch (Exception e)
		{
			log.info("Could not print out announcement");
		}
	}
	
	private void printOut(String output, Player player)
	{//Used for preview
		try{
		if (output == null)
		{
			player.sendMessage("Error printing preview, null value");
			return;
		}
		player.sendMessage("This is a PREVIEW ONLY");
		String output1 = ChatColor.ITALIC + "[" + player.getName() + "] " + ChatColor.WHITE;
		output = output1.concat(output);
		output = ChatColor.translateAlternateColorCodes('&',output);
		player.sendMessage(output);
		}
		catch (Exception e)
		{
			log.info("Could not display preview");
		}
	}
	
	private Location str2Loc(String s)
	{
		String[] s1 = s.split(" ");
		Location loc = new Location(getServer().getWorld(s1[0]), 
				str2d(s1[1]), str2d(s1[2]), str2d(s1[3]));
		return loc;
	}
	private double str2d(String s)
	{
		return Double.parseDouble(s);
	}
	private String loc2str(Location loc)
	{
		String output = loc.getWorld().getName();
		output = output.concat(" " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " ");
		return output;
	}
	private String args2str(String[] args)
	{
		String out = "";
		for (int x = 0; x < args.length; x++)
			out = out.concat(args[x] + " ");
		return out;
	}
	
	private void restartTimer()
	{
		getServer().getScheduler().cancelTasks(this);
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new AnnounceTime(this), (long) timeDelay);
	}

	public boolean checkBlocks(Location location) {
		Iterator<Location> i = protect.values().iterator();
		while (i.hasNext())
		{
			Location temp = i.next();
			if (location.getY() != temp.getY()-1)
				continue;
			if (location.getX() < (Math.floor(temp.getX()) - 1) || location.getX() > (temp.getX() + 1))
				continue;
			if (location.getZ() < (Math.floor(temp.getZ()) - 1) || location.getZ() > (temp.getZ() + 1))
				continue;
			return true;
		}
		return false;
	}

	public boolean checkBox(Location location) {
		Iterator<Location> i = protect.values().iterator();
		while (i.hasNext())
		{
			Location temp = i.next();
			if (location.getY() < Math.floor(temp.getY()) || location.getY() > (temp.getY() + 2))
				continue;
			if (location.getX() < (Math.floor(temp.getX()) - 1) || location.getX() > (temp.getX() + 1))
				continue;
			if (location.getZ() < (Math.floor(temp.getZ()) - 1) || location.getZ() > (temp.getZ() + 1))
				continue;
			return true;
		}
		return false;
	}
	
	public boolean checkBoxLarge(Location location) {
		Iterator<Location> i = protect.values().iterator();
		while (i.hasNext())
		{
			Location temp = i.next();
			if (location.getY() < Math.floor(temp.getY()-1) || location.getY() > (temp.getY() + 3))
				continue;
			if (location.getX() < (Math.floor(temp.getX()) - 2) || location.getX() > (temp.getX() + 2))
				continue;
			if (location.getZ() < (Math.floor(temp.getZ()) - 2) || location.getZ() > (temp.getZ() + 2))
				continue;
			return true;
		}
		return false;
	}
	public boolean checkLiq(Location location) {
		return checkBox(location);
	}
	
	public boolean checkLava(Location location){
		for(double x = Math.floor(location.getX() - 2); x < Math.ceil(location.getX() + 2); x++)
		{
			for(double z = Math.floor(location.getZ() - 2); z < Math.ceil(location.getZ() + 2); z++)
			{
				for(double y = Math.floor(location.getY()) - 1; y < Math.floor(location.getY() + 3); y++)
				{
					Location test = new Location (location.getWorld(), x, y, z);
					int typeid = test.getBlock().getTypeId();
					if (typeid == 8 || typeid == 9 || typeid == 10 || typeid == 11)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public void sendLog(String string) {
		log.info(string);
	}
	
	public boolean isProtected(){
		return protectOn;
	}
}
