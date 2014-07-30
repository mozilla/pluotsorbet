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

package com.sun.midp.pki.ocsp;

import javax.microedition.pki.Certificate;

/**
 * Validates the certificates.
 * This stubbed out implementation is used when OCSP is disabled.
 */
public class OCSPValidatorImpl implements OCSPValidator {
    /**
     * Retrieves the status of the given certificate.
     *
     * @param cert X.509 certificate status of which must be checked
     * @param issuerCert certificate of the trusted authority issued
     *                   the certificate given by cert
     * @return status of the certificate
     * @throws OCSPException if the OCSP Responder returned an error message
     */
    public int checkCertStatus(Certificate cert, Certificate issuerCert)
            throws OCSPException {
        return CertStatus.GOOD;
    }
}
