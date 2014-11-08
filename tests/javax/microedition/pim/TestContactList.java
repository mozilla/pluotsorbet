package javax.microedition.pim;

import java.util.Enumeration;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestContactList implements Testlet {
    public void test(TestHarness th) {
        try {
            PIM pimInst = PIM.getInstance();
            ContactList contactList = (ContactList)pimInst.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
            Contact contact = contactList.createContact();
            contact.addString(Contact.UID, Contact.ATTR_NONE, "aUniqueID");
        } catch (PIMException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
