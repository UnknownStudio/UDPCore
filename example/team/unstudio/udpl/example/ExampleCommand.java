package team.unstudio.udpl.example;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import team.unstudio.udpl.command.anno.Alias;
import team.unstudio.udpl.command.anno.Command;
import team.unstudio.udpl.command.anno.Optional;
import team.unstudio.udpl.command.anno.Required;
import team.unstudio.udpl.command.anno.TabComplete;
import team.unstudio.udpl.nms.NmsHelper;
import team.unstudio.udpl.nms.inventory.NmsItemStack;
import team.unstudio.udpl.nms.nbt.NBTTagCompound;

public class ExampleCommand {

	/**
	 * 该方法将会新增一个名字为 example 的子命令
	 */
	@Command(value = {}, // 指令名，省略时自动设置为方法名
			senders = Player.class) // 接收的指令发送者类型，
	@Alias("help") // 指令别名
	@Alias("info") // 指令别名
	public void example(Player player) { // 可选参数，value是默认值，usage是参数的用法
		player.sendMessage(ExamplePlugin.I18N.format(player, "example.i18n"));
	}

	/**
	 * 该方法将自动生成一个名字为 sendmessage 的子命令，包含 2 个参数：
	 * 发送的信息、前缀（可选）
	 */
	@Command(value = "sendmessage", // 指令名
			senders = Player.class, // 接收的指令发送者类型
			permission = "example.sendmessage")// 执行指令必须的权限
	public void sendMessage(Player player, 
							@Required(name = "消息") String message, // 必要参数，usage是参数的用法
							@Optional(value = "false", name = "前缀") boolean prefix) { // 可选参数，value是默认值，usage是参数的用法
		player.sendMessage(prefix?player.getDisplayName()+":"+message:message);
	}
	
	private static final String[] COMMON_WORDS = new String[] { "233", "666", "大吉大利，晚上吃鸡" };
	private static final String[] BOOLEANS = new String[] { "true", "false" };

	@TabComplete("sendmessage") // 需要处理自动补全的指令
	public List<String> sendMessageTabComplete(String[] args) {
		switch (args.length) {
		case 1: {
			String prefix = args[args.length - 1];
			return Arrays.stream(COMMON_WORDS).filter(value -> value.startsWith(prefix)).collect(Collectors.toList());
		}
		case 2: {
			String prefix = args[args.length - 1];
			return Arrays.stream(BOOLEANS).filter(value -> value.startsWith(prefix)).collect(Collectors.toList());
		}
		default:
			return Collections.emptyList();
		}
	}
	
	@Command(value = "give",
			senders = Player.class,
			permission = "example.give")
	public void give(Player player, 
					@Required(name = "物品") Material material,
					@Optional(value = "1", name = "数量") int amount,
					@Optional(value = "0", name = "损害值") short damage) {
		player.getInventory().addItem(new ItemStack(material, amount, damage));
	}
	
	@Command(value = "nmsitem",
			senders = Player.class)
	public void nmsitem(Player player){
		ItemStack itemStack = player.getInventory().getItemInMainHand();
		NmsItemStack nmsItemStack = NmsHelper.createNmsItemStack(itemStack);
		NBTTagCompound tag = nmsItemStack.hasTag() ? nmsItemStack.getTag() : new NBTTagCompound();
		NBTTagCompound display = tag.getCompound("display");
		display.setString("Name", "UDPL Item");
		tag.set("display", display);
		nmsItemStack.setTag(tag);
		itemStack = nmsItemStack.getBukkitItemStack();
	}
}
