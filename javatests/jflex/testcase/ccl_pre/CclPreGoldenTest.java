// test: ccl2

package jflex.testcase.ccl_pre;

import java.io.File;
import jflex.testing.testsuite.golden.AbstractGoldenTest;
import jflex.testing.testsuite.golden.GoldenInOutFilePair;
import jflex.util.scanner.ScannerFactory;
import org.junit.Test;

/**
 * bug-test for [:jletter:] style predefined character classes (#467827)
 *
 * <p>Note: This test was generated from {@code jflex-testsuite-maven-plugin} test cases. The test
 * relies on golden files for testing, expecting the scanner to output logs on the {@code
 * System.out}. Please migrate to proper unit tests, as describe in <a
 * href="https://github.com/jflex-de/jflex/tree/master/javatests/jflex/testcase">
 * //javatest/jflex/testcase</a>.
 */
// TODO Migrate this test to proper unit tests.
public class CclPreGoldenTest extends AbstractGoldenTest {

  /** Creates a scanner conforming to the {@code ccl.flex} specification. */
  private final ScannerFactory<Ccl> scannerFactory = ScannerFactory.of(Ccl::new);

  private File testRuntimeDir = new File("javatests/jflex/testcase/ccl_pre");

  @Test
  public void goldenTest0() throws Exception {
    GoldenInOutFilePair golden =
        new GoldenInOutFilePair(
            new File(testRuntimeDir, "ccl-0.input"), new File(testRuntimeDir, "ccl-0.output"));
    compareSystemOutWith(golden);

    Ccl scanner = scannerFactory.createScannerForFile(golden.inputFile);
    scanner.yylex();
  }
}
