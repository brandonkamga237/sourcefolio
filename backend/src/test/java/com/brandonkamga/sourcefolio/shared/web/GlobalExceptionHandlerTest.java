package com.brandonkamga.sourcefolio.shared.web;

import com.brandonkamga.sourcefolio.shared.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GlobalExceptionHandlerTest.Probe.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandlerTest.Probe.class, LocaleConfig.class})
class GlobalExceptionHandlerTest {

    @RestController
    static class Probe {
        @GetMapping("/probe/missing")
        String missing() {
            throw new NotFoundException();
        }

        @GetMapping("/probe/broken")
        String broken() {
            throw new IllegalStateException("database on fire");
        }

        @PostMapping("/probe/feedback")
        String feedback(@Valid @RequestBody Feedback feedback) {
            return feedback.body();
        }
    }

    record Feedback(@Size(min = 10, max = 1000) String body) {
    }

    @Autowired
    private MockMvc mvc;

    @Test
    @DisplayName("a refused rule answers with its status, a stable code and a message in the caller's language")
    void appExceptionIsLocalised() throws Exception {
        mvc.perform(get("/probe/missing").header("Accept-Language", "fr-FR,fr;q=0.9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Rien n’a été trouvé à cette adresse."));

        mvc.perform(get("/probe/missing").header("Accept-Language", "en-US"))
                .andExpect(jsonPath("$.message").value("Nothing was found at this address."));
    }

    @Test
    @DisplayName("a language we do not speak gets English")
    void unknownLanguageFallsBackToEnglish() throws Exception {
        mvc.perform(get("/probe/missing").header("Accept-Language", "de-DE"))
                .andExpect(jsonPath("$.message").value("Nothing was found at this address."));
    }

    @Test
    @DisplayName("invalid fields come back one by one, with the overall message localised")
    void validationListsFields() throws Exception {
        mvc.perform(post("/probe/feedback").header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Nice!\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("Certains champs ne sont pas valides."))
                .andExpect(jsonPath("$.fields.body").exists());
    }

    @Test
    @DisplayName("an unexpected failure hides its cause and gives a reference to find the log line")
    void unexpectedFailureHasReference() throws Exception {
        mvc.perform(get("/probe/broken"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.reference").exists())
                .andExpect(jsonPath("$.message").value("Something went wrong on our side. Try again in a moment."));
    }
}
