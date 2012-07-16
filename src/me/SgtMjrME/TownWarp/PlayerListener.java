package me.SgtMjrME.TownWarp;

import java.util.ArrayList;
import java.util.Timer;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerListener implements Listener{
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
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent e)
	{
		if (plugin.checkBlocks(e.getBlock().getLocation())){
			e.setCancelled(true);
			e.getPlayer().sendMessage("Cannot edit blocks in Town Warp zone");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e)
	{
		if (plugin.checkBox(e.getBlock().getLocation())){
			e.setCancelled(true);
			e.getPlayer().sendMessage("Cannot edit blocks in Town Warp zone");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onLiquid(BlockFromToEvent e)
	{
		if (plugin.checkLiq(e.getToBlock().getLocation()))
			e.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void PlayerEvent(PlayerBucketEmptyEvent e)
	{
		Location check = e.getPlayer().getTargetBlock(null, 100).getLocation();
		check.add(0, 1, 0);
		if (plugin.checkBoxLarge(check))
			e.setCancelled(true);
	}

	public void deactivate(String player) {
		delay.remove(player);
	}
}
