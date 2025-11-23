# Briar Project

## Overview

Briar is a secure, decentralized messaging application designed for activists, journalists, and anyone requiring robust, censorship-resistant communication. Unlike traditional messaging apps, Briar doesn't rely on central servers - messages sync directly between user devices. The application supports multiple transport mechanisms including Tor network, Bluetooth, and Wi-Fi, allowing communication even during internet outages or surveillance scenarios.

The project consists of multiple components:
- **briar-android**: Native Android application with full UI
- **bramble-android**: Android-specific core library components
- **briar-headless**: REST API server for programmatic/headless usage

Key features include private messaging, groups, forums, blogs, and built-in Tor network support with all data stored locally on user devices unless explicitly shared.

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Multi-Platform Architecture

The project is structured as a multi-module Gradle build targeting multiple platforms:
- **Android Applications**: Two variants (bramble-android, briar-android) with separate ProGuard configurations
- **Headless Server**: Cross-platform JAR supporting Linux (x86, ARM, aarch64), Windows, and macOS (x86, aarch64)

**Rationale**: Separating platform-specific code from core business logic enables code reuse across Android and headless deployments while maintaining platform-specific optimizations.

### Decentralized Peer-to-Peer Design

The application implements direct device-to-device message synchronization without central servers.

**Transport Layer Strategy**:
- **Primary**: Tor network for internet-based sync (provides anonymity and censorship resistance)
- **Fallback**: Bluetooth and Wi-Fi Direct for offline scenarios
- **Benefit**: Resilient communication during internet disruptions or network censorship

**Alternatives Considered**: Client-server architecture was rejected to eliminate single points of failure and reduce metadata exposure.

### Data Storage Architecture

Uses embedded H2 database with MVStore engine for local data persistence.

**Key Design Choices**:
- All user data stored exclusively on device
- No cloud synchronization or server-side storage
- H2 loaded via reflection (preserved in ProGuard rules)

**Rationale**: Local-only storage ensures user privacy and enables offline functionality. H2 provides a lightweight, embeddable SQL database suitable for mobile and headless deployments.

### Security & Privacy Architecture

**Cryptographic Foundation**:
- EdDSA (Ed25519) for digital signatures via `net.i2p.crypto.eddsa`
- Curve25519 for key agreement via `org.whispersystems.curve25519`

**Network Privacy**:
- Mandatory Tor integration for all internet-based communication
- Prevents metadata leakage and protects user relationships from surveillance

**Authentication System** (Headless API):
- Bearer token authentication stored in `~/.briar/auth_token`
- Token-based API access control for REST endpoints

**Rationale**: Using established cryptographic libraries (EdDSA, Curve25519) ensures security while leveraging peer-reviewed implementations. Tor integration is fundamental to the threat model targeting activists and journalists.

### Code Obfuscation Strategy (Android)

ProGuard configuration with selective retention:
- **Disabled Obfuscation**: Debug symbols and class names preserved (`-dontobfuscate`)
- **Preserved Classes**: All application code, dependency injection annotations, serialization classes
- **Stripped**: Internal implementation details of third-party libraries

**Rationale**: Transparency is prioritized (source code is open and auditable). ProGuard primarily serves to reduce APK size rather than obscurity.

### Android UI Architecture

**Third-Party UI Components**:
- **QR Codes**: Google ZXing for contact exchange
- **Image Loading**: Glide for efficient image rendering
- **HTML Sanitization**: JSoup Whitelist for safe content display
- **Emoji Support**: Vanniktech emoji library
- **Custom Components**: KeyboardAwareLinearLayout (from Signal codebase, LGPL licensed)

**Rationale**: Leveraging mature, well-tested libraries reduces development time and improves reliability for non-core features.

### RSS Feed Support

Integrates Rome Tools libraries for RSS/Atom feed parsing (forums/blogs feature).

**Preserved Classes**: `com.rometools.rome.feed.synd.impl.*`, `com.rometools.rome.io.impl.*`

**Trade-offs**: Adds dependency complexity but enables critical blog/forum syndication features.

### Build Reproducibility

All releases are reproducible builds, allowing verification that published binaries match source code.

**Implementation**: Deterministic build configuration in Gradle with fastlane for Android deployment automation.

**Rationale**: Critical for trust in security-focused applications. Users and auditors can verify no backdoors were introduced during compilation.

## External Dependencies

### Core Libraries

- **H2 Database**: Embedded SQL database with MVStore engine for local data storage
- **Dagger**: Dependency injection framework
- **Jackson**: JSON serialization/deserialization
- **OkHttp**: HTTP client library (with Conscrypt SSL provider)

### Cryptography Libraries

- **I2P EdDSA**: Ed25519 signature implementation
- **Whisper Systems Curve25519**: Key agreement protocol

### Android-Specific Dependencies

- **Google ZXing**: QR code generation and scanning
- **Rome Tools**: RSS/Atom feed parsing
- **Glide**: Image loading and caching
- **JSoup**: HTML parsing and sanitization
- **Vanniktech Emoji**: Emoji rendering support

### Network & Privacy

- **Tor Network**: Built-in support for anonymous communication (integration details not visible in provided files)
- **Conscrypt**: Modern SSL/TLS provider for Android

### Testing Infrastructure

- **JUnit**: Unit testing framework
- **JMock**: Mock object framework
- **Objenesis**: Object instantiation library for testing

### Development Tools

- **ProGuard**: Code shrinking and optimization for Android
- **Fastlane**: Automation for Android app deployment and screenshot management
- **Gradle**: Multi-platform build system with architecture-specific JAR generation

### Platform Support

The headless variant supports:
- Linux: x86_64, aarch64, armhf
- Windows: x86
- macOS: x86_64, aarch64

Each platform requires architecture-specific JAR builds via Gradle tasks.