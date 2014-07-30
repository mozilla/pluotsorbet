/*
 *
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.midp.appmanager;

import java.util.Vector;

import javax.microedition.lcdui.*;

import javax.microedition.midlet.*;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.InstallInfo;
import com.sun.midp.midletsuite.MIDletSuiteImpl;
import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.publickeystore.PublicKeyInfo;
import com.sun.midp.publickeystore.WebPublicKeyStore;

import com.sun.midp.security.Permissions;


/**
 * This class enabled the user to manage the state of Third Party certificate
 * authorities.
 */
public class CaManager extends MIDlet {
    /** Construct a CA manager MIDlet. */
    public CaManager() {
        Display display = Display.getDisplay(this);

        display.setCurrent(new CaForm(this, display));
    }

    /** Start; there are not resource to re-allocate. */
    public void startApp() {}

    /** Pause; there are no resources that need to be released. */
    public void pauseApp() {}

    /**
     * Destroy cleans up.
     *
     * @param unconditional is ignored; this object always
     * destroys itself when requested.
     */
    public void destroyApp(boolean unconditional) {}

    /** Exits this application. */
    void exit() {
        notifyDestroyed();
    }
}

/**
 * A list of certificate authorities, each with a check box next to it.
 * Check means enable, no check mean disabled.
 */
class CaForm extends Form implements CommandListener {

    /** MIDlet suite storage object. */
    private MIDletSuiteStorage suiteStorage;

    /** List of suites to disable. */
    private Vector disableSuites;

    /** List of suites to enable. */
    private Vector enableSuites;

    /** Command object for "Save" command for the form. */
    private Command confirmCmd =
        new Command(Resource.getString(ResourceConstants.SAVE),
                    Command.OK, 1);


    /** Command object for "Exit" command for splash screen. */
    private Command exitCmd =
        new Command(Resource.getString(ResourceConstants.EXIT),
                    Command.BACK, 1);

    /** Command object for "Save" command for the confirmation form. */
    private Command saveCmd =
        new Command(Resource.getString(ResourceConstants.SAVE),
                    Command.OK, 1);

    /** Command object for "Exit" command for splash screen. */
    private Command cancelCmd =
        new Command(Resource.getString(ResourceConstants.CANCEL),
                    Command.BACK, 1);

    /** List of CAs. */
    private Vector caList;

    /** Parent CA manager. */
    private CaManager parent;

    /** Parent display. */
    private Display display;

    /** CA choices. */
    private ChoiceGroup choices;

    /** The index of the first item in the choice group. */
    private int firstIndex;

    /**
     * Construct a certificate form from a list of CA's.
     *
     * @param theParent Parent CaManager object
     * @param theDisplay parent Display object
     */
    CaForm(CaManager theParent, Display theDisplay) {
        super("Certificate Authorities");

        String manufacturerCa = null;
        String operatorCa = null;
        StringBuffer label = new StringBuffer(80);
        Item item = null;

        parent = theParent;
        display = theDisplay;


        suiteStorage = MIDletSuiteStorage.getMIDletSuiteStorage();

        WebPublicKeyStore keystore = WebPublicKeyStore.getTrustedKeyStore();
        caList = new Vector();

        for (int i = 0; i < keystore.numberOfKeys(); i++) {
            PublicKeyInfo key = keystore.getKey(i);
            if (Permissions.MANUFACTURER_DOMAIN_BINDING.
                equals(key.getDomain())) {
                manufacturerCa = key.getOwner();
                continue;
            }

            if (Permissions.OPERATOR_DOMAIN_BINDING.
                equals(key.getDomain())) {
                operatorCa = key.getOwner();
                continue;
            }

            caList.addElement(new Ca(key.getOwner(), key.isEnabled()));
        }

        if (manufacturerCa != null) {
            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.MANUFACTURER));
            label.append(": ");

            item = new StringItem(label.toString(), manufacturerCa);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);
        }

        if (operatorCa != null) {
            label.setLength(0);
            label.append(
                Resource.getString(ResourceConstants.SERVICE_PROVIDER));
            label.append(": ");

            item = new StringItem(label.toString(), operatorCa);
            item.setLayout(Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_2);
            append(item);
        }

        if (caList.size() > 0) {
            label.setLength(0);
            label.append(Resource.getString(ResourceConstants.THIRD_PARTIES));
            label.append(": ");

            choices = new ChoiceGroup(label.toString(), ChoiceGroup.MULTIPLE);
            item = choices;

            firstIndex = addCa((Ca)caList.elementAt(0));
            for (int i = 1; i < caList.size(); i++) {
                addCa((Ca)caList.elementAt(i));
            }

            append(item);
        }

        if (item == null) {
            append(new StringItem(null,
                Resource.getString(ResourceConstants.AMS_NO_CA_FOUND)));
        } else {
            addCommand(confirmCmd);
        }

        addCommand(exitCmd);
        setCommandListener(this);
    }

    /**
     * Respond to a command issued on any Screen.
     *
     * @param c command activated by the user
     * @param s the Displayable the command was on.
     */
    public void commandAction(Command c, Displayable s) {
        if (c == confirmCmd) {
            confirmChanges();
            return;
        }

        if (c == exitCmd) {
            parent.exit();
            return;
        }

        if (c == saveCmd) {
            save();
            return;
        }

        if (c == cancelCmd) {
            display.setCurrent(this);
            return;
        }
    }

    /** Add a CA to the form. */
    private int addCa(Ca ca) {
        int index = choices.append(ca.name, null);

        choices.setSelectedIndex(index, ca.enabled);

        return index;
    }

    /** Confirm the changes with the user. */
    private void confirmChanges() {
        boolean saveNeeded = false;
        enableSuites = new Vector();
        disableSuites = new Vector();

        // Determine which CA's will be enabled.
        for (int i = 0; i < caList.size(); i++) {
            Ca ca = (Ca)caList.elementAt(i);
            ca.willBeEnabled = choices.isSelected(firstIndex + i);
        }

        for (int i = 0; i < caList.size(); i++) {
            Ca ca = (Ca)caList.elementAt(i);
            if (ca.willBeEnabled && !ca.enabled) {
                saveNeeded = true;
                addSuitesAuthorizedBy(ca, enableSuites);
            } else if (!ca.willBeEnabled && ca.enabled) {
                saveNeeded = true;
                addSuitesAuthorizedBy(ca, disableSuites);
            }
        }

        if (!saveNeeded) {
            parent.exit();
            return;
        }

        if (disableSuites.size() == 0) {
            // we only confirm the disabling of suites
            save();
            return;
        }

        StringBuffer toBeDisabled = new StringBuffer();
        toBeDisabled.append(
            Resource.getString(ResourceConstants.AMS_SUITES_TO_BE_DISABLED));
        for (int i = 0; i < disableSuites.size(); i++) {
            if (i > 0) {
                toBeDisabled.append(",\n");
            }

            toBeDisabled.append(
                ((MIDletSuite)disableSuites.elementAt(i)).
                    getProperty(MIDletSuiteImpl.SUITE_NAME_PROP));
        }

        Alert a = new Alert(
                      Resource.getString(ResourceConstants.AMS_CONFIRMATION),
                      toBeDisabled.toString(), null, AlertType.WARNING);
        a.setTimeout(Alert.FOREVER);
        a.addCommand(cancelCmd);
        a.addCommand(saveCmd);
        a.setCommandListener(this);
        display.setCurrent(a);
    }

    /** Save the enable status of Third Party certificate authorities. */
    private void save() {
        for (int i = 0; i < caList.size(); i++) {
            Ca ca = (Ca)caList.elementAt(i);
            if (ca.willBeEnabled && !ca.enabled) {
                WebPublicKeyStore.enableCertAuthority(ca.name);
            } else if (!ca.willBeEnabled && ca.enabled) {
                WebPublicKeyStore.disableCertAuthority(ca.name);
            }
        }

        disableSuites(disableSuites);
        enableSuites(enableSuites);

        display.setCurrent(null);
        parent.exit();
    }

    /**
     * Get a list of suites authorized by a CA.
     *
     * @param ca desired CA
     *
     * @return list of suites
     */
    private void addSuitesAuthorizedBy(Ca ca, Vector suites) {
        int[] suiteIds = null;
        MIDletSuite midletSuite = null;
        InstallInfo installInfo = null;

        suiteIds = suiteStorage.getListOfSuites();

        for (int i = 0; i < suiteIds.length; i++) {
            try {
                midletSuite = suiteStorage.getMIDletSuite(suiteIds[i], false);
            } catch (Throwable t) {
                continue;
            }

            installInfo = ((MIDletSuiteImpl)midletSuite).getInstallInfo();

            String[] authPath = installInfo.getAuthPath();

            if (authPath != null &&
                ca.name.equals(authPath[0])) {
                    suites.addElement(midletSuite);
                continue;
            }

            midletSuite.close();
        }
    }

    /**
     * Disable a list suites. Closes each suite.
     *
     * @param suites list of MIDlet suites
     */
    private void disableSuites(Vector suites) {
        MIDletSuite midletSuite = null;

        for (int i = 0; i < suites.size(); i++) {
            midletSuite = (MIDletSuite)suites.elementAt(i);
            try {
                suiteStorage.disable(midletSuite.getID());
            } catch (Throwable t) {
                // nothing can be done
            }

            midletSuite.close();
        }
    }

    /**
     * Enable a list suites. Closes each suite.
     *
     * @param suites list of MIDlet suites
     */
    private void enableSuites(Vector suites) {
        MIDletSuite midletSuite = null;

        for (int i = 0; i < suites.size(); i++) {
            midletSuite = (MIDletSuite)suites.elementAt(i);
            try {
                suiteStorage.enable(midletSuite.getID());
            } catch (Throwable t) {
                // nothing can be done
            }

            midletSuite.close();
        }
    }
}

/**
 * Represents a CA.
 */
class Ca {
    /** Name of the CA. */
    String name;

    /** If true the CA is enabled. */
    boolean enabled;

    /** If true the CA will be enabled after the user presses "save". */
    boolean willBeEnabled;

    /**
     * Construct a CA.
     *
     * @param theName name of the CA
     * @param isEnabled true if the CA is enabled
     */
    Ca(String theName, boolean isEnabled) {
        name = theName;
        enabled = isEnabled;
    }
}
