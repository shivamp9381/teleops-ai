// ============================================================
// MongoDB Initialization Script
// Runs once when MongoDB container first starts
// Creates the teleopsdb database and a dedicated user
// ============================================================

db = db.getSiblingDB('teleopsdb');

db.createUser({
    user: 'teleops_user',
    pwd: 'teleops_db_pass',
    roles: [
        { role: 'readWrite', db: 'teleopsdb' }
    ]
});

// Create initial collections with validation hints
db.createCollection('users');
db.createCollection('devices');
db.createCollection('alarms');
db.createCollection('tickets');
db.createCollection('audit_logs');

print('TeleOps database initialized successfully');