package com.brandonkamga.sourcefolio.waitlist.api;

import com.brandonkamga.sourcefolio.shared.exception.BadRequestException;
import com.brandonkamga.sourcefolio.shared.security.SecurityTestConfig;
import com.brandonkamga.sourcefolio.waitlist.application.JoinWaitlist;
import com.brandonkamga.sourcefolio.waitlist.application.Waitlist;
import com.brandonkamga.sourcefolio.waitlist.infra.security.WaitlistRoutesTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WaitlistController.class)
@Import({SecurityTestConfig.class, WaitlistRoutesTestConfig.class})
class WaitlistControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private Waitlist waitlist;

    @Test
    @DisplayName("a visitor signs up without an account and learns nothing about who is listed")
    void signsUp() throws Exception {
        mvc.perform(post("/api/waitlist").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ada@acme.africa\",\"consent\":true}"))
                .andExpect(status().isNoContent());

        verify(waitlist).join(new JoinWaitlist("ada@acme.africa", true));
    }

    @Test
    @DisplayName("a refusal is a 400 in the visitor's language")
    void refusal() throws Exception {
        doThrow(new BadRequestException("waitlist.email-invalid")).when(waitlist).join(new JoinWaitlist("nope", true));

        mvc.perform(post("/api/waitlist").with(csrf()).header("Accept-Language", "fr").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nope\",\"consent\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Indique une adresse e-mail valide."));
    }

    @Test
    @DisplayName("without the CSRF token the write is refused")
    void needsCsrf() throws Exception {
        mvc.perform(post("/api/waitlist").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"ada@acme.africa\",\"consent\":true}"))
                .andExpect(status().isForbidden());
    }
}
