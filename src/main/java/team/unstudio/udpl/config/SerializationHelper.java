package team.unstudio.udpl.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public interface SerializationHelper {

	static Map<String,Object> serialize(Object obj){
		Validate.notNull(obj);
		
		Map<String,Object> map = Maps.newLinkedHashMap();
		Class<?> clazz = obj.getClass();
		while(!clazz.equals(Object.class)){
			for(Field field:clazz.getDeclaredFields()){
				int modifiers = field.getModifiers();
				if(Modifier.isFinal(modifiers)||Modifier.isStatic(modifiers)||Modifier.isTransient(modifiers))
					continue;
				
				field.setAccessible(true);
				
				String key = field.getName();
				
				ConfigItem setting = field.getAnnotation(ConfigItem.class);
				if(setting != null && !Strings.isNullOrEmpty(key))
					key = setting.value();
				
				try {
					map.put(key, field.get(obj));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SerializationException("Serialize failed.",e);
				}
			}
			clazz = clazz.getSuperclass();
		}
		
		return map;
	}
	
	static <T> T deserialize(T obj, Map<String,Object> map){
		Validate.notNull(obj);
		Validate.notNull(map);
		
		Class<?> clazz = obj.getClass();
		while(!clazz.equals(Object.class)){
			for(Field field:clazz.getDeclaredFields()){
				int modifiers = field.getModifiers();
				if(Modifier.isFinal(modifiers)||Modifier.isStatic(modifiers)||Modifier.isTransient(modifiers))
					continue;
				
				field.setAccessible(true);
				
				String key = field.getName();
				
				ConfigItem setting = field.getAnnotation(ConfigItem.class);
				if(setting != null && !Strings.isNullOrEmpty(key))
					key = setting.value();
				
				if(map.containsKey(key)){
					try {
						field.set(obj, map.get(key));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new SerializationException("Deserialize failed.",e);
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		
		return obj;
	}
}