# ToolTrail
Where we learned it. The Android Developers documentation on WorkManager (developer.android.com/develop/background-work/background-tasks/persistent/getting-started) and on notifications (developer.android.com/develop/ui/views/notifications), and Claude as an AI tutor and code assistant.

What went wrong.
- From Android 13 an app must have the notification permission. During testing no notification appeared until the permission was granted on the phone.
- The first build did not show the permission prompt because the old version of MainActivity was still installed; the prompt appeared after reinstalling the new build.
- WorkManager does not fire at the exact second, so the notification appears a short time after the due time.

### AI tools used

We used Claude as a tutor and code assistant for explanations and code. We checked each step by building and testing on a real Samsung phone.

## 10. Testing

We tested on a real Samsung phone (Android API 37) after a fresh install. All of these passed:
1. Saving the Add equipment form with blank fields shows the required-field errors.
2. A new item appears in the list as Available.
3. Borrowing an item moves the app to the Loans screen with the loan listed.
4. The item shows On loan with Borrow disabled, and the Dashboard counts update.
5. Mark returned removes the loan from Loans and adds it to History, and the item is Available again.
6. Text typed in a form survives rotating the phone.
7. Data is still there after swiping the app away and reopening it.
8. Returning an item before it is due cancels its reminder, so no notification appears.
9. A reminder notification appears after a short loan (0.02 hours) has passed.

## 11. Known limitations

- There is no separate equipment details screen. Details are shown on the list cards.
- The return time is entered as hours from now. It is shown as a date and time in the app.
- The reminder fires when the loan becomes due. There is no earlier warning.
- If notification permission is denied, the app works normally but shows no reminders and no message about it.
- The database is version 1 with no migrations. Changing a table needs a new database version.

## 12. Team

| Member | Student number | Role |
|---|---|---|
| Innocent Kabwe | 2410109 | Technical lead: setup, navigation and integration |
| Joshua Mazaza | 2410123 | Coordination, code review and quality assurance |
| Gabriel Mwila | 2410234 | Equipment management |
| Ndame Sepete | 2410559 | Borrowing and loan management |
| Moses Phiri | 2410103 | Database and reminders |
