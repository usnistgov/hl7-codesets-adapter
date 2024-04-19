package gov.nist.hit.hl7.codeset.adapter.configuration;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.sql.DataSource;
import java.util.logging.Logger;

@Configuration
public class DataSourceConfig  extends AbstractMongoClientConfiguration {

    private static final String DB_NAME = "db.name";
    private static final String DB_HOST = "db.host";
    private static final String DB_PORT = "db.port";

    @Autowired
    Environment env;



    @Override
    protected String getDatabaseName() {
        String dbName = env.getProperty(DB_NAME);
        return dbName;
    }
    @Override
    public MongoClient mongoClient() {
        String host = env.getProperty(DB_HOST, "localhost");
        int port = env.getProperty(DB_PORT, Integer.class, 27017);
        String uri = env.getProperty("spring.data.mongodb.uri");
        System.out.println("URI: "+uri);
        return MongoClients.create(uri);
    }
}

//{
//
//    private final  Environment env;
//    public DataSourceConfig(Environment env){
//        this.env = env;
//    }
//
//
//    @Bean
//    public DataSource dataSource() {
//        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        dataSource.setDriverClassName(env.getProperty("driverClassName"));
//        dataSource.setUrl(env.getProperty("url"));
//        dataSource.setUsername(env.getProperty("user"));
//        dataSource.setPassword(env.getProperty("password"));
//        return dataSource;
//    }
//}