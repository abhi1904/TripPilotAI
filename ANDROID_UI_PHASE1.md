AI Travel Companion — Phase 1: Android UI
Goal
Build and run the Android UI first. I want to inspect the screens in Android Studio, understand the code, and request UI changes before implementing the backend. This phase is UI-only.
Technology
- Kotlin
- Jetpack Compose and Material 3
- MVVM and Navigation Compose
- Coroutines/StateFlow where appropriate
- Reuse the project's existing architecture and dependencies where possible.
  Design direction
  Use the supplied AI Travel Companion design-board image as a visual reference.
- Premium, modern travel-app appearance
- Teal and white palette, scenic destination imagery
- Clear typography, consistent spacing, rounded cards and accessible controls
- Reusable Compose components and a consistent theme
- Responsive layouts for Android screen sizes
- Native Compose UI only; do not use screenshots, a static image, or a WebView as the screen implementation.
  Screens
1. Home
- App branding and profile/notification actions
- Scenic hero section and “Plan a Trip” CTA
- Explore Destinations cards using local mock data
- Feature shortcuts for itinerary planning, budget estimates and trip assistance
- Bottom navigation
2. Plan Trip
- Destination selection/input
- Start and end dates
- Traveller count
- Total budget range
- Interests: nature, culture, food, adventure, relaxation and shopping
- Form validation and “Generate AI Itinerary”
- Use a representative mock itinerary; no AI service calls.
3. AI Itinerary & Chat
- Destination and trip summary
- Day selector and day-by-day itinerary
- Activities with times and short descriptions
- Estimated daily cost
- Chat-style UI for itinerary changes with mock responses only
- No LLM or MCP calls.
4. Budget Summary
- Destination and trip summary
- Estimated total trip cost
- Itemized estimated accommodation, transport, activities, food and other costs
- Clearly state that costs are estimates, not live prices
- “Proceed to Checkout” CTA
5. Checkout & Payment
- Order summary and estimated amount
- Simulated payment-method choices (UPI, card, net banking or wallet as visual choices only)
- Clearly identify payment as a local simulation
- “Pay” action opens a mock processing state
- Never collect real card numbers, bank credentials, UPI PINs or other sensitive payment information. Do not integrate a real payment gateway.
6. My Trips
- All, Upcoming and Completed filters
- Saved trip cards, trip details and payment-status labels
- Empty state when no trips exist
- “Plan Another Trip” CTA
  Simulated payment states
  Create distinct UI states or screens for:
1. Processing — loading indicator and progress message.
2. Success — confirmation, mock order ID, simulated method and trip-details action.
3. Failed — error message, Retry Payment and Change Payment Method actions.
4. Cancelled — cancellation message and Pay Now action.
   Provide a simple development-only way to preview every state. Do not imply that real money was transferred. Saving a trip must work independently of payment; users can save an itinerary without checkout.
   Architecture and code quality
- Inspect the existing repository before editing.
- Preserve current Gradle, AGP, Kotlin, Compose and SDK configuration where possible.
- Follow existing package conventions if architecture already exists.
- If the project is empty, use a simple structure such as ui/screens, ui/components, ui/theme, navigation, data/mock, domain/model and ui/viewmodel.
- Keep composables focused and reusable. Separate UI state where appropriate.
- Use mock repositories/interfaces where useful so real data sources can be added later.
- Avoid overengineering and unnecessary dependencies.
- Add previews for key composables where practical.
- Handle loading, empty and error states where appropriate.
  Explicit exclusions for Phase 1
  Do not implement or configure:
- Spring Boot or PostgreSQL
- Retrofit or real network APIs
- FastAPI
- Ollama or another LLM
- MCP tools or server integration
- Real payment processing
- Authentication or production account management
- Cloud hosting
  Use local mock data and in-memory state only. Persistence beyond the current app session is not required.
  Work process
1. Inspect the repository and summarize the existing setup.
2. Propose a short implementation plan.
3. Implement the UI and navigation.
4. Build the Android project and run available tests.
5. Fix compilation errors caused by your changes.
6. Explain the important packages and files.
7. Report remaining issues honestly.
   Do not begin backend or AI integration. Stop after Phase 1 and wait for my explicit approval before Phase 2.
   Acceptance criteria
- The Android app compiles, or existing unrelated blockers are clearly reported.
- The app launches on an emulator or device.
- All six screens are reachable through working navigation.
- Processing, success, failure and cancelled payment states can be previewed.
- The UI uses native Compose and follows the design direction.
- Trip saving works without payment.
- Trip and payment data are clearly mock data.
- No backend, AI service, MCP server or real payment gateway is needed to run this phase.
- The code structure is explained in the final report.