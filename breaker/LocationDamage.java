package breaker;

import java.util.*;

public class LocationDamage{
	public LocationDamage(String locationName, float damageSpeed) {
		if(locationName == null) {
			throw new NullPointerException("Location name is null");
		}
		if(damageSpeed <= 0) {
			System.err.println("Damage speed " + damageSpeed + " <= 0");
			damageSpeed = DEFAULT_DAMAGE_SPEED;
		}
		this.locationName = locationName;
		this.damageSpeed = damageSpeed;
	}
	
	public float getDamage() {
		return damage;
	}
	
	public float getDamageSpeed() {
		return damageSpeed;
	}
	
	public void stopTimer() {
		resettingDamageTimer.cancel();
	}
	
	public void setDamage(float damage) {
		if(damage < 0) {
			damage = 0.0f;
		}
		this.damage = damage;
		float currentDamage = damage;
		resettingDamageTimer.cancel();
		resettingDamageTimer = new Timer();
		LocationDamage currentLocationDamage = this;
		resettingDamageTimer.schedule(new TimerTask() {
   			@Override
   			public void run() {
   				synchronized(locationName) {
   					float damage = currentLocationDamage.getDamage();
		   			if(currentDamage == damage && damage > 0) {
		   				currentLocationDamage.setDamage(0.0f);
		   			}
   				}
   			}
   		}, TIMER_DELAY_MILLIS);
	}

	private float DEFAULT_DAMAGE_SPEED = 0.05F;
	private long TIMER_DELAY_MILLIS = 10000;
	
	private float damage = 0.0f;
	private float damageSpeed;
	private String locationName;
	private Timer resettingDamageTimer = new Timer();
}