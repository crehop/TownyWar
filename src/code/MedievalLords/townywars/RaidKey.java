package code.MedievalLords.townywars;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class RaidKey {
	// TODO FIX DISPLAY NAME OF RAIDKEY AFTER EXPIRATION AND ON DROP/PICKUP IF
	// INVALID
	private int timeLeft;
	private int blocksLeft;
	private int itemsLeft;
	private String townname;
	private int validation;
	private int flash = 0;
	private Town town;
	private ItemStack stack;
	private Player keyHolder;
	private int teleportTimer;

	public int getValidation() {
		return this.validation;
	}
	public RaidKey(ItemStack stack, Town town, Player player) {
		Random rand = new Random();
		this.setKeyItemStack(stack);
		this.townname = town.toString();
		this.timeLeft = 1800;
		this.blocksLeft = 5;
		this.itemsLeft = 15;
		this.setTown(town);
		this.setKeyHolder(player);
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName("§5§lRaid Key");
		lore.add("§aTown Name§b: " + this.townname);
		lore.add("§aTime Remaining§b: "
				+ convertSecondsToMinutes(this.timeLeft));
		lore.add("§aBlocks Destroyable§b: " + this.blocksLeft);
		lore.add("§aItems Lootable§b: " + this.itemsLeft);
		this.validation = rand.nextInt(100000000);
		lore.add("§0" + validation);
		meta.setLore(lore);
		stack.setItemMeta(meta);
		WarManager.keys.add(this);
		keyHolder.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "YOU GOT A RAID KEY TO RAID " + ChatColor.RED + town.getName() + ChatColor.GREEN +  " CHECK YOUR INVENTORY!");
		keyHolder.sendMessage(ChatColor.GREEN + "Say " + ChatColor.RED + "/raid " + ChatColor.GREEN +  "to raid the enemy town!");
		keyHolder.sendMessage(ChatColor.GREEN + "Say " + ChatColor.RED + "/rules raid " + ChatColor.GREEN +  "for more info.");
	}

	private void setKeyItemStack(ItemStack stack) {
		this.stack = stack;
	}

	public void tick() {
		if (this.town.isPVP() == false) {
			this.town.setPVP(true);
		}
		if ((this.itemsLeft == 0) && (this.blocksLeft == 0)) {
			this.voidKey();
		}
		String color;
		if (this.flash == 0) {
			this.flash = 1;
			color = ChatColor.AQUA + "";
		} else {
			this.flash = 0;
			color = ChatColor.RED + "";
		}
		if (this.teleportTimer > 0) {
			this.teleportTimer--;
		}
		this.timeLeft--;
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = new ArrayList<String>();
		meta.setDisplayName("§5§lRaid Key");
		lore.add("§aTown Name§b: " + this.townname);
		lore.add("§aTime Remaining§b: " + color
				+ convertSecondsToMinutes(this.timeLeft));
		lore.add("§aBlocks Destroyable§b: " + this.blocksLeft);
		lore.add("§aItems Lootable§b: " + this.itemsLeft);
		lore.add("§0" + validation);
		meta.setLore(lore);
		int found = 0;
		if (this.getKeyHolder().getItemOnCursor() != null) {
			if (this.getKeyHolder().getItemOnCursor().hasItemMeta()) {
				if (this.getKeyHolder().getItemOnCursor().getItemMeta()
						.hasLore()) {
					if (this.getKeyHolder().getItemOnCursor().getItemMeta()
							.getLore().get(4).contains(this.validation + "")) {
						found = 1;
					}
				}
			}
		}
		if (found != 1) {
			for (ItemStack stack : this.getKeyHolder().getInventory()) {
				if (stack != null) {
					if (stack.hasItemMeta()) {
						if (stack.getItemMeta().hasLore()) {
							if (stack.getItemMeta().getLore().get(4)
									.contains(this.validation + "")) {
								if (timeLeft <= 0) {
									meta.setDisplayName("§5§lWar Trophy");
									lore.set(0, "§aTown Name§b: "
											+ this.townname);
									lore.set(1, "§aTime Remaining§b: §c0:00");
									lore.set(2, "§aBlocks Destroyable§b: "
											+ this.blocksLeft);
									lore.set(3, "§aItems Lootable§b: "
											+ this.itemsLeft);
									lore.set(4, "§cExpired");
									meta.setLore(lore);
									stack.setItemMeta(meta);
									for (Resident res : town.getResidents()) {
										Player member = Bukkit.getPlayer(res
												.getName());
										if (member.isOnline()) {
											member.sendMessage("§cThe raid on your town has stopped.");
										}
									}
									keyHolder
											.sendMessage("§cYour raid on§b: "
													+ town.getName()
													+ " §chas ceased.");
									this.town.setPVP(false);
									WarManager.keys.remove(this);
									found = 1;
								} else {
									stack.setItemMeta(meta);
									found = 1;
								}
							}
						}
					}
				}
			}
			if (found != 1) {
				this.voidKey();
				this.keyHolder.closeInventory();
				WarManager.keys.remove(this);
			}
		}
	}

	public void voidKey() {
		this.setTimeLeft(0);
	}

	public boolean validate(int check) {
		if (check == validation) {
			return true;
		}
		return false;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public int getBlocksLeft() {
		return blocksLeft;
	}

	public void setBlocksLeft(int i) {
		this.blocksLeft = i;
	}

	public void minusBlock() {
		List<String> lore = stack.getItemMeta().getLore();
		ItemMeta meta = stack.getItemMeta();
		if (Integer.parseInt(lore.get(2)
				.replace("§aBlocks Destroyable§b: ", "")) <= 0) {
			lore.set(2, "0");
		} else {
			this.blocksLeft--;
			lore.set(2, this.blocksLeft + "");
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	public int getItemsLeft() {
		return itemsLeft;
	}

	public void minusItemsLeft() {
		List<String> lore = stack.getItemMeta().getLore();
		ItemMeta meta = stack.getItemMeta();
		if (Integer.parseInt(lore.get(3).replace("§aItems Lootable§b: ", "")) <= 0) {
			lore.set(3, "0");
		} else {
			this.itemsLeft--;
			lore.set(3, this.itemsLeft + "");
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
	}

	// public String getNationName() {
	// return nationname;
	// }
	//
	// public void setNationName(String nation) {
	// this.nationname = nation;
	// }

	public String getTownName() {
		return townname;
	}

	public void setTownName(String town) {
		this.townname = town;
	}

	public Player getKeyHolder() {
		return keyHolder;
	}

	public void setKeyHolder(Player keyHolder) {
		this.keyHolder = keyHolder;
	}

	public RaidKey clone(RaidKey another) {
		another = this;
		return another;
	}

	public static String convertSecondsToMinutes(int time) {
		int minutes = time / 60;
		int seconds = time % 60;
		String disMinu = "" + minutes;
		String disSec = (seconds < 10 ? "0" : "") + seconds;
		String formattedTime = disMinu + ":" + disSec;
		return formattedTime;
	}

	public Town getTown() {
		return town;
	}

	public void setTown(Town town) {
		this.town = town;
	}
}
