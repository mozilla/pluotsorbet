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

package com.sun.mmedia;
import  javax.microedition.media.*;
import  javax.microedition.media.protocol.SourceStream;
import java.io.IOException;

/**
 * The thread that's downloads media data
 *
 */
class MediaDownload {
    /**
     * the stream instance
     */
    private SourceStream stream;
    private long contLength = -1;
    private int packetSize = 0;
    private int javaBufSize = 0;
    private byte[] buffer = null;
    private boolean eom = false;
    private int hNative;
    private boolean needMoreData = false;
    private Thread downloadThread = null;
    private boolean stopDownloadFlag = false;


    // get java buffer size to determine media format
    static native int nGetJavaBufferSize(int handle);
    // get first packet size to determine media format
    static native int nGetFirstPacketSize(int handle);
    // buffering media data
    static native int nBuffering(int handle, byte[] buffer, int offset, int size);
    // ask Native Player if it needs more data immediatelly
    static native boolean nNeedMoreDataImmediatelly(int hNative);    
    // Provide whole media content size, if known
    static native void nSetWholeContentSize(int hNative, long contentSize);

    /**
     * The constructor
     *
     * @param  stream  the instance of stream.
     */
    MediaDownload(int hNative, SourceStream stream) {
        this.hNative = hNative;
        this.stream = stream;
        eom = false;
        contLength = -1;
    }

    void deallocate() {
        stopDownload();
        eom = false;
        contLength = -1;
        buffer = null;
        javaBufSize = 0;
        packetSize = 0;
    }
    
    /**
     * 
     */
    void fgDownload() throws IOException, MediaException {
        download(false);
    }

    /**
     * 
     */
    void bgDownload() {
        if (!eom) {
            downloadThread = new Thread() {
                public void run() {
                    try {
                        download(true);
                    } catch (Exception e) {
                    }
                }
            };
            downloadThread.start();
        }
    }
    
    synchronized void continueDownload() {
        needMoreData = true;
        notifyAll();
    }

    void stopDownload() {
        if (downloadThread != null && downloadThread.isAlive()) {
           stopDownloadFlag = true;
           try {
               downloadThread.join();
           } catch(InterruptedException ex) {;}
           stopDownloadFlag = false;
           downloadThread = null;
           needMoreData = false;
        }
    }

    private synchronized void download( boolean inBackground ) throws MediaException, IOException {
        int roffset = 0;
        int woffset = 0;

        if (contLength == -1) {
            contLength = stream.getContentLength();
            if (contLength > 0) {
                nSetWholeContentSize(hNative, contLength);
            }
        }

        int newJavaBufSize = nGetJavaBufferSize(hNative);
        packetSize  = nGetFirstPacketSize(hNative);

        if(packetSize > 0 && !eom) {
            
            if (newJavaBufSize < packetSize) {
                newJavaBufSize = packetSize;
            }
            
            if (buffer == null || newJavaBufSize > buffer.length) {
                do {
                    try {
                        buffer = new byte[ newJavaBufSize ];
                    } catch(OutOfMemoryError er) {
                        if (newJavaBufSize == packetSize) {
                            throw new MediaException("Not enough memory");
                        } else {
                            newJavaBufSize = newJavaBufSize/2;
                            if (newJavaBufSize < packetSize) {
                                newJavaBufSize = packetSize;
                            }
                        }
                    };
                }while (buffer == null);

                javaBufSize = newJavaBufSize;
            }

            if (inBackground) {
                woffset = bgDownloadAndWait(woffset);
            }

            if (!stopDownloadFlag) {
                do {
                    int num_read = woffset - roffset;
                    int ret;
                    if (num_read > packetSize) {
                        num_read = packetSize;
                    }
                    if (num_read < packetSize && !eom) {
                        if ((roffset + packetSize) > javaBufSize) {
                            woffset = moveBuff(roffset, woffset);
                            roffset = 0;
                        }
                        do {
                            ret = stream.read(buffer, woffset, packetSize-num_read);
                            if (ret == -1) {
                                eom = true;
                                break;
                            }
                            num_read += ret;
                            woffset += ret;
                        }while(num_read<packetSize);
                    }
                
                    packetSize = nBuffering(hNative, buffer, roffset, num_read);
                    roffset += num_read;
                    if (packetSize == -1) {
                        packetSize = 0;
                        needMoreData = false;
                        throw new MediaException("Error data buffering or encoding");
                    } else if (packetSize > javaBufSize){
                        if ((woffset - roffset)==0) {
                            javaBufSize = packetSize;
                            buffer = new byte[ javaBufSize ];
                        } else {
                            javaBufSize = packetSize;
                            byte[] b = new byte[ javaBufSize ];
                            for (int i=0, j=roffset; j<woffset; i++, j++) {
                                b[i] = buffer[j];
                            }
                            buffer = b;
                            woffset -= roffset;
                            roffset = 0;
                        }
                    }
                    if (roffset == woffset) {
                        roffset = 0;
                        woffset = 0;
                        if (eom) {
                            break;
                        }   
                    }
                    needMoreData = nNeedMoreDataImmediatelly(hNative);
                    if (inBackground && !needMoreData) {
                        woffset = moveBuff(roffset, woffset);
                        roffset = 0;
                        woffset = bgDownloadAndWait(woffset);
                    }
                }while (needMoreData && !stopDownloadFlag);
            }
            if (eom) {
                packetSize = nBuffering(hNative, null, 0, 0);
                needMoreData = false;
            }
            buffer = null;
        }
    }

    private int moveBuff(int roffset, int woffset) {
        for (int i=0, j=roffset; j<woffset; i++, j++) {
            buffer[i] = buffer[j];
        }
        return woffset-roffset;
    }

    private int bgDownloadAndWait(int offset) throws IOException {
        while (!needMoreData && !stopDownloadFlag) {
            if (offset<javaBufSize && !eom) {
                int num_read = packetSize;
                if (offset + num_read >javaBufSize) {
                    num_read = javaBufSize - offset;
                }
                int ret = stream.read(buffer, offset, num_read);
                if (ret == -1) {
                    eom = true;
                } else {
                    offset += ret;
                }
            } else {
                try {
                    wait(500);
                } catch (InterruptedException e) {
                }
            }
        }
        return offset;
    }
}

