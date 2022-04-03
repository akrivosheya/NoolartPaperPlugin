package breaker;

import java.util.*;
import java.util.concurrent.*;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.*;

public class DamageMaster{
	public DamageMaster(FileConfiguration instruments, FileConfiguration materials) throws BadFileConfigurationException{
		if(materials == null) {
			throw new BadFileConfigurationException("materials is null");
		}
        this.instruments = instruments;
        this.materials = materials;
	}
	
	public void clear() {
		damages.clear();
		threadpools.clear();
	}
	
	public void removePlayer(String playerName) {
		if(playerName == null) {
			return;
		}
		if(damages.containsKey(playerName)) {
			DamageData damage = damages.get(playerName);
			Location location = damage.getLocation();
			if(location != null) {
				Player player = Bukkit.getPlayer(playerName);
				if(player != null) {
					player.sendBlockDamage(location, 0.0f);
				}
			}
			damages.remove(playerName);
		}
		threadpools.remove(playerName);
	}
	
	public void setDefaultMode(Player player) {
		if(damages.containsKey(player.getName())) {
			damages.get(player.getName()).setLocation(null);
		}
	}
	
	public boolean initDamage(Player player, Block block, ItemStack item) {
		if(player == null) {
			System.err.println("Player must not be null for initDamage");
			return false;
		}
		if(block == null) {
			System.err.println("Block must not be null for initDamage");
			return false;
		}
		if(item == null) {
			System.err.println("Item must not be null for initDamage");
			return false;
		}
		DamageData damageData;
		float damageSpeed = getDamageSpeed(materials.getLong(block.getType().toString().toLowerCase() + "." + "Density", DEFAULT_DENSITY));
		if(damageSpeed <= NO_DAMAGE) {
			System.err.println("Wrong damage speed " + damageSpeed + ": less or equal zero");
			return false;
		}
		float damageCoefficient = getDamageCoefficient(item);
		if(damageCoefficient <= NO_DAMAGE) {
			System.err.println("Wrong damage coefficient " + damageSpeed + ": less or equal zero");
			return false;
		}
   		Location newLocation = block.getLocation();
		if(!damages.containsKey(player.getName())) {
			damageData = new DamageData(newLocation, damageSpeed, damageCoefficient);
		   	damages.put(player.getName(), damageData);
		}
	    else {
	    	damageData = damages.get(player.getName());
	   		Location oldLocation = damageData.getLocation();
		   	if(oldLocation == null || !sameLocations(oldLocation, newLocation)) {
		   		damageData.setLocation(newLocation);
				damageData.setDamageSpeed(damageSpeed);
		    	damageData.clearDamage();
		    }
		    damageData.setDamageCoefficient(damageCoefficient);
	    }
		return true;
	}
	
	public void increaseDamage(Player player){
		if(player == null) {
			System.err.println("Player must not be null for increaseDamage");
		}
		else if(damages.containsKey(player.getName())) {
			String playerName = player.getName();
			DamageData damageData = damages.get(playerName);
			Location location = damageData.getLocation();
			Block block = player.getTargetBlock(DAMAGE_LENGTH);
   			if(location != null) {
   				String locationName = location.toString();
   				synchronized(locationName) {
		    		if(!location.getBlock().getBlockData().getMaterial().equals(Material.AIR) && 
		    				sameLocations(location, block.getLocation())) {
				    	if(damageData.getDamage() == FULL_DAMAGE) {
				    		String blockType = block.getType().toString().toLowerCase();
				    		block.breakNaturally();
				    		dropLoot(blockType, location, block.getWorld());
			    			damageData.setLocation(null);
			    			return;
				   		}
				   		location.getBlock().setType(block.getBlockData().getMaterial());
				   		player.sendBlockDamage(location, damageData.getDamage());
				   		damageData.increaseDamage(getExtraDamageCoefficient(player, location));
				   		float currentDamage = damageData.getDamage();
				   		if(!threadpools.containsKey(playerName)) {
				   			threadpools.put(playerName, new ScheduledThreadPoolExecutor(1));
				   		}
				   		threadpools.get(playerName).execute(()->{
				   			if(currentDamage >= damageData.getDamage()) {
					   			try {
					   				Thread.sleep(WAITING_TIME_MILLIS);
					   			}
					   			catch(InterruptedException e) {
					   				System.err.println("Problem with sending damage thread: " + e.getMessage());
					   			}
						   		player.sendBlockDamage(location, damageData.getDamage());
				   			}
				   		});
		   			}
   				}
   			}
   		}
    }
	
	private void dropLoot(String block, Location location, World world) {
		if(location == null || world == null) {
			return;
		}
		List<String> items = materials.getStringList(block + ".Items");
		if(items.size() == 0) {
			return;
		}
		Iterator<String> iterator = items.iterator();
		Random random = new Random();
		while(iterator.hasNext()) {
			String lootName = iterator.next();
			Material loot;
			try {
				loot = Material.valueOf(lootName.toUpperCase());
			}
			catch(IllegalArgumentException e) {
				System.err.println("In path " + block + ".Items is illegal material: " + lootName);
				continue;
			}
			Long probability = materials.getLong(block + ".Loots." + lootName + ".probability", -1);
			Long amount = materials.getLong(block + ".Loots." + lootName + ".amount", -1);
			if(probability.intValue() > random.nextInt(99) && amount.intValue() > 0) {
				ItemStack item = new ItemStack(loot, amount.intValue());
				world.dropItem(location, item);
			}
		}
	}
    
    private boolean sameLocations(Location locationA, Location locationB) {
    	if(locationA == null || locationB == null) {
    		return locationA == locationB;
    	}
    	return locationA.getX() == locationB.getX() && 
    			locationA.getY() == locationB.getY() && 
    			locationA.getZ() == locationB.getZ();
    }
    
    private float getDamageSpeed(long density) {
    	return (MAX_DENSITY - density) / MAX_DENSITY / DAMAGE_SPEED_COEFFICIENT;
    }
    
    private float getDamageCoefficient(ItemStack item) {
    	if(item == null) {
    		System.err.println("Can't return damageCoefficient: item is null");
    		return DEFAULT_DAMAGE_COEFFICIENT;
    	}
    	if(instruments == null) {
    		return DEFAULT_DAMAGE_COEFFICIENT;
    	}
        Double damageCoefficient = instruments.getDouble(
        		item.getType().toString().toLowerCase() + ".damageCoefficient", Double.NaN);
        if (Double.isNaN(damageCoefficient)) {
        	return DEFAULT_DAMAGE_COEFFICIENT;
        }
        return damageCoefficient.floatValue();
    }
    
    private float getExtraDamageCoefficient(Player player, Location location) {
    	if(player == null) {
    		System.err.println("Can't return extraDamageCoefficient: player is null");
    		return DEFAULT_DAMAGE_COEFFICIENT;
    	}
    	if(location == null) {
    		System.err.println("Can't return extraDamageCoefficient: location is null");
    		return DEFAULT_DAMAGE_COEFFICIENT;
    	}
    	Location playerLocation = player.getLocation();
    	if(location.getY() + 1.0f <= playerLocation.getY()) {
    		return MAX_EXTRA_DAMAGE_COEFFICIENT;
    	}
    	else if(location.getY() >= playerLocation.getY() + 1.0f) {
    		return MIN_EXTRA_DAMAGE_COEFFICIENT;
    	}
    	else {
    		return DEFAULT_DAMAGE_COEFFICIENT;
    	}
    }
    
    private float FULL_DAMAGE = 1.0f;
    private float NO_DAMAGE = 0.0f;
    private int DAMAGE_LENGTH = 5;
    private float MAX_DENSITY = 19300.0f;
    private int DAMAGE_SPEED_COEFFICIENT = 10;
    private float DEFAULT_DAMAGE_COEFFICIENT = 1.0F;
    private float MAX_EXTRA_DAMAGE_COEFFICIENT = 1.5F;
    private float MIN_EXTRA_DAMAGE_COEFFICIENT = 0.5F;
    private long WAITING_TIME_MILLIS = 100;
    private long DEFAULT_DENSITY = 10000;
    
    private Map<String, Executor> threadpools = new ConcurrentHashMap<String, Executor>();
    private FileConfiguration instruments;
    private FileConfiguration materials;
    private Map<String, DamageData> damages = new ConcurrentHashMap<String, DamageData>();
}