package gov.va.api.health.patientgenerateddata.api;

import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.health.r4.api.resources.OperationOutcome;
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

public interface R4ObservationApi {
  @Operation(
      summary = "Observation Create",
      description = "https://www.hl7.org/fhir/r4/observation.html",
      tags = {"Observation"})
  @POST
  @Path("Observation")
  @ApiResponse(
      responseCode = "201",
      description = "Record created",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Observation.class)))
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
  Observation observationCreate(
      @RequestBody(
              required = true,
              description = "The FHIR resource in JSON format.",
              content = @Content(mediaType = "application/fhir+json"))
          Observation body);

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
  Observation observationRead(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "0b9d2e37-f84d-4f9e-9ba3-995772f368d3")
          String id);

  @Operation(
      summary = "Observation Search",
      description = "https://www.hl7.org/fhir/R4/observation.html",
      tags = {"Observation"})
  @GET
  @Path("Observation")
  @ApiResponse(
      responseCode = "200",
      description = "Record found",
      content =
          @Content(
              schema = @Schema(implementation = Observation.Bundle.class),
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
  Observation.Bundle observationSearch(
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_id",
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "0b9d2e37-f84d-4f9e-9ba3-995772f368d3")
          String id,
      @Parameter(
              in = ParameterIn.QUERY,
              name = "_lastUpdated",
              description = "The date when the record was last updated.",
              example = "gt2021-01-01T00:00:00Z")
          String lastUpdated,
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
      summary = "Observation Update",
      description = "https://www.hl7.org/fhir/r4/observation.html",
      tags = {"Observation"})
  @PUT
  @Path("Observation/{id}")
  @ApiResponse(
      responseCode = "200",
      description = "Record updated",
      content =
          @Content(
              mediaType = "application/fhir+json",
              schema = @Schema(implementation = Observation.class)))
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
  Observation observationUpdate(
      @Parameter(
              in = ParameterIn.PATH,
              name = "id",
              required = true,
              description =
                  "The logical ID of the resource. Once assigned, this value never changes.",
              example = "0b9d2e37-f84d-4f9e-9ba3-995772f368d3")
          String id,
      @RequestBody(
              required = true,
              description = "The FHIR resource in JSON format.",
              content = @Content(mediaType = "application/fhir+json"))
          Observation body);
}
