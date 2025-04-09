package net.reumann.sf.service;
 
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Slf4j
@Getter
@Setter
@ConfigurationProperties(prefix="sfdb", ignoreUnknownFields = true)
public class SfDataSourceProperties implements InitializingBean{
	private String driverClassName;
	private String jdbcUrl;
	private String username;
	private String password;
	private int maximumPoolSize;
	private long connectionTimeout;

	@Override
	public void afterPropertiesSet() throws Exception {
	}

}