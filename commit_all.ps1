# 1. chore(backend): update dependencies and configuration in pom.xml and application properties
git add backend/lostandfound/pom.xml backend/lostandfound/src/main/resources/application.properties
git commit -m "chore(backend): update dependencies and configuration in pom.xml and application properties"

# 2. feat(backend): implement JWT security config and AuthController
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/auth/AuthController.java backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/shared/config/SecurityConfig.java
git commit -m "feat(backend): implement JWT security config and AuthController"

# 3. test(backend): update AuthController unit tests
git add backend/lostandfound/src/test/java/edu/cit/tiongzon/lostandfound/feature/auth/AuthControllerTest.java
git commit -m "test(backend): update AuthController unit tests"

# 4. feat(backend): add UserController and UserDTO for profile management
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/users/UserController.java backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/users/UserDTO.java
git commit -m "feat(backend): add UserController and UserDTO for profile management"

# 5. test(backend): update UserController unit tests
git add backend/lostandfound/src/test/java/edu/cit/tiongzon/lostandfound/feature/users/UserControllerTest.java
git commit -m "test(backend): update UserController unit tests"

# 6. test(backend): update ItemController unit tests
git add backend/lostandfound/src/test/java/edu/cit/tiongzon/lostandfound/feature/items/ItemControllerTest.java
git commit -m "test(backend): update ItemController unit tests"

# 7. test(backend): update general application tests
git add backend/lostandfound/src/test/java/edu/cit/tiongzon/lostandfound/LostandfoundApplicationTests.java
git commit -m "test(backend): update general application tests"

# 8. feat(backend): implement admin controller and moderation logic
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/admin/
git commit -m "feat(backend): implement admin controller and moderation logic"

# 9. feat(backend): implement claims feature endpoints
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/claims/
git commit -m "feat(backend): implement claims feature endpoints"

# 10. feat(backend): integrate Cloudinary in files feature
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/files/
git commit -m "feat(backend): integrate Cloudinary in files feature"

# 11. feat(backend): add private messaging endpoints
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/messages/
git commit -m "feat(backend): add private messaging endpoints"

# 12. feat(backend): implement SMTP email notifications
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/notifications/
git commit -m "feat(backend): implement SMTP email notifications"

# 13. feat(backend): implement payment gateway backend processing
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/feature/payments/
git commit -m "feat(backend): implement payment gateway backend processing"

# 14. feat(backend): configure WebSockets via STOMP
git add backend/lostandfound/src/main/java/edu/cit/tiongzon/lostandfound/shared/config/WebSocketConfig.java
git commit -m "feat(backend): configure WebSockets via STOMP"

# 15. chore(root): add global .gitignore
git add .gitignore
git commit -m "chore(root): add global .gitignore to ignore node_modules and env"

# 16. chore(mobile): update gradle build scripts and toml versions
git add mobile/app/build.gradle.kts mobile/gradle/libs.versions.toml mobile/.idea/misc.xml mobile/.idea/inspectionProfiles/Project_Default.xml
git commit -m "chore(mobile): update gradle build scripts and toml versions"

# 17. feat(mobile): configure Retrofit API client and Auth API
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/shared/api/RetrofitClient.kt mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/shared/api/AuthApi.kt
git commit -m "feat(mobile): configure Retrofit API client and Auth API"

# 18. feat(mobile): implement Google Login Request model and Login Screen
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/auth/data/model/GoogleLoginRequest.kt mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/auth/ui/LoginScreen.kt
git commit -m "feat(mobile): implement Google Login Request model and Login Screen"

# 19. feat(mobile): setup NavGraph and Home Screen
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/navigation/NavGraph.kt mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/home/ui/HomeScreen.kt
git commit -m "feat(mobile): setup NavGraph and Home Screen"

# 20. feat(mobile): configure StompClient for WebSockets
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/shared/api/StompClient.kt
git commit -m "feat(mobile): configure StompClient for WebSockets"

# 21. feat(mobile): build admin moderation screens
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/admin/
git commit -m "feat(mobile): build admin moderation screens"

# 22. feat(mobile): build item catalog and details view
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/catalog/ mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/items/
git commit -m "feat(mobile): build item catalog and details view"

# 23. feat(mobile): build real-time chat feature
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/chat/
git commit -m "feat(mobile): build real-time chat feature"

# 24. feat(mobile): implement claims and dashboard functionality
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/claims/ mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/dashboard/
git commit -m "feat(mobile): implement claims and dashboard functionality"

# 25. feat(mobile): build report item screen
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/report/
git commit -m "feat(mobile): build report item screen"

# 26. feat(mobile): build user profile screen
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/profile/
git commit -m "feat(mobile): build user profile screen"

# 27. feat(mobile): implement mobile payment gateway checkout
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/feature/payments/
git commit -m "feat(mobile): implement mobile payment gateway checkout"

# 28. style(mobile): update theme colors and add Google icon drawable
git add mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/shared/ui/theme/Color.kt mobile/app/src/main/res/drawable/ic_google.xml mobile/app/src/main/java/edu/cit/tiongzon/lostandfound/shared/ui/components/
git commit -m "style(mobile): update theme colors and add Google icon drawable"

# 29. test(mobile): update instrumented and unit tests
git add mobile/app/src/androidTest/java/edu/cit/tiongzon/lostandfound/ExampleInstrumentedTest.kt mobile/app/src/test/java/edu/cit/tiongzon/lostandfound/ExampleUnitTest.kt
git commit -m "test(mobile): update instrumented and unit tests"

# 30. chore(web): update package.json, lockfiles, and next.config.ts
git add web/lostandfound/package.json web/lostandfound/package-lock.json web/lostandfound/next.config.ts package.json package-lock.json
git commit -m "chore(web): update package.json, lockfiles, and next.config.ts"

# 31. feat(web): setup base layout, page, and responsive Navbar
git add web/lostandfound/app/layout.tsx web/lostandfound/app/page.tsx web/lostandfound/components/layout/Navbar.tsx
git commit -m "feat(web): setup base layout, page, and responsive Navbar"

# 32. feat(web): build Google OAuth providers and Login Form
git add web/lostandfound/features/auth/api.ts web/lostandfound/features/auth/components/LoginForm.tsx web/lostandfound/components/providers/
git commit -m "feat(web): build Google OAuth providers and Login Form"

# 33. feat(web): implement user dashboard (my-items) and profile pages
git add web/lostandfound/app/my-items/page.tsx web/lostandfound/app/profile/page.tsx web/lostandfound/features/users/api.ts
git commit -m "feat(web): implement user dashboard (my-items) and profile pages"

# 34. feat(web): build catalog and item details views
git add web/lostandfound/app/items/page.tsx web/lostandfound/app/items/[id]/page.tsx web/lostandfound/features/items/api.ts
git commit -m "feat(web): build catalog and item details views"

# 35. feat(web): add real-time chat and private messages UI
git add web/lostandfound/app/chat/ web/lostandfound/app/messages/
git commit -m "feat(web): add real-time chat and private messages UI"

# 36. feat(web): build claims processing and payment UI
git add web/lostandfound/app/claims/ web/lostandfound/components/ui/stripe-payment-panel.tsx
git commit -m "feat(web): build claims processing and payment UI"

# 37. feat(web): build admin dashboard interface
git add web/lostandfound/app/admin/
git commit -m "feat(web): build admin dashboard interface"

# 38. style(web): add reusable UI components (avatar, badge, tabs)
git add web/lostandfound/components/ui/avatar.tsx web/lostandfound/components/ui/status-badge.tsx web/lostandfound/components/ui/tabs.tsx
git commit -m "style(web): add reusable UI components (avatar, badge, tabs)"

# 39. chore(root): clean root directory and catch remaining files
git add .
git commit -m "chore(root): perform final project cleanup and format codebase"
