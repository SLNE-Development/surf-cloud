package dev.slne.surf.data.core.config;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class SurfDataConfig {

  @Comment("Config for various connections")
  @Setting("connection")
  public ConnectionConfig connectionConfig = new ConnectionConfig();

  @ConfigSerializable
  public static class ConnectionConfig {

    @Comment("Config for database connection")
    @Setting("database")
    public DatabaseConfig databaseConfig = new DatabaseConfig();

    @Comment("Config for redis connection")
    @Setting("redis")
    public RedisConfig redisConfig = new RedisConfig();

    @ConfigSerializable
    public static class DatabaseConfig {

      @Comment("URL for database connection. Should be in the format of jdbc:<db_type>://<host>:<port>/<database>")
      @Setting("url")
      public String url = "jdbc:mariadb://127.0.0.1/surf_data";

      @Comment("Username for database connection")
      @Setting("username")
      public String username = "root";

      @Comment("Password for database connection")
      @Setting("password")
      public String password = "";
    }

    @ConfigSerializable
    public static class RedisConfig {

      @Comment("Host for redis connection")
      @Setting("host")
      public String host = "127.0.0.1";

      @Comment("Port for redis connection")
      @Setting("port")
      public int port = 6379;

      @Comment("Username for redis connection")
      @Setting("username")
      public String username = "";

      @Comment("Password for redis connection")
      @Setting("password")
      public String password = "";
    }
  }
}
