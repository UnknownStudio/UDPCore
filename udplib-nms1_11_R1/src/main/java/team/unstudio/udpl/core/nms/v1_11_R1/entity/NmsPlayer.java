package team.unstudio.udpl.core.nms.v1_11_R1.entity;

import java.util.Locale;

import org.bukkit.craftbukkit.v1_11_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_11_R1.EntityPlayer;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle;
import team.unstudio.udpl.util.PlayerUtils;

public class NmsPlayer extends NmsLivingEntity<Player, EntityPlayer> implements team.unstudio.udpl.nms.entity.NmsPlayer{

	public NmsPlayer(Player player) {
		super(player);
	}

	@Override
	public Locale getLocale() {
		return Locale.forLanguageTag(PlayerUtils.normalizeLanguageTag(getNmsEntity().locale));
	}

	@Override
	public void sendPacket(Object packet){
		if(!(packet instanceof Packet))
			throw new IllegalArgumentException("Packet isn't net.minecraft.server.Packet");
		sendPacket((Packet<?>) packet);
	}

	public <P extends Packet<?>> void sendPacket(P packet) {
		getNmsEntity().playerConnection.sendPacket(packet);
	}

	@Override
	public void sendTitle(String title, String subtitle) {
		if (title != null) {
			PacketPlayOutTitle packetTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
					CraftChatMessage.fromString(title)[0]);
			sendPacket(packetTitle);
		}

		if (subtitle != null) {
			PacketPlayOutTitle packetSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
					CraftChatMessage.fromString(subtitle)[0]);
			sendPacket(packetSubtitle);
		}
	}

	@Override
	public void resetTitle() {
		PacketPlayOutTitle packetReset = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.RESET, null);
		sendPacket(packetReset);
	}

	@Override
	public void sendActionBar(String message){
	    sendPacket(new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}"), (byte)2));
	}
}
