/*
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

package com.sun.midp.security;

import com.sun.midp.log.Logging;





/**
 * A utility class that initializes internal security. Creates the internal
 * security token and supports its distribution to various packages around
 * the system that need it.
 * <p>
 * The security token is created here and can be passed to various
 * implementation classes around the system. There are two mechanisms
 * to pass the token. The fastest way is to pass the security token to
 * static methods of an implementation classes. These methods, typically
 * named <code>initSecurityToken()</code>, must each ensure they are called
 * only once. This technique works for classes other than those in public
 * API packages (that is, java.* and javax.*). This technique cannot be
 * used for class that reside in public API packages, since the
 * initialization method must be public and adding methods to public APIs
 * is prohibited. Also passing the token to lot of optional classes around
 * the system will cause the classes loading and static initialization,
 * which affects system performance and slowdowns system start up. That
 * is why only a few core system classes are handed with security token
 * over the explicit method call. For most of the other classes the
 * following alternative approach is used.
 * <p>
 * Any implementation class trusted by the internal security system can
 * request <code>SecurityInitializer</code> for security token. The check
 * whether a requester class is really trusted can be implemented in a
 * different ways, e.g. using static list of a trusted class names, or
 * using more specific features of the VM. The need in the optimized
 * implementation for the trusted classes check can be regarded at the
 * product porting time.
 * <p>
 * The approach with security token request on demand makes it possible
 * for numerous optional subsystem to be not loaded and initialized
 * until the very last moment when their functionality is really requested.
 * <p>
 * The important note about trusted classes check. In the case static list
 * of trusted class names is used for the check, it is important to prevent
 * ROMizer from these classes renaming at the build time. Otherwise security
 * system won't be able to identify requester classes by class name.
 */
public final class SecurityInitializer {

    /**
     * Internal implementation of the SecurityInitializer logic common
     * for other possible security initializer e.g. JSR specific ones.
     */
    private static SecurityInitializerImpl impl;

    /**
     * Request security token using trusted object instance.
     * Note that the impossibility to create trusted objects
     * for untrusted requesters is the only guarantee of
     * secure tokens dispatching.
     *
     * @param trusted object to check whether caller have the right to
     *   request for security token
     * @return in the case transferred object is really trusted for
     *   SecurityInitializer the internal security token is returned,
     *   otherwise SecurityException will be thrown
     * @throws SecurityException in the case internal security system
     *   is uninitialized or transfered trusted object in unknown to
     *   <code>SecurityInitializer</code>
     */
    public static SecurityToken requestToken(
            ImplicitlyTrustedClass trusted) throws SecurityException {










        return impl.requestToken(trusted);
    }

    /**
     * Explicitly init security token for core system classes
     * in the fastest possible way. The other optional classes
     * should request for security token on their own.
     */
    public static void initSystem() {
        SecurityToken internalSecurityToken =
            impl.internalSecurityToken;

        com.sun.midp.security.SecurityHandler.initSecurityToken(
            internalSecurityToken);
        com.sun.midp.midletsuite.MIDletSuiteStorage.initSecurityToken(
            internalSecurityToken);

        com.sun.midp.io.j2me.push.PushRegistryInternal.initSecurityToken(
            internalSecurityToken);

    }

    /**
     * Prevents creation of any instances.
     */
    private SecurityInitializer() {
    }

    /**
     * List of trusted class names known to internal security system.
     * Using the instances of these classes it is possible to receive
     * a security token, that means the developer of the classes is
     * reposnsible for impossibility to create them for user.
     */
    final static private String[] trustedClasses = new String[] {

        // A few public classes from javax.microedition package
        // also need SecurityToken, but their interface can't be
        // extended because of MIDP spec. The classes are requesting
        // for a token on their own in static initializers.

        // The main security token consumer causing security system
        // initialization over SecurityToken request
        "com.sun.midp.main.AbstractMIDletSuiteLoader$SecurityTrusted",





        





        



        "com.sun.midp.main.TrustedMIDletIcon$SecurityTrusted",

        
        "com.sun.midp.chameleon.skins.resources.SkinResourcesImpl$SecurityTrusted",
        

        



        "javax.microedition.rms.RecordStore$SecurityTrusted",
        "javax.microedition.midlet.MIDlet$SecurityTrusted",
        "javax.microedition.lcdui.Display$SecurityTrusted",
        "com.sun.midp.io.j2me.http.Protocol$SecurityTrusted",
        "com.sun.midp.io.j2me.datagram.Protocol$SecurityTrusted",

        




        



        
        "com.sun.midp.publickeystore.WebPublicKeyStore$SecurityTrusted",
        

        



        



        



        




        



        



        



        




        



        



        




        




        // Pass the security token to the i3 test framework.
        





        "com.sun.midp.util.isolate.SecurityTokenProvider$SecurityTrusted",
        "com.sun.midp.io.j2me.pipe.SecurityTokenProvider$SecurityTrusted",






        // MMAPI event handler stabbed in MIDP in the case of no JSR 135/234
        "com.sun.mmedia.MMEventHandler$SecurityTrusted",

        
        "com.sun.midp.main.AmsUtil$SecurityTrusted",
        "com.sun.midp.io.j2me.pipe.Protocol$SecurityTrusted",
        "com.sun.midp.installer.AutoTester$SecurityTrusted",

        






        "com.sun.midp.suspend.AbstractSubsystem$SecurityTrusted",
        "com.sun.midp.io.j2me.socket.Protocol$SecurityTrusted",
        "com.sun.midp.main.MIDletProxyList$SecurityTrusted"
    };

    /**
     * Creates the primary security token and internal imlementer object
     * for core <code>SecurityInitializer</code> class.
     */
    static {
        try {
            // As the first caller to create a token we can pass in null
            // for the security token.
            SecurityToken internalSecurityToken =
                new SecurityToken(null,
                    Permissions.forDomain(
                        Permissions.MANUFACTURER_DOMAIN_BINDING));

            impl = new SecurityInitializerImpl(
                internalSecurityToken, trustedClasses);

        } catch (Throwable t) {
            Logging.trace(t, "Fatal error while initializing security token");
            throw new RuntimeException(t.toString());
        }
    }
}
