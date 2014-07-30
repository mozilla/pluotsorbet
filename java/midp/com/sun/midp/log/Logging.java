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

package com.sun.midp.log;

import com.sun.midp.configurator.Constants;

/**
 * The purpose of the logging service is to provide a standard means
 * to report runtime information from within Java or native code.
 * The porting process is eased by having to modify one logging
 * service implementation in place of handling the ad hoc use of
 * <code>println()</code>, <code>printf()</code>, <code>putc()</code>,
 * and other functions currently used.
 *
 * An assert mechanism for Java code, implemented using the logging
 * service is also provided here for convenience.
 *
 * This class consists of the Java interface to the functionality
 * of the logging service.
 */
public class Logging extends LoggingBase {

    /**
     * Flag allowing client code with reporting levels less
     * than this to be compiled out of the build.  Callers
     * should use this flag as a way to remove bytecodes
     * related to unneeded levels of reporting from the
     * resulting classfiles.
     * For Example:
     * <pre><code>
     *     if (Logging.REPORT_LEVEL &lt;= severity) {
     *         Logging.report(Logging.&lt;severity&gt;,
     *                        LogChannels.&lt;channel&gt;,
     *                        "[meaningful message]");
     *     }
     * </code></pre>
     */
    public static final int REPORT_LEVEL = Constants.REPORT_LEVEL;

    /**
     * Flag allowing client code with assert statements
     * to be compiled out of a production build.  Clients of
     * the assertion service should wrap calls to the
     * <code>assert()</code> method to enable them to be
     * removed from builds when desired
     * <pre><code>
     *      if (Logging.ASSERT_ENABLED) {
     *          Logging.assertTrue([eval to boolean], "message");
     *      }
     * </code></pre>
     */
    public static final boolean ASSERT_ENABLED = true;

    /**
     * Flag to indicate whether tracing is enabled in the
     * Logging service. If the flag is <code>false</code>,
     * calls to the <code>trace()</code> method will have
     * no effect. Callers should use this flag as a type of
     * compile option to remove unnecessary bytecodes from
     * resulting classfiles.
     *
     * For example:
     * <code><pre>
     * } catch (Throwable t) {
     *     if (Logging.TRACE_ENABLED) {
     *         Logging.trace(t, "[meaningful message]");
     *     }
     * }
     * </pre></code>
     */
    public static final boolean TRACE_ENABLED   = false;

    /**
     * Loads the logging settings for the specified suite.
     *
     * @param suiteId ID of the suite for which the settings must be loaded
     */
    public static void initLogSettings(int suiteId) {
    }

    /**
     * Sets a new report level.
     *
     * @param newReportLevel new report level
     */
    public static void setReportLevel(int newReportLevel) {
    }

    /**
     * Enables or disables tracing.
     *
     * @param enabled > 0 - enable tracing, == 0 - disable it,
     *             &lt; 0 - don't change the current setting
     */
    public static void enableTrace(int enabled) {
    }

    /**
     * Enables or disables assertions.
     *
     * @param enabled > 0 - enable assertions, == 0 - disable it,
     *             &lt; 0 - don't change the current setting
     */
    public static void enableAssert(int enabled) {
    }
}
