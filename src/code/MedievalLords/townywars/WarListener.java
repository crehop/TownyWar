package code.MedievalLords.townywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.yi.acru.bukkit.Lockette.Lockette;
import org.yi.acru.bukkit.Lockette.LocketteBlockListener;

import com.palmergames.bukkit.blockqueue.BlockQueue;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.listeners.TownyBlockListener;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.object.TownyUniverse;

public class WarListener implements Listener {

	public static HashMap<Inventory, String> open = new HashMap<Inventory, String>();
	public static HashMap<Player, Integer> currentKills = new HashMap<Player, Integer>();
	public static ArrayList<Material> blockedMaterials = new ArrayList<Material>();
	public static ArrayList<Player> isBeingRaided = new ArrayList<Player>();
	public static boolean force = false;

	public static void initBlockedMats() {
		blockedMaterials.add(Material.CHEST);
		blockedMaterials.add(Material.TRAPPED_CHEST);
		blockedMaterials.add(Material.SIGN);
		blockedMaterials.add(Material.SIGN_POST);
	}

	@EventHandler
	public void inventoryMoveEvent(InventoryClickEvent e) {
		if (e.getInventory().getItem(e.getSlot()) != null && e.getSlot() < 44 && e.getSlot() > 0) {
			if (e.getInventory().getItem(e.getSlot()).hasItemMeta()) {
				if (e.getInventory().getItem(e.getSlot()).getItemMeta().hasDisplayName()) {
					if (e.getInventory().getItem(e.getSlot()).getItemMeta().getDisplayName().contains("RAID KEY!")) {
						e.setCancelled(true);
						Player p = (Player) e.getWhoClicked();
						p.updateInventory();
					}
				}
			}
		}
	}
	
	@EventHandler
	public void gameChangeEvent(PlayerGameModeChangeEvent e){
		try {
			if (WarManager.isBeingRaided(e.getPlayer())) {
				e.setCancelled(true);
				e.getPlayer().sendMessage("§cYou can not change gamemodes while being raided.");
			}
		} catch (Exception e1) {
		}
	}
	@EventHandler
	public void playerToggleFlightEventent(PlayerToggleFlightEvent e){
		if (e.getPlayer().getWorld().getName() != "world") {
			try {
				if (WarManager.isBeingRaided(e.getPlayer())) {
					e.setCancelled(true);
					e.getPlayer().sendMessage("§cYou can not fly while being raided.");
				}
			} catch (Exception e1) {
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void inventoryClickEvent(InventoryClickEvent e) {
		if (!(e.getInventory().getType() == InventoryType.PLAYER)) {
			ItemStack item = e.getInventory().getItem(e.getSlot());
			if ((item != null) && item.hasItemMeta()) {
				if (item.getItemMeta().hasDisplayName()) {
					if (item.getItemMeta().getDisplayName().contains("Raid Key")) {
						e.setCancelled(true);
						Player p = (Player) e.getWhoClicked();
						p.updateInventory();
					}
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void chestCheckEvent(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (e.getInventory().getType() == InventoryType.CHEST) {
			ItemStack clicked = e.getInventory().getItem(e.getSlot());
			InventoryHolder holder = e.getInventory().getHolder();
			if (holder instanceof Chest) {
				Chest chest = (Chest) holder;
				Location location = chest.getLocation();
				if (TownyUniverse.getTownName(location) != null) {
					try {
						Town town = TownyUniverse.getDataSource().getTown(TownyUniverse.getTownName(location));
						if (WarManager.getValidKey(p).getType() != Material.AIR) {
							RaidKey key = WarManager.getRaidKey(Integer.parseInt(WarManager.getValidKey(p).getItemMeta().getLore().get(4).replace("§0", "")));
							if (TownyUniverse.getTownName(location)
									.equalsIgnoreCase(key.getTownName())) {
								if (e.getRawSlot() == e.getSlot()) {
									if (key.getItemsLeft() > 0) {
										key.minusItemsLeft();
										e.setCancelled(false);
										Effects.openChestEffect(p.getLocation());
										for (Resident res : town.getResidents()) {
											Player member = Bukkit.getPlayer(res.getName());
											String string = clicked.getType().toString().toLowerCase().replaceAll("_", " ");
											String[] split = string.split(" ");

											for (int i = 0; i < split.length; i++) {
												char first = split[i].charAt(0);
												split[i] = split[i].replace(first,Character.toUpperCase(first));
											}
											if (member.isOnline()) {
												member.sendMessage(p.getDisplayName() + " §chas taken " + e.getCurrentItem().getType() +" from a chest!"
														+ "\n§aItem§b: " + string
														+ "\n§aLocation§b:"
														+ " §cX§b: " + p.getLocation().getBlockX()
														+ ", §cY§b: " + p.getLocation().getBlockY()
														+ ", §cZ§b: " + p.getLocation().getBlockZ());
											}
										}
									} else {
										p.sendMessage("§cYour Raid Key has no more Lootable Items left.");
										e.setCancelled(true);
										p.closeInventory();
										return;
									}
								}
							}
						}
					} catch (Exception e1) {
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void doublechestcheck(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (e.getInventory().getType() == InventoryType.CHEST) {
			ItemStack clicked = e.getInventory().getItem(e.getSlot());
			InventoryHolder holder = e.getInventory().getHolder();
			if (holder instanceof DoubleChest) {
				DoubleChest dchest = (DoubleChest) holder;
				Location location = dchest.getLocation();
				if (TownyUniverse.getTownName(location) != null) {
					try {
						Town town = TownyUniverse.getDataSource().getTown(TownyUniverse.getTownName(location));
						if (WarManager.getValidKey(p).getType() != Material.AIR) {
							RaidKey key = WarManager.getRaidKey(Integer.parseInt(WarManager.getValidKey(p).getItemMeta().getLore().get(4).replace("§0", "")));
							if (TownyUniverse.getTownName(location)
									.equalsIgnoreCase(key.getTownName())) {
								if (e.getRawSlot() == e.getSlot()) {
									if (key.getItemsLeft() > 0) {
										key.minusItemsLeft();
										e.setCancelled(false);
										p.openInventory(dchest.getInventory());
										Effects.openChestEffect(p.getLocation());
										for (Resident res : town.getResidents()) {
											Player member = Bukkit.getPlayer(res.getName());
											if (member.isOnline()) {
												String string = clicked.getType().toString().toLowerCase().replaceAll("_", " ");
												String[] split = string.split(" ");
												for (int i = 0; i < split.length; i++) {
													char first = split[i].charAt(0);
													split[i] = split[i].replace(first,Character.toUpperCase(first));
												}
												member.sendMessage(p.getDisplayName() + " §chas taken an item from a chest!"
														+ "\n§aItem§b: " + string
														+ "\n§aLocation§b:"
														+ " §cX§b: " + p.getLocation().getBlockX()
														+ ", §cY§b: " + p.getLocation().getBlockY()
														+ ", §cZ§b: " + p.getLocation().getBlockZ());
											}
										}
									} else {
										p.sendMessage("§cYour Raid Key has no more Lootable Items left.");
										e.setCancelled(true);
										p.closeInventory();
										return;
									}
								}
							}
						}
					} catch (Exception e1) {
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void chest(PlayerInteractEvent e) {
		Block clicked = e.getClickedBlock();
		if (clicked.getType() == Material.CHEST) {
			Player p = e.getPlayer();
			if (TownyUniverse.getTownName(clicked.getLocation()) != null) {
				Player player = e.getPlayer();
				try {
					Town town = TownyUniverse.getDataSource().getTown(TownyUniverse.getTownName(clicked.getLocation()));
					if (WarManager.getValidKey(player).getType() != Material.AIR) {
						RaidKey key = WarManager.getRaidKey(Integer.parseInt(WarManager.getValidKey(player).getItemMeta().getLore().get(4).replace("§0", "")));
						if (TownyUniverse.getTownName(clicked.getLocation()).equalsIgnoreCase(key.getTownName())) {
							for (Resident res : town.getResidents()) {
								if (key.getItemsLeft() > 0) {
									Effects.openChestEffect(p.getLocation());
									Player member = Bukkit.getPlayer(res.getName());
									if (member.isOnline()) {
										member.sendMessage(p.getDisplayName() + " §chas opened a chest in your Town!"
												+ "\n§aLocation§b:" + " §cX§b: " + p.getLocation().getBlockX()
												+ ", §cY§b: " + p.getLocation().getBlockY()
												+ ", §cZ§b: " + p.getLocation().getBlockZ());
									}
									Chest chest = (Chest) clicked.getLocation().getBlock().getState();
									e.setCancelled(false);
									p.openInventory(chest.getInventory());
									p.sendMessage("Bypassing Chest Security");
								} else {
									p.sendMessage("§cYou cannot loot anymore items.");
									e.setCancelled(true);
								}
							}
						}
						if (key.getItemsLeft() <= 0) {
							p.sendMessage("§cYou cannot loot anymore items.");
							e.setCancelled(true);
						}
					}
				} catch (Exception e1) {
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void BlockBreakEvent(BlockBreakEvent e) {
		Block block = e.getBlock();
		if (TownyUniverse.getTownName(block.getLocation()) != null) {
			Player p = e.getPlayer();
			try {
				Town town = TownyUniverse.getDataSource().getTown(TownyUniverse.getTownName(block.getLocation()));
				if (WarManager.getValidKey(p).getType() != Material.AIR) {
					RaidKey key = WarManager.getRaidKey(Integer.parseInt(WarManager.getValidKey(p).getItemMeta().getLore().get(4).replace("§0", "")));
					if (TownyUniverse.getTownName(block.getLocation()).equalsIgnoreCase(key.getTownName())) {
						if (key.getBlocksLeft() > 0) {
							key.minusBlock();
							e.setCancelled(false);
							Effects.breakBlockEffect(p.getLocation());
							if (!blockedMaterials.contains(block.getType())) {
								block.breakNaturally();
							}
							for (Resident res : town.getResidents()) {
								Player member = Bukkit.getPlayer(res.getName());
								if (member.isOnline()) {
									member.sendMessage(p.getDisplayName() + " §chas broken a block in your Town!"
											+ "\n§aLocation§b:" + " §cX§b: " + p.getLocation().getBlockX()
											+ ", §cY§b: " + p.getLocation().getBlockY()
											+ ", §cZ§b: " + p.getLocation().getBlockZ());
								}
							}
						} else {
							p.sendMessage("§cYour Raid Key has no more Breakable Blocks left.");
						}
					}
				}
			} catch (Exception e1) {
			}
		}
	}
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void BlockPlaceEvent(BlockPlaceEvent e) {
		Block block = e.getBlock();
		if (TownyUniverse.getTownName(block.getLocation()) != null) {
			Player p = e.getPlayer();
			try {
				Town town = TownyUniverse.getDataSource().getTown(TownyUniverse.getTownName(block.getLocation()));
				if (WarManager.getValidKey(p).getType() != Material.AIR) {
					RaidKey key = WarManager.getRaidKey(Integer.parseInt(WarManager.getValidKey(p).getItemMeta().getLore().get(4).replace("§0", "")));
					if (TownyUniverse.getTownName(block.getLocation()).equalsIgnoreCase(key.getTownName())) {
						if (key.getBlocksLeft() > 0) {
							if (!blockedMaterials.contains(block.getType())) {
								Effects.breakBlockEffect(p.getLocation());
								key.minusBlock();
								e.setCancelled(false);
								e.setBuild(true);
							}
							for (Resident res : town.getResidents()) {
								Player member = Bukkit.getPlayer(res.getName());
								if (member.isOnline()) {
									member.sendMessage(p.getDisplayName() + " §chas placed a block in your Town!"
											+ "\n§aLocation§b:" + " §cX§b: " + p.getLocation().getBlockX()
											+ ", §cY§b: " + p.getLocation().getBlockY()
											+ ", §cZ§b: " + p.getLocation().getBlockZ());
								}
							}
						} else {
							p.sendMessage("§cYour Raid Key has no more Breakable Blocks left.");
						}
					}
				}
			} catch (Exception e1) {
			}
		}
	}
	
	@EventHandler
	public void inventoryDropEvent(PlayerDropItemEvent e) {
		ItemStack drop = e.getItemDrop().getItemStack();
		if (drop.hasItemMeta()) {
			if (drop.getItemMeta().hasDisplayName()) {
				if (drop.getItemMeta().getDisplayName().contains("Raid Key")) {
					if (drop.getItemMeta().hasLore() && drop.getItemMeta().getLore().size() > 4) {
						if (WarManager.checkValidation(Integer.parseInt(drop.getItemMeta().getLore().get(4).replace("§0", "")))) {
							e.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		List<ItemStack> drops = e.getDrops();
		ListIterator<ItemStack> litr = drops.listIterator();
		while (litr.hasNext()) {
			ItemStack key = litr.next();
			if (key.hasItemMeta()) {
				if (key.getItemMeta().hasDisplayName()) {
					if (key.getItemMeta().getDisplayName().contains("Raid Key")) {
						litr.remove();
						e.getDrops().remove(key);
					}
				}
			}
		}
	}

	@EventHandler
	public void keyGeneration(PlayerDeathEvent e) {
		Player killed = e.getEntity();
		Random rand = new Random();
		int random = rand.nextInt(100);
		if(WarListener.force == true){
			random = 100;
			WarListener.force = false;
		}
		if (killed.getKiller() instanceof Player &&  random >= 95) {
			Player killer = killed.getKiller();
				try {
					if (WarManager.isRaiding(killer) == false) {
						Resident killedRes = TownyUniverse.getDataSource().getResident(killed.getName());
						Resident killerRes = TownyUniverse.getDataSource().getResident(killer.getName());
						if (killedRes.hasTown() && killerRes.hasTown()) {
							if (killedRes.getTown().getName() != killerRes.getTown().getName()) {
								if ((killedRes.getTown().hasNation() == true) && (killerRes.getTown().hasNation() == true)) {
									if (killedRes.getTown().getNation().getName() != killerRes.getTown().getNation().getName()) {
										// Add percentage to get a raid key.
										ItemStack key = new ItemStack(Material.GOLD_NUGGET);
										new RaidKey(key, killedRes.getTown(), killer);
										killer.getInventory().addItem(key);
										Town town = TownyUniverse.getDataSource().getTown(WarManager.getKey(key).getTownName());
										for (Resident res : town.getResidents()) {
											Player member = Bukkit.getPlayer(res.getName());
											if (member.isOnline()) {
												member.sendMessage("§cYour town is currently being raided by " + killer.getDisplayName() + "§c.");
												member.setGameMode(GameMode.SURVIVAL);
												member.setFlying(false);
											}
										}
										Bukkit.broadcastMessage(killer.getDisplayName() + " §chas picked up a Raid Key from killing §r" + killed.getDisplayName()
												+ "\n§cTown§b: " + town.getName()
												+ " §cis Under-Attack!");
									}
								}
							}
						}
					}
				} catch (Exception e1) {
				}
		}
	}

	@EventHandler
	public void onHungerLoss(FoodLevelChangeEvent e) {
		Random rand = new Random();
		if (((e.getFoodLevel() < ((Player) e.getEntity()).getFoodLevel()) && (rand.nextInt(100) > 4))) {
			e.setCancelled(true);
		}
	}
    public void onPreCommand(PlayerCommandPreprocessEvent event) {
    	Player player = event.getPlayer();
    	String message = event.getMessage();
    	if(message.startsWith("/") && message.contains(":")) {
    		event.setCancelled(true);
    		player.sendMessage(ChatColor.RED + "DO NOT TRY TO CIRCUMVENT COMMAND BLOCKING!");
    	}
    }

	@EventHandler
	public void onTownChange(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		String message = e.getMessage().toLowerCase();
		try {
			if (WarManager.isBeingRaided(p)) {
				if (message.startsWith("/t set name") || message.startsWith("/town set name")) {
					e.setCancelled(true);
					p.sendMessage("§cYou can not change your town name while being raided.");
				}
				else if (message.startsWith("/t toggle pvp") || message.startsWith("/town toggle pvp") || message.startsWith("/plot toggle pvp")) {
					e.setCancelled(true);
					p.sendMessage("§cYou can not change PvP in your town while being raided.");
				}
			}
		} catch (Exception e1) {
		}
	}
	@EventHandler
	public void inventoryClose(InventoryCloseEvent event) {
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		for (ItemStack stack : event.getPlayer().getInventory()) {
			if (stack != null) {
				if (stack.hasItemMeta()) {
					if (stack.getItemMeta().hasDisplayName() && stack.getItemMeta().hasLore() && stack.getItemMeta().getLore().size() > 4) {
						if(stack.getItemMeta().getDisplayName().contains("Raid Key")){
							ItemMeta meta = stack.getItemMeta();
							meta.setDisplayName("§5§lWar Trophy");
							meta.getLore().set(4, "§cExpired");
							stack.setItemMeta(meta);
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		for (ItemStack stack:event.getPlayer().getInventory()) {
			if (stack != null) {
				if (stack.getType() == Material.GOLD_NUGGET) {
					if (stack.hasItemMeta()) {
						if (stack.getItemMeta().hasDisplayName()) {
							if (stack.getItemMeta().getDisplayName().contains("Raid Key")) {
								RaidKey key = WarManager.getKey(stack);
								if (key != null) {
									key.voidKey();
									if (WarManager.keys.contains(key)) {
										WarManager.keys.remove(key);
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
