package ru.kapahgaiii.qa.config;

import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBuilder;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class HibernateConfig {

    private @Value("${jdbc.driverClassName}")
    String jdbcDriver;
    private @Value("${jdbc.url}")
    String jdbcUrl;
    private @Value("${jdbc.username}")
    String jdbcUsername;
    private @Value("${jdbc.password}")
    String jdbcPassword;

    @Bean(name = "dataSource")
    public DataSource getDataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);

        return dataSource;
    }

    public Properties getHibernateProperties() {
        Properties p = new Properties();
        p.put("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
        p.put("hibernate.show_sql","false");
        p.put("hibernate.format_sql", "true");
        return p;
    }

    @Bean(name = "sessionFactory")
    public SessionFactory getSessionFactory() {
        LocalSessionFactoryBuilder sessionBuilder = new LocalSessionFactoryBuilder(getDataSource());
        sessionBuilder.addProperties(getHibernateProperties());
        sessionBuilder.scanPackages("ru.kapahgaiii.qa.domain");
        return sessionBuilder.buildSessionFactory();
    }

    @Bean(name = "transactionManager")
    public HibernateTransactionManager getTransactionManager() {
        return new HibernateTransactionManager(getSessionFactory());
    }

    @Bean
    public static PropertyPlaceholderConfigurer getHibernateConfig(){
        PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
        configurer.setLocation(new ClassPathResource("jdbc.properties"));
        return configurer;
    }
}
