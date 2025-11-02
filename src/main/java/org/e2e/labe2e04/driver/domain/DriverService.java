package org.e2e.labe2e04.driver.domain;

import lombok.RequiredArgsConstructor;
import org.e2e.labe2e04.coordinate.domain.Coordinate;
import org.e2e.labe2e04.coordinate.infrastructure.CoordinateRepository;
import org.e2e.labe2e04.driver.dto.DriverDto;
import org.e2e.labe2e04.driver.dto.DriverRequestDto;
import org.e2e.labe2e04.driver.dto.UpdateDriverRequestDto;
import org.e2e.labe2e04.driver.exception.DriverNotFoundException;
import org.e2e.labe2e04.driver.infrastructure.DriverRepository;
import org.e2e.labe2e04.exception.ConflictException;
import org.e2e.labe2e04.vehicle.dto.VehicleBasicDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DriverService {
    private final DriverRepository driverRepository;

    private final CoordinateRepository coordinateRepository;

    private final ModelMapper modelMapper;

    public DriverDto getDriverById(Long id) {
        Driver driver = driverRepository
                .findById(id)
                .orElseThrow(DriverNotFoundException::new);
        return modelMapper.map(driver, DriverDto.class);
    }

    public DriverDto getDriver() {
        return modelMapper.map(driver, DriverDto.class);
    }

    public Driver createDriver(DriverRequestDto driverRequestDto) {
        if (driverRepository.existsByEmail(driverRequestDto.getEmail()))
            throw new ConflictException("Driver with this email already exists");
        return driverRepository.save(modelMapper.map(driverRequestDto, Driver.class));
    }

    public void deleteDriver() {
        driverRepository.delete(driver);
    }

    public DriverDto updateDriver(UpdateDriverRequestDto driverDto) {
        return modelMapper.map(driverRepository.save(existingDriver), DriverDto.class);
    }

    public Driver updateDriverLocation(Double latitude, Double longitude) {
        Coordinate coordinate = new Coordinate();
        coordinate.setLatitude(latitude);
        coordinate.setLongitude(longitude);
        coordinateRepository.save(coordinate);
        existingDriver.setCoordinate(coordinate);
        return driverRepository.save(existingDriver);
    }

    public DriverDto updateDriverCar(VehicleBasicDto vehicleBasicDto) {
        modelMapper.map(vehicleBasicDto, existingDriver.getVehicle());
        return modelMapper.map(driverRepository.save(existingDriver), DriverDto.class);
    }
}