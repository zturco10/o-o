package org.e2e.labe2e04.review;

import jakarta.transaction.Transactional;
import org.e2e.labe2e04.coordinate.domain.Coordinate;
import org.e2e.labe2e04.driver.domain.Category;
import org.e2e.labe2e04.driver.domain.Driver;
import org.e2e.labe2e04.driver.exception.DriverNotFoundException;
import org.e2e.labe2e04.driver.infrastructure.DriverRepository;
import org.e2e.labe2e04.passenger.domain.Passenger;
import org.e2e.labe2e04.passenger.exception.PassengerNotFoundException;
import org.e2e.labe2e04.passenger.infrastructure.PassengerRepository;
import org.e2e.labe2e04.review.domain.Review;
import org.e2e.labe2e04.review.infrastructure.ReviewRepository;
import org.e2e.labe2e04.ride.domain.Ride;
import org.e2e.labe2e04.ride.domain.Status;
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

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ReviewControllerSecurityTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        ride.setStatus(Status.COMPLETED);
        ride.setPassenger(passenger);
        ride.setDriver(driver);
        rideRepository.save(ride);
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnCreatedWhenPassengerCreatesReview() throws Exception {
        Long rideId = rideRepository.findAll().get(0).getId();
        Long driverId = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new)
                .getId();
        String jsonContent = JsonTestUtils.readJsonFile("/review/create-review.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "rideId", rideId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "targetId", driverId);
        mockMvc.perform(post("/review")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated());
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnForbiddenWhenUnauthenticatedUserCreatesReview() throws Exception {
        Long rideId = rideRepository.findAll().get(0).getId();
        Long targetId = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new)
                .getId();
        String jsonContent = JsonTestUtils.readJsonFile("/review/create-review.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "rideId", rideId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "targetId", targetId);
        mockMvc.perform(post("/review")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNoContentWhenPassengerDeletesOwnReview() throws Exception {
        Long reviewId = createReview();
        mockMvc.perform(delete("/review/{id}", reviewId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "DRIVER", value = "other@example.com")
    public void shouldReturnForbiddenWhenDriverDeletesOthersReview() throws Exception {
        Long reviewId = createReview();
        mockMvc.perform(delete("/review/{id}", reviewId)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnBadRequestWhenCreatingReviewForIncompleteRide() throws Exception {
        Coordinate origin = new Coordinate();
        origin.setLatitude(38.0);
        origin.setLongitude(-123.0);

        Coordinate destination = new Coordinate();
        destination.setLatitude(39.0);
        destination.setLongitude(-124.0);

        Passenger passenger = passengerRepository.findByEmail("janedoe@example.com").orElseThrow();
        Driver driver = driverRepository.findByEmail("johndoe@example.com").orElseThrow();

        Ride incompleteRide = new Ride();
        incompleteRide.setOriginName("Home");
        incompleteRide.setDestinationName("Work");
        incompleteRide.setPrice(50d);
        incompleteRide.setOriginCoordinates(origin);
        incompleteRide.setDestinationCoordinates(destination);
        incompleteRide.setStatus(Status.IN_PROGRESS);
        incompleteRide.setPassenger(passenger);
        incompleteRide.setDriver(driver);
        Long incompleteRideId = rideRepository.save(incompleteRide).getId();

        String jsonContent = JsonTestUtils.readJsonFile("/review/create-review.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "rideId", incompleteRideId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "targetId", driver.getId());

        mockMvc.perform(post("/review")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNotFoundWhenCreatingReviewForNonExistentRide() throws Exception {
        Long driverId = driverRepository.findByEmail("johndoe@example.com").orElseThrow().getId();

        String jsonContent = JsonTestUtils.readJsonFile("/review/create-review.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "rideId", 99999L);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "targetId", driverId);

        mockMvc.perform(post("/review")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNotFoundWhenCreatingReviewWithInvalidTarget() throws Exception {
        Long rideId = rideRepository.findAll().get(0).getId();

        String jsonContent = JsonTestUtils.readJsonFile("/review/create-review.json");
        jsonContent = JsonTestUtils.updateValue(jsonContent, "rideId", rideId);
        jsonContent = JsonTestUtils.updateValue(jsonContent, "targetId", 99999L);

        mockMvc.perform(post("/review")
                        .contentType(APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "PASSENGER", value = "janedoe@example.com")
    public void shouldReturnNotFoundWhenDeletingNonExistentReview() throws Exception {
        mockMvc.perform(delete("/review/{id}", 99999L)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    private Long createReview() throws Exception {
        Review review = new Review();
        Passenger author = passengerRepository
                .findByEmail("janedoe@example.com")
                .orElseThrow(PassengerNotFoundException::new);
        review.setAuthor(author);
        Driver target = driverRepository
                .findByEmail("johndoe@example.com")
                .orElseThrow(DriverNotFoundException::new);
        review.setTarget(target);
        Ride ride = rideRepository
                .findAll()
                .get(0);
        review.setRide(ride);
        review.setComment("Good ride");
        review.setRating(5);
        return reviewRepository.save(review).getId();
    }
}
