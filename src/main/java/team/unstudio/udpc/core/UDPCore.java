package team.unstudio.udpc.core;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import team.unstudio.udpc.api.command.tree.TreeCommandManager;
import team.unstudio.udpc.api.area.Area;
import team.unstudio.udpc.api.area.AreaListener;
import team.unstudio.udpc.api.area.AreaManager;
import team.unstudio.udpc.api.command.tree.CommandNode;
import team.unstudio.udpc.test.Example;

/**
 * UDPCore
 * @author AAA
 *
 */
public class UDPCore extends JavaPlugin{
	
	public static final String NAME = "UDPCore";
	public static final String VERSION = "1.0.0-SANPSHOT";
	//TODO:BossBar,Scoreboard,Hologram,Tab
	
	private static final File PLUGIN_PATH = new File("plugins");
	
	public static UDPCore INSTANCE;
	public static boolean debug;
	
	@Override
	public void onLoad() {
		INSTANCE = this;
		
		ConfigurationSerialization.registerClass(Area.class);
	}
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		ConfigurationHandler.reload();
		
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
		
		Example.INSTANCE.onEnable();
		
		getServer().getPluginManager().registerEvents(new AreaListener(), this);
		AreaManager.loadAll();
	}
	
	@Override
	public void onDisable() {
		AreaManager.saveAll();
	}
	
	public static void debug(String arg){
		if(debug)INSTANCE.getLogger().info(arg);
	}
}
