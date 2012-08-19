package me.SgtMjrME.TownWarp;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener{
	private TownWarp plugin;
	private HashMap<String, Long> delayTime = new HashMap<String, Long>();
	private long time;
	
	PlayerListener(TownWarp townWarp, long time)
	{
		plugin = townWarp;
		this.time = time;
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void checkdelay(PlayerCommandPreprocessEvent e)
	{
		if (e.getPlayer().hasPermission("TW.mod") || e.getPlayer().isOp())
			return;
		String[] broken = e.getMessage().split(" ");
		if (broken.length >= 1)
		{
			if (broken[0].equalsIgnoreCase("/tw") || broken[0].equalsIgnoreCase("/townwarp")
					|| broken[0].equalsIgnoreCase("/twpreview") || broken[0].equalsIgnoreCase("/twset"))
			{
				if (delayTime.containsKey(e.getPlayer().getName()) && 
						(System.currentTimeMillis() - delayTime.get(e.getPlayer().getName())) > (time * 1000)){
					e.setCancelled(true);
				}
				else
				{
					delayTime.put(e.getPlayer().getName(), System.currentTimeMillis());
				}
				
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent e)
	{
		if (!plugin.isProtected())
			return;
		if (plugin.checkBlocks(e.getBlock().getLocation())){
			e.setCancelled(true);
			e.getPlayer().sendMessage("Cannot edit blocks in Town Warp zone");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent e)
	{
		if (!plugin.isProtected())
			return;
		if (plugin.checkBox(e.getBlock().getLocation())){
			e.setCancelled(true);
			e.getPlayer().sendMessage("Cannot edit blocks in Town Warp zone");
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onLiquid(BlockFromToEvent e)
	{
		if (!plugin.isProtected())
			return;
		if (plugin.checkLiq(e.getToBlock().getLocation()))
			e.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void PlayerEvent(PlayerBucketEmptyEvent e)
	{
		if (!plugin.isProtected())
			return;
		Location check = e.getPlayer().getTargetBlock(null, 100).getLocation();
		check.add(0, 1, 0);
		if (plugin.checkBoxLarge(check))
			e.setCancelled(true);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onplayerLeave1(PlayerQuitEvent e){
		delayTime.remove(e.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLeave2(PlayerKickEvent e){
		if (!e.isCancelled())
			delayTime.remove(e.getPlayer());
	}

	public void deactivate(String player) {
		delayTime.remove(player);
	}
}
