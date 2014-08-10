package code.MedievalLords.townywars;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class TownyWars extends JavaPlugin {

	public static TownyUniverse tUniverse;

	@Override
	public void onEnable() {
		tUniverse = ((Towny) Bukkit.getPluginManager().getPlugin("Towny"))
				.getTownyUniverse();

		getServer().getPluginManager().registerEvents(new WarListener(), this);

		getServer().getScheduler().scheduleSyncRepeatingTask(this,
				new Runnable() {
					@Override
					public void run() {
						Iterator<RaidKey> keyChain = WarManager.keys.iterator();
						while (keyChain.hasNext()) {
							RaidKey key = keyChain.next();
							key.tick();
						}
					}
				}, 0L, 20L);

		WarListener.initBlockedMats();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (commandLabel.equalsIgnoreCase("raid")) {
				try {
					if (WarManager.getValidKey(p).getType() != Material.AIR) {
						ItemStack key = WarManager.getValidKey(p);
						WarManager.teleportToTown(
								p,
								WarManager.getRaidKey(Integer.parseInt(key
										.getItemMeta().getLore().get(4)
										.replace("§0", ""))));
						return true;
					} else {
						p.sendMessage("§cYou do not have a valid Raid Key.");
					}
				} catch (NumberFormatException e) {
					p.sendMessage("§cCould not initiate raid.");
					e.printStackTrace();
					return false;
				} catch (NotRegisteredException e) {
					p.sendMessage("§cThat town is not registered.");
					e.printStackTrace();
					return false;
				}
			}
		}
		return false;
	}
}
