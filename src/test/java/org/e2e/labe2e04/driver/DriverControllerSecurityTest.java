package org.e2e.labe2e04.driver;

import jakarta.transaction.Transactional;
import org.e2e.labe2e04.driver.domain.Category;
import org.e2e.labe2e04.driver.domain.Driver;
import org.e2e.labe2e04.driver.exception.DriverNotFoundException;
import org.e2e.labe2e04.driver.infrastructure.DriverRepository;
import org.e2e.labe2e04.user.domain.Role;
import org.e2e.labe2e04.utils.JsonTestUtils;
import org.e2e.labe2e04.vehicle.domain.Vehicle;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DriverControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        Vehicle vehicle = new Vehicle();
        vehicle.setBrand("Toyota");
        vehicle.setModel("Camry");
        vehicle.setLicensePlate("ABC124");
        vehicle.setFabricationYear(2018);
        vehicle.setCapacity(5);
        vehicle.setColor("Blue");

        Driver driver = new Driver();
        driver.setFirstName("John");
        driver.setLastName("Doe");
        driver.setEmail("johndoe@example.com");
        driver.setPassword(passwordEncoder.encode("mysecretpassword"));
        driver.setPhoneNumber("123-456-7890");
        driver.setRole(Role.DRIVER);
        driver.setCategory(Category.X);
        driver.setVehicle(vehicle);
        driverRepository.save(driver);
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverAccessesOwnDriverById() throws Exception {
        Long authorizedDriverId = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new)
                .getId();
        mockMvc.perform(get("/driver/{id}", authorizedDriverId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PASSENGER")
    public void shouldReturnOkWhenPassengerAccessesDriverById() throws Exception {
        Long driverId = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new)
                .getId();
        mockMvc.perform(get("/driver/{id}", driverId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverAccessesOwnInfo() throws Exception {
        mockMvc.perform(get("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserAccessesDriverInfo() throws Exception {
        mockMvc.perform(get("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserDeletesDriver() throws Exception {
        mockMvc.perform(delete("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverUpdatesOwnInfo() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/driver/update-driver.json");
        mockMvc.perform(patch("/driver/me")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserUpdatesDriverInfo() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/driver/update-driver.json");
        mockMvc.perform(patch("/driver/me")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverUpdatesOwnCar() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/vehicle/create-vehicle.json");
        mockMvc.perform(patch("/driver/me/car")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserUpdatesDriverCar() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/vehicle/create-vehicle.json");
        mockMvc.perform(patch("/driver/me/car")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnNoContentWhenDriverDeletesOwnAccount() throws Exception {
        mockMvc.perform(delete("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnNotFoundWhenGettingNonExistentDriver() throws Exception {
        mockMvc.perform(get("/driver/{id}", 99999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserGetsDriverById() throws Exception {
        Long driverId = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new)
                .getId();
        mockMvc.perform(get("/driver/{id}", driverId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnForbiddenWhenPassengerTriesToAccessDriverMe() throws Exception {
        mockMvc.perform(get("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnForbiddenWhenPassengerTriesToUpdateDriver() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/driver/update-driver.json");
        mockMvc.perform(patch("/driver/me")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnForbiddenWhenPassengerTriesToDeleteDriver() throws Exception {
        mockMvc.perform(delete("/driver/me")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
