# Google Play Store Publishing Guide for CediWise

This guide provides all the necessary metadata, configurations, and answers required to publish **CediWise** on the **Google Play Console**.

---

## 1. App Store Listing Metadata

### App Title (Max 30 characters)
```text
CediWise: Expense & Budget
```
*(or simply `CediWise`)*

### Short Description (Max 80 characters)
```text
Smart personal finance, expense tracker, and budget manager for Ghana & beyond.
```

### Full Description (Up to 4000 characters)
```markdown
Take complete control of your money with CediWise — the smart personal expense tracker and budget manager tailored for everyday financial discipline in Ghana and worldwide.

Whether tracking daily Trotro commutes, Mobile Money (MTN MoMo, Telecel Cash, AT Money) transfers, groceries, rent advances, utility bills, or business inflows, CediWise gives you a clear, instant view of your cashflow.

🌟 KEY FEATURES:

📊 Smart Financial Dashboard
• Monitor Total Balance, Income, and Expenses at a single glance.
• Daily, weekly, and monthly spending pills keep you conscious of where your money goes.
• Proactive smart status banner alerts you when approaching budget limits.

🎯 Visual Budget Tracking
• Set customized Daily, Weekly, and Monthly spending limits.
• Accessible visual progress meters with clear Safe, Warning, and Exceeded badges.
• Early warning alerts before you accidentally overspend.

📱 Ghana & Multi-Currency Ready
• Default currency: Ghanaian Cedi (GH₵ / GHS).
• Multi-currency support: Easily switch between USD ($), GBP (£), EUR (€), NGN (₦), KES, ZAR, and CAD.
• Designed with Ghanaian expense patterns: MoMo, Airtime & Data, Trotro, Fuel, School Fees, Church & Tithes, Family Remittances, and Utilities.
• Custom categories: Create personalized categories with customized icons and color palettes.

⏰ Automatic Daily Reminders & Bill Due Alerts
• Reliable Morning & Evening notifications remind you to record your spending.
• Reminders continue working reliably even when the app is completely closed.
• Track recurring bills (ECG Electricity, Water, Internet, DSTV) and receive alerts before they are due.

📈 Analytics & Financial Insights
• Spending by category with percentage breakdowns.
• Income vs. Expense cashflow comparison charts.
• Actionable financial insights: identify your highest spending days and top expense categories.

🎯 Savings Goals
• Create targets for an emergency fund, laptop, vehicle, or rent.
• Record deposit contributions and visualize your progress bar.

🔒 Privacy-First & 100% Offline Capable
• Privacy by Design: All your data is stored locally in an encrypted SQLite database on your device.
• Zero Data Selling: We do not sell or share your data with advertisers.
• App Lock PIN: Secure your financial records with an optional 4-digit PIN.
• Data Export: Export your complete transaction ledger to CSV anytime.
• Account Deletion: Full in-app account and data deletion control.

Download CediWise today and achieve financial awareness and peace of mind!
```

### Store Categorization
- **Application Category:** Finance
- **Tags:** Personal Finance, Expense Tracker, Budget Manager, Money Management, Spending Tracker

---

## 2. App Review & Test Credentials for Google Reviewers

When submitting the app under **App access** in Google Play Console:
- **Access Type:** "All or some functionality is restricted"
- **Title:** Demo Reviewer Account
- **User name / Email:** `kwame@cediwise.com`
- **Password:** `Password123`
- **Instructions:** *"The login screen features a 1-tap 'Demo Account' button that instantly authenticates with preloaded sample data for testing. You can also create a new account with any email and password."*

---

## 3. Data Safety Form Answers (Google Play Console)

Google Play requires accurate Data Safety disclosures:

| Section | Question | Answer |
| :--- | :--- | :--- |
| **Data Collection** | Does your app collect or share user data? | **Yes** (Only locally on-device for core app functionality) |
| **Data Sharing** | Is data shared with third parties or data brokers? | **No** (Zero third-party trackers or ad SDKs) |
| **Financial Info** | What financial info is collected? | User-entered transaction amounts and categories (stored locally on device for budget management) |
| **Personal Info** | Name & Email address? | Yes, provided by the user for local authentication and profile personalization |
| **Encryption** | Is data encrypted? | **Yes**, stored locally in device sandbox with encrypted SQLite/Room and SHA-256 hashed credentials |
| **Account Deletion** | Do you provide a way for users to request data deletion? | **Yes**, users can permanently delete their account and wipe all records directly in Settings -> "Permanently Delete Account & Financial Data" |

---

## 4. Target Audience and Content Rating

- **Target Age:** 18 and older (or General Audience 13+)
- **Content Rating:** Everyone (PEGI 3, ESRB Everyone)
- **Monetization / Ads:** Contains NO ads (`isCrunchPngs = false`, zero ad SDKs)

---

## 5. Building the Production Release (AAB & APK)

To build the signed Android App Bundle (`.aab`) for Google Play:

1. **Via Android Terminal / CLI:**
   ```bash
   # Build the release Android App Bundle (recommended for Google Play Store):
   gradle :app:bundleRelease

   # Or build the release APK:
   gradle :app:assembleRelease
   ```

2. **Generated Files Location:**
   - **AAB File:** `app/build/outputs/bundle/release/app-release.aab`
   - **APK File:** `app/build/outputs/apk/release/app-release.apk`

3. **Upload to Google Play Console:**
   - Navigate to **Production** (or **Closed Testing / Open Testing** track).
   - Click **Create new release**.
   - Drag and drop `app-release.aab`.
   - Add release notes: *"Initial production release of CediWise personal expense tracker."*
   - Review and rollout!
