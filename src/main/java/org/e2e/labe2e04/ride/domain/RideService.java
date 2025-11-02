package org.e2e.labe2e04.ride.domain;

import lombok.RequiredArgsConstructor;
import org.e2e.labe2e04.driver.infrastructure.DriverRepository;
import org.e2e.labe2e04.exception.BadRequestException;
import org.e2e.labe2e04.passenger.domain.Passenger;
import org.e2e.labe2e04.passenger.exception.PassengerNotFoundException;
import org.e2e.labe2e04.passenger.infrastructure.PassengerRepository;
import org.e2e.labe2e04.ride.dto.RideRequestDto;
import org.e2e.labe2e04.ride.dto.RideResponseDto;
import org.e2e.labe2e04.ride.exception.RideNotFoundException;
import org.e2e.labe2e04.ride.infrastructure.RideRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.e2e.labe2e04.security.utils.SecurityUtils.getCurrentUsername;

@Service
@RequiredArgsConstructor
public class RideService {
    private final PassengerRepository passengerRepository;

    private final RideRepository rideRepository;

    private final DriverRepository driverRepository;

    private final ModelMapper modelMapper;

    public Ride createRide(RideRequestDto rideRequestDto) {
        Ride ride = modelMapper.map(rideRequestDto, Ride.class);

        if (Objects.equals(rideRequestDto.getDestinationCoordinates().getLatitude(),
                rideRequestDto.getOriginCoordinates().getLatitude()) &&
                Objects.equals(rideRequestDto.getDestinationCoordinates().getLongitude(),
                        rideRequestDto.getOriginCoordinates().getLongitude()))
            throw new BadRequestException("Origin and destination coordinates cannot be the same");

        ride.setPassenger(passenger);
        ride.setDriver(driver);
        return rideRepository.save(ride);
    }

    public Ride assignDriverToRide(Long id) {
        Ride ride = rideRepository.findById(id).orElseThrow(RideNotFoundException::new);
        ride.setDriver(driver);
        ride.setStatus(Status.ACCEPTED);
        return rideRepository.save(ride);
    }

    public Page<RideResponseDto> getPassengerRides(Long passengerId, Pageable pageable) {
        Passenger passenger =
                passengerRepository
                        .findById(passengerId)
                        .orElseThrow(PassengerNotFoundException::new);
        Page<Ride> rides = rideRepository.findAllByPassengerIdAndStatus(passenger.getId(), Status.COMPLETED, pageable);
        return rides.map(ride -> modelMapper.map(ride, RideResponseDto.class));
    }

    public Ride updateRideStatus(Long id, Status status) {
        Ride ride = rideRepository.findById(id).orElseThrow(RideNotFoundException::new);
        ride.setStatus(status);
        return rideRepository.save(ride);
    }

    public Page<RideResponseDto> getUserRides(Pageable pageable) {
        Page<Ride> rides = rideRepository.findAllByPassengerIdAndStatus(passenger.getId(), Status.COMPLETED, pageable);
        return rides.map(ride -> modelMapper.map(ride, RideResponseDto.class));
    }
}