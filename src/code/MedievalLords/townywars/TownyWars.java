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
						try {
							WarManager.teleportToTown(p,WarManager.getRaidKey(Integer.parseInt(key.getItemMeta().getLore().get(4).replace("§0", ""))));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return true;
					} else {
						p.sendMessage("§cYou do not have a valid Raid Key.");
					}
				} catch (NumberFormatException e) {
					p.sendMessage("§cCould not initiate raid.");
					e.printStackTrace();
					return false;
				} catch (Exception e) {
					p.sendMessage("§cThat town is not registered.");
					e.printStackTrace();
					return false;
				}
			}
			else if (commandLabel.equalsIgnoreCase("checkkeys") && p.isOp()) {
				WarListener.force = true;
				p.sendMessage("§aNEXT KEY WILL BE FORCED");
				p.sendMessage("§aCurrent Keys§b: " + WarManager.keys.size());
				p.sendMessage("§aTotal Keys Created§b: " + WarManager.getKeysCreated());
				for (RaidKey check: WarManager.keys) {
					p.sendMessage("§c=============================================");
					p.sendMessage("§aTown Name§b: " + check.getTownName());
					p.sendMessage("§aKey Holder§b: " + check.getKeyHolder().getName());
					p.sendMessage("§aTime Left§b: " + check.convertSecondsToMinutes(check.getTimeLeft()));
					p.sendMessage("§aBlocks Left§b: " + check.getBlocksLeft());
					p.sendMessage("§aItems Left§b: " + check.getItemsLeft());
					p.sendMessage("§c=============================================");
				}
				p.sendMessage("Names Of KeyGetters:");
				for(String check:WarManager.names){
					p.sendMessage(check);
				}
			}
		}
		return false;
	}
}
