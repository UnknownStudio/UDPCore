package team.unstudio.udpl.core.test;

import java.io.File;

import team.unstudio.udpl.config.ConfigurationHandler;
import team.unstudio.udpl.core.UDPLib;

public class TestConfiguration extends ConfigurationHandler{

	@ConfigItem("test")
	public String test = "Unload configuration";
	
	public TestConfiguration() {
		super(new File(UDPLib.getInstance().getDataFolder(),"test.yml"));
	}

}
