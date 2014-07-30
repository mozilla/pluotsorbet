
/**
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
 * 
 * These are constant defines both in native and Java layers.
 * NOTE: DO NOT EDIT. THIS FILE IS GENERATED. If you want to 
 * edit it, you need to modify the corresponding XML files.
 *
 * Patent pending.
 */
package com.sun.midp.configurator;

public class Constants {

    /**
     * Threshold for image cache storage
     */
    public final static int IMAGE_CACHE_THRESHOLD = (100*1024);

    /**
     * Whether your platform supports File System. Set to true when File 
     * System is not supported; set to false when File System is supported.
     */
    public final static boolean PORTING_NO_FILESYSTEM = false;

    /**
     * When false, Chameleon will not even attempt to load images for the UI.
     */
    public final static boolean CHAM_USE_IMAGES = true;

    /**
     * When false, Chameleon will not even attempt to load romized images for 
     * the UI.
     */
    public final static boolean CHAM_ROMIZED_IMAGES = true;

    /**
     * (Chameleon Only) display width
     */
    public final static int CHAM_WIDTH = 240;

    /**
     * (Chameleon Only) display height
     */
    public final static int CHAM_HEIGHT = 286;

    /**
     * (Chameleon Only) full display height
     */
    public final static int CHAM_FULLHEIGHT = 320;

    /**
     * (Chameleon Only) 2-D Traversal flag
     */
    public final static boolean TRAVERSAL2D = true;

    /**
     * The maximum number of events in the event queue
     */
    public final static int MAX_EVENTS = 20;

    /**
     * (Fixed) full display width
     */
    public final static int FULLWIDTH = 238;

    /**
     * (Fixed) full display height
     */
    public final static int FULLHEIGHT = 263;

    /**
     * (Fixed) normal width
     */
    public final static int NORMALWIDTH = 236;

    /**
     * (Fixed) normal height
     */
    public final static int NORMALHEIGHT = 244;

    /**
     * the width of the vertical scrollbar
     */
    public final static int VERT_SCROLLBAR_WIDTH = 16;

    /**
     * Color, in hex, to use as the erase color (the color used to clear away 
     * any existing pixels from a graphics object before painting the new 
     * pixels).
     */
    public final static int ERASE_COLOR = 0x00FFFFFF;

    /**
     * Whether the viewport is capable of scrolling horizontally
     */
    public final static boolean SCROLLS_HORIZONTAL = false;

    /**
     * Whether the viewport is capable of scrolling vertically
     */
    public final static boolean SCROLLS_VERTICAL = true;

    /**
     * Color depth the platform supports, in bits per pixel (bpp).
     */
    public final static int DISPLAY_DEPTH = 16;

    /**
     * Depth of the platform image (in bits). @note This is the bit depth of 
     * an image and it may not be the same as the bit depth of the entire 
     * system.
     */
    public final static int IMAGE_DEPTH = 32;

    /**
     * Whether the platform supports color: set it to true when color is 
     * supported; set it to false when color is not supported.
     */
    public final static boolean DISPLAY_IS_COLOR = true;

    /**
     * Number of color the platform supports
     */
    public final static int DISPLAY_NUM_COLOR = 65536;

    /**
     * Whether both the platform and MIDP implementation provide support for 
     * a pointer (press and release). Set to true when pointer is supported; 
     * set to false when pointer is not supported.
     */
    public final static boolean POINTER_SUPPORTED = true;

    /**
     * If your platform supports pointer motion and you want MIDP 
     * implementation provides support for pointer motions, set it to true; 
     * otherwise, set it to false so that MIDP runtime can provide this 
     * information to MIDlet.
     */
    public final static boolean MOTION_SUPPORTED = true;

    /**
     * If your platform support key repeat and you want MIDP implementation 
     * provides support for key repeat, set this to true, otherwise set it to 
     * false.
     */
    public final static boolean REPEAT_SUPPORTED = true;

    /**
     * Whether the platform and MIDP implementation supports 
     * double-buffering: set to true when double-buffering is supported; set 
     * to false when double-buffering is not supported.
     */
    public final static boolean IS_DOUBLE_BUFFERED = true;

    /**
     * Number of alpha channel levels the platform supports
     */
    public final static int ALPHA_LEVELS = 256;

    /**
     * key up code (internal)
     */
    public final static int KEYCODE_UP = -1;

    /**
     * key down code (internal)
     */
    public final static int KEYCODE_DOWN = -2;

    /**
     * key left code (internal)
     */
    public final static int KEYCODE_LEFT = -3;

    /**
     * key right code (internal)
     */
    public final static int KEYCODE_RIGHT = -4;

    /**
     * key select code (internal)
     */
    public final static int KEYCODE_SELECT = -5;

    /**
     * Turn on/off startup time measurement printouts.
     */
    public final static boolean MEASURE_STARTUP = false;

    /**
     * Java heap capacity in SVM mode
     */
    public final static int JAVA_HEAP_CAPACITY_SVM = 1024;

    /**
     * Java heap capacity in MVM mode
     */
    public final static int JAVA_HEAP_CAPACITY_MVM = (4*1024);

    /**
     * A value indicating that there is no need to change the current logging 
     * level.
     */
    public final static int LOG_CURRENT = -1;

    /**
     * Logging level used to report information that is not associated with 
     * any significant condition.
     */
    public final static int LOG_INFORMATION = 0;

    /**
     * Logging level used to report unexpected conditions that are typically 
     * fully recoverable.
     */
    public final static int LOG_WARNING = 1;

    /**
     * Logging level used to report unexpected conditions that are typically 
     * at least partially recoverable.
     */
    public final static int LOG_ERROR = 2;

    /**
     * Logging level used to report unexpected conditions that are typically 
     * not recoverable, or are catastrophic to the system.
     */
    public final static int LOG_CRITICAL = 3;

    /**
     * Logging level used to disable all output and all logging calls will be 
     * compiled out of the build.
     */
    public final static int LOG_DISABLED = 4;

    /**
     * Threshhold for the severity level below which log messages will not be 
     * recorded. Severity levels less than this value can be compiled out of 
     * the build.
     */
    public final static int REPORT_LEVEL = LOG_ERROR;

    /**
     * Flag to indicate whether tracing is enabled in the Logging service.
     */
    public final static boolean TRACE_ENABLED = true;

    /**
     * Flag allowing client code with assert statements to be compiled out of 
     * a production build.
     */
    public final static boolean ASSERT_ENABLED = true;

    /**
     * Default AMS memory reserved in MVM mode
     */
    public final static int AMS_MEMORY_RESERVED_MVM = 1024;

    /**
     * Default AMS memory limit in MVM mode
     */
    public final static int AMS_MEMORY_LIMIT_MVM = -1;

    /**
     * MIDlet start error status, when a MIDlet's constructor fails to catch 
     * a runtime exception
     */
    public final static int MIDLET_CONSTRUCTOR_FAILED = -1;

    /**
     * MIDlet start error status, when a MIDlet suite is not found
     */
    public final static int MIDLET_SUITE_NOT_FOUND = -2;

    /**
     * MIDlet start error status, when a class needed to create a MIDlet is 
     * not found.
     */
    public final static int MIDLET_CLASS_NOT_FOUND = -3;

    /**
     * MIDlet start error status, when intantiation exception is thrown 
     * during the intantiation of a MIDlet.
     */
    public final static int MIDLET_INSTANTIATION_EXCEPTION = -4;

    /**
     * MIDlet start error status, when illegal access exception is thrown 
     * during the intantiation of a MIDlet.
     */
    public final static int MIDLET_ILLEGAL_ACCESS_EXCEPTION = -5;

    /**
     * MIDlet start error status, when a MIDlet's constructor runs out of 
     * memory.
     */
    public final static int MIDLET_OUT_OF_MEM_ERROR = -6;

    /**
     * MIDlet start error status, when a the system cannot reserve enough 
     * resource to start a MIDlet suite.
     */
    public final static int MIDLET_RESOURCE_LIMIT = -7;

    /**
     * MIDlet start error status, when a MIDlet's isolate constructor throws 
     * to catch a runtime exception
     */
    public final static int MIDLET_ISOLATE_CONSTRUCTOR_FAILED = -9;

    /**
     * MIDlet start error status, when a MIDlet's ID is not given
     */
    public final static int MIDLET_ID_NOT_GIVEN = -10;

    /**
     * MIDlet start error status, when a MIDlet's class is not given
     */
    public final static int MIDLET_CLASS_NOT_GIVEN = -11;

    /**
     * MIDlet start error status, when a MIDlet's suite is disabled
     */
    public final static int MIDLET_SUITE_DISABLED = -12;

    /**
     * MIDlet start error status, when system has exceeded the maximum 
     * Isolate count.
     */
    public final static int MIDLET_ISOLATE_RESOURCE_LIMIT = -13;

    /**
     * MIDlet start error status, when that MIDlet is being updated.
     */
    public final static int MIDLET_INSTALLER_RUNNING = -14;

    /**
     * Reserved external application id for setting no MIDlet in foreground
     */
    public final static int MIDLET_APPID_NO_FOREGROUND = 0;

    /**
     * Number of times an install or delete notification has to be retried by 
     * the notification manager. Check the specification for the minimal 
     * allowed value.
     */
    public final static int MAX_INSTALL_DELETE_NOTIFICATION_RETRIES = 5;

    /**
     * The period in milliseconds after the first call to GameCanvas.flush() 
     * when any subsequent call to the method will be ignored. This is the 
     * performance optimization limiting too frequent calls to 
     * GameCanvas.flush(). 40 seems to be the optimum value. Be careful to 
     * enable the optimization as it may lead to the MIDP spec violation in 
     * some cases. The value set 0 disables the optimization.
     */
    public final static int MINIMUM_FLUSH_INTERVAL = 0;

    /**
     * The period in milliseconds of processing the last delayed call to 
     * GameCanvas.flush().
     */
    public final static int DELAYED_FLUSH_PERIOD = 80;

    /**
     * Maximal number of notifications about record store changes that can be 
     * placed into event queue of VM task.
     */
    public final static int RECORD_STORE_NOTIFICATION_QUEUE_SIZE = 10;

    /**
     * Allowed time in milliseconds to block record store changer until its 
     * notifications are acknowledged by receiver VM task.
     */
    public final static int RECORD_STORE_NOTIFICATION_TIMEOUT = 1000;

    /**
     * Number of attempts to send notification event about record store 
     * change in the case there is receiver VM task that cannot accept the 
     * event.
     */
    public final static int RECORD_STORE_NOTIFICATION_ATTEMPTS = 5;

    /**
     * Default URL to install dynamic components from. Provider-defined.
     */
    public final static String AMS_CMGR_DEFAULT_COMPONENT_URL = "http://host/x.jar";

    /**
     * Storage ID for the internal storage.
     */
    public final static int INTERNAL_STORAGE_ID = 0;

    /**
     * Unused storage ID.
     */
    public final static int UNUSED_STORAGE_ID = (-1);

    /**
     * Number of the supported storages: 2 for internal and only one 
     * external. This value should be changed if more than one external 
     * storage is supported.
     */
    public final static int MAX_STORAGE_NUM = 2;

    /**
     * Force binary application image generation during the suite installation
     */
    public final static boolean MONET_ENABLED = false;

    /**
     * Force verification of all MIDlet suite classes to be done only once 
     * during the suite installation
     */
    public final static boolean VERIFY_ONCE = false;

    /**
     * Defines whether suite JAR should be checked for changes since the 
     * suite installation.
     */
    public final static boolean VERIFY_SUITE_HASH = false;

    /**
     * Name of the system MIDlet to verify MIDlet suite classes
     */
    public final static String SUITE_VERIFIER_MIDLET = "";

    /**
     * Flag to indicate whether extended MIDlet attributes support is enabled.
     */
    public final static boolean EXTENDED_MIDLET_ATTRIBUTES_ENABLED = false;

    /**
     * Support of finger touch event
     */
    public final static boolean FINGER_TOUCH = false;

    /**
     * MAX_ISOLATES
     */
    public final static int MAX_ISOLATES = 4;

    /**
     * Default MIDletSuite memory reserved.
     */
    public final static int SUITE_MEMORY_RESERVED = 100;

    /**
     * Default MIDletSuite Memory Limit.
     */
    public final static int SUITE_MEMORY_LIMIT = -1;

    /**
     * Global resource limit for TCP client sockets
     */
    public final static int TCP_CLI_GLOBAL_LIMIT = 16;

    /**
     * Per suite resource limit for TCP client sockets
     */
    public final static int TCP_CLI_AMS_RESERVED = 2;

    /**
     * Per suite resource limit for TCP client sockets
     */
    public final static int TCP_CLI_AMS_LIMIT = (TCP_CLI_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for TCP client sockets
     */
    public final static int TCP_CLI_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for TCP client sockets
     */
    public final static int TCP_CLI_SUITE_LIMIT = (TCP_CLI_GLOBAL_LIMIT - TCP_CLI_AMS_RESERVED);

    /**
     * Global resource limit for TCP server sockets
     */
    public final static int TCP_SER_GLOBAL_LIMIT = 12;

    /**
     * Per suite resource limit for TCP server sockets
     */
    public final static int TCP_SER_AMS_RESERVED = 0;

    /**
     * Per suite resource limit for TCP server sockets
     */
    public final static int TCP_SER_AMS_LIMIT = (TCP_SER_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for TCP server sockets
     */
    public final static int TCP_SER_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for TCP server sockets
     */
    public final static int TCP_SER_SUITE_LIMIT = (TCP_SER_GLOBAL_LIMIT - TCP_SER_AMS_RESERVED);

    /**
     * Global resource limit for UDP sockets
     */
    public final static int UDP_GLOBAL_LIMIT = 12;

    /**
     * Per suite resource limit for UDP sockets
     */
    public final static int UDP_AMS_RESERVED = 0;

    /**
     * Per suite resource limit for UDP sockets
     */
    public final static int UDP_AMS_LIMIT = (UDP_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for UDP sockets
     */
    public final static int UDP_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for UDP sockets
     */
    public final static int UDP_SUITE_LIMIT = (UDP_GLOBAL_LIMIT - UDP_AMS_RESERVED);

    /**
     * Global resource limit for File handlers
     */
    public final static int FILE_GLOBAL_LIMIT = 30;

    /**
     * Per suite resource limit for File handlers
     */
    public final static int FILE_AMS_RESERVED = 6;

    /**
     * Per suite resource limit for File handlers
     */
    public final static int FILE_AMS_LIMIT = (FILE_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for File handlers
     */
    public final static int FILE_SUITE_RESERVED = 4;

    /**
     * Per suite resource limit for File handlers
     */
    public final static int FILE_SUITE_LIMIT = (FILE_GLOBAL_LIMIT - FILE_AMS_RESERVED);

    /**
     * Global resource limit for Audio channels
     */
    public final static int AUDIO_CHA_GLOBAL_LIMIT = 5;

    /**
     * Per suite resource limit for Audio channels
     */
    public final static int AUDIO_CHA_AMS_RESERVED = 0;

    /**
     * Per suite resource limit for Audio channels
     */
    public final static int AUDIO_CHA_AMS_LIMIT = (AUDIO_CHA_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for Audio channels
     */
    public final static int AUDIO_CHA_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for Audio channels
     */
    public final static int AUDIO_CHA_SUITE_LIMIT = (AUDIO_CHA_GLOBAL_LIMIT - AUDIO_CHA_AMS_RESERVED);

    /**
     * Global resource limit for mutable images
     */
    public final static int IMAGE_MUT_GLOBAL_LIMIT = 1200000;

    /**
     * Per suite resource limit for mutable images
     */
    public final static int IMAGE_MUT_AMS_RESERVED = 200000;

    /**
     * Per suite resource limit for mutable images
     */
    public final static int IMAGE_MUT_AMS_LIMIT = (IMAGE_MUT_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for mutable images
     */
    public final static int IMAGE_MUT_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for mutable images
     */
    public final static int IMAGE_MUT_SUITE_LIMIT = (IMAGE_MUT_GLOBAL_LIMIT - IMAGE_MUT_AMS_RESERVED);

    /**
     * Global resource limit for immutable images
     */
    public final static int IMAGE_IMMUT_GLOBAL_LIMIT = 1300000;

    /**
     * Per suite resource limit for immutable images
     */
    public final static int IMAGE_IMMUT_AMS_RESERVED = 700000;

    /**
     * Per suite resource limit for immutable images
     */
    public final static int IMAGE_IMMUT_AMS_LIMIT = (IMAGE_IMMUT_GLOBAL_LIMIT);

    /**
     * Per suite resource limit for immutable images
     */
    public final static int IMAGE_IMMUT_SUITE_RESERVED = 0;

    /**
     * Per suite resource limit for immutable images
     */
    public final static int IMAGE_IMMUT_SUITE_LIMIT = (IMAGE_IMMUT_GLOBAL_LIMIT - IMAGE_IMMUT_AMS_RESERVED);

}
