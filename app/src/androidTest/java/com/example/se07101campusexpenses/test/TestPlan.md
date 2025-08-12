# CampusExpense Manager Testing Plan

## Functional Requirements (FRs) Testing

### FR 1: User Account Management
- **Test Case 1.1:** User registration with valid credentials
- **Test Case 1.2:** User registration with existing username (should fail)
- **Test Case 1.3:** User registration with invalid email format (should fail)
- **Test Case 1.4:** User login with correct credentials
- **Test Case 1.5:** User login with incorrect credentials (should fail)
- **Test Case 1.6:** User logout functionality
- **Test Case 1.7:** Password security (minimum length, complexity)

### FR 2: Expense Entry
- **Test Case 2.1:** Add a new expense with valid data
- **Test Case 2.2:** Add an expense with empty fields (should fail)
- **Test Case 2.3:** Add an expense with negative amount (should fail)
- **Test Case 2.4:** Edit an existing expense
- **Test Case 2.5:** Delete an existing expense
- **Test Case 2.6:** Add expense with various categories
- **Test Case 2.7:** Adding expense with date in past
- **Test Case 2.8:** Adding expense with future date

### FR 3: Budget Management
- **Test Case 3.1:** Create a new budget with valid data
- **Test Case 3.2:** Create a budget with empty fields (should fail)
- **Test Case 3.3:** Create a budget with negative amount (should fail)
- **Test Case 3.4:** Edit an existing budget
- **Test Case 3.5:** Delete an existing budget
- **Test Case 3.6:** Create multiple budgets for different categories
- **Test Case 3.7:** Budget visualization (progress bar)

### FR 4: Expense Overview
- **Test Case 4.1:** View total spending
- **Test Case 4.2:** View remaining budget
- **Test Case 4.3:** View expense breakdown by category
- **Test Case 4.4:** View expense trends over time
- **Test Case 4.5:** Test with no expenses (empty state)
- **Test Case 4.6:** Test with multiple months of data
- **Test Case 4.7:** Verify expense chart data accuracy

### FR 5: Recurring Expenses
- **Test Case 5.1:** Add a valid recurring expense
- **Test Case 5.2:** Add a recurring expense with invalid data (should fail)
- **Test Case 5.3:** Verify automatic addition of weekly recurring expense
- **Test Case 5.4:** Verify automatic addition of monthly recurring expense
- **Test Case 5.5:** Edit a recurring expense
- **Test Case 5.6:** Delete a recurring expense
- **Test Case 5.7:** Verify recurring expenses with start/end dates
- **Test Case 5.8:** Verify no duplicate expenses created for same period

### FR 6: Expense Reports
- **Test Case 6.1:** Generate report for current month
- **Test Case 6.2:** Generate report for custom date range
- **Test Case 6.3:** Verify category breakdown in reports
- **Test Case 6.4:** Generate report with no data (empty state)
- **Test Case 6.5:** Verify total amounts in reports
- **Test Case 6.6:** Verify individual expense listing in reports
- **Test Case 6.7:** Verify pie chart data accuracy

### FR 7: Expense Notifications
- **Test Case 7.1:** Receive notification when exceeding budget (100%)
- **Test Case 7.2:** Receive notification when approaching budget limit (90%)
- **Test Case 7.3:** No notifications when well below budget
- **Test Case 7.4:** Multiple category budget notifications
- **Test Case 7.5:** Notification content and formatting

## Non-Functional Requirements (NFRs) Testing

### NFR 1: Performance
- **Test Case P1:** Load time with 100+ expenses
- **Test Case P2:** Scroll performance with large expense list
- **Test Case P3:** Chart rendering with large dataset
- **Test Case P4:** App responsiveness during background operations
- **Test Case P5:** Memory usage monitoring
- **Test Case P6:** CPU usage monitoring
- **Test Case P7:** Battery consumption

### NFR 2: User-Friendly Interface
- **Test Case U1:** UI consistency across different screens
- **Test Case U2:** Navigation between main sections
- **Test Case U3:** Form validation feedback
- **Test Case U4:** Empty state handling
- **Test Case U5:** Error messaging
- **Test Case U6:** Icon and label clarity
- **Test Case U7:** Accessibility testing (text sizes, contrast)
- **Test Case U8:** Touch target size appropriateness

### NFR 3: Data Security
- **Test Case S1:** Database encryption
- **Test Case S2:** Secure password storage
- **Test Case S3:** Session management
- **Test Case S4:** Data privacy in logs
- **Test Case S5:** Secure user data during export
- **Test Case S6:** Protection against SQL injection
- **Test Case S7:** Data persistence across app updates

### NFR 4: Feedback and Support
- **Test Case F1:** Feedback form functionality
- **Test Case F2:** Email client integration
- **Test Case F3:** Support contact information display
- **Test Case F4:** Form field validation
- **Test Case F5:** Device information inclusion in feedback

## Integration Testing
- **Test Case I1:** End-to-end budget cycle (create budget, add expenses, receive notifications)
- **Test Case I2:** End-to-end recurring expense cycle
- **Test Case I3:** User journey from registration to expense analysis
- **Test Case I4:** Data consistency across app components

## Device Compatibility Testing
- **Test Case D1:** Small screen devices (phones)
- **Test Case D2:** Large screen devices (tablets)
- **Test Case D3:** Different Android OS versions
- **Test Case D4:** Low-end device performance
- **Test Case D5:** Different screen orientations

## User Acceptance Testing
- **Test Case UAT1:** New user onboarding experience
- **Test Case UAT2:** Daily expense tracking workflow
- **Test Case UAT3:** Monthly budget review workflow
- **Test Case UAT4:** Report generation and analysis workflow
