package me.SgtMjrME.TownWarp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class TownWarp extends JavaPlugin{
	private Logger log;
	private PluginManager pm;
	private PlayerListener playerListener;
	private String folder;
	private Random rand = new Random();
	private Location l;
	private Timer t = new Timer();
	private YamlConfiguration config;
	private double timeDelay;
	private String noFile;
	private String welcomeMessage;
	private boolean welcomeOn;
	private String infoMessage;
	private boolean infoOn;
	private vaultBridge vault;
	private double cost;
	
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
		}
		l = new Location(null, 0, 0, 0);
		reset();
		playerListener = new PlayerListener(this, config.getLong("delay"));
		pm.registerEvents(playerListener, this);
		log.info("[TownWarp] Loaded");
	}
	
	@Override
	public void onDisable()
	{
		t.cancel();
	}
	
	public void reset()
	{
		loadConfig();
		timeDelay = config.getDouble("delay") * 1000 * 60;
		noFile = config.getString("nofile");
		welcomeMessage = config.getString("welcome");
		welcomeOn = config.getBoolean("welcomeOn");
		infoMessage = config.getString("info");
		infoOn = config.getBoolean("infoOn");
		cost = config.getDouble("cost");
		activate();
	}
	
	private void loadConfig() {
		File configFile = new File(getDataFolder(), "config.yml");
	    config = YamlConfiguration.loadConfiguration(configFile);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (!(sender instanceof Player))
			return true;
		Player player = (Player) sender;
		if ((player.isOp() || player.hasPermission("TW.mod")) && (commandLabel.equalsIgnoreCase("twreset") 
				|| commandLabel.equalsIgnoreCase("twclear")))
		{//All OP commands (reset and clear)
			if (commandLabel.equalsIgnoreCase("twreset"))
				reset();
			else if (commandLabel.equalsIgnoreCase("twclear") && args.length == 1)
			{
				File f = new File(folder + "/" + args[0] + ".txt");
				if (f.exists())
					if (f.delete())
						player.sendMessage(ChatColor.GREEN + args[0] + " deleted");
					else
						player.sendMessage(ChatColor.RED + "Could not delete file");
				else
					player.sendMessage(ChatColor.RED + "Could not locate file");
			}
			else
				player.sendMessage("Incorrect command");
			return true;
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
				String output = ChatColor.ITALIC + "[" + player.getName() + "] " + ChatColor.WHITE;
				in.readLine();
				output = output.concat(in.readLine());
				printOut(output, player);
				in.close();
			} catch (FileNotFoundException e) {
				log.info("Could not load player file " + player.getName());
				e.printStackTrace();
			} catch (IOException e) {
				log.info("Could not output player file " + player.getName());
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
			printOut(test, player);
			if (!player.hasPermission("TW.mod"))
			{
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
			temp = player.getLocation();
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

	public void activate() {
		if (welcomeOn)
			printOut(welcomeMessage);
		if (infoOn)
			printOut(infoMessage);
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
			l = str2Loc(in.readLine());
			String out = in.readLine();
			printOut(out);
			in.close();
		}
		catch (Exception e)
		{
			log.info("Could not announce, reload?");
			e.printStackTrace();
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
		t.purge();
		t.schedule(new AnnounceTime(this), (long) timeDelay);
	}
}
