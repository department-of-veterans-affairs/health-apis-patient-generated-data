package gov.va.api.health.dataquery.service.controller.medication;

import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.Datamart;
import gov.va.api.health.dataquery.service.controller.medication.MedicationSamples.R4;
import java.util.Optional;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class R4MedicationTransformerTest {
  @Test
  public void bestCode() {
    Datamart datamart = Datamart.create();
    DatamartMedication medication = datamart.medication();
    Optional<DatamartMedication.RxNorm> rxnorm = datamart.rxNorm();
    Optional<DatamartMedication.Product> product = datamart.product();
    String localDrugName = medication.localDrugName();
    R4 r4 = R4.create();
    // case: rxnorm = null, product = null, localDrugName = null -> result = localDrugName
    medication.rxnorm(null);
    medication.product(null);
    medication.localDrugName(null);
    assertThat(tx(medication).bestCode()).isEqualTo(r4.codeLocalDrugNameOnly("Unknown"));
    // case: rxnorm = null, product = null, localDrugName = good -> result = localDrugName
    medication.localDrugName(localDrugName);
    assertThat(tx(medication).bestCode()).isEqualTo(r4.codeLocalDrugNameOnly(localDrugName));
    // case: rxnorm = good, product = null,localDrugName = good -> result = rxnorm + localDrugName
    medication.rxnorm(rxnorm);
    assertThat(tx(medication).bestCode()).isEqualTo(r4.codeRxNorm());
    // case: rxnorm = good, product = null,localDrugName = null -> result = rxnorm
    medication.localDrugName(null);
    assertThat(tx(medication).bestCode()).isEqualTo(r4.codeRxNorm());
    // case: rxnorm = null, product = good,localDrugName = good -> result = product + localDrugName
    medication.rxnorm(null);
    medication.product(product);
    medication.localDrugName(localDrugName);
    assertThat(tx(medication).bestCode()).isEqualTo(r4.codeLocalDrugNameWithProduct(localDrugName));
  }

  @Test
  public void bestText() {
    Datamart datamart = Datamart.create();
    DatamartMedication medication = datamart.medication();
    Optional<DatamartMedication.RxNorm> rxnorm = datamart.rxNorm();
    String localDrugName = medication.localDrugName();
    R4 r4 = R4.create();
    // case: rxnorm = good, localDrugName = good -> result = rxnorm
    medication.rxnorm(rxnorm);
    medication.localDrugName(localDrugName);
    assertThat(tx(medication).bestText()).isEqualTo(r4.textRxNorm());
    // case: rxnorm = good, localDrugName = null -> result = rxnorm
    medication.localDrugName(null);
    assertThat(tx(medication).bestText()).isEqualTo(r4.textRxNorm());
    // case: rxnorm = null, localDrugName = good -> result = localDrugName
    medication.rxnorm(null);
    medication.localDrugName(localDrugName);
    assertThat(tx(medication).bestText()).isEqualTo(r4.textLocalDrugName());
    // case: rxnorm = null, localDrugName = null -> result = localDrugName
    medication.rxnorm(null);
    medication.localDrugName(null);
    assertThat(tx(medication).bestText())
        .isEqualTo(r4.textLocalDrugName().div("<div>Unknown</div>"));
  }

  @Test
  public void form() {
    Datamart datamart = Datamart.create();
    Optional<DatamartMedication.Product> product;

    product = Optional.empty();

    // case: product = null -> result = null form
    assertThat(R4MedicationTransformer.form(product)).isNull();

    // case: product = good -> result = product form
    product = datamart.product();
    assertThat(R4MedicationTransformer.form(product)).isEqualTo(R4.create().formFromProduct());
  }

  @Test
  public void identifierFromProduct() {
    R4MedicationTransformer transformer =
        tx(MedicationSamples.Datamart.create().medication().product(null));
    Datamart datamart = Datamart.create();
    Optional<DatamartMedication.Product> product = datamart.product();

    // case: product = null -> result = null identifier
    assertThat(transformer.identifierFromProduct()).isNull();

    // case: product = good -> result = product identifier
    transformer = tx(MedicationSamples.Datamart.create().medication().product(product));
    assertThat(transformer.identifierFromProduct()).isEqualTo(R4.create().identifierFromProduct());
  }

  @SneakyThrows
  String json(Object o) {
    return JacksonConfig.createMapper().writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }

  @Test
  public void medication() {
    assertThat(json(tx(Datamart.create().medication()).toFhir()))
        .isEqualTo(json(R4.create().medication()));
  }

  @Test
  public void toFhir() {
    DatamartMedication dmMedication = Datamart.create().medication();
    assertThat(R4MedicationTransformer.builder().datamart(dmMedication).build().toFhir())
        .isEqualTo(MedicationSamples.R4.create().medication());
  }

  R4MedicationTransformer tx(DatamartMedication dm) {
    return R4MedicationTransformer.builder().datamart(dm).build();
  }
}
