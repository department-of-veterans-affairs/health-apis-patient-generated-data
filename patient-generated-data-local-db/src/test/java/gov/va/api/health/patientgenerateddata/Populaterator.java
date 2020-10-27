package gov.va.api.health.patientgenerateddata;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.Value;

@Value
@AllArgsConstructor(staticName = "of")
public final class Populaterator {
  @SneakyThrows
  private static void bootstrap(Db db) {
    log("Bootstrapping " + db.name());
    var conn = db.bootstrapConnection();
    if (conn.isEmpty()) {
      return;
    }
    for (var n : db.bootstrapDatabases()) {
      log("Creating database " + n);
      conn.get().prepareStatement("DROP DATABASE IF EXISTS " + n).execute();
      conn.get().prepareStatement("CREATE DATABASE " + n).execute();
    }
    conn.get().commit();
    conn.get().close();
  }

  private static void log(String msg) {
    System.out.println(msg);
  }

  @SneakyThrows
  public static void main(String[] args) {
    if (Boolean.parseBoolean(System.getProperty("populaterator.h2", "true"))) {
      String baseDir = System.getProperty("basedir", ".");
      populate(H2.builder().dbFile(baseDir + "/target/mock-pgd-db").build());
    }
    if (Boolean.parseBoolean(System.getProperty("populaterator.sqlserver", "false"))) {
      populate(
          SqlServer.builder()
              .host("localhost")
              .port("1633")
              .user("SA")
              .password("<YourStrong!Passw0rd>")
              .database("pgd")
              .build());
      System.exit(0);
    }
  }

  @SneakyThrows
  private static void populate(Db db) {
    log("Populating " + db.name() + " database");
    bootstrap(db);
    var connection = db.connection();
    log("Creating 'app' schema");
    connection.prepareStatement("CREATE SCHEMA APP").execute();
    // TODO create individual tables
    connection.commit();
    connection.close();
    log("Finished " + db.name());
  }

  interface Db {
    Optional<Connection> bootstrapConnection();

    default List<String> bootstrapDatabases() {
      return Collections.emptyList();
    }

    Connection connection();

    default String name() {
      return getClass().getSimpleName();
    }
  }

  @Value
  @Builder
  static final class H2 implements Db {
    String dbFile;

    @Override
    public Optional<Connection> bootstrapConnection() {
      return Optional.empty();
    }

    @Override
    @SneakyThrows
    public Connection connection() {
      new File(dbFile + ".mv.db").delete();
      new File(dbFile + ".trace.db").delete();
      return DriverManager.getConnection("jdbc:h2:" + dbFile, "sa", "sa");
    }
  }

  @Value
  @Builder
  static final class SqlServer implements Db {
    String host;

    String port;

    String user;

    String password;

    String database;

    @Override
    @SneakyThrows
    public Optional<Connection> bootstrapConnection() {
      String bootstrapUrl =
          String.format("jdbc:sqlserver://%s:%s;user=%S;password=%s", host, port, user, password);
      return Optional.of(DriverManager.getConnection(bootstrapUrl));
    }

    @Override
    public List<String> bootstrapDatabases() {
      return List.of(database);
    }

    @Override
    @SneakyThrows
    public Connection connection() {
      return DriverManager.getConnection(
          String.format(
              "jdbc:sqlserver://%s:%s;user=%S;password=%s;database=%s",
              host, port, user, password, database));
    }
  }
}
