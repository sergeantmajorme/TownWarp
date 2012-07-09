package me.SgtMjrME.TownWarp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerListener implements Listener{
	private TownWarp plugin;
	
	PlayerListener(TownWarp townWarp)
	{
		plugin = townWarp;
	}
}
