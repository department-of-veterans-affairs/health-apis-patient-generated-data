package gov.va.api.health.patientgenerateddata;

import static com.google.common.base.Preconditions.checkState;
import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.r4.api.elements.Reference;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
import org.apache.commons.lang3.StringUtils;

public final class Populaterator {
  private static final ObjectMapper MAPPER = JacksonConfig.createMapper();

  private static String baseDir() {
    return System.getProperty("basedir", ".");
  }

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
    try (Liquibase liquibase =
        new Liquibase(
            baseDir()
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
      String dbFile = baseDir() + "/target/pgd-db";
      new File(dbFile + ".mv.db").delete();
      new File(dbFile + ".trace.db").delete();
      populate(H2.builder().dbFile(dbFile).build());
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
    }
  }

  @SneakyThrows
  public static void observation(@NonNull Connection connection) {
    Set<String> ids = new HashSet<>();
    for (File f : new File(baseDir() + "/src/test/resources/observation").listFiles()) {
      Observation observation = MAPPER.readValue(f, Observation.class);
      Set<ConstraintViolation<Observation>> violations =
          Validation.buildDefaultValidatorFactory().getValidator().validate(observation);
      checkState(violations.isEmpty(), "Invalid payload: " + violations);
      String id = observation.id();
      checkState(id != null);
      checkState(!ids.contains(id), "Duplicate ID " + id);
      ids.add(id);
      String sqlInsert = sqlInsert("app.Observation", List.of("id", "payload", "version"));
      try (PreparedStatement statement = connection.prepareStatement(sqlInsert)) {
        statement.setObject(1, id);
        statement.setObject(2, MAPPER.writeValueAsString(observation));
        statement.setObject(3, 0);
        statement.execute();
      }
    }
  }

  private static Instant parseDateTime(String datetime) {
    if (StringUtils.isBlank(datetime)) {
      return null;
    }
    String str = datetime.trim();
    if (str.endsWith("Z")) {
      return Instant.parse(datetime);
    }
    // Assume time zone offset is provided
    return OffsetDateTime.parse(datetime).toInstant();
  }

  @SneakyThrows
  private static void patient(@NonNull Connection connection) {
    Set<String> ids = new HashSet<>();
    for (File f : new File(baseDir() + "/src/test/resources/patient").listFiles()) {
      Patient patient = MAPPER.readValue(f, Patient.class);
      Set<ConstraintViolation<Patient>> violations =
          Validation.buildDefaultValidatorFactory().getValidator().validate(patient);
      checkState(violations.isEmpty(), "Invalid payload: " + violations);
      String id = patient.id();
      checkState(id != null);
      checkState(!ids.contains(id), "Duplicate ID " + id);
      ids.add(id);
      String sqlInsert = sqlInsert("app.Patient", List.of("id", "payload", "version"));
      try (PreparedStatement statement = connection.prepareStatement(sqlInsert)) {
        statement.setObject(1, id);
        statement.setObject(2, MAPPER.writeValueAsString(patient));
        statement.setObject(3, 0);
        statement.execute();
      }
    }
  }

  @SneakyThrows
  private static void populate(@NonNull Db db) {
    log("Populating " + db.name());
    waitForStartup(db);
    bootstrap(db);
    liquibase(db);
    var connection = db.connection();
    observation(connection);
    patient(connection);
    questionnaire(connection);
    questionnaireResponse(connection);
    connection.commit();
    connection.close();
    log("Finished " + db.name());
  }

  @SneakyThrows
  private static void questionnaire(@NonNull Connection connection) {
    Set<String> ids = new HashSet<>();
    for (File f : new File(baseDir() + "/src/test/resources/questionnaire").listFiles()) {
      Questionnaire questionnaire = MAPPER.readValue(f, Questionnaire.class);
      Set<ConstraintViolation<Questionnaire>> violations =
          Validation.buildDefaultValidatorFactory().getValidator().validate(questionnaire);
      checkState(violations.isEmpty(), "Invalid payload: " + violations);
      String id = questionnaire.id();
      checkState(id != null);
      checkState(!ids.contains(id), "Duplicate ID " + id);
      ids.add(id);
      String sqlInsert = sqlInsert("app.Questionnaire", List.of("id", "payload", "version"));
      try (PreparedStatement statement = connection.prepareStatement(sqlInsert)) {
        statement.setObject(1, id);
        statement.setObject(2, MAPPER.writeValueAsString(questionnaire));
        statement.setObject(3, 0);
        statement.execute();
      }
    }
  }

  @SneakyThrows
  private static void questionnaireResponse(@NonNull Connection connection) {
    Set<String> ids = new HashSet<>();
    for (File f : new File(baseDir() + "/src/test/resources/questionnaire-response").listFiles()) {
      QuestionnaireResponse response = MAPPER.readValue(f, QuestionnaireResponse.class);
      Set<ConstraintViolation<QuestionnaireResponse>> violations =
          Validation.buildDefaultValidatorFactory().getValidator().validate(response);
      checkState(violations.isEmpty(), "Invalid payload: " + violations);
      String id = response.id();
      checkState(id != null);
      checkState(!ids.contains(id), "Duplicate ID " + id);
      ids.add(id);
      String sqlInsert =
          sqlInsert(
              "app.QuestionnaireResponse",
              List.of("id", "payload", "version", "authored", "author"));
      try (PreparedStatement statement = connection.prepareStatement(sqlInsert)) {
        statement.setObject(1, id);
        statement.setObject(2, MAPPER.writeValueAsString(response));
        statement.setObject(3, 0);
        statement.setTimestamp(4, timestamp(parseDateTime(response.authored())));
        statement.setObject(5, resourceId(response.author()));
        statement.execute();
      }
    }
  }

  private static String resourceId(Reference ref) {
    if (ref == null) {
      return null;
    }
    checkState(ref.reference().contains("/"));
    int lastSlashLocation = ref.reference().lastIndexOf("/") + 1;
    return ref.reference().substring(lastSlashLocation);
  }

  private static String sqlInsert(@NonNull String table, @NonNull Collection<String> columns) {
    return "insert into "
        + table
        + " ("
        + columns.stream().collect(joining(","))
        + ") values ("
        + IntStream.range(0, columns.size()).mapToObj(v -> "?").collect(joining(","))
        + ")";
  }

  private static Timestamp timestamp(Instant instant) {
    if (instant == null) {
      return null;
    }
    return new Timestamp(instant.toEpochMilli());
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
