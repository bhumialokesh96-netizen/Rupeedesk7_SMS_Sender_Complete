Rupeedesk7 SMS Sender - Complete Project (Prototype)
======================================================

What I included:
- Firebase integrated Android app (Login, Dashboard, background SMS worker)
- Firestore inventory fetch (worker selects an unsent inventory doc and sends SMS)
- Runtime SEND_SMS permission handling
- google-services.json (you provided) is already included in app/

IMPORTANT: real device required to send SMS (emulator will not send SMS).
Also, make sure you have permission to message target numbers.

Required steps before building:
1. (Optional) Generate Gradle wrapper locally if missing:
   - If you have Gradle installed locally: from project root run:
       gradle wrapper
     This will generate gradlew, gradlew.bat and gradle/wrapper files.
   - If you prefer, Android Studio will generate wrapper when you open the project and sync Gradle.
2. Open the project in Android Studio, sync Gradle and install on a device.
3. In Firebase Console: enable Firestore and create 'inventory' collection with at least one document:
   fields: target (string), message (string), price (number), sent (boolean=false)
4. On device: grant SEND_SMS permission when prompted.
5. Press Start Sending to send one SMS from inventory and update your Firestore user doc.

Security & Deployment Notes:
- This app is a prototype. Do NOT publish to Play Store without meeting SMS verification and policy requirements.
- Secure Firestore rules before production.
- Obtain consent from recipients before sending messages.

If you want, I can:
- Add Admin web UI to manage inventory and users.
- Implement batch/background scheduling with WorkManager repeating work.
- Add withdrawal flow and admin approval.
