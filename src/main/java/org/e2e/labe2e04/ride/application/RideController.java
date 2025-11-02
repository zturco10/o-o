package org.e2e.labe2e04.ride.application;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.e2e.labe2e04.ride.domain.Ride;
import org.e2e.labe2e04.ride.domain.RideService;
import org.e2e.labe2e04.ride.domain.Status;
import org.e2e.labe2e04.ride.dto.RideRequestDto;
import org.e2e.labe2e04.ride.dto.RideResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/ride")
@RequiredArgsConstructor
public class RideController {
    private final RideService rideService;

    @PostMapping
    public ResponseEntity<Ride> createRide(@Valid @RequestBody RideRequestDto rideRequestDto) {
        Ride createdRide = rideService.createRide(rideRequestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdRide.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdRide);
    }

    @PatchMapping("/assign/{id}")
    public ResponseEntity<Ride> assignDriverToRide(@PathVariable Long id) {
        Ride updatedRide = rideService.assignDriverToRide(id);
        return ResponseEntity.ok(updatedRide);
    }

    @GetMapping("/{passengerId}")
    public ResponseEntity<Page<RideResponseDto>> getRidesByPassengerId(@PathVariable Long passengerId,
                                                                       @RequestParam(defaultValue = "0") Integer page,
                                                                       @RequestParam(defaultValue = "10") Integer size) {
        Page<RideResponseDto> rides = rideService.getPassengerRides(passengerId, PageRequest.of(page, size));
        return ResponseEntity.ok(rides);
    }

    @PatchMapping("/{id}/status/{status}")
    public ResponseEntity<Ride> updateRideStatus(@PathVariable Long id, @PathVariable Status status) {
        Ride ride = rideService.updateRideStatus(id, status);
        return ResponseEntity.ok(ride);
    }

    public ResponseEntity<Page<RideResponseDto>> getUserRides(@RequestParam(defaultValue = "0") Integer page,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Page<RideResponseDto> rides = rideService.getUserRides(PageRequest.of(page, size));
        return ResponseEntity.ok(rides);
    }
}