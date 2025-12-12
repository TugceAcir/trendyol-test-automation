# Trendyol Web Automation Framework

Professional test automation framework for **Trendyol.com** built with Selenium, TestNG, and Maven.

---

## 🎯 Framework Features

- ✅ **Page Object Model (POM)** - Maintainable and reusable page classes
- ✅ **Singleton WebDriver** - Thread-safe driver management with ThreadLocal
- ✅ **Parallel Execution** - Run tests in parallel for faster execution
- ✅ **ExtentReports** - Professional HTML reports with screenshots
- ✅ **Log4j2 Logging** - Structured logging for debugging
- ✅ **Centralized Configuration** - Easy configuration via `config.properties`
- ✅ **Cross-Browser Support** - Chrome, Firefox, Edge
- ✅ **CI/CD Ready** - Maven profiles for different test suites
- ✅ **Screenshot on Failure** - Automatic screenshot capture on test failure
- ✅ **Turkish Language Support** - Handles Turkish special characters

---

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.8.4** or higher
- **Git**

---

## 🚀 Getting Started

### 1. Clone Repository
```bash
git clone <repository-url>
cd trendyol-automation
```

### 2. Install Dependencies
```bash
mvn clean install -DskipTests
```

### 3. Run Tests
```bash
# Run all tests
mvn clean test

# Run smoke tests only
mvn clean test -Psmoke

# Run regression tests
mvn clean test -Pregression

# Run E2E tests
mvn clean test -Pe2e
```

---

## 📁 Project Structure

```
trendyol-automation/
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── base/                  # Base classes
│   │       │   ├── BaseTest.java      # Parent test class
│   │       │   └── DriverManager.java # WebDriver singleton
│   │       ├── pages/                 # Page Object classes
│   │       │   ├── HomePage.java
│   │       │   ├── SearchResultsPage.java
│   │       │   └── ...
│   │       ├── utils/                 # Utility classes
│   │       │   ├── WaitHelper.java
│   │       │   ├── ElementHelper.java
│   │       │   ├── ConfigReader.java
│   │       │   └── ScreenshotHelper.java
│   │       └── constants/             # Constants
│   │           ├── URLs.java
│   │           └── Timeouts.java
│   └── test/
│       ├── java/
│       │   └── tests/
│       │       ├── smoke/             # Smoke test cases
│       │       ├── regression/        # Regression test cases
│       │       └── e2e/               # End-to-end test cases
│       └── resources/
│           ├── testng.xml             # TestNG suite configuration
│           ├── config.properties      # Framework configuration
│           └── log4j2.xml             # Logging configuration
├── reports/                           # Test reports (auto-generated)
├── screenshots/                       # Screenshots (auto-generated)
├── logs/                              # Log files (auto-generated)
├── pom.xml                            # Maven configuration
└── README.md                          # This file
```

---

## ⚙️ Configuration

All configurations are in `src/test/resources/config.properties`:

### Browser Configuration
```properties
browser=chrome              # chrome, firefox, edge
headless=false             # true for headless execution
browser.maximize=true      # Maximize browser window
```

### Timeout Configuration
```properties
implicit.wait=10           # Implicit wait in seconds
explicit.wait=20           # Explicit wait in seconds
page.load.timeout=30       # Page load timeout
```

### Parallel Execution
```properties
parallel.thread.count=3    # Number of parallel threads
```

### Reporting
```properties
screenshot.on.failure=true # Capture screenshot on failure
report.directory=./reports/
screenshot.directory=./screenshots/
```

---

## 🧪 Writing Tests

### Example Test Class

```java
package tests.smoke;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;

public class HomePageTests extends BaseTest {
    
    @Test(description = "Verify Trendyol homepage loads successfully")
    public void testHomePageLoad() {
        HomePage homePage = new HomePage(driver);
        
        logInfo("Verifying homepage title");
        String title = homePage.getPageTitle();
        Assert.assertTrue(title.contains("Trendyol"), "Homepage title verification failed");
        
        logPass("Homepage loaded successfully");
    }
}
```

---

## 📊 Test Execution

### Run Specific Test Suite
```bash
# Smoke tests
mvn clean test -Psmoke

# Regression tests
mvn clean test -Pregression

# E2E tests
mvn clean test -Pe2e
```

### Run with Different Browser
```bash
mvn clean test -Dbrowser=firefox
mvn clean test -Dbrowser=edge
```

### Run in Headless Mode
```bash
mvn clean test -Dheadless=true
```

### Run Specific Test Class
```bash
mvn clean test -Dtest=HomePageTests
```

### Run Specific Test Method
```bash
mvn clean test -Dtest=HomePageTests#testHomePageLoad
```

---

## 📈 Reports

After test execution:

1. **ExtentReports**: `reports/Trendyol_Test_Report_<timestamp>.html`
2. **TestNG Reports**: `test-output/index.html`
3. **Logs**: `logs/trendyol-automation.log`
4. **Screenshots**: `screenshots/` (on test failure)

---

## 🔧 Maven Commands Reference

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn clean test

# Run tests and skip compilation
mvn test

# Run specific profile
mvn clean test -P<profile-name>

# Run with custom TestNG XML
mvn clean test -DsuiteXmlFile=src/test/resources/testng-custom.xml

# Install to local repository
mvn clean install

# Generate site documentation
mvn site
```

---

## 🏗️ Framework Architecture

### Design Patterns Used

1. **Page Object Model (POM)**
   - Each page is represented as a Java class
   - Page elements and methods are encapsulated
   - Tests interact with pages, not raw Selenium

2. **Singleton Pattern**
   - WebDriver instances managed via DriverManager
   - Single instance per thread (ThreadLocal)
   - Prevents multiple browser windows

3. **Factory Pattern**
   - BrowserFactory for dynamic browser creation
   - Based on configuration, not hard-coded

---

## 🔍 Parallel Execution

Framework supports parallel execution at multiple levels:

### Class-Level (Default)
```xml
<suite name="Trendyol" parallel="classes" thread-count="3">
```

### Method-Level (Aggressive)
```xml
<suite name="Trendyol" parallel="methods" thread-count="5">
```

### Test-Level
```xml
<suite name="Trendyol" parallel="tests" thread-count="2">
```

**ThreadLocal** ensures thread-safety for WebDriver instances.

---

## 🐛 Debugging

### Enable Debug Logging
In `config.properties`:
```properties
log.level=DEBUG
```

### Print Configuration
```java
ConfigReader.printAllProperties();
```

### Manual Screenshot
```java
ScreenshotHelper.captureScreenshot(driver, "debug_screenshot");
```

---

## 🚀 CI/CD Integration

### Jenkins
```groovy
stage('Run Tests') {
    steps {
        sh 'mvn clean test -Psmoke'
    }
}
```

### GitHub Actions
```yaml
- name: Run Tests
  run: mvn clean test -Psmoke
```

---

## 📝 Best Practices

1. ✅ Always extend `BaseTest` for test classes
2. ✅ Use Page Objects for page interactions
3. ✅ Use explicit waits via `WaitHelper`
4. ✅ Log important actions via `logInfo()`, `logPass()`, `logFail()`
5. ✅ Never use `Thread.sleep()` - use explicit waits
6. ✅ Keep test data in constants or external files
7. ✅ One assertion per test method (where possible)
8. ✅ Clean, descriptive test method names
9. ✅ Use `@Test(description = "...")` for clarity
10. ✅ Never hard-code URLs, credentials, or timeouts

---

## 🤝 Contributing

1. Create feature branch
2. Write tests following framework patterns
3. Ensure all tests pass: `mvn clean test`
4. Submit pull request

---

## 📧 Contact

For questions or support, contact the QA team.

---

## 📜 License

Internal use only - Trendyol Automation Framework

---

**Built with ❤️ by QA Automation Team**
