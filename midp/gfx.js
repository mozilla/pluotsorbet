/* -*- Mode: Java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*- */
/* vim: set shiftwidth=4 tabstop=4 autoindent cindent expandtab: */

'use strict';

var currentlyFocusedTextEditor;
var GFX = (function(Native) {
    var screenContextInfo = new ContextInfo(MIDP.context2D.canvas.width, MIDP.context2D.canvas.height, document.getElementById("canvas").getContext("2d"));

    var tempContext = document.createElement("canvas").getContext("2d");
    tempContext.canvas.width = 0;
    tempContext.canvas.height = 0;

    function ContextInfo(w, h, context) {
      if (context) {
          this.context = context;
      } else {
          this.context = document.createElement("canvas").getContext("2d");
      }
      this.context.canvas.width = w;
      this.context.canvas.height = h;
      this.context.save();

      this.applied = {
          transX: 0,
          transY: 0,
          clipX1: 0,
          clipY1: 0,
          clipX2: w,
          clipY2: h,
          font: this.context.font,
          pixel: 0xFF000000,
      }
    }

    ContextInfo.prototype.resize = function(w, h) {
        this.context.canvas.width = w;
        this.context.canvas.height = h;
        this.context.save();

        this.applied.transX = 0;
        this.applied.transY = 0;
        this.applied.clipX1 = 0;
        this.applied.clipY1 = 0;
        this.applied.clipX2 = w;
        this.applied.clipY2 = h;
        this.applied.font = this.context.font;
        // Canvas context `fillStyle` and `strokeStyle` are initially
        // the string value "#000000" which is fully opaque black
        this.applied.pixel = 0xFF000000;
    }

    ContextInfo.prototype.applyGraphics = function(g) {
        var transX = g.klass.classInfo.getField("I.transX.I").get(g);
        var transY = g.klass.classInfo.getField("I.transY.I").get(g);
        var clipX1 = g.klass.classInfo.getField("I.clipX1.S").get(g);
        var clipY1 = g.klass.classInfo.getField("I.clipY1.S").get(g);
        var clipX2 = g.klass.classInfo.getField("I.clipX2.S").get(g);
        var clipY2 = g.klass.classInfo.getField("I.clipY2.S").get(g);
        var font = g.klass.classInfo.getField("I.currentFont.Ljavax/microedition/lcdui/Font;").get(g);
        var pixel = g.klass.classInfo.getField("I.pixel.I").get(g);

        if (clipX1 < this.applied.clipX1 || clipY1 < this.applied.clipY1 || clipX2 > this.applied.clipX2 || clipY2 > this.applied.clipY2) {
            this.context.restore();
            this.context.save();

            this.applied.transX = 0;
            this.applied.transY = 0;
            this.applied.clipX1 = 0;
            this.applied.clipY1 = 0;
            this.applied.clipX2 = this.context.canvas.width;
            this.applied.clipY2 = this.context.canvas.height;
            this.applied.pixel = 0xFF000000;
        }

        this.context.translate(transX - this.applied.transX, transY - this.applied.transY);
        this.applied.transX = transX;
        this.applied.transY = transY;

        if (clipX1 != this.applied.clipX1 || clipY1 != this.applied.clipY1 || clipX2 != this.applied.clipX2 || clipY2 != this.applied.clipY2) {
            this.context.beginPath();
            this.context.rect(clipX1 - this.applied.transX, clipY1 - this.applied.transY, clipX2 - clipX1, clipY2 - clipY1);
            this.context.clip();

            this.applied.clipX1 = clipX1;
            this.applied.clipY1 = clipY1;
            this.applied.clipX2 = clipX2;
            this.applied.clipY2 = clipY2;
        }

        if (this.context.font != font.css) {
          this.context.font = font.css;
        }

        if (this.applied.pixel != pixel) {
          this.context.fillStyle = this.context.strokeStyle = util.abgrIntToCSS(pixel);
          this.applied.pixel = pixel;
        }
    }

    Native["com/sun/midp/lcdui/DisplayDeviceContainer.getDisplayDevicesIds0.()[I"] = function() {
        var ids = J2ME.newIntArray( 1);
        ids[0] = 1;
        return ids;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayName0.(I)Ljava/lang/String;"] = function(id) {
        return null;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPrimary0.(I)Z"] = function(id) {
        console.warn("DisplayDevice.isDisplayPrimary0.(I)Z not implemented (" + id + ")");
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isbuildInDisplay0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getDisplayCapabilities0.(I)I"] = function(id) {
        return 0x3ff;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenSupported0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.isDisplayPenMotionSupported0.(I)Z"] = function(id) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.reverseOrientation0.(I)Z"] = function(id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getReverseOrientation0.(I)Z"] = function(id) {
        return 0;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenWidth0.(I)I"] = function(id) {
        return MIDP.context2D.canvas.width;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.getScreenHeight0.(I)I"] = function(id) {
        return MIDP.context2D.canvas.height;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.displayStateChanged0.(II)V"] = function(hardwareId, state) {
        console.warn("DisplayDevice.displayStateChanged0.(II)V not implemented (" + hardwareId + ", " + state + ")");
    };

    Native["com/sun/midp/lcdui/DisplayDevice.gainedForeground0.(II)V"] = function(hardwareId, displayId) {
        document.getElementById("background-screen").style.display = "none";
        document.getElementById("splash-screen").style.display = "none";

        asyncImpl("V", emoji.loadData());

        if (profile === 2) {
          // Use setTimeout to make sure our profiling enter/leave stack is not unpaired.
          setTimeout(function () {
            stopAndSaveTimeline();
          }, 0);
        }
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.vibrate0.(IZ)Z"] = function(displayId, on) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDeviceAccess.isBacklightSupported0.(I)Z"] = function(displayId) {
        return 1;
    };

    Native["com/sun/midp/lcdui/DisplayDevice.refresh0.(IIIIII)V"] = function() {
        // This is a no-op: The foreground display gets drawn directly
        // to the screen.
    }

    function swapRB(pixel) {
        return (pixel & 0xff00ff00) | ((pixel >> 16) & 0xff) | ((pixel & 0xff) << 16);
    }

    function swapRBAndSetAlpha(pixel) {
        return swapRB(pixel) | 0xff000000;
    }

    function ABGRToARGB(abgrData, argbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                argbData[j++] = swapRB(abgrData[i++]);
            }
        }
    }

    function ABGRToARGB4444(abgrData, argbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                var abgr = abgrData[i++];
                argbData[j++] = (abgr & 0xF0000000) >>> 16 |
                                (abgr & 0x000000F0) << 4 |
                                (abgr & 0x0000F000) >> 8 |
                                (abgr & 0x00F00000) >>> 20;
            }
        }
    }

    var ABGRToRGB565_R_MASK = parseInt("000000000000000011111000", 2);
    var ABGRToRGB565_G_MASK = parseInt("000000001111110000000000", 2);
    var ABGRToRGB565_B_MASK = parseInt("111110000000000000000000", 2);

    function ABGRToRGB565(abgrData, rgbData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; y++) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; x++) {
                var abgr = abgrData[i++];
                rgbData[j++] = (abgr & ABGRToRGB565_R_MASK) << 8 |
                               (abgr & ABGRToRGB565_G_MASK) >>> 5 |
                               (abgr & ABGRToRGB565_B_MASK) >>> 19;
            }
        }
    }

    function ARGBToABGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                abgrData[i++] = swapRB(argbData[j++]);
            }
        }
    }

    function ARGBTo1BGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                abgrData[i++] = swapRB(argbData[j++]) | 0xFF000000;
            }
        }
    }

    function ARGB4444ToABGR(argbData, abgrData, width, height, offset, scanlength) {
        var i = 0;
        for (var y = 0; y < height; ++y) {
            var j = offset + y * scanlength;

            for (var x = 0; x < width; ++x) {
                var argb = argbData[j++];
                abgrData[i++] = (argb & 0xF000) << 16 |
                                (argb & 0x0F00) >>> 4 |
                                (argb & 0x00F0) << 8 |
                                (argb & 0x000F) << 20;
            }
        }
    }

    function initImageData(imageData, width, height) {
        if (imageData.contextInfo) {
          console.error("initImageData called on already-initialized `ImageData`");
        }

        imageData.contextInfo = new ContextInfo(width, height);
        imageData.width = width;
        imageData.height = height;
        return imageData.contextInfo.context;
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeImage.(Ljavax/microedition/lcdui/ImageData;[BII)V"] =
    function(imageData, bytes, offset, length) {
        var ctx = $.ctx;
        asyncImpl("V", new Promise(function(resolve, reject) {
            var blob = new Blob([bytes.subarray(offset, offset + length)], { type: "image/png" });
            var img = new Image();
            img.src = URL.createObjectURL(blob);
            img.onload = function() {
                var context = initImageData(imageData, img.naturalWidth, img.naturalHeight);
                context.drawImage(img, 0, 0, img.naturalWidth, img.naturalHeight, 0, 0, img.naturalWidth, img.naturalHeight);

                URL.revokeObjectURL(img.src);
                resolve();
            }
            img.onerror = function(e) {
               URL.revokeObjectURL(img.src);
               ctx.setAsCurrentContext();
               reject($.newIllegalArgumentException("error decoding image"));
            }
        }));
    };

    function copyImage(c, texture, sx, sy, width, height, transform, x, y) {
        if (transform !== TRANS_NONE) {
            c.save();
        }

        switch (transform) {
            case TRANS_ROT90:
                y -= height;
                c.rotate(Math.PI / 2);
                break;
            case TRANS_ROT180:
                x -= width;
                y -= height;
                c.rotate(Math.PI);
                break;
            case TRANS_ROT270:
                x -= width;
                c.rotate(Math.PI * 1.5);
                break;
            case TRANS_MIRROR:
                x -= height;
                c.scale(-1, 1);
                break;
            case TRANS_MIRROR_ROT90:
                x -= width;
                y -= height;
                c.scale(-1, 1);
                c.rotate(MATH.PI * 1.5);
                break;
            case TRANS_MIRROR_ROT180:
                y -= height;
                c.scale(-1, 1);
                c.rotate(MATH.PI);
                break;
            case TRANS_MIRROR_ROT270:
                c.scale(-1, 1);
                c.rotate(MATH.PI / 2);
                break;
        }

        c.drawImage(texture, sx, sy, width, height, x, y, width, height);

        if (transform !== TRANS_NONE) {
            c.restore();
        }
    }

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDataRegion.(Ljavax/microedition/lcdui/ImageData;Ljavax/microedition/lcdui/ImageData;IIIIIZ)V"] =
    function(dataDest, dataSource, x, y, width, height, transform, isMutable) {
        var c = initImageData(dataDest, width, height);
        copyImage(c, dataSource.contextInfo.context.canvas, 0, 0, width, height, transform, x, y);
        dataDest.klass.classInfo.getField("I.isMutable.Z").set(dataDest, isMutable);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDataCopy.(Ljavax/microedition/lcdui/ImageData;Ljavax/microedition/lcdui/ImageData;)V"] =
    function(dest, source) {
        var srcCanvas = source.contextInfo.context.canvas;
        var context = initImageData(dest, srcCanvas.width, srcCanvas.height);
        context.drawImage(srcCanvas, 0, 0, srcCanvas.width, srcCanvas.height, 0, 0, srcCanvas.width, srcCanvas.height);
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createMutableImageData.(Ljavax/microedition/lcdui/ImageData;II)V"] =
    function(imageData, width, height) {
        var context = initImageData(imageData, width, height);
        var oldStyle = context.fillStyle;
        context.fillStyle = "rgb(255,255,255)"; // white
        context.fillRect(0, 0, width, height);
        context.fillStyle = oldStyle;
    };

    Native["javax/microedition/lcdui/ImageDataFactory.createImmutableImageDecodeRGBImage.(Ljavax/microedition/lcdui/ImageData;[IIIZ)V"] =
    function(imageData, rgbData, width, height, processAlpha) {
        var context = initImageData(imageData, width, height);
        var ctxImageData = context.createImageData(width, height);
        var abgrData = new Int32Array(ctxImageData.data.buffer);

        var converterFunc = processAlpha ? ARGBToABGR : ARGBTo1BGR;
        converterFunc(rgbData, abgrData, width, height, 0, width);

        context.putImageData(ctxImageData, 0, 0);
    };

    Native["javax/microedition/lcdui/ImageData.getRGB.([IIIIIII)V"] = function(rgbData, offset, scanlength, x, y, width, height) {
        var abgrData = new Int32Array(this.contextInfo.context.getImageData(x, y, width, height).data.buffer);
        ABGRToARGB(abgrData, rgbData, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectUtils.makeMutable.(Ljavax/microedition/lcdui/Image;)V"] = function(image) {
        var imageData = image.imageData;
        imageData.klass.classInfo.getField("I.isMutable.Z").set(imageData, 1);
    };

    Native["com/nokia/mid/ui/DirectUtils.setPixels.(Ljavax/microedition/lcdui/Image;I)V"] = function(image, argb) {
        var width = image.width;
        var height = image.height;
        var imageData = image.imageData;

        if (imageData.width != width || imageData.height != height) {
            imageData.contextInfo.resize(width, height);
            imageData.width = width;
            imageData.height = height;
        }

        var ctx = imageData.contextInfo.context;

        var ctxImageData = ctx.createImageData(width, height);
        var pixels = new Int32Array(ctxImageData.data.buffer);

        var color = swapRB(argb);

        var i = 0;
        for (var y = 0; y < height; ++y) {
            for (var x = 0; x < width; ++x) {
                pixels[i++] = color;
            }
        }

        ctx.putImageData(ctxImageData, 0, 0);
    };

    var FACE_SYSTEM = 0;
    var FACE_MONOSPACE = 32;
    var FACE_PROPORTIONAL = 64;
    var STYLE_PLAIN = 0;
    var STYLE_BOLD = 1;
    var STYLE_ITALIC = 2;
    var STYLE_UNDERLINED = 4;
    var SIZE_SMALL = 8;
    var SIZE_MEDIUM = 0;
    var SIZE_LARGE = 16;

    Native["javax/microedition/lcdui/Font.init.(III)V"] = function(face, style, size) {
        var defaultSize = config.fontSize ? config.fontSize : Math.max(19, (MIDP.context2D.canvas.height / 35) | 0);
        if (size & SIZE_SMALL)
            size = defaultSize / 1.25;
        else if (size & SIZE_LARGE)
            size = defaultSize * 1.25;
        else
            size = defaultSize;
        size |= 0;

        if (style & STYLE_BOLD)
            style = "bold ";
        else if (style & STYLE_ITALIC)
            style = "italic ";
        else
            style = "";

        if (face & FACE_MONOSPACE)
            face = "monospace";
        else if (face & FACE_PROPORTIONAL)
            face = "sans-serif";
        else
            face = "Arial,Helvetica,sans-serif";

        this.klass.classInfo.getField("I.baseline.I").set(this, size | 0);
        this.klass.classInfo.getField("I.height.I").set(this, (size * 1.3)|0);

        // Note:
        // When a css string, such as ` 10 pt Arial, Helvetica`, is set to
        // tempContext.font, it will be formatted to `10 pt Arial,Helvetica`
        // with some spaces removed.
        // We want our `css` property to be the canonical string
        // representation, so we set `tempContext.font` to this
        // value and then actually use the string that it is formatted to
        var css = style + size + "px " + face;
        tempContext.font = css;
        this.css = tempContext.font;
        this.size = size;
        this.style = style;
        this.face = face;
    };

    function calcStringWidth(font, str) {
        var emojiLen = 0;

        tempContext.font = font.css;
        var len = measureWidth(tempContext, str.replace(emoji.regEx, function() {
            emojiLen += font.size;
            return "";
        }));

        return len + emojiLen;
    }

    function getGraphicsContext(g) {
        var contextInfo;
        var displayId = g.klass.classInfo.getField("I.displayId.I").get(g);
        var img = g.klass.classInfo.getField("I.img.Ljavax/microedition/lcdui/Image;").get(g);
        if (displayId != -1 && MIDP.isForegroundDisplay(displayId)) {
            contextInfo = screenContextInfo;
        } else if (img && img.imageData) {
            contextInfo = img.imageData.contextInfo;
        }

        if (contextInfo && contextInfo.context.canvas.width > 0 && contextInfo.context.canvas.height > 0) {
            contextInfo.applyGraphics(g);
            return contextInfo.context;
        }

        return null;
    }

    Native["javax/microedition/lcdui/Font.stringWidth.(Ljava/lang/String;)I"] = function(str) {
        return calcStringWidth(this, J2ME.fromJavaString(str));
    };

    Native["javax/microedition/lcdui/Font.charWidth.(C)I"] = function(char) {
        tempContext.font = this.css;
        return measureWidth(tempContext, String.fromCharCode(char));
    };

    Native["javax/microedition/lcdui/Font.charsWidth.([CII)I"] = function(str, offset, len) {
        return calcStringWidth(this, util.fromJavaChars(str).slice(offset, offset + len));
    };

    Native["javax/microedition/lcdui/Font.substringWidth.(Ljava/lang/String;II)I"] = function(str, offset, len) {
        return calcStringWidth(this, J2ME.fromJavaString(str).slice(offset, offset + len));
    };

    var HCENTER = 1;
    var VCENTER = 2;
    var LEFT = 4;
    var RIGHT = 8;
    var TOP = 16;
    var BOTTOM = 32;
    var BASELINE = 64;

    function getAnchorX(anchor, x, w) {
        if (anchor & RIGHT) {
            x -= w;
        } else if (anchor & HCENTER) {
            x -= (w >>> 1) | 0;
        }
        return x;
    }

    function getAnchorY(anchor, y, h) {
        if (anchor & BOTTOM) {
            y -= h;
        } else if (anchor & VCENTER) {
            y -= (h >>> 1) | 0;
        }
        return y;
    }

    function measureWidth(c, str) {
        return c.measureText(str).width | 0;
    }

    function getTextAnchorX(c, anchor, x, str) {
        if (anchor & RIGHT || anchor & HCENTER) {
            var w = measureWidth(c, str);

            if (anchor & RIGHT) {
                x -= w;
            } else if (anchor & HCENTER) {
                x -= (w >>> 1) | 0;
            }
        }
        return x;
    }

    function withTextAnchor(c, anchor) {
        if (anchor & BOTTOM) {
            c.textBaseline = "bottom";
        } else if (anchor & BASELINE) {
            c.textBaseline = "alphabetic";
        } else if (anchor & VCENTER) {
            throw $.newIllegalArgumentException("VCENTER not allowed with text");
        } else {
            c.textBaseline = "top";
        }
    }

    /**
     * create the outline of an elliptical arc
     * covering the specified rectangle.
     * @param x the x-coordinate of the center of the ellipse.
     * @param y y-coordinate of the center of the ellipse.
     * @param rw the horizontal radius of the arc.
     * @param rh the vertical radius of the arc.
     * @param arcStart the beginning angle
     * @param arcEnd the ending angle
     * @param closed if true, draw a closed arc sector.
     */
    function createEllipticalArc(c, x, y, rw, rh, arcStart, arcEnd, closed) {
          c.save();
          c.translate(x, y);
          if (closed) {
            c.moveTo(0, 0);
          }
          // draw circle arc which will be stretched into an oval arc
          c.scale(1, rh / rw);
          c.arc(0, 0, rw, arcStart, arcEnd, false);
          if (closed) {
            c.lineTo(0, 0);
          }
          c.restore();
    }

    /**
     * Create a round rectangle path.
     * @param x the x coordinate of the rectangle
     * @param y the y coordinate of the rectangle
     * @param width the width of the rectangle
     * @param height the height of the rectangle
     * @param arcWidth the horizontal diameter of the arc at the four corners
     * @param arcHeight the vertical diameter of the arc at the four corners
     */
    function createRoundRect(c, x, y, width, height, arcWidth, arcHeight) {
        var rw = arcWidth / 2;
        var rh = arcHeight / 2;
        c.moveTo(x + rw, y);
        c.lineTo(x + width - rw, y);
        createEllipticalArc(c, x + width - rw, y + rh, rw, rh, 1.5 * Math.PI, 2 * Math.PI, false);
        c.lineTo(x + width, y + height - rh);
        createEllipticalArc(c, x + width - rw, y + height - rh, rw, rh, 0, 0.5 * Math.PI, false);
        c.lineTo(x + rw, y + height);
        createEllipticalArc(c, x + rw, y + height - rh, rw, rh, 0.5 * Math.PI, Math.PI, false);
        c.lineTo(x, y + rh);
        createEllipticalArc(c, x + rw, y + rh, rw, rh, Math.PI, 1.5 * Math.PI, false);
    }

    Native["javax/microedition/lcdui/Graphics.getDisplayColor.(I)I"] = function(color) {
        return color;
    };

    function getPixel(rgb) {
        return swapRB(rgb) | 0xff000000;
    }

    // DirectGraphics constants
    var TYPE_USHORT_4444_ARGB = 4444;
    var TYPE_USHORT_565_RGB = 565;

    Native["com/nokia/mid/ui/DirectGraphicsImp.setARGBColor.(I)V"] = function(argb) {
        argb = argb & 0xFFFFFFFF;
        var g = this.graphics;
        var red = (argb >> 16) & 0xff;
        var green = (argb >> 8) & 0xff;
        var blue = argb & 0xff;
        var alpha = (argb >> 24) & 0xff;

        g.klass.classInfo.getField("I.pixel.I").set(g, swapRB(argb));
        g.klass.classInfo.getField("I.rgbColor.I").set(g, argb & 0x00ffffff);
        // Conversion matches Graphics#grayVal(int, int, int).
        g.klass.classInfo.getField("I.gray.I").set(g, grayVal(red, green, blue));
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getAlphaComponent.()I"] = function() {
        var g = this.graphics;
        var pixel = g.klass.classInfo.getField("I.pixel.I").get(g);
        var alpha = (pixel >> 24) & 0xff;
        return alpha;
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.getPixels.([SIIIIIII)V"] =
    function(pixels, offset, scanlength, x, y, width, height, format) {
        if (pixels == null) {
            throw $.newNullPointerException("Pixels array is null");
        }

        var converterFunc = null;
        if (format == TYPE_USHORT_4444_ARGB) {
            converterFunc = ABGRToARGB4444;
        } else if (format == TYPE_USHORT_565_RGB) {
            converterFunc = ABGRToRGB565;
        } else {
            throw $.newIllegalArgumentException("Format unsupported");
        }

        var context = getGraphicsContext(this.graphics);
        if (!context) {
          for (var i = 0; i < (width * height); i++) {
            pixels[i] = 0x00000000;
          }
          return;
        }

        var abgrData = new Int32Array(context.getImageData(x, y, width, height).data.buffer);
        converterFunc(abgrData, pixels, width, height, offset, scanlength);
    };

    Native["com/nokia/mid/ui/DirectGraphicsImp.drawPixels.([SZIIIIIIII)V"] =
    function(pixels, transparency, offset, scanlength, x, y, width, height, manipulation, format) {
        if (pixels == null) {
            throw $.newNullPointerException("Pixels array is null");
        }

        var c = getGraphicsContext(this.graphics);
        if (!c) {
          return;
        }

        var converterFunc = null;
        if (format == TYPE_USHORT_4444_ARGB && transparency && !manipulation) {
            converterFunc = ARGB4444ToABGR;
        } else {
            throw $.newIllegalArgumentException("Format unsupported");
        }

        tempContext.canvas.width = width;
        tempContext.canvas.height = height;
        var imageData = tempContext.createImageData(width, height);
        var abgrData = new Int32Array(imageData.data.buffer);

        converterFunc(pixels, abgrData, width, height, offset, scanlength);

        tempContext.putImageData(imageData, 0, 0);

        c.drawImage(tempContext.canvas, 0, 0, width, height, x, y, width, height);

        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    Native["javax/microedition/lcdui/Graphics.render.(Ljavax/microedition/lcdui/Image;III)Z"] = function(image, x, y, anchor) {
        return renderImage(this, image, x, y, anchor);
    };

    function renderImage(g, image, x, y, anchor) {
        var c = getGraphicsContext(g);
        if (!c) {
            return 1;
        }

        var texture = image.imageData.contextInfo.context.canvas;

        x = getAnchorX(anchor, x, texture.width);
        y = getAnchorY(anchor, y, texture.height);

        c.drawImage(texture, 0, 0, texture.width, texture.height, x, y, texture.width, texture.height);

        return 1;
    }

    Native["javax/microedition/lcdui/Graphics.renderRegion.(Ljavax/microedition/lcdui/Image;IIIIIIII)Z"] = function(image, x_src, y_src, width, height, transform, x_dest, y_dest, anchor) {
        return renderRegion(this, image, x_src, y_src, width, height, transform, x_dest, y_dest, anchor);
    };

    function parseEmojiString(str) {
        var parts = [];

        var match;
        var lastIndex = 0;
        emoji.regEx.lastIndex = 0;
        while (match = emoji.regEx.exec(str)) {
            parts.push({ text: str.substring(lastIndex, match.index), emoji: match[0] });
            lastIndex = match.index + match[0].length;
        }

        parts.push({ text: str.substring(lastIndex), emoji: null });

        return parts;
    }

    function grayVal(red, green, blue) {
        /* CCIR Rec 601 luma (nonlinear rgb to nonlinear "gray") */
        return (red*76 + green*150 + blue*29) >> 8;
    }

    function drawString(g, str, x, y, anchor, isOpaque) {
        var c = getGraphicsContext(g);
        if (!c) {
            return;
        }

        var font = g.klass.classInfo.getField("I.currentFont.Ljavax/microedition/lcdui/Font;").get(g);

        // TODO: Is this still necessary?
        var oldStyle;
        if (isOpaque) {
            var pixel = g.klass.classInfo.getField("I.pixel.I").get(g);
            var b = (pixel >> 16) & 0xff;
            var g = (pixel >> 8) & 0xff;
            var r = pixel & 0xff;
            var style = "rgba(" + r + "," + g + "," + b + "," + 1 + ")";
            oldStyle = c.fillStyle;
            c.fillStyle = c.strokeStyle = style;
        }

        parseEmojiString(str).forEach(function(part) {
            if (part.text) {
                var textX = getTextAnchorX(c, anchor, x, part.text);
                var textY = y;
                withTextAnchor(c, anchor);

                c.fillText(part.text, textX, textY);

                // If there are emojis in the string that we need to draw,
                // we need to calculate the string width
                if (part.emoji) {
                    x += measureWidth(c, part.text);
                }
            }

            if (part.emoji) {
                var emojiData = emoji.getData(part.emoji, font.size);
                c.drawImage(emojiData.img, emojiData.x, 0, emoji.squareSize, emoji.squareSize, x, y, font.size, font.size);
                x += font.size;
            }
        });

        if (isOpaque) {
          c.fillStyle = c.strokeStyle = oldStyle;
        }
    }

    Native["javax/microedition/lcdui/Graphics.getPixel.(IIZ)I"] = function(rgb, gray, isGray) {
        if (isGray) {
            rgb = gray | (gray << 8) | (gray << 16);
        }
        var red = (rgb >> 16) & 0xff;
        var green = (rgb >> 8) & 0xff;
        var blue = rgb & 0xff;
        var pixel = getPixel(rgb);

        return pixel;
    }

    Native["javax/microedition/lcdui/Graphics.drawString.(Ljava/lang/String;III)V"] = function(str, x, y, anchor) {
        drawString(this, J2ME.fromJavaString(str), x, y, anchor, true);
    };

    Native["javax/microedition/lcdui/Graphics.drawSubstring.(Ljava/lang/String;IIIII)V"] = 
    function(str, offset, len, x, y, anchor) {
        drawString(this, J2ME.fromJavaString(str).substr(offset, len), x, y, anchor, false);
    };

    Native["javax/microedition/lcdui/Graphics.drawChars.([CIIIII)V"] = function(data, offset, len, x, y, anchor) {
        drawString(this, util.fromJavaChars(data, offset, len), x, y, anchor, false);
    };

    Native["javax/microedition/lcdui/Graphics.drawChar.(CIII)V"] = function(jChr, x, y, anchor) {
        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        var chr = String.fromCharCode(jChr);

        x = getTextAnchorX(c, anchor, x, chr);
        withTextAnchor(c, anchor);

        c.fillText(chr, x, y);
    };

    Native["javax/microedition/lcdui/Graphics.fillTriangle.(IIIIII)V"] = function(x1, y1, x2, y2, x3, y3) {
        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        var dx1 = (x2 - x1) || 1;
        var dy1 = (y2 - y1) || 1;
        var dx2 = (x3 - x1) || 1;
        var dy2 = (y3 - y1) || 1;

        c.beginPath();
        c.moveTo(x1, y1);
        c.lineTo(x1 + dx1, y1 + dy1);
        c.lineTo(x1 + dx2, y1 + dy2);
        c.fill();
    };

    Native["javax/microedition/lcdui/Graphics.drawRect.(IIII)V"] = function(x, y, w, h) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        w = w || 1;
        h = h || 1;

        c.strokeRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.drawRoundRect.(IIIIII)V"] = function(x, y, w, h, arcWidth, arcHeight) {
        if (w < 0 || h < 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillRect.(IIII)V"] = function(x, y, w, h) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        w = w || 1;
        h = h || 1;

        c.fillRect(x, y, w, h);
    };

    Native["javax/microedition/lcdui/Graphics.fillRoundRect.(IIIIII)V"] = function(x, y, w, h, arcWidth, arcHeight) {
        if (w <= 0 || h <= 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        w = w || 1;
        h = h || 1;

        c.beginPath();
        createRoundRect(c, x, y, w, h, arcWidth, arcHeight);
        c.fill();
    };

    Native["javax/microedition/lcdui/Graphics.drawArc.(IIIIII)V"] = function(x, y, width, height, startAngle, arcAngle) {
        if (width < 0 || height < 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        var endRad = -startAngle * 0.0175;
        var startRad = endRad - arcAngle * 0.0175;
        c.beginPath();
        createEllipticalArc(c, x, y, width / 2, height / 2, startRad, endRad, false);
        c.stroke();
    };

    Native["javax/microedition/lcdui/Graphics.fillArc.(IIIIII)V"] = function(x, y, width, height, startAngle, arcAngle) {
        if (width <= 0 || height <= 0) {
            return;
        }

        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        var endRad = -startAngle * 0.0175;
        var startRad = endRad - arcAngle * 0.0175;
        c.beginPath();
        c.moveTo(x, y);
        createEllipticalArc(c, x, y, width / 2, height / 2, startRad, endRad, true);
        c.moveTo(x, y);
        c.fill();
    };

    var TRANS_NONE = 0;
    var TRANS_MIRROR_ROT180 = 1;
    var TRANS_MIRROR = 2;
    var TRANS_ROT180 = 3;
    var TRANS_MIRROR_ROT270 = 4;
    var TRANS_ROT90 = 5;
    var TRANS_ROT270 = 6;
    var TRANS_MIRROR_ROT90 = 7;

    function renderRegion(g, image, sx, sy, sw, sh, transform, x, y, anchor) {
        var c = getGraphicsContext(g);
        if (!c) {
            return 1;
        }

        var texture = image.imageData.contextInfo.context.canvas;

        tempContext.canvas.width = sw;
        tempContext.canvas.height = sh;
        copyImage(tempContext, texture, sx, sy, sw, sh, transform, 0, 0);

        x = getAnchorX(anchor, x, sw);
        y = getAnchorY(anchor, y, sh);

        c.drawImage(tempContext.canvas, 0, 0, sw, sh, x, y, sw, sh);

        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;

        return 1;
    };

    Native["javax/microedition/lcdui/Graphics.drawLine.(IIII)V"] = function(x1, y1, x2, y2) {
        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        var dx = (x2 - x1);
        var dy = (y2 - y1);
        if (dx === 0) {
            x1 += .5;
        }
        if (dy === 0) {
            y1 += .5;
        }

        c.beginPath();
        c.moveTo(x1, y1);
        c.lineTo(x1 + dx, y1 + dy);
        c.stroke();
        c.closePath();
    };

    Native["javax/microedition/lcdui/Graphics.drawRGB.([IIIIIIIZ)V"] =
    function(rgbData, offset, scanlength, x, y, width, height, processAlpha) {
        var c = getGraphicsContext(this);
        if (!c) {
            return;
        }

        tempContext.canvas.height = height;
        tempContext.canvas.width = width;
        var imageData = tempContext.createImageData(width, height);
        var abgrData = new Int32Array(imageData.data.buffer);

        var converterFunc = processAlpha ? ARGBToABGR : ARGBTo1BGR;
        converterFunc(rgbData, abgrData, width, height, offset, scanlength);

        tempContext.putImageData(imageData, 0, 0);

        c.drawImage(tempContext.canvas, 0, 0, width, height, x, y, width, height);
        tempContext.canvas.width = 0;
        tempContext.canvas.height = 0;
    };

    var textEditorId = 0,
        textEditorResolve = null,
        dirtyEditors = [];

    function wakeTextEditorThread(textEditor) {
        dirtyEditors.push(textEditor);
        if (textEditorResolve) {
            textEditorResolve();
            textEditorResolve = null;
        }
    }

    Native["com/nokia/mid/ui/TextEditor.init.(Ljava/lang/String;IIII)V"] =
    function(text, maxSize, constraints, width, height) {
        if (constraints != 0) {
            console.warn("TextEditor.constraints not implemented");
        }

        this.textEditorId = ++textEditorId;
        this.textEditor = TextEditorProvider.getEditor(constraints, null, this.textEditorId);
        this.visible = false;
        this.textEditor.setBackgroundColor(0xFFFFFFFF | 0); // opaque white
        this.textEditor.setForegroundColor(0xFF000000 | 0); // opaque black

        this.getCaretPosition = function() {
            if (this.textEditor.isAttached()) {
                return this.textEditor.getSelectionStart();
            }
            if (this.caretPosition !== null) {
                return this.caretPosition;
            }
            return 0;
        };

        this.setCaretPosition = function(index) {
            if (this.textEditor.isAttached()) {
                this.textEditor.setSelectionRange(index, index);
            } else {
                this.caretPosition = index;
            }
        };

        this.textEditor.setAttribute("maxlength", maxSize);
        this.textEditor.setSize(width, height);
        this.textEditor.setVisible(false);
        var font = this.klass.classInfo.getField("I.font.Ljavax/microedition/lcdui/Font;").get(this);
        this.textEditor.setFont(font);

        this.textEditor.setContent(J2ME.fromJavaString(text));
        this.setCaretPosition(this.textEditor.getContentSize());

        this.textEditor.oninput(function(e) {
            wakeTextEditorThread(this);
        }.bind(this));
    };

    Native["com/nokia/mid/ui/CanvasItem.attachNativeImpl.()V"] = function() {
        this.textEditor.attach();
        if (this.caretPosition !== null) {
            this.textEditor.setSelectionRange(this.caretPosition, this.caretPosition);
            this.caretPosition = null;
        }
    };

    Native["com/nokia/mid/ui/CanvasItem.detachNativeImpl.()V"] = function() {
        this.caretPosition = this.textEditor.getSelectionStart();
        this.textEditor.detach();
    };

    Native["javax/microedition/lcdui/Display.setTitle.(Ljava/lang/String;)V"] = function(title) {
        document.getElementById("display_title").textContent = J2ME.fromJavaString(title);
    };

    Native["com/nokia/mid/ui/CanvasItem.setSize.(II)V"] = function(width, height) {
        this.textEditor.setSize(width, height);
    };

    Native["com/nokia/mid/ui/CanvasItem.setVisible.(Z)V"] = function(visible) {
        this.textEditor.setVisible(visible ? true : false);
        this.visible = visible;
    };

    Native["com/nokia/mid/ui/CanvasItem.getWidth.()I"] = function() {
        return this.textEditor.getWidth();
    };

    Native["com/nokia/mid/ui/CanvasItem.getHeight.()I"] = function() {
        return this.textEditor.getHeight();
    };

    Native["com/nokia/mid/ui/CanvasItem.setPosition0.(II)V"] = function(x, y) {
        this.textEditor.setPosition(x, y);
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionX.()I"] = function() {
        return this.textEditor.getLeft();
    };

    Native["com/nokia/mid/ui/CanvasItem.getPositionY.()I"] = function() {
        return this.textEditor.getTop();
    };

    Native["com/nokia/mid/ui/CanvasItem.isVisible.()Z"] = function() {
        return this.visible ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setConstraints.(I)V"] = function(constraints) {
        this.textEditor = TextEditorProvider.getEditor(constraints, this.textEditor, this.textEditorId);
    };

    Native["com/nokia/mid/ui/TextEditor.getConstraints.()I"] = function() {
        return this.textEditor.constraints;
    };

    Native["com/nokia/mid/ui/TextEditor.setFocus.(Z)V"] = function(shouldFocus) {
        var promise;
        if (shouldFocus && (currentlyFocusedTextEditor != this.textEditor)) {
            promise = this.textEditor.focus();
            currentlyFocusedTextEditor = this.textEditor;
        } else if (!shouldFocus && (currentlyFocusedTextEditor == this.textEditor)) {
            promise = this.textEditor.blur();
            currentlyFocusedTextEditor = null;
        } else {
            return;
        }
        asyncImpl("V", promise);
    };

    Native["com/nokia/mid/ui/TextEditor.hasFocus.()Z"] = function() {
        return (this.textEditor == currentlyFocusedTextEditor) ? 1 : 0;
    };

    Native["com/nokia/mid/ui/TextEditor.setCaret.(I)V"] = function(index) {
        if (index < 0 || index > this.textEditor.getContentSize()) {
            throw $.newStringIndexOutOfBoundsException();
        }

        this.setCaretPosition(index);
    };

    Native["com/nokia/mid/ui/TextEditor.getCaretPosition.()I"] = function() {
        return this.getCaretPosition();
    };

    Native["com/nokia/mid/ui/TextEditor.getBackgroundColor.()I"] = function() {
        return this.textEditor.getBackgroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.getForegroundColor.()I"] = function() {
        return this.textEditor.getForegroundColor();
    };
    Native["com/nokia/mid/ui/TextEditor.setBackgroundColor.(I)V"] = function(backgroundColor) {
        this.textEditor.setBackgroundColor(backgroundColor);
    };
    Native["com/nokia/mid/ui/TextEditor.setForegroundColor.(I)V"] = function(foregroundColor) {
        this.textEditor.setForegroundColor(foregroundColor);
    };

    Native["com/nokia/mid/ui/TextEditor.getContent.()Ljava/lang/String;"] = function() {
        return J2ME.newString(this.textEditor.getContent());
    };

    Native["com/nokia/mid/ui/TextEditor.setContent.(Ljava/lang/String;)V"] = function(jStr) {
        var str = J2ME.fromJavaString(jStr);
        this.textEditor.setContent(str);
        this.setCaretPosition(this.textEditor.getContentSize());
    };

    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getLineMarginHeight.()I", 0);
    addUnimplementedNative("com/nokia/mid/ui/TextEditor.getVisibleContentPosition.()I", 0);

    Native["com/nokia/mid/ui/TextEditor.getContentHeight.()I"] = function() {
        return this.textEditor.getContentHeight();
    };

    Native["com/nokia/mid/ui/TextEditor.insert.(Ljava/lang/String;I)V"] = function(jStr, pos) {
        var str = J2ME.fromJavaString(jStr);
        var len = util.toCodePointArray(str).length;
        if (this.textEditor.getContentSize() + len > this.textEditor.getAttribute("maxlength")) {
            throw $.newIllegalArgumentException();
        }
        this.textEditor.setContent(this.textEditor.getSlice(0, pos) + str + this.textEditor.getSlice(pos));
        this.setCaretPosition(pos + len);
    };

    Native["com/nokia/mid/ui/TextEditor.delete.(II)V"] = function(offset, length) {
        var old = this.textEditor.getContent();

        var size = this.textEditor.getContentSize();
        if (offset < 0 || offset > size || length < 0 || offset + length > size) {
            throw $.newStringIndexOutOfBoundsException("offset/length invalid");
        }

        this.textEditor.setContent(this.textEditor.getSlice(0, offset) + this.textEditor.getSlice(offset + length));
        this.setCaretPosition(offset);
    };

    Native["com/nokia/mid/ui/TextEditor.getMaxSize.()I"] = function() {
        return parseInt(this.textEditor.getAttribute("maxlength"));
    };

    Native["com/nokia/mid/ui/TextEditor.setMaxSize.(I)I"] = function(maxSize) {
        if (this.textEditor.getContentSize() > maxSize) {
            var oldCaretPosition = this.getCaretPosition();

            this.textEditor.setContent(this.textEditor.getSlice(0, maxSize));

            if (oldCaretPosition > maxSize) {
                this.setCaretPosition(maxSize);
            }
        }

        this.textEditor.setAttribute("maxlength", maxSize);

        // The return value is the assigned size, which could be less than
        // the size that was requested, although in this case we always set it
        // to the requested size.
        return maxSize;
    };

    Native["com/nokia/mid/ui/TextEditor.size.()I"] = function() {
        return this.textEditor.getContentSize();
    };

    Native["com/nokia/mid/ui/TextEditor.setFont.(Ljavax/microedition/lcdui/Font;)V"] = function(font) {
        this.klass.classInfo.getField("I.font.Ljavax/microedition/lcdui/Font;").set(this, font);
        this.textEditor.setFont(font);
    };

    Native["com/nokia/mid/ui/TextEditorThread.getNextDirtyEditor.()Lcom/nokia/mid/ui/TextEditor;"] = function() {
        if (dirtyEditors.length) {
            return dirtyEditors.shift();
        }

        asyncImpl("Lcom/nokia/mid/ui/TextEditor;", new Promise(function(resolve, reject) {
            textEditorResolve = function() {
                resolve(dirtyEditors.shift());
            }
        }));
    };

    var curDisplayableId = 0;
    var nextMidpDisplayableId = 1;
    var PLAIN = 0;

    Native["javax/microedition/lcdui/DisplayableLFImpl.initialize0.()V"] = function() {
    };

    Native["javax/microedition/lcdui/DisplayableLFImpl.deleteNativeResource0.(I)V"] = function(nativeId) {
        var el = document.getElementById("displayable-" + nativeId);
        if (el) {
            el.parentElement.removeChild(el);
            if (currentlyFocusedTextEditor) {
                currentlyFocusedTextEditor.focus();
            }
        } else if (currentlyFocusedTextEditor) {
            currentlyFocusedTextEditor.blur();
        }
    };

    Native["javax/microedition/lcdui/DisplayableLFImpl.setTitle0.(ILjava/lang/String;)V"] = function(nativeId, title) {
        document.getElementById("display_title").textContent = J2ME.fromJavaString(title);
    };

    Native["javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I"] = function(title, ticker) {
        console.warn("javax/microedition/lcdui/CanvasLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I not implemented");
        curDisplayableId = nextMidpDisplayableId++;
        return curDisplayableId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;I)I"] = function(title, ticker, type) {
        var nativeId = nextMidpDisplayableId++;
        var alertTemplateNode = document.getElementById("lcdui-alert");
        var el = alertTemplateNode.cloneNode(true);
        el.id = "displayable-" + nativeId;
        el.querySelector('h1.title').textContent = J2ME.fromJavaString(title);
        alertTemplateNode.parentNode.appendChild(el);

        return nativeId;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.setNativeContents0.(ILjavax/microedition/lcdui/ImageData;[ILjava/lang/String;)Z"] =
    function(nativeId, imgId, indicatorBounds, text) {
        var el = document.getElementById("displayable-" + nativeId);
        el.querySelector('p.text').textContent = J2ME.fromJavaString(text);

        return 0;
    };

    Native["javax/microedition/lcdui/AlertLFImpl.showNativeResource0.(I)V"] = function(nativeId) {
        var el = document.getElementById("displayable-" + nativeId);
        el.style.display = 'block';
        el.classList.add('visible');
        if (currentlyFocusedTextEditor) {
            currentlyFocusedTextEditor.blur();
        }

        curDisplayableId = nativeId;
    };

    var INDEFINITE = -1;
    var CONTINUOUS_RUNNING = 2;

    Native["javax/microedition/lcdui/GaugeLFImpl.createNativeResource0.(ILjava/lang/String;IZII)I"] =
    function(ownerId, label, layout, interactive, maxValue, initialValue) {
        if (label != null) {
            console.error("Expected null label");
        }

        if (layout != PLAIN) {
            console.error("Expected PLAIN layout");
        }

        if (interactive) {
            console.error("Expected not interactive gauge");
        }

        if (maxValue != INDEFINITE) {
            console.error("Expected INDEFINITE maxValue");
        }

        if (initialValue != CONTINUOUS_RUNNING) {
            console.error("Expected CONTINUOUS_RUNNING initialValue")
        }

        var el = document.getElementById("displayable-" + ownerId);
        el.querySelector("progress").style.display = "inline";

        return nextMidpDisplayableId++;
    };

    Native["javax/microedition/lcdui/TextFieldLFImpl.createNativeResource0.(ILjava/lang/String;ILcom/sun/midp/lcdui/DynamicCharacterArray;ILjava/lang/String;)I"] =
    function(ownerId, label, layout, buffer, constraints, initialInputMode) {
        console.warn("javax/microedition/lcdui/TextFieldLFImpl.createNativeResource0.(ILjava/lang/String;ILcom/sun/midp/lcdui/DynamicCharacterArray;ILjava/lang/String;)I not implemented");
        return nextMidpDisplayableId++;
    };

    Native["javax/microedition/lcdui/ImageItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjavax/microedition/lcdui/ImageData;Ljava/lang/String;I)I"] =
    function(ownerId, label, layout, imageData, altText, appearanceMode) {
        console.warn("javax/microedition/lcdui/ImageItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjavax/microedition/lcdui/ImageData;Ljava/lang/String;I)I not implemented");
        return nextMidpDisplayableId++;
    };

    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.setScrollPosition0.(I)V");
    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.getScrollPosition0.()I", 0);

    addUnimplementedNative(
        "javax/microedition/lcdui/FormLFImpl.createNativeResource0.(Ljava/lang/String;Ljava/lang/String;)I",
        function() { return nextMidpDisplayableId++ }
    );

    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.showNativeResource0.(IIII)V");
    addUnimplementedNative("javax/microedition/lcdui/FormLFImpl.getViewportHeight0.()I", 0);

    addUnimplementedNative(
        "javax/microedition/lcdui/StringItemLFImpl.createNativeResource0.(ILjava/lang/String;ILjava/lang/String;ILjavax/microedition/lcdui/Font;)I",
        function() { return nextMidpDisplayableId++ }
    );

    Native["javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V"] = function(nativeId, w, h) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setSize0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V"] = function(nativeId, x, y) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.setLocation0.(III)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.show0.(I)V"] = function(nativeId) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.show0.(I)V not implemented");
    };

    Native["javax/microedition/lcdui/ItemLFImpl.hide0.(I)V"] = function(nativeId) {
        console.warn("javax/microedition/lcdui/ItemLFImpl.hide0.(I)V not implemented");
    };

    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getMinimumWidth0.(I)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getMinimumHeight0.(I)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getPreferredWidth0.(II)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.getPreferredHeight0.(II)I", 10);
    addUnimplementedNative("javax/microedition/lcdui/ItemLFImpl.delete0.(I)V");

    var BACK = 2;
    var CANCEL = 3;
    var OK = 4;

    Native["javax/microedition/lcdui/NativeMenu.updateCommands.([Ljavax/microedition/lcdui/Command;I[Ljavax/microedition/lcdui/Command;I)V"] =
    function(itemCommands, numItemCommands, commands, numCommands) {
        if (numItemCommands != 0) {
            console.error("NativeMenu.updateCommands: item commands not yet supported");
        }

        var el = document.getElementById("displayable-" + curDisplayableId);

        if (!el) {
            document.getElementById("sidebar").querySelector("nav ul").innerHTML = "";
        }

        if (!commands) {
            return;
        }

        var validCommands = commands.filter(function(command) {
            return !!command;
        }).sort(function(a, b) {
            return a.klass.classInfo.getField("I.priority.I").get(a) -
                   b.klass.classInfo.getField("I.priority.I").get(b);
        });

        function sendEvent(command) {
            MIDP.sendCommandEvent(command.klass.classInfo.getField("I.id.I").get(command));
        }

        if (el) {
            if (numCommands > 2 && validCommands.length > 2) {
                console.error("NativeMenu.updateCommands: max two commands supported");
            }

            validCommands.slice(0, 2).forEach(function(command, i) {
                var button = el.querySelector(".button" + i);
                button.style.display = 'inline';
                button.textContent = J2ME.fromJavaString(command.klass.classInfo.getField("I.shortLabel.Ljava/lang/String;").get(command));

                var commandType = command.klass.classInfo.getField("I.commandType.I").get(command);
                if (numCommands == 1 || commandType == OK) {
                    button.classList.add('recommend');
                } else if (commandType == CANCEL || commandType == BACK) {
                    button.classList.add('cancel');
                }

                button.onclick = function(e) {
                    e.preventDefault();
                    sendEvent(command);
                };
            });
        } else {
            var menu = document.getElementById("sidebar").querySelector("nav ul");

            var okCommand = null;
            var backCommand = null;

            var isSidebarEmpty = true;
            validCommands.forEach(function(command) {
                var commandType = command.klass.classInfo.getField("I.commandType.I").get(command);
                // Skip the OK command which will shown in the header.
                if (commandType == OK) {
                    okCommand = command;
                    return;
                }
                // Skip the BACK command which will shown in the footer.
                if (commandType == BACK) {
                    backCommand = command;
                    return;
                }
                var li = document.createElement("li");
                var text = J2ME.fromJavaString(command.klass.classInfo.getField("I.shortLabel.Ljava/lang/String;").get(command));
                var a = document.createElement("a");
                a.textContent = text;
                li.appendChild(a);

                li.onclick = function(e) {
                    e.preventDefault();

                    window.location.hash = "";

                    sendEvent(command);
                };

                menu.appendChild(li);
                isSidebarEmpty = false;
            });

            document.getElementById("header-drawer-button").style.display =
                isSidebarEmpty ? "none" : "block";

            // If existing, the OK command will be shown in the header.
            var headerBtn = document.getElementById("header-ok-button");
            if (okCommand) {
                headerBtn.style.display = "block";
                headerBtn.onclick = sendEvent.bind(headerBtn, okCommand);
            } else {
                headerBtn.style.display = "none";
            }

            // If existing, the BACK command will be shown in the footer.
            var backBtn = document.getElementById("back-button");
            if (backCommand) {
                backBtn.style.display = "block";
                backBtn.onclick = sendEvent.bind(backBtn, backCommand);
            } else {
                backBtn.style.display = "none";
            }
        }
    };

    return {
        screenContextInfo: screenContextInfo,
    };
})(Native);
