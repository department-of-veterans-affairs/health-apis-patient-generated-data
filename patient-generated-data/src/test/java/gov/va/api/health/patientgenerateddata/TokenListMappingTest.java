package gov.va.api.health.patientgenerateddata;

import gov.va.api.health.r4.api.datatypes.Coding;
import gov.va.api.health.r4.api.elements.Meta;
import gov.va.api.health.r4.api.resources.QuestionnaireResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static gov.va.api.health.patientgenerateddata.TokenListMapping.addTerminators;
import static org.assertj.core.api.Assertions.assertThat;

public class TokenListMappingTest {

    private static QuestionnaireResponse _questionnaireResponse(String valueSystem, String valueCode) {
        return QuestionnaireResponse.builder()
                .meta(Meta.builder()
                        .tag(List.of(
                                Coding.builder()
                                        .code(valueCode)
                                        .system(valueSystem)
                                        .build()))
                        .build())
                .build();
    }

    @Test
    void metadataValueJoin_falseMatch() {
        String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", "123"));
        assertThat(join).doesNotContain("clinics|12");
        assertThat(join).doesNotContain("linics|123");
    }

    @Test
    void metadataValueJoin_missingValueSystemAndCode() {
        String join = TokenListMapping.metadataValueJoin(_questionnaireResponse(null, null));
        assertThat(join).isEmpty();
    }

    @Test
    void metadataValueJoin_valueSystem() {
        String join = TokenListMapping.metadataValueJoin(_questionnaireResponse(null, "123"));
        assertThat(join).contains(addTerminators("|123"));
        assertThat(join).doesNotContain(addTerminators("clinics|123"));
        assertThat(join).doesNotContain(addTerminators("clinics|"));

    }

    @Test
    void metadataValueJoin_valueCode() {
        String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", null));
        assertThat(join).contains(addTerminators("clinics|"));
        assertThat(join).doesNotContain(addTerminators("clinics|123"));
        assertThat(join).doesNotContain(addTerminators("|123"));

    }

    @Test
    void metadataValueJoin_valueSystem_valueCode() {
        String join = TokenListMapping.metadataValueJoin(_questionnaireResponse("clinics", "123"));
        assertThat(join).contains(addTerminators("clinics|123"));
        assertThat(join).contains(addTerminators("clinics|"));
        assertThat(join).contains(addTerminators("|123"));

    }

}
