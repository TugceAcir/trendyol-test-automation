package listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.TimeUnit;

/**
 * TestListener - Listens to test execution events
 *
 * PURPOSE:
 * - Log test start/finish
 * - Calculate test duration
 * - Professional test output format
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);
    private long startTime;

    @Override
    public void onTestStart(ITestResult result) {
        startTime = System.currentTimeMillis();

        String testName = result.getMethod().getDescription();
        if (testName == null || testName.isEmpty()) {
            testName = result.getMethod().getMethodName();
        }

        logger.info("========================================");
        logger.info("TEST: {} - STARTING", testName);
        logger.info("========================================");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = System.currentTimeMillis() - startTime;
        String testName = result.getMethod().getDescription();
        if (testName == null || testName.isEmpty()) {
            testName = result.getMethod().getMethodName();
        }

        logger.info("========================================");
        logger.info("TEST: {} - PASSED", testName);
        logger.info("DURATION: {} seconds", String.format("%.2f", duration / 1000.0));
        logger.info("========================================");
        System.out.println(); // Empty line for readability
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = System.currentTimeMillis() - startTime;
        String testName = result.getMethod().getDescription();
        if (testName == null || testName.isEmpty()) {
            testName = result.getMethod().getMethodName();
        }

        logger.error("========================================");
        logger.error("TEST: {} - FAILED", testName);
        logger.error("DURATION: {} seconds", String.format("%.2f", duration / 1000.0));
        logger.error("ERROR: {}", result.getThrowable().getMessage());
        logger.error("========================================");
        System.out.println(); // Empty line
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getDescription();
        if (testName == null || testName.isEmpty()) {
            testName = result.getMethod().getMethodName();
        }

        logger.warn("========================================");
        logger.warn("TEST: {} - SKIPPED", testName);
        logger.warn("========================================");
        System.out.println();
    }
}