//package com.teleops.teleops_ai.audit.model;
//
///**
// * Audit Action Enum
// *
// * Every action that gets recorded in the audit log.
// *
// * Naming convention: MODULE_ACTION
// *   AUTH_LOGIN, AUTH_LOGOUT
// *   DEVICE_CREATED, DEVICE_UPDATED, etc.
// */
//public enum AuditAction {
//
//    // Authentication
//    AUTH_LOGIN,
//    AUTH_LOGOUT,
//    AUTH_REGISTER,
//
//    // Device actions
//    DEVICE_CREATED,
//    DEVICE_UPDATED,
//    DEVICE_STATUS_CHANGED,
//    DEVICE_DELETED,
//
//    // Alarm actions
//    ALARM_RAISED,
//    ALARM_ACKNOWLEDGED,
//    ALARM_RESOLVED,
//
//    // Ticket actions
//    TICKET_CREATED,
//    TICKET_ASSIGNED,
//    TICKET_RESOLVED,
//    TICKET_CLOSED,
//
//    // AI actions
//    AI_RCA_PERFORMED,
//    AI_LOG_SUMMARY,
//    AI_INCIDENT_REPORT,
//    AI_CHAT
//}


package com.teleops.teleops_ai.audit.model;

/**
 * Audit Action Enum
 *
 * Every action that gets recorded in the audit log.
 *
 * Naming convention: MODULE_ACTION
 */
public enum AuditAction {

    // Authentication
    AUTH_LOGIN,
    AUTH_LOGOUT,
    AUTH_REGISTER,

    // Device actions
    DEVICE_CREATED,
    DEVICE_UPDATED,
    DEVICE_STATUS_CHANGED,
    DEVICE_DELETED,

    // Alarm actions
    ALARM_RAISED,
    ALARM_ACKNOWLEDGED,
    ALARM_RESOLVED,

    // Ticket actions
    TICKET_CREATED,
    TICKET_ASSIGNED,
    TICKET_RESOLVED,
    TICKET_CLOSED,

    // AI actions
    AI_RCA_PERFORMED,
    AI_LOG_SUMMARY,
    AI_INCIDENT_REPORT,
    AI_CHAT,

    // User management actions
    USER_PROFILE_UPDATED,
    USER_PASSWORD_CHANGED,
    USER_ACTIVATED,
    USER_DEACTIVATED,
    USER_ROLE_CHANGED
}