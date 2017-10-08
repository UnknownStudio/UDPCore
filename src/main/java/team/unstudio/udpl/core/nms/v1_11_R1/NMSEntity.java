package team.unstudio.udpl.core.nms.v1_11_R1;

import org.bukkit.craftbukkit.v1_11_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

import net.minecraft.server.v1_11_R1.NBTTagCompound;
import team.unstudio.udpl.nms.NmsManager;

public class NMSEntity implements team.unstudio.udpl.nms.NmsEntity{
	
	private final Entity entity;
	
	public NMSEntity(Entity entity) {
		this.entity = entity;
	}

	@Override
	public Entity getEntity() {
		return entity;
	}

	@Override
	public team.unstudio.udpl.nbt.NBTTagCompound getNBT() throws Exception {
		return NmsManager.getNBT().toMap(((CraftEntity)entity).getHandle().e(new NBTTagCompound()));
	}

	@Override
	public team.unstudio.udpl.nms.NmsEntity setNBT(team.unstudio.udpl.nbt.NBTTagCompound nbt) throws Exception {
		((CraftEntity)entity).getHandle().f((NBTTagCompound)NmsManager.getNBT().toNBT(nbt));
		return this;
	}

}
