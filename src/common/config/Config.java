package common.config;

public class Config {
	private final boolean enabled;
	
	public Config(boolean enabled) {
		this.enabled = enabled;
	}
	
	public boolean isEnabled() {
		return this.enabled;
	}
}
