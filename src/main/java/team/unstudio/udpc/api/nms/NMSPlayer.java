package team.unstudio.udpc.api.nms;

import org.bukkit.entity.Player;

public interface NMSPlayer extends NMSEntity{

	public Player getPlayer();
	
	/**
	 * 发送一个包
	 * @param packet
	 * @throws Exception
	 */
	public void sendPacket(Object packet) throws Exception;
	
	/**
	 * 发送一个ActionBar
	 * @param message
	 * @throws Exception
	 */
	public void sendActionBar(String message) throws Exception;
	
	/**
	 * 发送一个Title
	 * @param title
	 * @param subtitle
	 * @throws Exception
	 */
	public void sendTitle(String title, String subtitle) throws Exception;
	
	/**
	 * 重置Title
	 * @throws Exception
	 */
	public void resetTitle() throws Exception;
}
