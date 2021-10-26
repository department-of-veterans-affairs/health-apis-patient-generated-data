package gov.va.api.health.patientgenerateddata.api;

import gov.va.api.health.r4.api.resources.OperationOutcome;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public interface R4QuestionnaireResponseApi {
  @Operation(
      summary = "QuestionnaireResponse Create",
      description = "https://www.hl7.org/fhir/R4/questionnaireresponse.html",
      tags = {"QuestionnaireResponse"})
  @POST
  @Path("QuestionnaireResponse")
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
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "429",
      description = "Too many requests",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse questionnaireResponseCreate(
      @RequestBody(
              required = true,
              description = "The FHIR resource in JSON format.",
              content = @Content(mediaType = "application/fhir+json"))
          QuestionnaireResponse body);

  @Operation(
      summary = "QuestionnaireResponse Delete",
      description = "https://www.hl7.org/fhir/R4/questionnaireresponse.html",
      tags = {"QuestionnaireResponse"})
  @DELETE
  @Path("QuestionnaireResponse/{id}")
  @ApiResponse(responseCode = "204", description = "Record deleted")
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "429",
      description = "Too many requests",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  void questionnaireResponseDelete(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "f003043a-9047-4c3a-b15b-a26c67f4e723")
          String id);

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
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "429",
      description = "Too many requests",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse questionnaireResponseRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "f003043a-9047-4c3a-b15b-a26c67f4e723")
          String id);

  @Operation(
      summary = "QuestionnaireResponse Search",
      description = "https://www.hl7.org/fhir/R4/questionnaireresponse.html",
      tags = {"QuestionnaireResponse"})
  @GET
  @Path("QuestionnaireResponse")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              schema = @Schema(implementation = QuestionnaireResponse.Bundle.class),
              mediaType = "application/fhir+json"))
  @ApiResponse(
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "429",
      description = "Too many requests",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse.Bundle questionnaireResponseSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "f003043a-9047-4c3a-b15b-a26c67f4e723")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_lastUpdated",
              description = "The date when the record was last updated.",
              example = "gt2021-01-01T00:00:00Z")
          String lastUpdated,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "author",
              description = "The person or entity who received and recorded the answers.",
              example = "1011537977V693883")
          String author,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "authored",
              description =
                  "A date or range of dates (maximum of 2) that describes "
                      + "the date that the response was recorded.",
              example = "2013-02-19T19:15:00Z")
          String[] authored,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "source",
              description = "The person who answered the questions",
              example = "1011537977V693883")
          String source,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "subject",
              description = "Who or what the answers apply to.",
              example = "1011537977V693883")
          String subject,
      @Parameter(in = ParameterIn.QUERY, name = "_tag", description = "A metadata tag") String tag,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "questionnaire",
              description = "The questionnaire that is being responded",
              example = "37953b72-961b-41ee-bd05-86c62bacc46b")
          String questionnaire,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "page",
              description = "The page number of the search result.",
              example = "1")
          @DefaultValue("1")
          int page,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_count",
              description =
                  "The number of resources that should be returned in a single page. "
                      + "The maximum count size is 100.",
              example = "15")
          @DefaultValue("15")
          int count);

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
      responseCode = "400",
      description = "Bad request",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "401",
      description = "Unauthorized",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "403",
      description = "Forbidden",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "429",
      description = "Too many requests",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "500",
      description = "Internal server error",
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse questionnaireResponseUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "f003043a-9047-4c3a-b15b-a26c67f4e723")
          String id,
      @RequestBody(
              required = true,
              description = "The FHIR resource in JSON format.",
              content = @Content(mediaType = "application/fhir+json"))
          QuestionnaireResponse body);
}
