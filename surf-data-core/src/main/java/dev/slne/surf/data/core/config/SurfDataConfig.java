package dev.slne.surf.data.core.config;

import org.spongepowered.configurate.objectmapping.meta.Required;
import org.spongepowered.configurate.objectmapping.meta.Setting;

public class SurfDataConfig {

  @Required
  @Setting("connection")
  public ConnectionConfig connectionConfig;

  public static class ConnectionConfig {

    @Required
    @Setting("database")
    public DatabaseConfig databaseConfig;

    @Required
    @Setting("redis")
    public RedisConfig redisConfig;

    public static class DatabaseConfig {

      @Required
      @Setting("url")
      public String url = "jdbc:mariadb://127.0.0.1/surf_data";

      @Required
      @Setting("username")
      public String username = "root";

      @Required
      @Setting("password")
      public String password = "";
    }

    public static class RedisConfig {

      @Required
      @Setting("host")
      public String host = "127.0.0.1";

      @Required
      @Setting("port")
      public int port = 6379;

      @Required
      @Setting("username")
      public String username = "";

      @Required
      @Setting("password")
      public String password = "";
    }
  }
}
