package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.entities.UCSBOrganizations;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationsRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationsController.class)
@Import(TestConfig.class)
public class UCSBOrganizationsControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationsRepository ucsbOrganizationsRepository;

    @MockBean
    UserRepository userRepository;

    // Tests for GET /api/ucsborganizations/all
    
    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().is(200)); // logged
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsb_organizations() throws Exception {

        // arrange

        UCSBOrganizations zpr = UCSBOrganizations.builder()
                        .orgCode("ZPR")
                        .orgTranslation("ZETA PHI RHO")
                        .orgTranslationShort("ZETA PHI RHO")
                        .inactive(false)
                        .build();

        LocalDateTime ldt2 = LocalDateTime.parse("2022-03-11T00:00:00");

        UCSBOrganizations sky = UCSBOrganizations.builder()
                        .orgCode("SKY")
                        .orgTranslation("SKYDIVING CLUB AT UCSB")
                        .orgTranslationShort("SKYDIVING CLUB")
                        .inactive(false)
                        .build();

        ArrayList<UCSBOrganizations> expectedUCSBOrganizations = new ArrayList<>();
        expectedUCSBOrganizations.addAll(Arrays.asList(zpr, sky));

        when(ucsbOrganizationsRepository.findAll()).thenReturn(expectedUCSBOrganizations);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganizations/all"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationsRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedUCSBOrganizations);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // Tests for POST /api/ucsborganizations/post...

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganizations/post"))
                        .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganizations/post"))
                        .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsb_organization() throws Exception {
        // arrange

        UCSBOrganizations sky = UCSBOrganizations.builder()
                        .orgCode("SKY")
                        .orgTranslation("SKYDIVINGCLUBATUCSB")
                        .orgTranslationShort("SKYDIVINGCLUB")
                        .inactive(true)
                        .build();

        when(ucsbOrganizationsRepository.save(eq(sky))).thenReturn(sky);

        // act
        MvcResult response = mockMvc.perform(
                        post("/api/ucsborganizations/post?orgCode=SKY&orgTranslation=SKYDIVINGCLUBATUCSB&orgTranslationShort=SKYDIVINGCLUB&inactive=true")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).save(sky);
        String expectedJson = mapper.writeValueAsString(sky);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }


    // Tests for GET /api/ucsborganizations?...

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/ucsborganizations?code=SKY"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        // arrange

        UCSBOrganizations sky = UCSBOrganizations.builder()
                        .orgCode("SKY")
                        .orgTranslation("SKYDIVINGCLUBATUCSB")
                        .orgTranslationShort("SKYDIVINGCLUB")
                        .inactive(false)
                        .build();

        when(ucsbOrganizationsRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=SKY"))
                        .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationsRepository, times(1)).findById(eq("SKY"));
        String expectedJson = mapper.writeValueAsString(sky);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

            // arrange

            when(ucsbOrganizationsRepository.findById(eq("DNE"))).thenReturn(Optional.empty());

            // act
            MvcResult response = mockMvc.perform(get("/api/ucsborganizations?code=DNE"))
                            .andExpect(status().isNotFound()).andReturn();

            // assert

            verify(ucsbOrganizationsRepository, times(1)).findById(eq("DNE"));
            Map<String, Object> json = responseToJson(response);
            assertEquals("EntityNotFoundException", json.get("type"));
            assertEquals("UCSBOrganizations with id DNE not found", json.get("message"));
        }

    // Tests for DELETE /api/ucsbdiningcommons?...

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_date() throws Exception {
        // arrange

        UCSBOrganizations sky = UCSBOrganizations.builder()
                        .orgCode("SKY")
                        .orgTranslation("SKYDIVINGCLUBATUCSB")
                        .orgTranslationShort("SKYDIVINGCLUB")
                        .inactive(false)
                        .build();

        when(ucsbOrganizationsRepository.findById(eq("SKY"))).thenReturn(Optional.of(sky));

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?code=SKY")
                                        .with(csrf()))
                        .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("SKY");
        verify(ucsbOrganizationsRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSB Organization with id SKY deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
                throws Exception {
        // arrange

        when(ucsbOrganizationsRepository.findById(eq("SKY"))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                        delete("/api/ucsborganizations?code=SKY")
                                        .with(csrf()))
                        .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationsRepository, times(1)).findById("SKY");
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganizations with id SKY not found", json.get("message"));
    }

}