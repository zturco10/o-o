package org.e2e.labe2e04.passenger;

import jakarta.transaction.Transactional;
import org.e2e.labe2e04.coordinate.domain.Coordinate;
import org.e2e.labe2e04.coordinate.infrastructure.CoordinateRepository;
import org.e2e.labe2e04.passenger.domain.Passenger;
import org.e2e.labe2e04.passenger.exception.PassengerNotFoundException;
import org.e2e.labe2e04.passenger.infrastructure.PassengerRepository;
import org.e2e.labe2e04.user.domain.Role;
import org.e2e.labe2e04.utils.JsonTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class PassengerControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private CoordinateRepository coordinateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Coordinate coordinate;

    @BeforeEach
    public void setUp() {
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(40.01);
        coordinate.setLongitude(30.02);
        this.coordinate = coordinateRepository.save(coordinate);

        Passenger passenger = new Passenger();
        passenger.setFirstName("Jane");
        passenger.setLastName("Doe");
        passenger.setEmail("janedoe@example.com");
        passenger.setPassword(passwordEncoder.encode("mysecretpassword"));
        passenger.setPhoneNumber("789-456-123");
        passenger.setRole(Role.PASSENGER);
        passengerRepository.save(passenger);
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenPassengerAccessesOwnInfo() throws Exception {
        mockMvc.perform(get("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserAccessesPassengerOwnInfo() throws Exception {
        mockMvc.perform(get("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenPassengerAccessesOwnInfoById() throws Exception {
        Long authorizedPassengerId = passengerRepository
                .findByEmail("janedoe@example.com")
                .orElseThrow(PassengerNotFoundException::new)
                .getId();
        mockMvc.perform(get("/passenger/{id}", authorizedPassengerId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserAccessesPassengerInfo() throws Exception {
        Long authorizedPassengerId = passengerRepository
                .findByEmail("janedoe@example.com")
                .orElseThrow(PassengerNotFoundException::new)
                .getId();
        mockMvc.perform(get("/passenger/{id}", authorizedPassengerId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNoContentWhenPassengerDeletesOwnAccount() throws Exception {
        mockMvc.perform(delete("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserDeletesPassenger() throws Exception {
        mockMvc.perform(delete("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnCreatedWhenPassengerAddsPlace() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/passenger/create-passenger-place.json");
        mockMvc.perform(post("/passenger/me/places")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserAddsPassengerPlace() throws Exception {
        mockMvc.perform(post("/passenger/me/places")
                        .contentType(APPLICATION_JSON)
                        .content(JsonTestUtils.readJsonFile("/passenger/create-passenger-place.json")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenPassengerGetsOwnPlaces() throws Exception {
        mockMvc.perform(post("/passenger/me/places")
                        .contentType(APPLICATION_JSON)
                        .content(JsonTestUtils.readJsonFile("/passenger/create-passenger-place.json")))
                .andReturn();
        mockMvc.perform(get("/passenger/me/places")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Home"));
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserGetsPassengerPlaces() throws Exception {
        mockMvc.perform(post("/passenger/me/places")
                        .contentType(APPLICATION_JSON)
                        .content(JsonTestUtils.readJsonFile("/passenger/create-passenger-place.json")))
                .andReturn();
        mockMvc.perform(get("/passenger/places")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNoContentWhenPassengerDeletesOwnPlace() throws Exception {
        mockMvc.perform(delete("/passenger/me/places/{coordinateId}", coordinate.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserDeletesPassengerPlace() throws Exception {
        mockMvc.perform(delete("/passenger/me/places/{coordinateId}", coordinate.getId())
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNotFoundWhenGettingNonExistentPassenger() throws Exception {
        mockMvc.perform(get("/passenger/{id}", 99999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserGetsPassengerById() throws Exception {
        Long passengerId = passengerRepository
                .findByEmail("janedoe@example.com")
                .orElseThrow(PassengerNotFoundException::new)
                .getId();
        mockMvc.perform(get("/passenger/{id}", passengerId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnForbiddenWhenDriverTriesToAccessPassengerMe() throws Exception {
        mockMvc.perform(get("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnForbiddenWhenDriverTriesToDeletePassenger() throws Exception {
        mockMvc.perform(delete("/passenger/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnForbiddenWhenDriverTriesToAddPassengerPlace() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/passenger/create-passenger-place.json");
        mockMvc.perform(post("/passenger/me/places")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNotFoundWhenDeletingNonExistentPlace() throws Exception {
        mockMvc.perform(delete("/passenger/me/places/{coordinateId}", 99999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenPassengerGetsEmptyPlacesList() throws Exception {
        coordinateRepository.deleteAll();
        mockMvc.perform(get("/passenger/me/places")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
