package me.SgtMjrME.TownWarp;

import java.util.TimerTask;

public class AnnounceTime extends TimerTask{
	private TownWarp plugin;
	
	public AnnounceTime(TownWarp plugin)
	{
		this.plugin = plugin;
	}
	
	public void run()
	{
		plugin.activate();
	}
}
