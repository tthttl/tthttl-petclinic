package tthttl.vetservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tthttl.vetservice.model.Vet;
import tthttl.vetservice.service.VetService;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tthttl.vetservice.VetServiceTestHelper.createVet;

@ExtendWith(MockitoExtension.class)
class VetControllerTest {

    @Mock
    private VetService vetService;

    @InjectMocks
    private VetController testee;

    MockMvc mockMvc;

    Vet vet;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(testee).build();
        vet = createVet();
    }

    @Test
    void saveVet() throws Exception {
        when(vetService.save(Mockito.any())).thenReturn(vet);

        mockMvc.perform
                (post("/vets")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(createRequestBody(vet))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(vet.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(vet.getLastName())))
                .andExpect(jsonPath("$.specialties", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.specialties[0].id", is(vet.getSpecialties()
                        .iterator().next().getId().intValue())))
                .andExpect(jsonPath("$.specialties[0].name", is(vet.getSpecialties().iterator().next().getName())));
    }

    @Test
    void findById() throws Exception {
        when(vetService.findById(Mockito.any())).thenReturn(Optional.of(vet));

        mockMvc.perform(get("/vets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(vet.getFirstName())));

    }

    @Test
    void findAll() throws Exception {
        when(vetService.findAll()).thenReturn(Collections.singletonList(vet));

        mockMvc.perform(get("/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].firstName", is(vet.getFirstName())));
    }

    @Test
    void update() throws Exception {
        Vet updatedVet = new Vet();
        String updatedName = "Morty";
        updatedVet.setFirstName(updatedName);
        when(vetService.findById(Mockito.any())).thenReturn(Optional.of(vet));
        when(vetService.save(Mockito.any())).thenReturn(updatedVet);

        mockMvc.perform(
                put("/vets/1")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(createRequestBody(vet)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(updatedName)));
    }

    private String createRequestBody(Vet vet) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writer()
                .withDefaultPrettyPrinter()
                .writeValueAsString(vet);
    }

}