package team.unstudio.udpl.item;

import org.bukkit.inventory.ItemStack;

import team.unstudio.udpl.core.UDPLib;
import team.unstudio.udpl.nms.ReflectionUtils;
import team.unstudio.udpl.nms.ReflectionUtils.PackageType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public enum ItemHelper {

	;
	
	private static final boolean debug = UDPLib.isDebug();
	
	public static Object getNMSItemStack(ItemStack item) {
		try {
			Method asNMSCopy = ReflectionUtils.getMethod(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"),
					"asNMSCopy", ItemStack.class);
			return asNMSCopy.invoke(PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack"), item);
		} catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			if (debug)
				e.printStackTrace();
		}
		return null;
	}
	   
    /**
     * 转换为JSON格式
     * @param itemStack 物品
     * @return 
     */
    public static String toJson(ItemStack itemStack){
		try {
			Class<?> ccitemstack = ReflectionUtils.PackageType.CRAFTBUKKIT_INVENTORY.getClass("CraftItemStack");
			Class<?> citemstack = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("ItemStack");
			Class<?> cmap = ReflectionUtils.PackageType.MINECRAFT_SERVER.getClass("NBTTagCompound");
			Object nbt = cmap.newInstance();
			Method asnmscopy = ccitemstack.getDeclaredMethod("asNMSCopy", ItemStack.class);
			asnmscopy.setAccessible(true);
			Method save = citemstack.getDeclaredMethod("save", cmap);
			save.setAccessible(true);
			return save.invoke(asnmscopy.invoke(null, itemStack), nbt).toString();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
			if(debug)
				e.printStackTrace();
		}
		return "";
    }
}
