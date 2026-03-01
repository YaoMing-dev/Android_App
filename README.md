# TradeUp — P2P Marketplace Android App

<p align="left">
  <img src="https://img.shields.io/badge/Android-Java-3DDC84?logo=android&logoColor=white&style=flat-square" />
  <img src="https://img.shields.io/badge/Min_SDK-24_(Android_7.0)-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Target_SDK-34_(Android_14)-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/Backend-Spring_Boot-6DB33F?logo=springboot&logoColor=white&style=flat-square" />
  <img src="https://img.shields.io/badge/Database-MySQL-4479A1?logo=mysql&logoColor=white&style=flat-square" />
  <img src="https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square" />
</p>

A peer-to-peer marketplace Android application that connects local buyers and sellers for secondhand item transactions. Supports real-time chat, GPS-based search, price negotiation, and OAuth authentication.

---

## System Architecture

```
┌────────────────────┐     REST API      ┌─────────────────────┐
│   Android Client   │ ◄────────────────► │  Spring Boot Backend │
│   (Java, MVVM)     │                   │  + MySQL Database    │
│                    │   WebSocket       │                     │
│   Firebase FCM     │ ◄────────────────► │  Real-time Events   │
└────────────────────┘                   └─────────────────────┘
```

- **Frontend:** Android (Java), Min SDK 24, Target SDK 34
- **Backend:** Spring Boot + MySQL
- **Real-time:** Java-WebSocket for chat and live location updates
- **Push Notifications:** Firebase Cloud Messaging
- **Auth:** JWT + Refresh Token, Google OAuth 2.0

---

## Features

**User Management**
- Email/password registration with OTP email verification
- Google Sign-In (OAuth 2.0)
- JWT authentication with automatic refresh token handling
- Public user profiles with ratings, listing count, and join date
- Edit profile (avatar, bio, contact info)
- Account deactivation and permanent deletion

**Product Listings**
- Create listings with title, description, price, category (9 categories), condition, and up to 10 photos
- GPS auto-location with manual override
- Negotiable/fixed price toggle
- Product preview before publishing
- Status management: Available, Sold, Paused
- Per-listing analytics: views, likes, contact requests
- Edit and delete listings from personal dashboard

**Search & Discovery**
- Keyword search with 200ms debounce
- Filters: category, price range, condition, distance radius (1–100 km)
- Sort by: relevance, newest, price (ascending/descending), distance, popularity
- GPS-based proximity search with manual location entry
- Personalized recommendations based on browsing history
- Category browsing with featured and recently added sections

**In-App Chat**
- Real-time messaging via WebSocket
- Text, image, and emoji support
- Delivery status indicators: Sent, Delivered, Read, Failed
- Typing indicators and online/offline status
- Block and report functionality
- Firebase push notifications for new messages

**Price Negotiation**
- Buyers can make offers on negotiable listings
- Sellers can accept, reject, or counter-offer
- Offer status tracking: Pending, Accepted, Rejected, Countered, Expired

**Transactions & Reviews**
- Mark items as sold and track transaction history
- Payment method records: Cash, Bank Transfer, Digital Wallet
- Post-transaction ratings (1–5 stars) with optional written reviews
- Average rating displayed on seller profiles
- Review moderation for abuse filtering

**Notifications**
- Firebase Cloud Messaging for push notifications
- Events: new messages, offer updates, listing status changes
- In-app notification center with configurable preferences

---

## Tech Stack

| Category | Library / Technology |
|---|---|
| **Language** | Java |
| **UI** | Material Design 3, ConstraintLayout, ViewPager2, RecyclerView |
| **Navigation** | Android Navigation Component (nav graph) |
| **Networking** | Retrofit 2.9.0, OkHttp 4.11.0 (logging interceptor), Gson 2.10.1 |
| **Real-time** | Java-WebSocket 1.5.4 |
| **Auth** | Google Sign-In (Play Services Auth 20.7.0), JWT |
| **Push Notifications** | Firebase Cloud Messaging (BOM 32.7.0), Firebase Analytics |
| **Location & Maps** | Google Play Services Location 21.0.1, Maps 18.2.0 |
| **Image Loading** | Glide 4.16.0, ExifInterface 1.3.6 |
| **Permissions** | Dexter 6.2.3 |
| **State Management** | ViewModel 2.7.0, LiveData 2.7.0 |
| **Build System** | Gradle (Kotlin DSL), Version Catalog (libs.versions.toml) |

---

## Project Structure

```
app/src/main/
├── java/
│   ├── activities/
│   │   ├── LoginActivity.java          # Email/password + Google Sign-In
│   │   ├── RegisterActivity.java       # Account creation
│   │   ├── OtpVerificationActivity.java # Email OTP verification
│   │   ├── ForgotPasswordActivity.java
│   │   ├── MainActivity.java           # Container: bottom nav + FAB
│   │   ├── ProductDetailActivity.java  # View product, make offer
│   │   ├── ChatActivity.java           # Real-time one-on-one chat
│   │   ├── EditProfileActivity.java
│   │   ├── SettingsActivity.java
│   │   ├── MyListingsActivity.java     # Seller dashboard
│   │   ├── SavedItemsActivity.java
│   │   ├── UserProfileActivity.java    # Public profile view
│   │   ├── LocationPickerActivity.java # Map-based location selection
│   │   └── RealtimeLocationActivity.java
│   ├── fragments/
│   │   ├── HomeFragment.java           # Feed: recommendations, categories
│   │   ├── SearchFragment.java         # Search + advanced filters
│   │   ├── AddProductFragment.java     # Create/edit listing
│   │   ├── MessagesFragment.java       # Conversation list
│   │   ├── ProfileFragment.java        # User dashboard
│   │   ├── FilterBottomSheetDialogFragment.java
│   │   └── MakeOfferBottomSheetDialogFragment.java
│   ├── services/
│   │   └── RealtimeWebSocketService.java # Persistent WebSocket connection
│   └── utils/
│       └── SharedPrefsManager.java     # JWT token & user prefs storage
└── res/
    ├── navigation/main_nav_graph.xml
    ├── values/colors.xml       # 200+ semantic color tokens
    ├── values/dimens.xml       # 160+ dimension constants
    ├── values/styles.xml       # Material 3 component styles
    └── values/themes.xml       # Light/dark theme definitions
```

---

## Screens Overview

| Screen | Description |
|---|---|
| **Home** | Personalized feed, category filters, nearby items |
| **Search** | Keyword search with price/distance/condition filters |
| **Add Product** | Multi-step listing creation with GPS and photo upload |
| **Messages** | Conversation list with unread indicators |
| **Profile** | User dashboard, listings, saved items, transaction history |
| **Product Detail** | Full listing view, offer panel, seller profile link |
| **Chat** | Real-time messaging with image and emoji support |
| **Settings** | Notifications, privacy (online status, location sharing), dark mode |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 11+
- A running instance of the Spring Boot backend
- `google-services.json` placed in `app/` (from Firebase Console)

### Setup

```bash
git clone https://github.com/YaoMing-dev/Android_App.git
cd Android_App
```

1. Open the project in Android Studio
2. Add your `google-services.json` to `app/`
3. Configure the backend base URL in the Retrofit client
4. Configure OAuth credentials (Google Sign-In client ID)
5. Sync Gradle and run on an emulator or physical device (API 24+)

### Required Permissions

```xml
INTERNET
ACCESS_NETWORK_STATE
ACCESS_FINE_LOCATION
ACCESS_COARSE_LOCATION
POST_NOTIFICATIONS
CAMERA
READ_EXTERNAL_STORAGE
```

---

## Security

- JWT access token + refresh token stored in SharedPreferences
- Automatic token refresh on expiry
- Google OAuth 2.0 for social login
- Email verification required on registration
- User blocking and content reporting
- Privacy controls: toggle online status and location sharing

---

## License

[MIT](LICENSE) © [YaoMing-dev](https://github.com/YaoMing-dev)
