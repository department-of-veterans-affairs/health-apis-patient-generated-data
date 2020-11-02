package gov.va.api.health.patientgenerateddata;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.FileSystemResourceAccessor;
import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

public final class Populaterator {
  @SneakyThrows
  private static void bootstrap(@NonNull Db db) {
    log("Bootstrapping " + db.name());
    var maybeConn = db.bootstrapConnection();
    if (maybeConn.isEmpty()) {
      return;
    }
    var conn = maybeConn.get();
    for (var n : db.bootstrapDatabases()) {
      log("Creating database " + n);
      conn.prepareStatement("DROP DATABASE IF EXISTS " + n).execute();
      conn.prepareStatement("CREATE DATABASE " + n).execute();
    }
    conn.commit();
    conn.close();
  }

  @SneakyThrows
  private static void liquibase(@NonNull Db db) {
    var connection = db.connection();
    Database database =
        DatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(new JdbcConnection(connection));
    String baseDir = System.getProperty("basedir", ".");
    try (Liquibase liquibase =
        new Liquibase(
            baseDir
                + "/../patient-generated-data/src/main/resources/db/changelog/db.changelog-master.yaml",
            new FileSystemResourceAccessor(),
            database)) {
      liquibase.update(new Contexts(), new LabelExpression());
    }
  }

  private static void log(@NonNull String msg) {
    System.out.println(msg);
  }

  @SneakyThrows
  public static void main(String[] args) {
    if (Boolean.parseBoolean(System.getProperty("populaterator.h2", "true"))) {
      String baseDir = System.getProperty("basedir", ".");
      populate(H2.builder().dbFile(baseDir + "/target/pgd-db").build());
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
  private static void populate(@NonNull Db db) {
    log("Populating " + db.name());
    waitForStartup(db);
    bootstrap(db);
    liquibase(db);
    var connection = db.connection();
    // populate individual resources
    connection.commit();
    connection.close();
    log("Finished " + db.name());
  }

  @SneakyThrows
  private static void waitForStartup(@NonNull Db db) {
    for (int i = 1; i <= 30; i++) {
      try {
        db.bootstrapConnection();
        return;
      } catch (Exception e) {
        log("Waiting for startup...");
        TimeUnit.SECONDS.sleep(2);
      }
    }
    throw new IllegalStateException("Failed to start");
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
  private static final class H2 implements Db {
    @NonNull String dbFile;

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
  private static final class SqlServer implements Db {
    @NonNull String host;

    @NonNull String port;

    @NonNull String user;

    @NonNull String password;

    @NonNull String database;

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
