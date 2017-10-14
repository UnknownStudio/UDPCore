package team.unstudio.udpl.nms.nbt;

import java.util.Map;

import com.google.common.collect.Maps;

public final class NBTTagFloat extends NBTNumber {
	private float value;

	public NBTTagFloat(float value) {
		super(NBTBaseType.FLOAT);
		this.value = value;
	}

	public float getValue() {
		return this.value;
	}

	public String toString() {
		return Float.toString(this.value)+"F";
	}
	
	@Override
	public byte getByte() {
		return (byte) value;
	}

	@Override
	public short getShort() {
		return (short) value;
	}

	@Override
	public int getInt() {
		return (int) value;
	}

	@Override
	public long getLong() {
		return (long) value;
	}

	@Override
	public float getFloat() {
		return value;
	}

	@Override
	public double getDouble() {
		return value;
	}
	
	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = Maps.newLinkedHashMap();
		map.put("==", getClass().getName());
		map.put("value", getValue());
		return map;
	}
	
	public static NBTTagFloat deserialize(Map<String, Object> map){
		return new NBTTagFloat(((Number)map.get("value")).floatValue());
	}
}
