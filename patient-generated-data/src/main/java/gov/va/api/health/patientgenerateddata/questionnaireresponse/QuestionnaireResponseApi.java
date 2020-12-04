package gov.va.api.health.patientgenerateddata.questionnaireresponse;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
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

public interface QuestionnaireResponseApi {
  @Operation(
      summary = "QuestionnaireResponse Read",
      description = "https://www.hl7.org/fhir/R4/questionnaireresponse.html",
      tags = {"QuestionnaireResponse"})
  @GET
  @Path("QuestionnaireResponse/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = QuestionnaireResponse.class)))
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
  QuestionnaireResponse questionnaireResponseRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "QuestionnaireResponse Update",
      description = "https://www.hl7.org/fhir/R4/questionnaireresponse.html",
      tags = {"QuestionnaireResponse"})
  @PUT
  @Path("QuestionnaireResponse/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record updated",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = QuestionnaireResponse.class)))
  @ApiResponse(
      responseCode = "201",
      description = "Record created",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = QuestionnaireResponse.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse questionnaireResponseUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical id of the resource. Once assigned, this value never changes.")
          String id,
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          QuestionnaireResponse body);
}
