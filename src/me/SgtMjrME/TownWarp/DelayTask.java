package me.SgtMjrME.TownWarp;

import java.util.TimerTask;

public class DelayTask extends TimerTask{
	private String player;
	private PlayerListener plugin;
	
	DelayTask(PlayerListener plugin, String player)
	{
		this.plugin = plugin;
		this.player = player;
	}
	
	public void run()
	{
		plugin.deactivate(player);
	}
}
