
package java_cup;

/** This class contains version and authorship information. 
 *  It contains only static data elements and basically just a central 
 *  place to put this kind of information so it can be updated easily
 *  for each release.  
 *
 *  Version numbers used here are broken into 3 parts: major, minor, and 
 *  update, and are written as v<major>.<minor><update> (e.g. v0.9a).  
 *  Major numbers will change at the time of major reworking of some 
 *  part of the system.  Minor numbers for each public release or 
 *  change big enough to cause incompatibilities.  Finally update
 *  letter will be incremented for small bug fixes and changes that
 *  probably wouldn't be noticed by a user.  
 *
 * @version last updated: 1/7/96
 * @author  Scott Hudson
 */

public class version {
  /** String for the current version. */
  public static final String version_str = "v0.9d";

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The major version number. */
  public static final int major = 0;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The minor version number. */
  public static final int minor = 9;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The update letter. */
  public static final char update = 'd';

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Full title of the system */
  public static final String title_str = "Java(tm) CUP " + version_str;

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** Name of the author */
  public static final String author_str = "Scott E. Hudson";

  /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

  /** The command name normally used to invoke this program */ 
  public static final String program_name = "java_cup";
};
