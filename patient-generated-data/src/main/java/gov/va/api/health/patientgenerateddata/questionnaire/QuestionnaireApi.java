package gov.va.api.health.patientgenerateddata.questionnaire;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.Questionnaire;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public interface QuestionnaireApi {
  @Operation(
      summary = "Questionnaire Create",
      description = "https://www.hl7.org/fhir/R4/questionnaire.html",
      tags = {"Questionnaire"})
  @POST
  @Path("Questionnaire")
  @ApiResponse(
      responseCode = "201",
      description = "Record created",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Questionnaire.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Questionnaire questionnaireCreate(
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          Questionnaire body);

  @Operation(
      summary = "Questionnaire Read",
      description = "https://www.hl7.org/fhir/R4/questionnaire.html",
      tags = {"Questionnaire"})
  @GET
  @Path("Questionnaire/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Questionnaire.class)))
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
  Questionnaire questionnaireRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.")
          String id);

  @Operation(
      summary = "Questionnaire Search",
      description = "https://www.hl7.org/fhir/R4/questionnaire.html",
      tags = {"Questionnaire"})
  @GET
  @Path("Questionnaire")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              schema = @Schema(implementation = Questionnaire.Bundle.class),
              mediaType = "application/fhir+json"))
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
  Questionnaire.Bundle questionnaireSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "context-type-value",
              description = "A use-context type and value assigned to the questionnaire.")
          String author,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number of the search result.")
          @DefaultValue("1")
          int page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page. "
                      + "The maximum count size is 100.")
          @DefaultValue("15")
          int count);

  @Operation(
      summary = "Questionnaire Update",
      description = "https://www.hl7.org/fhir/R4/questionnaire.html",
      tags = {"Questionnaire"})
  @PUT
  @Path("Questionnaire/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record updated",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Questionnaire.class)))
  @ApiResponse(
      responseCode = "201",
      description = "Record created",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Questionnaire.class)))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  Questionnaire questionnaireUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.")
          String id,
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          Questionnaire body);
}
