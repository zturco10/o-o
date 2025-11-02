package org.e2e.labe2e04.driver.application;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.e2e.labe2e04.driver.domain.Driver;
import org.e2e.labe2e04.driver.domain.DriverService;
import org.e2e.labe2e04.driver.dto.DriverDto;
import org.e2e.labe2e04.driver.dto.DriverRequestDto;
import org.e2e.labe2e04.driver.dto.UpdateDriverRequestDto;
import org.e2e.labe2e04.vehicle.dto.VehicleBasicDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/driver")
@RequiredArgsConstructor
public class DriverController {
    private final DriverService driverService;

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseDto> getDriverById(@PathVariable Long id) {
        DriverResponseDto driverResponseDto = driverService.getDriverById(id);
        return ResponseEntity.ok(driverResponseDto);
    }

    public ResponseEntity<DriverDto> getDriver() {
        DriverDto driverResponseDto = driverService.getDriver();
        return ResponseEntity.ok(driverResponseDto);
    }

    @PostMapping
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody DriverRequestDto driverRequestDto) {
        Driver createdDriver = driverService.createDriver(driverRequestDto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdDriver.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdDriver);
    }

    public ResponseEntity<Driver> deleteDriver() {
        driverService.deleteDriver();
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<DriverDto> updateDriver(@Valid @RequestBody UpdateDriverRequestDto updateDriverRequestDto) {
        DriverDto updatedDriver = driverService.updateDriver(updateDriverRequestDto);
        return ResponseEntity.ok(updatedDriver);
    }

    public ResponseEntity<Driver> updateDriverLocation(@RequestParam Double latitude,
                                                       @RequestParam Double longitude) {
        Driver updatedDriver = driverService.updateDriverLocation(latitude, longitude);
        return ResponseEntity.ok(updatedDriver);
    }

    public ResponseEntity<DriverDto> updateDriverCar(@Valid @RequestBody VehicleBasicDto vehicleBasicDto) {
        DriverDto driverResponseDto = driverService.updateDriverCar(vehicleBasicDto);
        return ResponseEntity.ok(driverResponseDto);
    }
}
