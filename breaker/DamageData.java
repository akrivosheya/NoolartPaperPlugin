package breaker;

import org.bukkit.*;
import java.util.*;
import java.util.concurrent.*;

public class DamageData{
	DamageData(Location location, float damageSpeed, float damageCoefficient){
		if(damageSpeed <= NO_DAMAGE) {
			System.err.println("For location " + location + " damageSpeed was " + damageSpeed + " <= 0");
			damageSpeed = DEFAULT_DAMAGE_SPEED;
		}
		if(location != null) {
			locationsData.put(location.toString(), new LocationDamage(location.toString(), damageSpeed));
		}
		if(damageCoefficient <= NO_DAMAGE) {
			damageCoefficient = DEFAULT_DAMAGE_COEFFICIENT;
		}
		this.damageCoefficient = damageCoefficient;
	}
	
	public boolean containsLocation(String locationName) {
		if(locationName == null) {
			return false;
		}
		return locationsData.containsKey(locationName);
	}
	
	public float getDamage(String locationName){
		return locationsData.get(locationName).getDamage();
	}
	
	public void putLocation(Location location, float damageSpeed){
		if(location != null) {
			String locationName = location.toString();
			if(locationsData.size() >= MAX_SIZE_LOCATIONS && !locationsData.containsKey(locationName)) {
				String locationToRemove = (String)(locationsData.keySet().toArray()[0]);
				locationsData.remove(locationToRemove);
			}
			if(damageSpeed <= NO_DAMAGE) {
				System.err.println("For location " + location + " damageSpeed was " + damageSpeed + " <= 0");
				damageSpeed = DEFAULT_DAMAGE_SPEED;
			}
			locationsData.put(locationName, new LocationDamage(location.toString(), damageSpeed));
		}
	}
	
	public void removeLocation(String locationName) {
		if(locationName != null && locationsData.containsKey(locationName)) {
			locationsData.get(locationName).stopTimer();
			locationsData.remove(locationName);
		}
	}
	
	public void increaseDamage(String locationName, float extraDamageCoefficient) {
		if(locationName == null || !locationsData.containsKey(locationName)) {
			return;
		}
		float damage = locationsData.get(locationName).getDamage();
		float damageSpeed = locationsData.get(locationName).getDamageSpeed();
		if(damage == FULL_DAMAGE) {
			return;
		}
		if(extraDamageCoefficient <= NO_DAMAGE) {
			extraDamageCoefficient = DEFAULT_DAMAGE_COEFFICIENT;
		}
		damage += damageSpeed * damageCoefficient * extraDamageCoefficient;
		if(damage > FULL_DAMAGE) {
			damage = FULL_DAMAGE;
		}
		locationsData.get(locationName).setDamage(damage);
	}
	
	public void setDamageCoefficient(float damageCoefficient){
		if(damageCoefficient <= NO_DAMAGE) {
			damageCoefficient = DEFAULT_DAMAGE_COEFFICIENT;
		}
		this.damageCoefficient = damageCoefficient;
	}

	private float NO_DAMAGE = 0.0f;
	private float FULL_DAMAGE = 1.0f;
	private float DEFAULT_DAMAGE_COEFFICIENT = 1.0f;
	private float DEFAULT_DAMAGE_SPEED = 0.05F;
	private int MAX_SIZE_LOCATIONS = 10;
	
	private Map<String, LocationDamage> locationsData = new ConcurrentHashMap<String, LocationDamage>();
	private float damageCoefficient;
}
