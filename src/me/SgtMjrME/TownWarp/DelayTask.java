package me.SgtMjrME.TownWarp;

import java.util.TimerTask;

public class DelayTask extends TimerTask{

	private TownWarp plugin;
	
	DelayTask(TownWarp plugin)
	{
		this.plugin = plugin;
	}
	
	public void run()
	{
		plugin.activate();
	}
}
