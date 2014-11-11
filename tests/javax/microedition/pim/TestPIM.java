package javax.microedition.pim;

import java.util.Enumeration;
import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestPIM implements Testlet {
    public void test(TestHarness th) {
        try {
            th.check(System.getProperty("microedition.pim.version") != null);

            PIM pimInst = PIM.getInstance();

            String[] contactLists = pimInst.listPIMLists(PIM.CONTACT_LIST);
            th.check(contactLists.length, 1);
            th.check(contactLists[0], "ContactList");

            ContactList contactList = (ContactList)pimInst.openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);

            Contact contact = contactList.createContact();
            th.check(contactList.isSupportedField(Contact.FORMATTED_NAME));
            th.check(contactList.isSupportedField(Contact.TEL));
            th.check(contactList.isSupportedField(Contact.UID));
            th.check(contactList.isSupportedAttribute(Contact.FORMATTED_NAME, Contact.ATTR_NONE));
            th.check(contactList.isSupportedAttribute(Contact.TEL, Contact.ATTR_NONE));
            th.check(contactList.isSupportedAttribute(Contact.UID, Contact.ATTR_NONE));
            contact.addString(Contact.UID, Contact.ATTR_NONE, "2");

            // Test reading all contacts
            Enumeration contacts = contactList.items();
            th.check(contacts.hasMoreElements());
            Contact foundContact = (Contact)contacts.nextElement();
            th.check(foundContact.getString(Contact.UID, Contact.ATTR_NONE), "1");
            th.check(contacts.hasMoreElements());
            foundContact = (Contact)contacts.nextElement();
            th.check(foundContact.getString(Contact.UID, Contact.ATTR_NONE), "2");
            th.check(!contacts.hasMoreElements());

            // Test filtering contacts
            contacts = contactList.items((PIMItem)contact);
            th.check(contacts.hasMoreElements());
            foundContact = (Contact)contacts.nextElement();
            th.check(foundContact.getString(Contact.TEL, Contact.ATTR_NONE), "+16505550102");
            th.check(foundContact.countValues(Contact.TEL), 2);
            th.check(!contacts.hasMoreElements());

            contactList.close();
        } catch (PIMException e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
