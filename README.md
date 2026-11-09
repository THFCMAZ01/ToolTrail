# ToolTrail

**ToolTrail — Shared Equipment Borrowing and Return Tracker**

BSE 3340 Mobile Application Development  
Zambia University College of Technology

- **Repository:** https://github.com/THFCMAZ01/ToolTrail
- **Demo video:** To be added after uploading the YouTube video as Unlisted.

## 1. The problem

Small organisations such as student clubs, churches, community groups and workshops often lend out shared equipment. Without a simple record, it is difficult to know who borrowed an item, when it is due back, and which items have not been returned. This can lead to lost equipment, delays and misunderstandings.

## 2. Who it is for

ToolTrail is for the person responsible for shared equipment in a small organisation who needs a simple, offline record of what is available, what is currently borrowed, who has each item, and when it is due back.

## 3. What the app does

- Register equipment with its name, category, description and condition.
- Display all equipment in name order, showing whether each item is **Available** or **On loan**.
- Record the borrower's name and contact details, together with the due date and time.
- Prevent the same item from being lent to two people at once.
- Display active loans in due-date order and label overdue loans.
- Record when an item is returned.
- Keep a history of completed loans.
- Display dashboard totals for total equipment, available items, active loans and overdue loans.
- Schedule a notification when a loan becomes due.

The app works offline. Equipment and borrowing records are stored locally on the device.

## 4. Screens

| Screen | What it does |
|---|---|
| **Dashboard** | Displays four totals: total equipment, available items, active loans and overdue loans. Shows a hint when no equipment has been registered. |
| **Equipment** | Shows a scrollable list of equipment. Each card displays the name, category, condition, description and availability status. The Borrow button is disabled while an item is on loan. A + button opens the Add equipment form. |
| **Add equipment** | Provides a form for registering equipment. The name and category are required. |
| **Borrow equipment** | Provides a form for entering the borrower's name, contact and number of hours until the item is due. The hours must be a number greater than zero. |
| **Loans** | Displays active loans, borrower details, due dates, overdue labels and a Mark returned action. |
| **History** | Displays completed loans with their borrowing and return times. |

Equipment details are displayed on the Equipment list cards; there is no separate equipment-details screen.

**Navigation and back behaviour:** The Dashboard, Equipment, Loans and History screens are accessible from the bottom navigation bar. Back from Add equipment or Borrow equipment returns to the Equipment list. Back from the other main tabs returns to the Dashboard.

**Empty states:** Equipment, Loans and History each display a message when there is no data to show.

## 5. Project structure

```text
app/src/main/java/com/zict/tooltrail/
├── MainActivity.kt                 # Entry point and notification permission request
├── data/
│   ├── entity/                     # EquipmentEntity, BorrowingEntity
│   ├── dao/                        # EquipmentDao, BorrowingDao
│   ├── database/                   # ToolTrailDatabase
│   └── repository/                 # ToolTrailRepository
├── ui/
│   ├── ToolTrailViewModel          # UI state and application actions
│   └── ToolTrailApp                # Compose screens and navigation
└── notifications/
    ├── ReminderWorker              # Background reminder work
    └── ReminderScheduler           # Schedules and cancels reminders
```

**How the layers connect:** The screens in ToolTrailApp observe state from the ViewModel. The ViewModel calls the repository, which applies validation and business rules before using the DAOs. The DAOs execute queries against the Room database. The screens do not access the database directly.

### Data model

**equipment table**

- `id`
- `name`
- `category`
- `description`
- `condition`
- `dateAdded`

**borrowings table**

- `id`
- `equipmentId` (foreign key to equipment)
- `borrowerName`
- `borrowerContact`
- `borrowedAt`
- `dueDate`
- `returnedAt`

The `returnedAt` field is null while an item is still borrowed. When the item is returned, the field is set to the return time. This allows the app to distinguish active loans from completed loans without deleting borrowing records.

The foreign key uses **RESTRICT**, preventing equipment with borrowing records from being deleted.

## 6. Lifecycle, validation and data persistence

### Rotation and leaving the app

The ViewModel holds screen state and survives configuration changes such as device rotation. The current screen, selected item and text entered into forms are saved with `rememberSaveable`. Room Flows are collected with `collectAsStateWithLifecycle`, so collection follows the Activity lifecycle and pauses when the app is not in the foreground.

### Saving data

Records are stored in a Room (SQLite) database named `tooltrail_database`. Data remains available after the app is closed or swiped away. Uninstalling the app removes its local database; reinstalling creates a new empty database.

### Validation

The forms display field-level errors when required information is missing or invalid. The repository also checks the business rules before saving, including:

- Blank equipment name or category.
- Blank borrower name or contact.
- A due date that is not in the future.
- An equipment item that does not exist.
- An item that is already borrowed.
- A loan that has already been returned.

Repository errors are displayed to the user through a Snackbar.

## 7. Technology

- Kotlin
- Jetpack Compose and Material 3
- Room database with KSP
- Kotlin Coroutines and Flow
- ViewModel
- WorkManager
- Minimum SDK: 24
- Compile SDK: 37
- Target SDK: 37
- Package: `com.zict.tooltrail`

## 8. How to build and run

1. Install a recent version of Android Studio with Android SDK Platform 37.
2. Clone the repository:

   ```bash
   git clone https://github.com/THFCMAZ01/ToolTrail.git
   ```

3. Open the cloned project folder in Android Studio.
4. Wait for Gradle sync to finish.
5. Select an Android emulator or connect a phone with USB debugging enabled. The device must run Android 7.0 (API 24) or higher.
6. Press **Run** in Android Studio.
7. On Android 13 or higher, allow the app to send notifications when prompted.

**Build from the command line (Windows PowerShell):**

```powershell
.\gradlew.bat --no-configuration-cache :app:assembleDebug
```

The current build may display an experimental-option warning for `android.disallowKotlinSourceSets` and a message that Kotlin falls back to JVM 24 when the build uses JDK 25. These messages appeared during development; the project build completed successfully in that environment.

**Quick reminder demonstration:** Create a loan with a due time of 0.02 hours (approximately 1.2 minutes), press Home, and wait one to two minutes. WorkManager is not guaranteed to run at the exact due second, so allow a little extra time.

## 9. Research Note

### Concept 1: Room database

**Owner:** Moses Phiri

**What it is:** Room is an Android persistence library built on top of SQLite. Developers define database tables using Kotlin entity classes and describe database operations using Data Access Objects (DAOs). Room generates the database implementation during the build.

In ToolTrail, `EquipmentEntity` and `BorrowingEntity` define the tables, `EquipmentDao` and `BorrowingDao` define database operations, and `ToolTrailDatabase` provides access to the database.

**Why ToolTrail needs it:** Equipment and borrowing records must remain available after the app is closed. The app also needs its lists and dashboard to reflect changes to stored records. Room queries can return Kotlin Flows, allowing the UI to observe database changes and update when records are inserted or modified.

**Where we learned it:** The official Android Developers documentation on [Room](https://developer.android.com/training/data-storage/room), with Claude used as an AI tutor and code assistant.

**What went wrong and how we addressed it:**

- The KSP plugin initially conflicted with the Android Gradle Plugin's built-in Kotlin support. The build worked after adding `android.disallowKotlinSourceSets=false` to `gradle.properties`. This setting is experimental and the build continues to display a warning about it.
- While implementing the return feature, we found that BorrowingDao did not have a query to retrieve a borrowing record by its ID. We added `getBorrowingById` to support that operation.
- After uninstalling and reinstalling the app on the test phone, the stored records were no longer present. This helped us understand that the Room database is local app data and is removed when the app is uninstalled.

### Concept 2: Notifications and WorkManager

**Owner:** Moses Phiri

**What it is:** WorkManager is an Android library for scheduling deferrable background work that should remain scheduled across app restarts and device reboots. Notifications allow an app to present information to the user when the app is not in the foreground.

**Why ToolTrail needs it:** A person responsible for shared equipment needs to know when a borrowed item becomes due. When a loan is created, the ViewModel asks ReminderScheduler to enqueue a one-time task with a delay based on the due time. When an item is returned, its scheduled reminder is cancelled. When ReminderWorker runs, it checks the database and posts a notification only if the loan has not already been returned.

**Where we learned it:** The official Android Developers documentation on [WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started) and [notifications](https://developer.android.com/develop/ui/views/notifications), with Claude used as an AI tutor and code assistant.

**What went wrong and how we addressed it:**

- On Android 13 and later, apps need notification permission before they can post notifications. During testing, reminders did not appear until permission was granted on the phone.
- The notification permission prompt did not appear during the first test because the older app version was still installed. After installing the updated build, the prompt appeared.
- WorkManager does not guarantee execution at the exact second a task becomes due. The reminder can therefore appear shortly after the due time rather than precisely at that moment.

## 10. Testing

The team tested the app on a real Samsung phone running Android API 37 after a fresh installation. The following checks passed:

- Submitting the Add equipment form with blank required fields displays validation errors.
- A newly registered item appears in the Equipment list as Available.
- Borrowing an item moves the app to the Loans screen and displays the new loan.
- A borrowed item displays On loan, its Borrow button is disabled, and the Dashboard totals update.
- Marking an item as returned removes the loan from Loans, adds it to History and makes the equipment Available again.
- Text entered into a form survives rotating the phone.
- Stored data remains available after swiping the app away and reopening it.
- Returning an item before its due time cancels its reminder, so no due notification is posted.
- A reminder notification appears after a short loan of 0.02 hours has passed.

## 11. Known limitations

- There is no separate equipment-details screen; equipment information is displayed on the list cards.
- The return time is entered as a number of hours from the current time and displayed as a date and time.
- The reminder is scheduled for when the loan becomes due; there is no earlier warning.
- If notification permission is denied, the app continues to work, but reminders are not displayed and the app does not show a separate explanation.
- The database is currently at version 1 and has no migration paths. Future changes to the database schema will require a version update and appropriate migrations.

## 12. Team

| Member | Student Number | Role |
|---|---:|---|
| Innocent Kabwe | 2410109 | Technical lead: setup, navigation and integration |
| Joshua Mazaza | 2410123 | Coordination, code review and quality assurance |
| Gabriel Mwila | 2410234 | Equipment management |
| Ndame Sepete | 2410559 | Borrowing and loan management |
| Moses Phiri | 2410103 | Database and reminders |

## AI tools used

The team used Claude as a tutor and code assistant for explanations and implementation support. We checked the work by building the project and testing the application on a real Samsung phone.
