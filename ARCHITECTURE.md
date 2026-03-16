# SentinelOS Architecture Documentation

## System Overview

SentinelOS implements a **modular, layered architecture** following MVVM (Model-View-ViewModel) and Clean Architecture principles. The system is designed for offline operation with encrypted local storage and real-time sensor processing.

## Architectural Layers

### 1. Presentation Layer (UI)

**Location**: `app/src/main/java/com/sentinel/os/ui/`

The presentation layer consists of:

- **SentinelOSApp.kt** - Root composition with tab-based navigation
- **Screens**:
  - `ScanScreen.kt` - Magnetometer visualization and control
  - `GuardScreen.kt` - Threat level display and sensor status
  - `BroadcastScreen.kt` - Mesh node discovery and messaging
- **Theme**:
  - `SentinelOSTheme.kt` - Material 3 dark AMOLED theme
  - `Type.kt` - Typography scale
  - `Shape.kt` - Shape tokens

**Technologies**: Jetpack Compose, Material 3, StateFlow

**Responsibilities**:
- Render UI components
- Handle user interactions
- Display real-time sensor data
- Manage navigation between modes

### 2. Domain Layer (Business Logic)

**Location**: `app/src/main/java/com/sentinel/os/domain/`

The domain layer contains:

- **Use Cases**:
  - `ThreatFusionEngine.kt` - Multi-sensor threat assessment
    - Combines acoustic, optical, vibration, magnetic, and RF scores
    - Applies weighted scoring and correlation multipliers
    - Produces unified threat score (0-100) every 500ms
    - Classifies threat levels: LOW, MEDIUM, HIGH, CRITICAL

**Technologies**: Kotlin, Flow, Coroutines

**Responsibilities**:
- Implement business rules
- Process sensor data
- Calculate threat assessments
- Apply signal processing algorithms

### 3. Data Layer (Persistence & Sources)

**Location**: `app/src/main/java/com/sentinel/os/data/`

The data layer includes:

#### Database (Room)
- **Entities.kt** - 7 data entities:
  - `ScanSessionEntity` - Magnetometer scan sessions
  - `MagneticReadingEntity` - Individual magnetic field readings
  - `SensorEventEntity` - Generic sensor events
  - `ThreatScoreEntity` - Threat assessments
  - `MeshNodeEntity` - Discovered mesh nodes
  - `MeshMessageEntity` - Mesh network messages
  - `NightSessionEntity` - Night guard sessions

- **SentinelDAO.kt** - Data Access Objects for each entity
- **SentinelDatabase.kt** - Room database instance with singleton pattern

#### Repositories
- Abstract data sources and provide clean API to domain layer
- Handle encryption/decryption of sensitive data
- Implement caching strategies

#### Data Sources
- **Sensor Data Sources** - Interface with infrastructure sensors
- **Network Data Sources** - Mesh network communication

**Technologies**: Room, SQLite, Coroutines, Flow

**Responsibilities**:
- Persist data locally
- Encrypt sensitive information
- Provide data access through DAOs
- Implement repository pattern

### 4. Infrastructure Layer (Hardware & Utilities)

**Location**: `app/src/main/java/com/sentinel/os/infrastructure/`

#### Sensor Abstraction
- **BaseSensor.kt** - Abstract base class for all sensors
  - Provides Flow-based data streaming
  - Manages active state and errors
  - Ensures non-blocking operations

- **MagnetometerSensor.kt** - Magnetic field sensor
  - Reads at SENSOR_DELAY_FASTEST
  - Computes vector magnitude
  - Tracks baseline and deviations
  - Detects anomalies

- **AccelerometerSensor.kt** - Motion and vibration sensor
  - High-frequency sampling
  - Gravity component filtering (high-pass filter)
  - Impulse detection for footsteps/impacts
  - Vibration intensity estimation

#### Encryption
- **EncryptionManager.kt** - AES-256 GCM encryption
  - Keys stored in Android Keystore
  - Transparent encryption/decryption
  - Base64 encoding for string data
  - IV prepended to ciphertext

#### Utilities
- Common functions for data transformation
- Signal processing helpers
- Logging and debugging utilities

**Technologies**: Android Sensors API, Android Keystore, Coroutines

**Responsibilities**:
- Interface with hardware sensors
- Manage encryption keys
- Provide low-level utilities
- Handle platform-specific operations

### 5. Services Layer (Background Processing)

**Location**: `app/src/main/java/com/sentinel/os/service/`

#### NightGuardService
- Foreground service for continuous monitoring
- Runs in background with persistent notification
- Collects data from magnetometer and accelerometer
- Feeds data to ThreatFusionEngine
- Triggers alerts when threat score exceeds threshold

#### MeshNetworkService
- Foreground service for mesh networking
- Manages WiFi Direct peer discovery
- Broadcasts BLE beacons
- Implements store-and-forward message routing
- Maintains mesh node registry

**Technologies**: Android Services, Foreground Services, Notifications

**Responsibilities**:
- Run background processing
- Maintain persistent connections
- Handle system lifecycle events
- Provide user notifications

## Data Flow

### Scanning Mode Flow
```
MagnetometerSensor
    ↓ (Flow<MagneticReadingEntity>)
ScanScreen ViewModel
    ↓
ScanScreen UI
    ↓ (user action)
Repository
    ↓
Room Database (encrypted)
```

### Guard Mode Flow
```
MagnetometerSensor ─┐
AccelerometerSensor ├─→ ThreatFusionEngine
(Other sensors)    ─┘
    ↓ (ThreatAssessment)
NightGuardService
    ↓
GuardScreen ViewModel
    ↓
GuardScreen UI
    ↓ (if score > threshold)
Alert Trigger
```

### Mesh Networking Flow
```
WiFi Direct Discovery ─┐
BLE Beacon Scanning   ├─→ MeshNetworkService
Message Routing       ─┘
    ↓
MeshMessageEntity (Room)
    ↓
BroadcastScreen ViewModel
    ↓
BroadcastScreen UI
```

## State Management

### ViewModel Pattern
Each screen has a corresponding ViewModel that:
- Holds UI state in StateFlow
- Collects sensor data from repositories
- Updates UI state reactively
- Survives configuration changes

### Flow-Based Streaming
- Sensors emit data through Flow
- Repositories transform and filter data
- UI collects and displays in real-time
- Backpressure handled by Flow operators

## Encryption Strategy

### Key Management
- Master key generated and stored in Android Keystore
- Keys never leave secure enclave
- Key rotation supported through Keystore

### Data Encryption
- All sensor recordings encrypted at rest
- Threat assessments encrypted before storage
- Mesh messages encrypted end-to-end
- Encryption transparent to application layer

### Encryption Scope
```
Plaintext Data
    ↓
EncryptionManager.encrypt()
    ↓
Encrypted Bytes + IV
    ↓
Base64 Encoding (for strings)
    ↓
Room Database
```

## Sensor Architecture

### Sensor Abstraction
```
Android Sensor API
    ↓
SensorEventListener
    ↓
BaseSensor<T>
    ↓
Flow<T> (non-blocking)
    ↓
Repository
    ↓
ViewModel/UseCase
```

### Sensor Sampling Rates
| Sensor | Rate | Purpose |
|--------|------|---------|
| Magnetometer | FASTEST | Field anomaly detection |
| Accelerometer | FASTEST | Vibration/impact detection |
| Gyroscope | FASTEST | Rotation detection |
| Light | NORMAL | Brightness changes |
| Microphone | 44.1kHz | Audio analysis |
| Camera | 30fps | Motion detection |

## Threat Fusion Algorithm

### Scoring Components
1. **Acoustic Score** (25% weight)
   - Voice activity detection
   - Unusual sound classification
   - FFT spectral analysis

2. **Optical Score** (25% weight)
   - Brightness change detection
   - Motion detection (frame differencing)
   - Camera anomalies

3. **Vibration Score** (20% weight)
   - Accelerometer impulse detection
   - Footstep patterns
   - Impact classification

4. **Magnetic Score** (15% weight)
   - Magnetic field deviations
   - Anomaly detection
   - Power line oscillations

5. **RF Score** (15% weight)
   - Unknown device detection
   - RSSI monitoring
   - Network environment changes

### Correlation Multipliers
- 2+ sensors active: 1.2x multiplier
- 3+ sensors active: 1.5x multiplier
- Detects multi-sensor confirmation

### Threat Levels
| Score | Level | Action |
|-------|-------|--------|
| 0-25 | LOW | Monitor |
| 25-50 | MEDIUM | Alert user |
| 50-75 | HIGH | Record data |
| 75-100 | CRITICAL | Trigger alarm |

## Database Schema

### Relationships
```
ScanSession ──1:N──→ MagneticReading
ScanSession ──1:N──→ SensorEvent
ScanSession ──1:N──→ ThreatScore

NightSession ──1:N──→ ThreatScore

MeshNode ──1:N──→ MeshMessage
```

### Encryption at Rest
- `ScanSessionEntity.encryptedData` - Encrypted raw readings
- `MeshMessageEntity.encryptedData` - Encrypted message payload
- All sensitive fields encrypted before storage

## Performance Considerations

### Battery Optimization
- Adaptive sensor sampling rates
- Batch processing of sensor data
- Efficient Flow operators (debounce, throttle)
- WorkManager for background tasks

### Memory Management
- Coroutine scopes tied to lifecycle
- Flow cancellation on screen exit
- Database query pagination
- Sensor listener cleanup

### Network Efficiency
- Store-and-forward for mesh messages
- Compression of sensor data
- Batch uploads when available
- Delta encoding for time-series data

## Error Handling

### Sensor Errors
- Graceful degradation if sensor unavailable
- Error state in Flow
- User notification of failures
- Fallback to alternative sensors

### Database Errors
- Transaction rollback on failure
- Retry logic with exponential backoff
- Data validation before insert
- Foreign key constraints

### Encryption Errors
- Key generation failures handled
- Decryption failures logged
- Fallback to unencrypted storage (dev only)
- User notification of security issues

## Testing Strategy

### Unit Tests
- Sensor data processing
- Threat fusion calculations
- Encryption/decryption
- Repository operations

### Integration Tests
- Database operations
- Service lifecycle
- Sensor data flow
- UI state updates

### Instrumented Tests
- Sensor access
- Foreground service behavior
- Notification display
- Permission handling

## Extensibility

### Adding New Sensors
1. Extend `BaseSensor<T>`
2. Implement `SensorEventListener`
3. Create Flow emission
4. Add to ThreatFusionEngine

### Adding New Threat Factors
1. Create new sensor implementation
2. Add score field to ThreatFusionEngine
3. Update weighting algorithm
4. Add UI visualization

### Adding New Modes
1. Create new Screen composable
2. Create ViewModel
3. Add navigation tab
4. Implement service if needed

## Security Considerations

### Threat Model
- **Confidentiality**: Encrypted storage protects data at rest
- **Integrity**: GCM mode provides authentication
- **Availability**: Offline-first design ensures function without network

### Attack Surface
- Sensor data interception (mitigated by encryption)
- Key extraction (mitigated by Android Keystore)
- Malicious apps (mitigated by permission model)
- Physical access (mitigated by encryption)

### Compliance
- GDPR-compliant data handling
- No external data transmission
- User-controlled data retention
- Transparent encryption

---

**This architecture enables SentinelOS to operate as a sophisticated, secure, and efficient environmental monitoring system with real-time threat assessment and mesh networking capabilities.**
