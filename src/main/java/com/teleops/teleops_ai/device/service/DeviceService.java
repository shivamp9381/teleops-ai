package com.teleops.teleops_ai.device.service;

import com.teleops.teleops_ai.common.exception.BadRequestException;
import com.teleops.teleops_ai.common.exception.DuplicateResourceException;
import com.teleops.teleops_ai.common.exception.ResourceNotFoundException;
import com.teleops.teleops_ai.common.util.CacheConstants;
import com.teleops.teleops_ai.device.dto.DeviceRequest;
import com.teleops.teleops_ai.device.dto.DeviceResponse;
import com.teleops.teleops_ai.device.dto.UpdateStatusRequest;
import com.teleops.teleops_ai.device.model.Device;
import com.teleops.teleops_ai.device.model.DeviceStatus;
import com.teleops.teleops_ai.device.model.DeviceType;
import com.teleops.teleops_ai.device.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Device Service with Redis Caching
 *
 * Caching annotations explained:
 *
 * @Cacheable(value, key)
 *   Before method executes: check Redis for key.
 *   If found: return cached value, skip method body.
 *   If not found: execute method, store result in Redis, return result.
 *
 * @CacheEvict(value, key)
 *   After method executes: delete the specified key from Redis.
 *   Forces next read to go to MongoDB and repopulate cache.
 *
 * @Caching
 *   Allows combining multiple cache annotations on one method.
 *   Used when a write operation should evict multiple cache keys.
 *
 * Spring Expression Language (SpEL) in cache keys:
 *   #id         = method parameter named "id"
 *   #request.ipAddress = field on request parameter
 *
 * allEntries = true
 *   Evicts ALL keys from that cache name.
 *   Used when a collection-level cache must be refreshed.
 *   Example: creating a new device evicts "devices:all"
 *   because the list has changed.
 */
@Service
public class DeviceService {

    private static final Logger log =
            LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // ─────────────────────────────────────────────────────────
    // Create Device
    // Evicts the "all devices" list cache after creation
    // ─────────────────────────────────────────────────────────

    /**
     * Create device and invalidate the all-devices cache.
     *
     * Why evict DEVICES_ALL?
     *   The list is now stale. Next call to getAllDevices()
     *   must go to MongoDB to get the updated list.
     *
     * We do NOT add the new device to the cache here
     * because that would bypass the @Cacheable check.
     * The Cache-Aside pattern handles it on next read.
     */
    @CacheEvict(value = CacheConstants.DEVICES_ALL, allEntries = true)
    public DeviceResponse createDevice(DeviceRequest request) {

        if (deviceRepository.existsByIpAddress(request.getIpAddress())) {
            throw new DuplicateResourceException(
                    "A device with IP address '" + request.getIpAddress()
                            + "' already exists.");
        }

        Device device = new Device(
                request.getName(),
                request.getType(),
                request.getStatus() != null
                        ? request.getStatus()
                        : DeviceStatus.ONLINE,
                request.getIpAddress(),
                request.getLocation(),
                request.getVendor(),
                request.getModel(),
                request.getDescription()
        );

        Device savedDevice = deviceRepository.save(device);

        log.info("Device created: {} [{}] at {}",
                savedDevice.getName(),
                savedDevice.getType(),
                savedDevice.getLocation());

        return DeviceResponse.fromDevice(savedDevice);
    }

    // ─────────────────────────────────────────────────────────
    // Get All Devices
    // Cached as a collection
    // ─────────────────────────────────────────────────────────

    /**
     * Get all devices with caching.
     *
     * Cache key: "devices:all" (no dynamic key needed, single entry)
     * TTL: 120 seconds (configured in RedisConfig)
     *
     * First call: MongoDB query, result cached.
     * Subsequent calls within 120s: return from Redis.
     * After 120s or on write: go back to MongoDB.
     */
    @Cacheable(value = CacheConstants.DEVICES_ALL)
    public List<DeviceResponse> getAllDevices() {
        log.debug("Cache MISS for devices:all - querying MongoDB");

        return deviceRepository.findAll()
                .stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────
    // Get Device By ID
    // Cached individually by device ID
    // ─────────────────────────────────────────────────────────

    /**
     * Get single device with per-device caching.
     *
     * Cache key: "devices::{id}" → e.g., "devices::device123"
     * TTL: 120 seconds
     *
     * Why cache individual devices?
     *   AI module frequently needs device details for RCA.
     *   Alarm module looks up device name on every alarm.
     *   Caching prevents repeated lookups for the same device.
     */
    @Cacheable(value = CacheConstants.DEVICES, key = "#id")
    public DeviceResponse getDeviceById(String id) {
        log.debug("Cache MISS for device:{} - querying MongoDB", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", id));

        return DeviceResponse.fromDevice(device);
    }

    // ─────────────────────────────────────────────────────────
    // Update Device
    // Evicts both individual device cache and all-devices cache
    // ─────────────────────────────────────────────────────────

    /**
     * Update device and evict related caches.
     *
     * @Caching allows multiple @CacheEvict annotations.
     * We evict:
     *   1. devices::{id}   - this specific device is now stale
     *   2. devices:all     - the list is now stale
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEVICES,
                    key = "#id"),
            @CacheEvict(value = CacheConstants.DEVICES_ALL,
                    allEntries = true)
    })
    public DeviceResponse updateDevice(String id, DeviceRequest request) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", id));

        boolean ipChanged = !device.getIpAddress()
                .equals(request.getIpAddress());

        if (ipChanged && deviceRepository.existsByIpAddress(
                request.getIpAddress())) {
            throw new DuplicateResourceException(
                    "A device with IP address '"
                            + request.getIpAddress()
                            + "' already exists.");
        }

        device.setName(request.getName());
        device.setType(request.getType());
        device.setStatus(request.getStatus());
        device.setIpAddress(request.getIpAddress());
        device.setLocation(request.getLocation());
        device.setVendor(request.getVendor());
        device.setModel(request.getModel());
        device.setDescription(request.getDescription());

        Device updatedDevice = deviceRepository.save(device);

        log.info("Device updated: {} [{}]",
                updatedDevice.getName(), updatedDevice.getId());

        return DeviceResponse.fromDevice(updatedDevice);
    }

    // ─────────────────────────────────────────────────────────
    // Update Device Status
    // Evicts individual device cache and all-devices cache
    // ─────────────────────────────────────────────────────────

    /**
     * Update status and evict related caches.
     *
     * Also evicts DASHBOARD_STATS because device status
     * changes affect dashboard counters
     * (online count, offline count, etc.)
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEVICES,
                    key = "#id"),
            @CacheEvict(value = CacheConstants.DEVICES_ALL,
                    allEntries = true),
            @CacheEvict(value = CacheConstants.DASHBOARD_STATS,
                    allEntries = true)
    })
    public DeviceResponse updateDeviceStatus(String id,
                                             UpdateStatusRequest request) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", id));

        DeviceStatus previousStatus = device.getStatus();
        device.setStatus(request.getStatus());

        Device updatedDevice = deviceRepository.save(device);

        log.info("Device status updated: {} from {} to {}",
                updatedDevice.getName(),
                previousStatus,
                updatedDevice.getStatus());

        return DeviceResponse.fromDevice(updatedDevice);
    }

    // ─────────────────────────────────────────────────────────
    // Delete Device
    // Evicts all device-related caches
    // ─────────────────────────────────────────────────────────

    /**
     * Delete device and evict all related caches.
     */
    @Caching(evict = {
            @CacheEvict(value = CacheConstants.DEVICES,
                    key = "#id"),
            @CacheEvict(value = CacheConstants.DEVICES_ALL,
                    allEntries = true),
            @CacheEvict(value = CacheConstants.DASHBOARD_STATS,
                    allEntries = true)
    })
    public void deleteDevice(String id) {

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Device", "id", id));

        deviceRepository.deleteById(id);

        log.info("Device deleted: {} [{}]", device.getName(), id);
    }

    // ─────────────────────────────────────────────────────────
    // Search Devices
    // Not cached — search results are too dynamic
    // ─────────────────────────────────────────────────────────

    /**
     * Search is NOT cached.
     *
     * Why?
     *   Search has many parameter combinations.
     *   Caching would require a unique key per combination.
     *   The result set changes as devices are updated.
     *   The benefit does not justify the complexity here.
     *
     *   If this becomes a performance bottleneck in production,
     *   add a proper search index in MongoDB instead.
     */
    public List<DeviceResponse> searchDevices(DeviceType type,
                                              DeviceStatus status,
                                              String location) {

        List<Device> devices;

        if (type != null && status != null) {
            devices = deviceRepository.findByTypeAndStatus(type, status);

        } else if (type != null) {
            devices = deviceRepository.findByType(type);

        } else if (status != null) {
            devices = deviceRepository.findByStatus(status);

        } else if (location != null && !location.isBlank()) {
            devices = deviceRepository
                    .findByLocationContainingIgnoreCase(location);

        } else {
            devices = deviceRepository.findAll();
        }

        return devices.stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
    }

    /**
     * Get devices by type.
     * Not cached for same reason as search.
     */
    public List<DeviceResponse> getDevicesByType(DeviceType type) {
        return deviceRepository.findByType(type)
                .stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
    }

    /**
     * Get devices by status.
     * Not cached for same reason as search.
     */
    public List<DeviceResponse> getDevicesByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status)
                .stream()
                .map(DeviceResponse::fromDevice)
                .collect(Collectors.toList());
    }
}