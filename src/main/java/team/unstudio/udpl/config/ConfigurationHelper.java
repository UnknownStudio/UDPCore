package team.unstudio.udpl.config;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import org.bukkit.configuration.file.YamlConfiguration;

public interface ConfigurationHelper {
	
    /**
     * 载入配置文件
     */
	@Nullable
    public static YamlConfiguration loadConfiguration(File file){
        if (!file.getAbsoluteFile().getParentFile().exists()) 
        	file.getAbsoluteFile().getParentFile().mkdirs();

        if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {}
        
		return AutoCharsetYamlConfiguration.loadConfiguration(file);
    }
}
