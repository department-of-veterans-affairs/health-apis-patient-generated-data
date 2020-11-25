package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.patientgenerateddata.patient.PatientApi;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import javax.ws.rs.Path;

@OpenAPIDefinition(
    servers = {
      @Server(url = "https://blue.lab.lighthouse.va.gov/pgd/v0/r4/", description = "Sandbox")
    },
    externalDocs =
        @ExternalDocumentation(
            description = "US Core Implementation Guide",
            url = "https://build.fhir.org/ig/HL7/US-Core-R4/index.html"))
@Path("/")
public interface OpenApi extends PatientApi {}
