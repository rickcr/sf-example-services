package net.reumann.sf.service;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.SpringBootVFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableConfigurationProperties(SfDataSourceProperties.class)
@EnableTransactionManagement
@ComponentScan(basePackages = {"net.reumann.sf"})
@MapperScan(value = {"net.reumann.sf.mapper"}, sqlSessionFactoryRef = "sfServicesSqlSessionFactory")
public class SfDataServicesConfig {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private SfDataSourceProperties sfDataSourceProperties;

	public static final String SF_SERVICES_SQL_SESSION_FACTORY = "sfServicesSqlSessionFactory";
	public static final String SF_SERVICES_TRANSACTION = "sfServicesTransaction";


	@Bean(name="sfDatasource")
	public DataSource dataSource() {
		return sfDataSource();
	}

	@Bean(name=SF_SERVICES_SQL_SESSION_FACTORY)
	public SqlSessionFactory sfServicesSqlSessionFactory() throws Exception {
		SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setTypeAliasesPackage("net.reumann.data.domain");
		factoryBean.setTypeHandlersPackage("net.reumann.sf.service.typehandler");
		factoryBean.setVfs(SpringBootVFS.class);
 
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		configuration.setCallSettersOnNulls(true);
		factoryBean.setConfiguration(configuration);

		return factoryBean.getObject();
	}

	@Bean(name="sfDataSource")
	public DataSource sfDataSource() {
		log.debug("sfDataSource: SfDataSourceProperties  = {}", sfDataSourceProperties);
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(sfDataSourceProperties.getDriverClassName());
		dataSource.setJdbcUrl(sfDataSourceProperties.getJdbcUrl());
		dataSource.setUsername(sfDataSourceProperties.getUsername());
		dataSource.setPassword(sfDataSourceProperties.getPassword());
		dataSource.setMaximumPoolSize(sfDataSourceProperties.getMaximumPoolSize());
		dataSource.setConnectionTimeout(sfDataSourceProperties.getConnectionTimeout());
		return dataSource;
	}
	@Bean(name = SF_SERVICES_TRANSACTION)
	public DataSourceTransactionManager transactionManager(@Qualifier("sfDataSource") DataSource datasource) {
		return new DataSourceTransactionManager(datasource);
	}

}