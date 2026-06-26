package com.teleops.teleops_ai.device.repository;

import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.model.DeviceType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Device Repository
 *
 * Spring Data MongoDB auto-implements all these methods.
 * We define the method signatures — Spring generates the queries.
 *
 * Naming convention that Spring understands:
 *   findBy{FieldName}           = match exact value
 *   findBy{Field1}And{Field2}   = match both conditions
 *   countBy{FieldName}          = count documents matching value
 *   existsBy{FieldName}         = boolean existence check
 *
 * These become:
 *   findByType(TOWER_5G)
 *     → db.devices.find({ type: "TOWER_5G" })
 *
 *   findByStatus(ONLINE)
 *     → db.devices.find({ status: "ONLINE" })
 *
 *   countByStatus(OFFLINE)
 *     → db.devices.count({ status: "OFFLINE" })
 *
 * No @Query annotation needed for these simple queries.
 * Complex queries requiring aggregation will use MongoTemplate
 * in the service layer when needed.
 */
@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    /**
     * Find all devices by type.
     * Used for: filtering devices on dashboard, type-specific lists.
     */
    List<Device> findByType(DeviceType type);

    /**
     * Find all devices by status.
     * Used for: active alarm correlations, status-based filtering.
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * Find all devices by location.
     * Used for: site-specific views, regional NOC operations.
     */
    List<Device> findByLocation(String location);

    /**
     * Find all devices by type AND status.
     * Used for: "Show all offline 5G towers" type queries.
     */
    List<Device> findByTypeAndStatus(DeviceType type, DeviceStatus status);

    /**
     * Find device by IP address.
     * Used for: looking up a device when an alarm fires
     * with an IP address as the identifier.
     */
    Optional<Device> findByIpAddress(String ipAddress);

    /**
     * Check if IP address is already in use.
     * Used for: preventing duplicate IP addresses on registration.
     */
    boolean existsByIpAddress(String ipAddress);

    /**
     * Count devices by status.
     * Used for: dashboard statistics (how many devices are offline?).
     */
    long countByStatus(DeviceStatus status);

    /**
     * Count devices by type.
     * Used for: dashboard statistics (how many 5G towers total?).
     */
    long countByType(DeviceType type);

    /**
     * Find devices by location containing a string (case-insensitive).
     * Used for: search feature where users type partial location.
     */
    List<Device> findByLocationContainingIgnoreCase(String location);

    /**
     * Find devices by name containing a string (case-insensitive).
     * Used for: search feature where users type partial device name.
     */
    List<Device> findByNameContainingIgnoreCase(String name);
}