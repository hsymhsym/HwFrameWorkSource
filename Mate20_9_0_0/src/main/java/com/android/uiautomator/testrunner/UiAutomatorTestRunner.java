package com.android.uiautomator.testrunner;

import android.app.IInstrumentationWatcher;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Debug;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.test.RepetitiveTest;
import android.util.Log;
import com.android.uiautomator.core.ShellUiAutomatorBridge;
import com.android.uiautomator.core.Tracer;
import com.android.uiautomator.core.Tracer.Mode;
import com.android.uiautomator.core.UiAutomationShellWrapper;
import com.android.uiautomator.core.UiDevice;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.runner.BaseTestRunner;
import junit.textui.ResultPrinter;

public class UiAutomatorTestRunner {
    private static final int EXIT_EXCEPTION = -1;
    private static final int EXIT_OK = 0;
    private static final String HANDLER_THREAD_NAME = "UiAutomatorHandlerThread";
    private static final String LOGTAG = UiAutomatorTestRunner.class.getSimpleName();
    private final IAutomationSupport mAutomationSupport = new IAutomationSupport() {
        public void sendStatus(int resultCode, Bundle status) {
            UiAutomatorTestRunner.this.mWatcher.instrumentationStatus(null, resultCode, status);
        }
    };
    private boolean mDebug;
    private HandlerThread mHandlerThread;
    private boolean mMonkey;
    private Bundle mParams = null;
    private List<String> mTestClasses = null;
    private final List<TestListener> mTestListeners = new ArrayList();
    private UiDevice mUiDevice;
    private final FakeInstrumentationWatcher mWatcher = new FakeInstrumentationWatcher(this, null);

    private class FakeInstrumentationWatcher implements IInstrumentationWatcher {
        private final boolean mRawMode;

        private FakeInstrumentationWatcher() {
            this.mRawMode = true;
        }

        /* synthetic */ FakeInstrumentationWatcher(UiAutomatorTestRunner x0, AnonymousClass1 x1) {
            this();
        }

        public IBinder asBinder() {
            throw new UnsupportedOperationException("I'm just a fake!");
        }

        public void instrumentationStatus(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                if (UiAutomatorTestRunner.EXIT_OK != null) {
                    System.out.print(null);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            PrintStream printStream = System.out;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSTRUMENTATION_STATUS: ");
                            stringBuilder.append(key);
                            stringBuilder.append("=");
                            stringBuilder.append(results.get(key));
                            printStream.println(stringBuilder.toString());
                        }
                    }
                    PrintStream printStream2 = System.out;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("INSTRUMENTATION_STATUS_CODE: ");
                    stringBuilder2.append(resultCode);
                    printStream2.println(stringBuilder2.toString());
                }
                notifyAll();
            }
        }

        public void instrumentationFinished(ComponentName name, int resultCode, Bundle results) {
            synchronized (this) {
                if (UiAutomatorTestRunner.EXIT_OK != null) {
                    System.out.println(null);
                } else {
                    if (results != null) {
                        for (String key : results.keySet()) {
                            PrintStream printStream = System.out;
                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append("INSTRUMENTATION_RESULT: ");
                            stringBuilder.append(key);
                            stringBuilder.append("=");
                            stringBuilder.append(results.get(key));
                            printStream.println(stringBuilder.toString());
                        }
                    }
                    PrintStream printStream2 = System.out;
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append("INSTRUMENTATION_CODE: ");
                    stringBuilder2.append(resultCode);
                    printStream2.println(stringBuilder2.toString());
                }
                notifyAll();
            }
        }
    }

    private interface ResultReporter extends TestListener {
        void print(TestResult testResult, long j, Bundle bundle);

        void printUnexpectedError(Throwable th);
    }

    private class SimpleResultPrinter extends ResultPrinter implements ResultReporter {
        private final boolean mFullOutput;

        public SimpleResultPrinter(PrintStream writer, boolean fullOutput) {
            super(writer);
            this.mFullOutput = fullOutput;
        }

        public void print(TestResult result, long runTime, Bundle testOutput) {
            printHeader(runTime);
            if (this.mFullOutput) {
                printErrors(result);
                printFailures(result);
            }
            printFooter(result);
        }

        public void printUnexpectedError(Throwable t) {
            if (this.mFullOutput) {
                getWriter().printf("Test run aborted due to unexpected exeption: %s", new Object[]{t.getMessage()});
                t.printStackTrace(getWriter());
            }
        }
    }

    private class WatcherResultPrinter implements ResultReporter {
        private static final String REPORT_KEY_NAME_CLASS = "class";
        private static final String REPORT_KEY_NAME_TEST = "test";
        private static final String REPORT_KEY_NUM_CURRENT = "current";
        private static final String REPORT_KEY_NUM_ITERATIONS = "numiterations";
        private static final String REPORT_KEY_NUM_TOTAL = "numtests";
        private static final String REPORT_KEY_STACK = "stack";
        private static final String REPORT_VALUE_ID = "UiAutomatorTestRunner";
        private static final int REPORT_VALUE_RESULT_ERROR = -1;
        private static final int REPORT_VALUE_RESULT_FAILURE = -2;
        private static final int REPORT_VALUE_RESULT_START = 1;
        private final SimpleResultPrinter mPrinter;
        private final Bundle mResultTemplate = new Bundle();
        private final ByteArrayOutputStream mStream;
        String mTestClass = null;
        int mTestNum = UiAutomatorTestRunner.EXIT_OK;
        Bundle mTestResult;
        int mTestResultCode = UiAutomatorTestRunner.EXIT_OK;
        private final PrintStream mWriter;

        public WatcherResultPrinter(int numTests) {
            this.mResultTemplate.putString("id", REPORT_VALUE_ID);
            this.mResultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
            this.mStream = new ByteArrayOutputStream();
            this.mWriter = new PrintStream(this.mStream);
            this.mPrinter = new SimpleResultPrinter(this.mWriter, false);
        }

        public void startTest(Test test) {
            String testClass = test.getClass().getName();
            String testName = ((TestCase) test).getName();
            this.mTestResult = new Bundle(this.mResultTemplate);
            this.mTestResult.putString(REPORT_KEY_NAME_CLASS, testClass);
            this.mTestResult.putString(REPORT_KEY_NAME_TEST, testName);
            Bundle bundle = this.mTestResult;
            String str = REPORT_KEY_NUM_CURRENT;
            int i = this.mTestNum + 1;
            this.mTestNum = i;
            bundle.putInt(str, i);
            if (testClass == null || testClass.equals(this.mTestClass)) {
                this.mTestResult.putString("stream", "");
            } else {
                this.mTestResult.putString("stream", String.format("\n%s:", new Object[]{testClass}));
                this.mTestClass = testClass;
            }
            try {
                Method testMethod = test.getClass().getMethod(testName, new Class[UiAutomatorTestRunner.EXIT_OK]);
                if (testMethod.isAnnotationPresent(RepetitiveTest.class)) {
                    this.mTestResult.putInt(REPORT_KEY_NUM_ITERATIONS, ((RepetitiveTest) testMethod.getAnnotation(RepetitiveTest.class)).numIterations());
                }
            } catch (NoSuchMethodException e) {
            }
            UiAutomatorTestRunner.this.mAutomationSupport.sendStatus(1, this.mTestResult);
            this.mTestResultCode = UiAutomatorTestRunner.EXIT_OK;
            this.mPrinter.startTest(test);
        }

        public void addError(Test test, Throwable t) {
            this.mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace(t));
            this.mTestResultCode = REPORT_VALUE_RESULT_ERROR;
            this.mTestResult.putString("stream", String.format("\nError in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace(t)}));
            this.mPrinter.addError(test, t);
        }

        public void addFailure(Test test, AssertionFailedError t) {
            this.mTestResult.putString(REPORT_KEY_STACK, BaseTestRunner.getFilteredTrace((Throwable) t));
            this.mTestResultCode = REPORT_VALUE_RESULT_FAILURE;
            this.mTestResult.putString("stream", String.format("\nFailure in %s:\n%s", new Object[]{((TestCase) test).getName(), BaseTestRunner.getFilteredTrace((Throwable) t)}));
            this.mPrinter.addFailure(test, t);
        }

        public void endTest(Test test) {
            if (this.mTestResultCode == 0) {
                this.mTestResult.putString("stream", ".");
            }
            UiAutomatorTestRunner.this.mAutomationSupport.sendStatus(this.mTestResultCode, this.mTestResult);
            this.mPrinter.endTest(test);
        }

        public void print(TestResult result, long runTime, Bundle testOutput) {
            this.mPrinter.print(result, runTime, testOutput);
            testOutput.putString("stream", String.format("\nTest results for %s=%s", new Object[]{getClass().getSimpleName(), this.mStream.toString()}));
            this.mWriter.close();
            UiAutomatorTestRunner.this.mAutomationSupport.sendStatus(REPORT_VALUE_RESULT_ERROR, testOutput);
        }

        public void printUnexpectedError(Throwable t) {
            this.mWriter.println(String.format("Test run aborted due to unexpected exception: %s", new Object[]{t.getMessage()}));
            t.printStackTrace(this.mWriter);
        }
    }

    public void run(List<String> testClasses, Bundle params, boolean debug, boolean monkey) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable ex) {
                Log.e(UiAutomatorTestRunner.LOGTAG, "uncaught exception", ex);
                Bundle results = new Bundle();
                results.putString("shortMsg", ex.getClass().getName());
                results.putString("longMsg", ex.getMessage());
                UiAutomatorTestRunner.this.mWatcher.instrumentationFinished(null, UiAutomatorTestRunner.EXIT_OK, results);
                System.exit(UiAutomatorTestRunner.EXIT_EXCEPTION);
            }
        });
        this.mTestClasses = testClasses;
        this.mParams = params;
        this.mDebug = debug;
        this.mMonkey = monkey;
        start();
        System.exit(EXIT_OK);
    }

    protected void start() {
        TestCaseCollector collector = getTestCaseCollector(getClass().getClassLoader());
        try {
            ResultReporter resultPrinter;
            collector.addTestClasses(this.mTestClasses);
            if (this.mDebug) {
                Debug.waitForDebugger();
            }
            this.mHandlerThread = new HandlerThread(HANDLER_THREAD_NAME);
            this.mHandlerThread.setDaemon(true);
            this.mHandlerThread.start();
            UiAutomationShellWrapper automationWrapper = new UiAutomationShellWrapper();
            automationWrapper.connect();
            long startTime = SystemClock.uptimeMillis();
            TestResult testRunResult = new TestResult();
            String outputFormat = this.mParams.getString("outputFormat");
            List<TestCase> testCases = collector.getTestCases();
            Bundle testRunOutput = new Bundle();
            if ("simple".equals(outputFormat)) {
                resultPrinter = new SimpleResultPrinter(System.out, true);
            } else {
                resultPrinter = new WatcherResultPrinter(testCases.size());
            }
            try {
                automationWrapper.setRunAsMonkey(this.mMonkey);
                this.mUiDevice = UiDevice.getInstance();
                this.mUiDevice.initialize(new ShellUiAutomatorBridge(automationWrapper.getUiAutomation()));
                String traceType = this.mParams.getString("traceOutputMode");
                if (traceType != null) {
                    Mode mode = (Mode) Mode.valueOf(Mode.class, traceType);
                    if (mode == Mode.FILE || mode == Mode.ALL) {
                        String filename = this.mParams.getString("traceLogFilename");
                        if (filename != null) {
                            Tracer.getInstance().setOutputFilename(filename);
                        } else {
                            throw new RuntimeException("Name of log file not specified. Please specify it using traceLogFilename parameter");
                        }
                    }
                    Tracer.getInstance().setOutputMode(mode);
                }
                testRunResult.addListener(resultPrinter);
                for (TestListener listener : this.mTestListeners) {
                    testRunResult.addListener(listener);
                }
                for (TestCase testCase : testCases) {
                    prepareTestCase(testCase);
                    testCase.run(testRunResult);
                }
            } catch (Throwable th) {
                resultPrinter.print(testRunResult, SystemClock.uptimeMillis() - startTime, testRunOutput);
                automationWrapper.disconnect();
                automationWrapper.setRunAsMonkey(false);
                this.mHandlerThread.quit();
            }
            resultPrinter.print(testRunResult, SystemClock.uptimeMillis() - startTime, testRunOutput);
            automationWrapper.disconnect();
            automationWrapper.setRunAsMonkey(false);
            this.mHandlerThread.quit();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected TestCaseCollector getTestCaseCollector(ClassLoader classLoader) {
        return new TestCaseCollector(classLoader, getTestCaseFilter());
    }

    public UiAutomatorTestCaseFilter getTestCaseFilter() {
        return new UiAutomatorTestCaseFilter();
    }

    protected void addTestListener(TestListener listener) {
        if (!this.mTestListeners.contains(listener)) {
            this.mTestListeners.add(listener);
        }
    }

    protected void removeTestListener(TestListener listener) {
        this.mTestListeners.remove(listener);
    }

    protected void prepareTestCase(TestCase testCase) {
        ((UiAutomatorTestCase) testCase).setAutomationSupport(this.mAutomationSupport);
        ((UiAutomatorTestCase) testCase).setUiDevice(this.mUiDevice);
        ((UiAutomatorTestCase) testCase).setParams(this.mParams);
    }
}
