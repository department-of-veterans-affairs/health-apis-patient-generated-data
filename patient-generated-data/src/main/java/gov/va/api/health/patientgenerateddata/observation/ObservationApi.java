package gov.va.api.health.patientgenerateddata.observation;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface ObservationApi {
  @Operation(
      summary = "Observation Read",
      description = "https://www.hl7.org/fhir/r4/observation.html",
      tags = {"Observation"})
  @GET
  @Path("Observation/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Observation.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Observation observationRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource." + " Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Observation Update",
      description = "https://www.hl7.org/fhir/r4/observation.html",
      tags = {"Observation"})
  @GET
  @Path("Observation/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = ResponseEntity.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  ResponseEntity<Void> observationUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource." + "Once assigned, this value never changes.")
          String id,
      @Valid @RequestBody Observation observation);
}
