package me.SgtMjrME.TownWarp;

import java.util.ArrayList;
import java.util.Timer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerListener implements Listener{
	@SuppressWarnings("unused")
	private TownWarp plugin;
	private ArrayList<String> delay = new ArrayList<String>();
	private long time;
	private Timer t;
	
	PlayerListener(TownWarp townWarp, long time)
	{
		plugin = townWarp;
		this.time = time;
		t = new Timer();
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void checkdelay(PlayerCommandPreprocessEvent e)
	{
		if (e.getPlayer().hasPermission("TW.mod") || e.getPlayer().isOp())
			return;
		String[] broken = e.getMessage().split(" ");
		if (broken.length > 1)
		{
			if (broken[0].equalsIgnoreCase("/tw") || broken[0].equalsIgnoreCase("/townwarp")
					|| broken[0].equalsIgnoreCase("/twpreview") || broken[0].equalsIgnoreCase("/twset"))
			{
				if (delay.contains(e.getPlayer().getName()))
					e.setCancelled(true);
				else
				{
					delay.add(e.getPlayer().getName());
					t.schedule(new DelayTask(this, e.getPlayer().getName()), time);
				}
				
			}
		}
	}

	public void deactivate(String player) {
		delay.remove(player);
	}
}
