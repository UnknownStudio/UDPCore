package team.unstudio.udpl.core;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import team.unstudio.udpl.area.Area;
import team.unstudio.udpl.area.AreaDataContainer;
import team.unstudio.udpl.bungeecord.ServerLocation;
import team.unstudio.udpl.command.tree.CommandNode;
import team.unstudio.udpl.command.tree.TreeCommandManager;
import team.unstudio.udpl.mapping.MappingHelper;
import team.unstudio.udpl.nms.NmsHelper;
import team.unstudio.udpl.nms.nbt.NBTUtils;
import team.unstudio.udpl.test.TestLoader;

public final class UDPLib extends JavaPlugin{

	public static final String NAME = "UDPLib";
	public static final String VERSION = "1.0.0";

	private static final File PLUGIN_PATH = new File("plugins");

	private static UDPLib INSTANCE;
	private static boolean DEBUG;
	private static UDPLConfiguration CONFIG;

	public UDPLib() {
		INSTANCE = this;
	}

	@Override
	public void onLoad() {
		ConfigurationSerialization.registerClass(AreaDataContainer.class);
		ConfigurationSerialization.registerClass(Area.class);
		ConfigurationSerialization.registerClass(ServerLocation.class);
		
		NBTUtils.registerAllNBTSerilizable();
		
		MappingHelper.loadMapping();
		NmsHelper.loadNmsHelper();
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		CONFIG = new UDPLConfiguration(new File(getDataFolder(), "config.yml"));
		CONFIG.reload();
		setDebug(CONFIG.debug);
		
		if(CONFIG.enableTest)
			TestLoader.INSTANCE.onLoad();
		
		loadPluginManager();
		
		if(CONFIG.enableTest)
			TestLoader.INSTANCE.onEnable();
	}

	@Override
	public void onDisable() {
		//防止玩家有未关闭的界面造成刷物品
		for(Player player:Bukkit.getOnlinePlayers())
			player.closeInventory();
	}
	
	private void loadPluginManager(){
		new TreeCommandManager("pm", this).addNode(new CommandNode() {
			@Override
			public boolean onCommand(CommandSender sender, Object[] args) {
				Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin((String)args[0]));
				sender.sendMessage("[PluginManager]卸载插件成功: "+args[0]);
				return true;
			}
		}.setNode("disable").setPermission("udpc.pm.disable").setParameterTypes(String.class).setUsage("<Plugin>"))
		.addNode(new CommandNode() {
			@Override
			public boolean onCommand(CommandSender sender, Object[] args) {
				String file = (String) args[0];
				if(!file.endsWith(".jar"))file=file+".jar";
				try {
					Bukkit.getPluginManager().enablePlugin(Bukkit.getPluginManager().loadPlugin(new File(PLUGIN_PATH, file)));
					sender.sendMessage("[PluginManager]加载插件成功: "+file);
				} catch (Exception e) {
					sender.sendMessage("[PluginManager]加载插件失败: "+file);
				}
				return true;
			}
		}.setNode("enable").setPermission("udpc.pm.enable").setParameterTypes(String.class).setUsage("<Plugin>"))
		.addNode(new CommandNode() {
			@Override
			public boolean onCommand(CommandSender sender, Object[] args) {
				StringBuilder b = new StringBuilder("[PluginManager]");
				for(Plugin p:Bukkit.getPluginManager().getPlugins())b.append(p.getName()+" ");
				sender.sendMessage(b.toString());
				return true;
			}
		}.setNode("plugins").setPermission("udpc.pm.plugins")).registerCommand();
	}

	public static UDPLConfiguration getUDPLConfig(){
		return CONFIG;
	}

	public static UDPLib getInstance(){
		return INSTANCE;
	}
	
	public static boolean isDebug(){
		return DEBUG;
	}
	
	public static void setDebug(boolean debug){
		DEBUG = debug;
	}
}
