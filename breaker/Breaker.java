package breaker;

import java.io.File;
import java.util.*;

import org.bukkit.configuration.file.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.*;
import org.bukkit.*;

public class Breaker extends JavaPlugin {
    @Override
    public void onEnable() {
    	FileConfiguration instruments = null, materials = null, recipes = null;
    	materials = getConfiguration("materials.yml");
    	if(materials == null) {
    		return;
    	}
    	instruments = getConfiguration("instruments.yml");
    	recipes = getConfiguration("recipes.yml");
    	if(recipes != null) {
    		addRecipes(recipes);
    	}
        getServer().getPluginManager().registerEvents(new BreakerListener(instruments, materials), this);
    }
    
    private void addRecipes(FileConfiguration recipes) {
    	if(recipes != null) {
    		Set<String> keys = recipes.getKeys(false);
    		Iterator<String> iterator = keys.iterator();
    		while(iterator.hasNext()) {
    			String key = iterator.next();
		    	NamespacedKey namespacedKey = new NamespacedKey(this, key);
		    	Material resultMaterial = getMaterial(recipes, key, "result.type");
		    	if(resultMaterial == null) {
		    		System.err.println("Cannot add recipe \"" + key + "\": some problem with path to \"" + key + ".result.type\"");
		    		continue;
		    	}
		    	int amount = recipes.getInt(key + "." + "result.amount", 0);
		    	if(amount <= 0) {
		    		System.err.println("Cannot add recipe \"" + key + "\": some problem with path to \"" + key + "result.amount\" or value (amount <= 0)");
		    		continue;
		    	}
		    	ItemStack result = new ItemStack(resultMaterial, amount);
		    	ShapedRecipe recipe = new ShapedRecipe(namespacedKey, result);
		    	recipe.shape("123", "456", "789");
		    	boolean addedIngredient = false;
		    	for(char i = '1'; i <= '9'; ++i) {
		    		Material ingredientMaterial = getMaterial(recipes, key, String.valueOf(i));
		    		if(ingredientMaterial == null) {
		    			continue;
		    		}
		    		recipe.setIngredient(i, ingredientMaterial);
		    		addedIngredient = true;
		    	}
		    	if(!addedIngredient) {
		    		System.err.println("Recipe " + key + " hasn't ingredients");
		    		continue;
		    	}
		    	if(Bukkit.addRecipe(recipe)) {
		    		System.out.println("Bukkit added recipe " + key);
		    	}
		    	else {
		    		System.err.println("Bukkit didn't add " + key);
		    	}
    		}
    	}
    }
    
    private Material getMaterial(FileConfiguration recipes, String recipeKey, String typeKey) {
    	String type = recipes.getString(recipeKey + "." + typeKey);
    	if(type == null) {
    		return null;
    	}
    	Material material = Material.getMaterial(type.toUpperCase());
    	if(material == null) {
    		System.err.println("Cannot add recipe \"" + recipeKey + "\": there is no material \"" + type.toUpperCase() + "\"");
    		return null;
    	}
    	return material;
    }
    
    private FileConfiguration getConfiguration(String fileName) {
    	File file = new File(this.getDataFolder() + File.separator + fileName);
        if (file.exists()) {
        	try {
        		return YamlConfiguration.loadConfiguration(file);
        	}
        	catch(IllegalArgumentException exception) {
        		System.err.println("Can't get configuration of  " + file + ": " + exception.getMessage());
        		return null;
        	}
        }
        else {
        	return null;
        }
    }
}

