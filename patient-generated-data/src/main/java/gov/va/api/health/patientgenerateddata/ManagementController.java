package gov.va.api.health.patientgenerateddata;

import static gov.va.api.health.patientgenerateddata.Controllers.checkRequestState;
import static org.apache.commons.lang3.StringUtils.isBlank;

import gov.va.api.health.patientgenerateddata.observation.ObservationController;
import gov.va.api.health.patientgenerateddata.observation.ObservationEntity;
import gov.va.api.health.patientgenerateddata.observation.ObservationRepository;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireController;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireEntity;
import gov.va.api.health.patientgenerateddata.questionnaire.QuestionnaireRepository;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseController;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseEntity;
import gov.va.api.health.patientgenerateddata.questionnaireresponse.QuestionnaireResponseRepository;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.Questionnaire;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import gov.va.api.health.r4.api.resources.Resource;
import java.net.URI;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal data management controller. */
@Validated
@RestController
@RequestMapping(
    value = "/management/r4",
    produces = {"application/json", "application/fhir+json"})
@AllArgsConstructor(onConstructor_ = @Autowired)
public class ManagementController {
  private final LinkProperties linkProperties;

  @Getter private final ObservationRepository observationRepository;

  @Getter private final QuestionnaireRepository questionnaireRepository;

  @Getter private final QuestionnaireResponseRepository questionnaireResponseRepository;

  @PostMapping(value = "/Observation")
  ResponseEntity<Observation> create(@Valid @RequestBody Observation observation) {
    String id = verifyAndGetId(observation, observationRepository);
    ObservationEntity entity = ObservationController.toEntity(observation);
    observationRepository.save(entity);
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Observation/" + id))
        .body(observation);
  }

  @PostMapping(value = "/Questionnaire")
  ResponseEntity<Questionnaire> create(@Valid @RequestBody Questionnaire questionnaire) {
    String id = verifyAndGetId(questionnaire, questionnaireRepository);
    QuestionnaireEntity entity = QuestionnaireController.toEntity(questionnaire);
    questionnaireRepository.save(entity);
    return ResponseEntity.created(URI.create(linkProperties.r4Url() + "/Questionnaire/" + id))
        .body(questionnaire);
  }

  @PostMapping(value = "/QuestionnaireResponse")
  ResponseEntity<QuestionnaireResponse> create(
      @Valid @RequestBody QuestionnaireResponse questionnaireResponse) {
    String id = verifyAndGetId(questionnaireResponse, questionnaireResponseRepository);
    QuestionnaireResponseEntity entity =
        QuestionnaireResponseController.toEntity(questionnaireResponse);
    questionnaireResponseRepository.save(entity);
    return ResponseEntity.created(
            URI.create(linkProperties.r4Url() + "/QuestionnaireResponse/" + id))
        .body(questionnaireResponse);
  }

  <R extends Resource, T extends PayloadEntity<R>> String verifyAndGetId(
      R resource, CrudRepository<T, String> repository) {
    String id = resource.id();
    checkRequestState(!isBlank(id), "ID is required");
    if (repository.existsById(id)) {
      throw new Exceptions.AlreadyExists(String.format("Already exists %s", id));
    }
    return id;
  }
}
