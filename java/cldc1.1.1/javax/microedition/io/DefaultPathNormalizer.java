/*
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.microedition.io;

import java.util.Stack;

/**
 * Implements default path normalization, used for http, 
 * https and file protocols.
 */
class DefaultPathNormalizer implements PathNormalizer {
  public String normalize(String path) {
    if ("".equals(path)) {
      return "/";
    }

    // Remove Dot Segments, as specified by RFC 3986, section 5.2.4
    {
      Stack segments = new Stack();
      int p = 0;
      do {
        int q = path.indexOf("/", p + 1);
        if (q == -1) {
          q = path.length();
        }

        String segment = path.substring(p, q);
        if (".".equals(segment) || "..".equals(segment)) {
          // Skip following slash
          q++;
          segment = null;
        } else if ("/.".equals(segment) || "/..".equals(segment)) {
          if (!segments.empty() && "/..".equals(segment)) {
            segments.pop();
          }

          if (q < path.length()) {
            segment = null;
          } else {
            segment = "/";
          } 
        }

        if (segment != null) {
          segments.push(segment);
        }
        p = q;
      } while (p < path.length());

      String normalizedPath = "";

      while (!segments.empty()) {
        String s = (String)segments.pop();
        normalizedPath = s + normalizedPath;
      }

      return normalizedPath;
    }
  }
}
