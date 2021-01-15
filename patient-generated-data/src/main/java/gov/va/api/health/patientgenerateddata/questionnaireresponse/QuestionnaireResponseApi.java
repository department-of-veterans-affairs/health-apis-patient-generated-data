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
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

public interface QuestionnaireResponseApi {
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
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse questionnaireResponseCreate(
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          QuestionnaireResponse body);

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
                  "The logical ID of the resource. Once assigned, this value never changes.")
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
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  @ApiResponse(
      responseCode = "404",
      description = "Not found",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = OperationOutcome.class)))
  QuestionnaireResponse.Bundle questionnaireResponseSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "author",
              description = "The person or entity who received and recorded the answers.")
          String author,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "authored",
              description =
                  "A date or range of dates (maximum of 2) that describes "
                      + "the date that the response was recorded.")
          String[] authored,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "subject",
              description = "Who or what the answers apply to.")
          String subject,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_tag",
              description = "The metadata tag for the system or code")
          String tag,
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
                  "The logical ID of the resource. Once assigned, this value never changes.")
          String id,
      @RequestBody(required = true, description = "The FHIR resource in JSON format.")
          QuestionnaireResponse body);
}
