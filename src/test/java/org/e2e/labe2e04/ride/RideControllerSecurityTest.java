package org.e2e.labe2e04.ride;

import jakarta.transaction.Transactional;
import org.e2e.labe2e04.coordinate.domain.Coordinate;
import org.e2e.labe2e04.driver.domain.Category;
import org.e2e.labe2e04.driver.domain.Driver;
import org.e2e.labe2e04.driver.infrastructure.DriverRepository;
import org.e2e.labe2e04.exception.ResourceNotFoundException;
import org.e2e.labe2e04.passenger.domain.Passenger;
import org.e2e.labe2e04.passenger.infrastructure.PassengerRepository;
import org.e2e.labe2e04.ride.domain.Ride;
import org.e2e.labe2e04.ride.domain.Status;
import org.e2e.labe2e04.ride.exception.RideNotFoundException;
import org.e2e.labe2e04.ride.infrastructure.RideRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RideControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long rideId;

    private Long driverId;

    private Long passengerId;

    @BeforeEach
    public void setUp() {
        Passenger passenger = new Passenger();
        passenger.setFirstName("Jane");
        passenger.setLastName("Doe");
        passenger.setEmail("janedoe@example.com");
        passenger.setPassword(passwordEncoder.encode("mysecretpassword"));
        passenger.setPhoneNumber("789-456-123");
        passenger.setRole(Role.PASSENGER);
        passenger = passengerRepository.save(passenger);
        passengerId = passenger.getId();

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
        driver = driverRepository.save(driver);
        driverId = driver.getId();

        Coordinate origin = new Coordinate();
        origin.setLatitude(37.775938);
        origin.setLongitude(-122.419664);

        Coordinate destination = new Coordinate();
        destination.setLatitude(37.775938);
        destination.setLongitude(-122.419664);

        Ride ride = new Ride();
        ride.setOriginName("Home");
        ride.setDestinationName("School");
        ride.setPrice(100d);
        ride.setOriginCoordinates(origin);
        ride.setDestinationCoordinates(destination);
        ride.setStatus(Status.REQUESTED);
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        rideId = rideRepository.save(ride).getId();
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnCreatedWhenPassengerCreatesRide() throws Exception {
        final var JSON_FILE_PATH = "/ride/create-ride.json";
        String jsonContent = JsonTestUtils.readJsonFile(JSON_FILE_PATH);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "passengerId", passengerId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "driverId", driverId);
        mockMvc.perform(post("/ride")
                .contentType(APPLICATION_JSON)
                .content(jsonContent)
        ).andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverAssignsRide() throws Exception {
        mockMvc.perform(patch("/ride/assign/{rideId}", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
        Ride ride = rideRepository
                .findById(rideId)
                .orElseThrow(RideNotFoundException::new);
        assertEquals(Status.ACCEPTED, ride.getStatus());
        assertEquals("johndoe@example.com", ride.getDriver().getEmail());

    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserAssignsRide() throws Exception {
        mockMvc.perform(patch("/ride/assign/{rideId}", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"PASSENGER", "DRIVER"}, value = "janedoe@example.com")
    public void shouldReturnOkWhenAuthorizedUserCancelsRide() throws Exception {
        mockMvc.perform(patch("/ride/{id}/status/CANCELLED", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserCancelsRide() throws Exception {
        assignRide(rideId);
        mockMvc.perform(patch("/ride/{rideId}", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenPassengerGetsOwnRides() throws Exception {
        completeRide(rideId);
        mockMvc.perform(get("/ride/me?page=0&size=10")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserGetsRides() throws Exception {
        completeRide(rideId);
        mockMvc.perform(get("/ride/me?page=0&size=10")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnForbiddenWhenDriverTriesToCreateRide() throws Exception {
        String jsonContent = JsonTestUtils.readJsonFile("/ride/create-ride.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "passengerId", passengerId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "driverId", driverId);
        mockMvc.perform(post("/ride")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnForbiddenWhenPassengerTriesToAssignRide() throws Exception {
        mockMvc.perform(patch("/ride/assign/{rideId}", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnNotFoundWhenAssigningNonExistentRide() throws Exception {
        mockMvc.perform(patch("/ride/assign/{rideId}", 99999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkWhenUpdatingRideStatusToCompleted() throws Exception {
        mockMvc.perform(patch("/ride/{id}/status/COMPLETED", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        Ride ride = rideRepository.findById(rideId).orElseThrow();
        assertEquals(Status.COMPLETED, ride.getStatus());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "johndoe@example.com")
    public void shouldReturnOkWhenDriverUpdatesRideStatus() throws Exception {
        mockMvc.perform(patch("/ride/{id}/status/IN_PROGRESS", rideId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        Ride ride = rideRepository.findById(rideId).orElseThrow();
        assertEquals(Status.IN_PROGRESS, ride.getStatus());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnOkAndEmptyPageWhenPassengerHasNoCompletedRides() throws Exception {
        rideRepository.deleteAll();
        mockMvc.perform(get("/ride/me?page=0&size=10")
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private void assignRide(Long rideId) {
        Ride ride = rideRepository
                .findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        ride.setStatus(Status.ACCEPTED);
        rideRepository.save(ride);
    }

    private void completeRide(Long rideId) {
        Ride ride = rideRepository
                .findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found"));
        ride.setStatus(Status.COMPLETED);
        rideRepository.save(ride);
    }
}
