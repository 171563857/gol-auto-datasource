package com.gol.auto.dataSource.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class DataSourceUtils {

    public static DataSource druid(RelaxedPropertyResolver propertyResolver, String dbName) {
        String url = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "url"));
        String userName = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "username"));
        String password = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "password"));
        String driveClassName = propertyResolver.getProperty("spring.datasource.driver-class-name");
        String filters = propertyResolver.getProperty("spring.datasource.filters");
        String maxActive = propertyResolver.getProperty("spring.datasource.maxActive");
        String initialSize = propertyResolver.getProperty("spring.datasource.initialSize");
        String maxWait = propertyResolver.getProperty("spring.datasource.maxWait");
        String minIdle = propertyResolver.getProperty("spring.datasource.minIdle");
        String timeBetweenEvictionRunsMillis = propertyResolver.getProperty("spring.datasource.timeBetweenEvictionRunsMillis");
        String minEvictableIdleTimeMillis = propertyResolver.getProperty("spring.datasource.minEvictableIdleTimeMillis");
        String validationQuery = propertyResolver.getProperty("spring.datasource.validationQuery");
        String validationQueryTimeout = propertyResolver.getProperty("spring.datasource.validationQueryTimeout");
        String testWhileIdle = propertyResolver.getProperty("spring.datasource.testWhileIdle");
        String testOnBorrow = propertyResolver.getProperty("spring.datasource.testOnBorrow");
        String testOnReturn = propertyResolver.getProperty("spring.datasource.testOnReturn");
        String poolPreparedStatements = propertyResolver.getProperty("spring.datasource.poolPreparedStatements");
        String maxPoolPreparedStatementPerConnectionSize = propertyResolver.getProperty("spring.datasource.maxPoolPreparedStatementPerConnectionSize");
        String maxOpenPreparedStatements = propertyResolver.getProperty("spring.datasource.maxOpenPreparedStatements");
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(userName);
        druidDataSource.setPassword(password);
        druidDataSource.setDriverClassName(
                StringUtils.isNotBlank(driveClassName) ? driveClassName : "com.mysql.jdbc.Driver");
        druidDataSource.setMaxActive(StringUtils.isNotBlank(maxActive) ? Integer.parseInt(maxActive) : 10);
        druidDataSource.setInitialSize(StringUtils.isNotBlank(initialSize) ? Integer.parseInt(initialSize) : 1);
        druidDataSource.setMaxWait(StringUtils.isNotBlank(maxWait) ? Integer.parseInt(maxWait) : 60000);
        druidDataSource.setMinIdle(StringUtils.isNotBlank(minIdle) ? Integer.parseInt(minIdle) : 3);
        druidDataSource.setTimeBetweenEvictionRunsMillis(StringUtils.isNotBlank(timeBetweenEvictionRunsMillis)
                ? Integer.parseInt(timeBetweenEvictionRunsMillis)
                : 60000);
        druidDataSource.setMinEvictableIdleTimeMillis(StringUtils.isNotBlank(minEvictableIdleTimeMillis)
                ? Integer.parseInt(minEvictableIdleTimeMillis)
                : 300000);
        druidDataSource.setValidationQuery(StringUtils.isNotBlank(validationQuery) ? validationQuery : "select 'x'");
        druidDataSource.setTestWhileIdle(!StringUtils.isNotBlank(testWhileIdle) || Boolean.parseBoolean(testWhileIdle));
        druidDataSource.setTestOnBorrow(StringUtils.isNotBlank(testOnBorrow) && Boolean.parseBoolean(testOnBorrow));
        druidDataSource.setTestOnReturn(StringUtils.isNotBlank(testOnReturn) && Boolean.parseBoolean(testOnReturn));
        druidDataSource.setPoolPreparedStatements(
                !StringUtils.isNotBlank(poolPreparedStatements) || Boolean.parseBoolean(poolPreparedStatements));
        druidDataSource.setMaxOpenPreparedStatements(StringUtils.isNotBlank(maxOpenPreparedStatements)
                ? Integer.parseInt(maxOpenPreparedStatements)
                : 20);
        try {
            druidDataSource.setFilters(StringUtils.isNotBlank(filters) ? filters : "stat, wall");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return druidDataSource;
    }

    public static DataSource atomikos(RelaxedPropertyResolver propertyResolver, String dbName) {
        AtomikosDataSourceBean atomikos = new AtomikosDataSourceBean();
        Properties prop = build(propertyResolver, dbName);
        atomikos.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
        atomikos.setUniqueResourceName(dbName);
        atomikos.setPoolSize(5);
        atomikos.setXaProperties(prop);
        return atomikos;
    }

    private static Properties build(RelaxedPropertyResolver propertyResolver, String dbName) {
        String url = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "url"));
        String userName = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "username"));
        String password = propertyResolver.getProperty(String.format("jdbc.%s.%s", dbName, "password"));
        String driveClassName = propertyResolver.getProperty("spring.datasource.driver-class-name");
        String filters = propertyResolver.getProperty("spring.datasource.filters");
        String maxActive = propertyResolver.getProperty("spring.datasource.maxActive");
        String initialSize = propertyResolver.getProperty("spring.datasource.initialSize");
        String maxWait = propertyResolver.getProperty("spring.datasource.maxWait");
        String minIdle = propertyResolver.getProperty("spring.datasource.minIdle");
        String timeBetweenEvictionRunsMillis = propertyResolver.getProperty("spring.datasource.timeBetweenEvictionRunsMillis");
        String minEvictableIdleTimeMillis = propertyResolver.getProperty("spring.datasource.minEvictableIdleTimeMillis");
        String validationQuery = propertyResolver.getProperty("spring.datasource.validationQuery");
        String validationQueryTimeout = propertyResolver.getProperty("spring.datasource.validationQueryTimeout");
        String testWhileIdle = propertyResolver.getProperty("spring.datasource.testWhileIdle");
        String testOnBorrow = propertyResolver.getProperty("spring.datasource.testOnBorrow");
        String testOnReturn = propertyResolver.getProperty("spring.datasource.testOnReturn");
        String poolPreparedStatements = propertyResolver.getProperty("spring.datasource.poolPreparedStatements");
        String maxPoolPreparedStatementPerConnectionSize = propertyResolver.getProperty("spring.datasource.maxPoolPreparedStatementPerConnectionSize");
        Properties prop = new Properties();
        prop.put("url", url);
        prop.put("username", userName);
        prop.put("password", password);
        // 封装mybatis参数
        prop.put("driverClassName", StringUtils.isNotBlank(driveClassName) ? driveClassName : "com.mysql.jdbc.Driver");
        prop.put("initialSize", StringUtils.isNotBlank(initialSize) ? Integer.parseInt(initialSize) : 1);
        prop.put("maxActive", StringUtils.isNotBlank(maxActive) ? Integer.parseInt(maxActive) : 10);
        prop.put("minIdle", StringUtils.isNotBlank(minIdle) ? Integer.parseInt(minIdle) : 3);
        prop.put("maxWait", StringUtils.isNotBlank(maxWait) ? Integer.parseInt(maxWait) : 60000);
        prop.put("poolPreparedStatements",
                !StringUtils.isNotBlank(poolPreparedStatements) || Boolean.parseBoolean(poolPreparedStatements));
        prop.put("maxPoolPreparedStatementPerConnectionSize",
                StringUtils.isNotBlank(maxPoolPreparedStatementPerConnectionSize)
                        ? Integer.valueOf(maxPoolPreparedStatementPerConnectionSize)
                        : 20);
        prop.put("validationQuery", StringUtils.isNotBlank(validationQuery) ? validationQuery : "select 'x'");
        prop.put("validationQueryTimeout", StringUtils.isNotBlank(validationQueryTimeout)
                ? Integer.valueOf(validationQueryTimeout)
                : 10000);
        prop.put("testOnBorrow", StringUtils.isNotBlank(testOnBorrow) && Boolean.parseBoolean(testOnBorrow));
        prop.put("testOnReturn", StringUtils.isNotBlank(testOnReturn) && Boolean.parseBoolean(testOnReturn));
        prop.put("testWhileIdle", StringUtils.isNotBlank(testWhileIdle) && Boolean.parseBoolean(testWhileIdle));
        prop.put("timeBetweenEvictionRunsMillis", StringUtils.isNotBlank(timeBetweenEvictionRunsMillis)
                ? Integer.parseInt(timeBetweenEvictionRunsMillis)
                : 60000);
        prop.put("minEvictableIdleTimeMillis", StringUtils.isNotBlank(minEvictableIdleTimeMillis)
                ? Integer.parseInt(minEvictableIdleTimeMillis)
                : 300000);
        prop.put("filters", StringUtils.isNotBlank(filters) ? filters : "stat, wall");
        return prop;
    }

    public static SqlSessionFactory sqlSessionFactory(DataSource dataSource, String mapperPath) {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        // 分页插件
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "false");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);
        // 添加XML目录
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Interceptor[] plugins = new Interceptor[]{pageHelper};
        bean.setPlugins(plugins);
        try {
            bean.setMapperLocations(resolver.getResources(mapperPath));
            //取消二级缓存
            bean.getObject().getConfiguration().setCacheEnabled(false);
            return bean.getObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
