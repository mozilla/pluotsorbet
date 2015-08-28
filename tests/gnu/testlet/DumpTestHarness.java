package gnu.testlet;
import gnu.testlet.TestHarness;

public class DumpTestHarness extends TestHarness {
    public int pass = 0;
    public int fail = 0;
    private String note = "";

    public void check(boolean ok) {
        if (ok) {
            pass++;
            System.out.println("PASS " + note);
        } else {
            fail++;
            System.out.println("FAIL " + note);
        }
        note = "";
    }

    public void todo(boolean ok) { throw new UnsupportedOperationException(); }
    public void debug(String msg) { }
    public void setNote(String note) {
        this.note = note;
    }
}
