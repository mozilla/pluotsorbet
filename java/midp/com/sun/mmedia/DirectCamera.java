/*
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

import javax.microedition.media.Control;

public class DirectCamera extends DirectPlayer 
{
    private Control _cameraControl;
    private Control _exposureControl;
    private Control _flashControl;
    private Control _focusControl;
    private Control _snapshotControl;
    private Control _zoomControl;
    private Control _imgFmtControl;

    protected static final String JSR234_CAMERA_PACKAGE_NAME =
        "javax.microedition.amms.control.camera.";

    /**
     * It does not need data source
     */
    public DirectCamera() {
    }

    protected Control doGetControl(String type) {
        Control c = super.doGetControl(type);
        if (c == null) {
            if (type.startsWith(JSR234_CAMERA_PACKAGE_NAME)) {
                String camType = type.substring( 
                        JSR234_CAMERA_PACKAGE_NAME.length() );
                if( camType.equals( "CameraControl" ) )
                {
                    if( null == _cameraControl )
                    {
                        _cameraControl = 
                           Jsr234Proxy.getInstance().getCameraControl( this );
                    }
                    return _cameraControl;
                } else if( camType.equals( "ExposureControl" ) )
                {
                    if( null == _exposureControl )
                    {
                        _exposureControl = 
                          Jsr234Proxy.getInstance().getExposureControl( this );
                    }
                    return _exposureControl;
                } else if ( camType.equals( "FlashControl" ) )
                {
                    if( null == _flashControl )
                    {
                        _flashControl = 
                           Jsr234Proxy.getInstance().getFlashControl( this );
                    }
                    return _flashControl;
                } else if( camType.equals( "FocusControl" ) )
                {
                    if( null == _focusControl )
                    {
                        _focusControl = 
                           Jsr234Proxy.getInstance().getFocusControl( this );
                    }
                    return _focusControl;
                } else if( camType.equals( "SnapshotControl" ) )
                {
                    if( null == _snapshotControl )
                    {
                        _snapshotControl = 
                          Jsr234Proxy.getInstance().getSnapshotControl( this );
                    }
                    return _snapshotControl;
                } else if( camType.equals( "ZoomControl" ) )
                {
                    if( null == _zoomControl )
                    {
                        _zoomControl = 
                              Jsr234Proxy.getInstance().getZoomControl( this );
                    }
                    return _zoomControl;
                }
                
            } else if (type.equals( 
                    "javax.microedition.amms.control.ImageFormatControl" )) {
                if( null == _imgFmtControl )
                {
                    _imgFmtControl = 
                       Jsr234Proxy.getInstance().getImageFormatControl( this );
                }
                return _imgFmtControl;
            }
        }
        return c;
    }
}
