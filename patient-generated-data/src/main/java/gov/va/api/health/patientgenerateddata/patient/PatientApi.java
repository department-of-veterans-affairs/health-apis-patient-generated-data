package gov.va.api.health.patientgenerateddata.patient;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Patient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public interface PatientApi {
  @Operation(
      summary = "Patient Read",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html",
      tags = {"Patient"})
  @GET
  @Path("Patient/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Patient.class)))
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
  Patient patientRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource."
                      + " Once assigned, this value never changes."
                      + " For Patients this id is an Integration Control Number (ICN)"
                      + " assigned by the Master Veteran Index (MVI).")
          String id);

  @Operation(
      summary = "Patient Update",
      description =
          "https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html",
      tags = {"Patient"})
  @PUT
  @Path("Patient/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record updated",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Patient.class)))
  @ApiResponse(
      responseCode = "201",
      description = "New record created",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Patient.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Patient patientUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource." + " Once assigned, this value never changes.")
          String id,
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          Patient body);
}
