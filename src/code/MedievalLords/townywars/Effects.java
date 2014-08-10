/**
 * 
 */
package code.MedievalLords.townywars;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;

/**
 * @author creho_000
 * 
 */
public class Effects {

	public static void openChestEffect(Location l) {
		FireworkExplosionPlayer.playToLocation(l, FireworkEffect.builder().
				with(Type.BALL_LARGE).
				withColor(Color.PURPLE).
				withColor(Color.RED).
				withFade(Color.BLACK).
				flicker(true).
				trail(true).
				build());
	}

	public static void breakBlockEffect(Location l) {
		FireworkExplosionPlayer.playToLocation(l, FireworkEffect.builder().
				with(Type.BALL_LARGE).
				withColor(Color.AQUA).
				withColor(Color.RED).
				withFade(Color.BLACK).
				flicker(true).
				trail(true).
				build());
	}

	public static void PlaceBlockEffect(Location l) {
		FireworkExplosionPlayer.playToLocation(l, FireworkEffect.builder().
				with(Type.BALL_LARGE).
				withColor(Color.GREEN).
				withColor(Color.RED).
				withFade(Color.BLACK).
				flicker(true).
				trail(true).
				build());
	}
}
