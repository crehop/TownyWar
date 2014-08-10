package code.MedievalLords.townywars;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class WarManager {

	public static List<RaidKey> keys = new CopyOnWriteArrayList<RaidKey>();

	public static RaidKey getKey(ItemStack stack) {
		RaidKey hold = null;
		int validate = Integer.parseInt(stack.getItemMeta().getLore().get(4).replace("§0", ""));
		for (RaidKey check : keys) {
			if (check.validate(validate) == true) {
				hold = check;
			}
		}
		return hold;
	}

	public static boolean isBeingRaided(Player player)
			throws NotRegisteredException {
		// TODO UNCOMMENT THIS
		// if(player.getWorld().toString().equalsIgnoreCase("world")){
		// return false;
		// }
		for (RaidKey check : WarManager.keys) {
			for(Resident resident:check.getTown().getResidents()){
				if(resident.getName().equals(player.getName())){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isRaiding(Player player)
			throws NotRegisteredException {
		for (RaidKey check : WarManager.keys) {
			if (player.getName().equals(check.getKeyHolder().getName())) {
				return true;
			}
		}
		return false;
	}

	public static void makeKey(Player player) throws TownyException {
		Resident resident = null;
		int count = 0;
		for (Resident res : TownyWars.tUniverse.getActiveResidents()) {
			if (res.getName().equalsIgnoreCase(player.getName())) {
				break;
			} else {
				count++;
			}
		}
		resident = TownyWars.tUniverse.getActiveResidents().get(count);
		if (resident != null) {
			Town town = resident.getTown();
			ItemStack key = new ItemStack(Material.GOLD_NUGGET);
			new RaidKey(key, town, player);
			player.getInventory().addItem(key);
		}
	}

	public static boolean confirmInNation(Player player) {
		try {
			Resident resident = TownyUniverse.getDataSource().getResident(player.getName());
			if (TownyUniverse.getPlayer(resident) != null) {
				Town town = resident.getTown();
				Nation nation = town.getNation();
				if ((nation != null) && (town != null) && (resident != null)) {
					return true;
				}

			}
		} catch (NotRegisteredException e) {
			e.printStackTrace();
		} catch (TownyException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static ItemStack getValidKey(Player player) {
		if (confirmInNation(player)) {
			ItemStack[] drops = player.getInventory().getContents();
			for (ItemStack check : drops) {
				if (check != null) {
					if (check.getType() == Material.GOLD_NUGGET) {
						if (check.hasItemMeta()) {
							if (check.getItemMeta().hasDisplayName()) {
								if (check.getItemMeta().getDisplayName().contains("Raid Key")) {
									if (check.getItemMeta().hasLore()) {
										List<String> lore = ItemUtils.getLore(check);
										if (!(lore.get(4).contains("Expired"))) {
											if (checkValidation(Integer.parseInt(lore.get(4).replace("§0", "")))) {
												return check;
											} else {
												check.getItemMeta().setDisplayName("§5§lWar Trophy");
												check.getItemMeta().getLore().set(4, "§cExpired");
												return check;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return new ItemStack(Material.AIR);
	}

	public static void teleportToTown(Player p, RaidKey key)
			throws NotRegisteredException {
		try {
			if (Cooldowns.tryCooldown(p, "TeleportCooldown", 60000)) {
				Town town = key.getTown();
				Random rand = new Random();
				Location location = town.getSpawn();
				int x = location.getBlockX();
				int z = location.getBlockZ();
				Location teleport = new Location(location.getWorld(), x + rand.nextInt(75) + 50, 230, z + rand.nextInt(75) + 50);
				p.teleport(teleport);
				p.sendMessage("§aYou have been teleported to somewhere near §b: " + key.getTownName());
				p.sendMessage("§aUse §e/towny map big §ato find the town!");
			} else {
				p.sendMessage("§cYou cannot teleport for another " + (Cooldowns.getCooldown(p, "TeleportCooldown") / 1000) + " §cseconds.");
				return;
			}
		} catch (TownyException e) {
			p.sendMessage("§cThat town is not valid.");
			e.printStackTrace();
		}
	}

	public static boolean checkValidation(int key) {
		for (RaidKey check : WarManager.keys) {
			if (check.getValidation() == key) {
				return true;
			}
		}
		return false;
	}

	public static void endRaid(RaidKey key) {
		key.setTimeLeft(0);
	}

	public static RaidKey getRaidKey(int lore4) {
		RaidKey key = null;
		for (RaidKey check : WarManager.keys) {
			if (check.getValidation() == lore4) {
				key = check;
			}
		}
		return key;
	}
}