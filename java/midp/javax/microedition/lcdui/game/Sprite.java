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

package javax.microedition.lcdui.game;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * A Sprite is a basic visual element that can be rendered with one of
 * several frames stored in an Image; different frames can be shown to 
 * animate the Sprite.  Several transforms such as flipping and rotation
 * can also be applied to a Sprite to further vary its appearance.  As with
 * all Layer subclasses, a Sprite's location can be changed and it can also
 * be made visible or invisible.
 * <P>
 * <h3>Sprite Frames</h3>
 * The raw frames used to render a Sprite are provided in a single Image
 * object, which may be mutable or immutable.  If more than one frame is used,
 * the Image is broken up into a series of equally-sized frames of a specified
 * width and height.  As shown in the figure below, the same set of frames may
 * be stored in several different arrangements depending on what is the most 
 * convenient for the game developer.  
 * <br>
 * <center><img src="doc-files/frames.gif" width=777 height=402 
 * ALT="Sprite Frames"></center>
 * <br>
 * <p>
 * Each frame is assigned a unique index number.  The frame located in the
 * upper-left corner of the Image is assigned an index of 0.  The remaining 
 * frames are then numbered consecutively in row-major order (indices are 
 * assigned across the first row, then the second row, and so on).  The method
 * {@link #getRawFrameCount()} returns the total number of raw frames.
 * <P>
 * <h3>Frame Sequence</h3>
 * A Sprite's frame sequence defines an ordered list of frames to be displayed.
 * The default frame sequence mirrors the list of available frames, so 
 * there is a direct mapping between the sequence index and the corresponding
 * frame index.  This also means that the length of the default frame sequence
 * is equal to the number of raw frames.  For example, if a Sprite has 4 
 * frames, its default frame sequence is {0, 1, 2, 3}.  
 * <center><img src="doc-files/defaultSequence.gif" width=182 height=269 
 * ALT="Default Frame Sequence"></center>
 * <P>
 * The developer must manually switch the current frame in the frame sequence.
 * This may be accomplished by calling {@link #setFrame}, 
 * {@link #prevFrame()}, or {@link #nextFrame()}.  Note that these methods 
 * always operate on the sequence index, they do not operate on frame indices;
 * however, if the default frame sequence is used, then the sequence indices
 * and the frame indices are interchangeable.
 * <P>
 * If desired, an arbitrary frame sequence may be defined for a Sprite.  
 * The frame sequence must contain at least one element, and each element must
 * reference a valid frame index.  By defining a new frame sequence, the 
 * developer can conveniently display the Sprite's frames in any order 
 * desired; frames may be repeated, omitted, shown in reverse order, etc.
 * <P>
 * For example, the diagram below shows how a special frame sequence might be
 * used to animate a mosquito.  The frame sequence is designed so that the 
 * mosquito flaps its wings three times and then pauses for a moment before 
 * the cycle is repeated.
 * <center><img src="doc-files/specialSequence.gif" width=346 height=510 
 * ALT="Special Frame Sequence"></center>
 * By calling {@link #nextFrame} each time the display is updated, the 
 * resulting animation would like this:
 * <br>
 * <center><img src="doc-files/sequenceDemo.gif" width=96 height=36></center>
 * <P>
 * <h3>Reference Pixel</h3>
 * Being a subclass of Layer, Sprite inherits various methods for setting and
 * retrieving its location such as {@link #setPosition setPosition(x,y)}, 
 * {@link #getX getX()}, and {@link #getY getY()}.  These methods all define
 * position in terms of the upper-left corner of the Sprite's visual bounds;
 * however, in some cases, it is more convenient to define the Sprite's position
 * in terms of an arbitrary pixel within its frame, especially if transforms
 * are applied to the Sprite.
 * <P>
 * Therefore, Sprite includes the concept of a <em>reference pixel</em>.    
 * The reference pixel is defined by specifying its location in the
 * Sprite's untransformed frame using 
 * {@link #defineReferencePixel defineReferencePixel(x,y)}.  
 * By default, the reference pixel is defined to be the pixel at (0,0) 
 * in the frame.  If desired, the reference pixel may be defined outside 
 * of the frame's bounds.    
 * <p>
 * In this example, the reference pixel is defined to be the pixel that
 * the monkey appears to be hanging from:
 * <p>
 * <center><img src="doc-files/refpixel.gif" width=304 height=199
 * ALT="Defining The Reference Pixel"></center>
 * <p>
 * {@link #getRefPixelX getRefPixelX()} and {@link #getRefPixelY getRefPixelY()}
 * can be used to query the location of the reference pixel in the painter's 
 * coordinate system.  The developer can also use 
 * {@link #setRefPixelPosition setRefPixelPosition(x,y)} to position the Sprite 
 * so that reference pixel appears at a specific location in the painter's
 * coordinate system.  These methods automatically account for any transforms
 * applied to the Sprite.
 * <p>
 * In this example, the reference pixel's position is set to a point at the end 
 * of a tree branch; the Sprite's location changes so that the reference pixel
 * appears at this point and the monkey appears to be hanging from the branch:
 * <p>
 * <center><img src="doc-files/setrefposition.gif" width=332 height=350
 * ALT="Setting The Reference Pixel Position"></center>
 * <p>
 * <a name="transforms"></a>
 * <h3>Sprite Transforms</h3>
 * Various transforms can be applied to a Sprite.  The available transforms
 * include rotations in multiples of 90 degrees, and mirrored (about
 * the vertical axis) versions of each of the rotations.  A Sprite's transform 
 * is set by calling {@link #setTransform setTransform(transform)}.
 * <p>
 * <center><img src="doc-files/transforms.gif" width=355 height=575 
 * ALT="Transforms"></center>
 * <br>
 * When a transform is applied, the Sprite is automatically repositioned 
 * such that the  reference pixel appears stationary in the painter's 
 * coordinate system.  Thus, the reference pixel effectively becomes the
 * center of the transform operation.  Since the reference pixel does not
 * move, the values returned by {@link #getRefPixelX()} and 
 * {@link #getRefPixelY()} remain the same; however, the values returned by
 * {@link #getX getX()} and {@link #getY getY()} may change to reflect the 
 * movement of the Sprite's upper-left corner.
 * <p>
 * Referring to the monkey example once again, the position of the 
 * reference pixel remains at (48, 22) when a 90 degree rotation
 * is applied, thereby making it appear as if the monkey is swinging
 * from the branch:
 * <p>
 * <center><img src="doc-files/transcenter.gif" width=333 height=350
 * ALT="Transform Center"></center>
 * <p>
 * <h3>Sprite Drawing</h3>
 * Sprites can be drawn at any time using the {@link #paint(Graphics)} method.  
 * The Sprite will be drawn on the Graphics object according to the current 
 * state information maintained by the Sprite (i.e. position, frame, 
 * visibility).  Erasing the Sprite is always the responsibility of code 
 * outside the Sprite class.<p>
 * <p>
 * Sprites can be implemented using whatever techniques a manufacturers 
 * wishes to use (e.g hardware acceleration may be used for all Sprites, for
 * certain sizes of Sprites, or not at all).
 * <p>
 * For some platforms, certain Sprite sizes may be more efficient than others;
 * manufacturers may choose to provide developers with information about
 * device-specific characteristics such as these.
 * <p>
 */

public class Sprite extends Layer
{

    // ----- definitions for the various transformations possible -----

    /**
     * No transform is applied to the Sprite.  
     * This constant has a value of <code>0</code>.
     */
    public static final int TRANS_NONE = 0;
    
    /**
     * Causes the Sprite to appear rotated clockwise by 90 degrees.
     * This constant has a value of <code>5</code>.
     */
    public static final int TRANS_ROT90 = 5;

    /**
     * Causes the Sprite to appear rotated clockwise by 180 degrees.
     * This constant has a value of <code>3</code>.
     */
    public static final int TRANS_ROT180 = 3;

    /**
     * Causes the Sprite to appear rotated clockwise by 270 degrees.
     * This constant has a value of <code>6</code>.
     */
    public static final int TRANS_ROT270 = 6;

    /**
     * Causes the Sprite to appear reflected about its vertical
     * center.
     * This constant has a value of <code>2</code>.
     */
    public static final int TRANS_MIRROR = 2;

    /**
     * Causes the Sprite to appear reflected about its vertical
     * center and then rotated clockwise by 90 degrees.
     * This constant has a value of <code>7</code>.
     */
    public static final int TRANS_MIRROR_ROT90 = 7;

    /**
     * Causes the Sprite to appear reflected about its vertical
     * center and then rotated clockwise by 180 degrees.
     * This constant has a value of <code>1</code>.
     */
    public static final int TRANS_MIRROR_ROT180 = 1;

    /**
     * Causes the Sprite to appear reflected about its vertical
     * center and then rotated clockwise by 270 degrees.
     * This constant has a value of <code>4</code>.
     */
    public static final int TRANS_MIRROR_ROT270 = 4;

    // ----- Constructors -----

    /**
     * Creates a new non-animated Sprite using the provided Image.  
     * This constructor is functionally equivalent to calling
     * <code>new Sprite(image, image.getWidth(), image.getHeight())</code>
     * <p>
     * By default, the Sprite is visible and its upper-left 
     * corner is positioned at (0,0) in the painter's coordinate system.
     * <br>
     * @param image the <code>Image</code> to use as the single frame
     * for the </code>Sprite
     * @throws NullPointerException if <code>img</code> is <code>null</code>
     */
    public Sprite(Image image) {
        super(image.getWidth(), image.getHeight());

        initializeFrames(image, image.getWidth(), image.getHeight(), false);

        // initialize collision rectangle
        initCollisionRectBounds();

        // current transformation is TRANS_NONE
        setTransformImpl(TRANS_NONE);

    }

    /**
     * Creates a new animated Sprite using frames contained in 
     * the provided Image.  The frames must be equally sized, with the 
     * dimensions specified by <code>frameWidth</code> and 
     * <code>frameHeight</code>.  They may be laid out in the image 
     * horizontally, vertically, or as a grid.  The width of the source 
     * image must be an integer multiple of the frame width, and the height
     * of the source image must be an integer multiple of the frame height.
     * The  values returned by {@link Layer#getWidth} and 
     * {@link Layer#getHeight} will reflect the frame width and frame height
     * subject to the Sprite's current transform.
     * <p>
     * Sprites have a default frame sequence corresponding to the raw frame
     * numbers, starting with frame 0.  The frame sequence may be modified
     * with {@link #setFrameSequence(int[])}.
     * <p>
     * By default, the Sprite is visible and its upper-left corner is 
     * positioned at (0,0) in the painter's coordinate system.
     * <p>
     * @param image the <code>Image</code> to use for <code>Sprite</code>
     * @param frameWidth the <code>width</code>, in pixels, of the
     * individual raw frames
     * @param frameHeight the <code>height</code>, in pixels, of the
     * individual raw frames
     * @throws NullPointerException if <code>img</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>frameHeight</code> or
     * <code>frameWidth</code> is less than <code>1</code>
     * @throws IllegalArgumentException if the <code>image</code>
     * width is not an integer multiple of the <code>frameWidth</code>
     * @throws IllegalArgumentException if the <code>image</code>
     * height is not an integer multiple of the <code>frameHeight</code>
     */
    public Sprite(Image image, int frameWidth, int frameHeight) {

        super(frameWidth, frameHeight);
        // if img is null img.getWidth() will throw NullPointerException
        if ((frameWidth < 1 || frameHeight < 1) ||
            ((image.getWidth() % frameWidth) != 0) ||
            ((image.getHeight() % frameHeight) != 0)) {
             throw new IllegalArgumentException();
        }

        // construct the array of images that 
        // we use as "frames" for the sprite.
        // use default frame , sequence index = 0  
        initializeFrames(image, frameWidth, frameHeight, false);

        // initialize collision rectangle
        initCollisionRectBounds();

        // current transformation is TRANS_NONE
        setTransformImpl(TRANS_NONE);

    }

    /**
     * Creates a new Sprite from another Sprite.  <p>
     *
     * All instance attributes (raw frames, position, frame sequence, current
     * frame, reference point, collision rectangle, transform, and visibility) 
     * of the source Sprite are duplicated in the new Sprite.  
     *
     * @param s the <code>Sprite</code> to create a copy of
     * @throws NullPointerException if <code>s</code> is <code>null</code>
     *
     */
    public Sprite(Sprite s) {

        super(s != null ? s.getWidth() : 0,
                     s != null ? s.getHeight() : 0);

        if (s == null) {
            throw new NullPointerException();
        }

        this.sourceImage = Image.createImage(s.sourceImage);

        this.numberFrames = s.numberFrames;

        this.frameCoordsX = new int[this.numberFrames];
        this.frameCoordsY = new int[this.numberFrames];

        System.arraycopy(s.frameCoordsX, 0, 
                         this.frameCoordsX, 0,
                         s.getRawFrameCount());

        System.arraycopy(s.frameCoordsY, 0, 
                         this.frameCoordsY, 0,
                         s.getRawFrameCount());

        this.x = s.getX();
        this.y = s.getY();

        // these fields are set by defining a reference point
        this.dRefX = s.dRefX;
        this.dRefY = s.dRefY;

        // these fields are set when defining a collision rectangle
        this.collisionRectX = s.collisionRectX;
        this.collisionRectY = s.collisionRectY;
        this.collisionRectWidth = s.collisionRectWidth;
        this.collisionRectHeight = s.collisionRectHeight;

        // these fields are set when creating a Sprite from an Image
        this.srcFrameWidth = s.srcFrameWidth;
        this.srcFrameHeight = s.srcFrameHeight;

        // the above fields are used in setTransform()
        // which is why we set them first, then  call setTransformImpl() 
        // to set up internally used data structures.
        setTransformImpl(s.t_currentTransformation);

        this.setVisible(s.isVisible()); 

        this.frameSequence = new int[s.getFrameSequenceLength()];
        this.setFrameSequence(s.frameSequence);
        this.setFrame(s.getFrame());

        this.setRefPixelPosition(s.getRefPixelX(), s.getRefPixelY());
        
    }


    // ----- public methods -----

    /**
     * Defines the reference pixel for this Sprite.  The pixel is
     * defined by its location relative to the upper-left corner of
     * the Sprite's un-transformed frame, and it may lay outside of
     * the frame's bounds.
     * <p>
     * When a transformation is applied, the reference pixel is
     * defined relative to the Sprite's initial upper-left corner
     * before transformation. This corner may no longer appear as the
     * upper-left corner in the painter's coordinate system under
     * current transformation.
     * <p>
     * By default, a Sprite's reference pixel is located at (0,0); that is,
     * the pixel in the upper-left corner of the raw frame.
     * <p>
     * Changing the reference pixel does not change the
     * Sprite's physical position in the painter's coordinate system;
     * that is, the values returned by {@link #getX getX()} and
     * {@link #getY getY()} will not change as a result of defining the
     * reference pixel.  However, subsequent calls to methods that
     * involve the reference pixel will be impacted by its new definition.
     *
     * @param inp_x the horizontal location of the reference pixel, relative
     * to the left edge of the un-transformed frame
     * @param inp_y the vertical location of the reference pixel, relative
     * to the top edge of the un-transformed frame
     * @see #setRefPixelPosition
     * @see #getRefPixelX
     * @see #getRefPixelY
     */
    public void defineReferencePixel(int inp_x, int inp_y) {
        dRefX = inp_x;
        dRefY = inp_y;
    }
    
    /**
     * Sets this Sprite's position such that its reference pixel is located
     * at (x,y) in the painter's coordinate system.
     * @param inp_x the horizontal location at which to place 
     * the reference pixel
     * @param inp_y the vertical location at which to place the reference pixel
     * @see #defineReferencePixel
     * @see #getRefPixelX
     * @see #getRefPixelY
     */         
    public void setRefPixelPosition(int inp_x, int inp_y) {

        // update x and y
        x = inp_x - getTransformedPtX(dRefX, dRefY, 
                                       t_currentTransformation);
        y = inp_y - getTransformedPtY(dRefX, dRefY, 
                                       t_currentTransformation);

    }

    /**
     * Gets the horizontal position of this Sprite's reference pixel
     * in the painter's coordinate system.  
     * @return the horizontal location of the reference pixel
     * @see #defineReferencePixel
     * @see #setRefPixelPosition
     * @see #getRefPixelY
     */         
    public int getRefPixelX() {
        return (this.x +
                getTransformedPtX(dRefX, dRefY, this.t_currentTransformation));
    }
        
    /**
     * Gets the vertical position of this Sprite's reference pixel
     * in the painter's coordinate system.
     * @return the vertical location of the reference pixel
     * @see #defineReferencePixel
     * @see #setRefPixelPosition
     * @see #getRefPixelX
     */         
    public int getRefPixelY() {
        return (this.y + 
                getTransformedPtY(dRefX, dRefY, this.t_currentTransformation));
    }

    /**
     * Selects the current frame in the frame sequence.  <p>
     * The current frame is rendered when {@link #paint(Graphics)} is called.
     * <p>
     * The index provided refers to the desired entry in the frame sequence, 
     * not the index of the actual frame itself.
     * @param inp_sequenceIndex the index of of the desired entry in the frame 
     * sequence 
     * @throws IndexOutOfBoundsException if <code>frameIndex</code> is
     * less than<code>0</code>
     * @throws IndexOutOfBoundsException if <code>frameIndex</code> is
     * equal to or greater than the length of the current frame
     * sequence (or the number of raw frames for the default sequence)
     * @see #setFrameSequence(int[])
     * @see #getFrame
     */
    public void setFrame(int inp_sequenceIndex) {
        if (inp_sequenceIndex < 0 || 
	    inp_sequenceIndex >= frameSequence.length) {
            throw new IndexOutOfBoundsException();
        }
        sequenceIndex = inp_sequenceIndex;
    }

    /**
     * Gets the current index in the frame sequence.  <p>
     * The index returned refers to the current entry in the frame sequence,
     * not the index of the actual frame that is displayed.
     *
     * @return the current index in the frame sequence 
     * @see #setFrameSequence(int[])
     * @see #setFrame
     */
    public final int getFrame() {
        return sequenceIndex;
    }

    /**
     * Gets the number of raw frames for this Sprite.  The value returned 
     * reflects the number of frames; it does not reflect the length of the 
     * Sprite's frame sequence.  However, these two values will be the same
     * if the default frame sequence is used.
     *
     * @return the number of raw frames for this Sprite
     * @see #getFrameSequenceLength
     */
    public int getRawFrameCount() {
        return numberFrames;
    }

    /**
     * Gets the number of elements in the frame sequence.  The value returned
     * reflects the length of the Sprite's frame sequence; it does not reflect
     * the number of raw frames.  However, these two values will be the same 
     * if the default frame sequence is used.
     *
     * @return the number of elements in this Sprite's frame sequence
     * @see #getRawFrameCount
     */
    public int getFrameSequenceLength() {
        return frameSequence.length;
    }

    /**
     * Selects the next frame in the frame sequence.  <p>
     *
     * The frame sequence is considered to be circular, i.e. if 
     * {@link #nextFrame} is called when at the end of the sequence,
     * this method will advance to the first entry in the sequence.
     *
     * @see #setFrameSequence(int[])
     * @see #prevFrame
     */
    public void nextFrame() {
        sequenceIndex = (sequenceIndex + 1) % frameSequence.length;
    }

    /**
     * Selects the previous frame in the frame sequence.  <p>
     *
     * The frame sequence is considered to be circular, i.e. if
     * {@link #prevFrame} is called when at the start of the sequence,
     * this method will advance to the last entry in the sequence.
     *
     * @see #setFrameSequence(int[])
     * @see #nextFrame
     */
    public void prevFrame() {
        if (sequenceIndex == 0) {
            sequenceIndex = frameSequence.length - 1;
        } else {
            sequenceIndex--;
        }
    }

    /**
     * Draws the Sprite.  
     * <P>
     * Draws current frame of Sprite using the provided Graphics object.
     * The Sprite's upper left corner is rendered at the Sprite's current
     * position relative to the origin of the Graphics object.  The current
     * position of the Sprite's upper-left corner can be retrieved by 
     * calling {@link #getX()} and {@link #getY()}.
     * <P>
     * Rendering is subject to the clip region of the Graphics object.
     * The Sprite will be drawn only if it is visible.
     * <p>
     * If the Sprite's Image is mutable, the Sprite is rendered using the
     * current contents of the Image.
     * 
     * @param g the graphics object to draw <code>Sprite</code> on
     * @throws NullPointerException if <code>g</code> is <code>null</code>
     *
     */
    public final void paint(Graphics g) {
        // managing the painting order is the responsibility of
        // the layermanager, so depth is ignored
        if (g == null) {
            throw new NullPointerException();
        }

        if (visible) {

                // width and height of the source
                // image is the width and height
                // of the original frame
                g.drawRegion(sourceImage, 
                             frameCoordsX[frameSequence[sequenceIndex]],
                             frameCoordsY[frameSequence[sequenceIndex]],
                             srcFrameWidth, 
                             srcFrameHeight,
                             t_currentTransformation,
                             this.x, 
                             this.y, 
                             Graphics.TOP | Graphics.LEFT);
        }

    }

    /**
     * Set the frame sequence for this Sprite.  <p>
     *
     * All Sprites have a default sequence that displays the Sprites
     * frames in order.  This method allows for the creation of an
     * arbitrary sequence using the available frames.  The current
     * index in the frame sequence is reset to zero as a result of 
     * calling this method.
     * <p>
     * The contents of the sequence array are copied when this method
     * is called; thus, any changes made to the array after this method
     * returns have no effect on the Sprite's frame sequence.
     * <P>
     * Passing in <code>null</code> causes the Sprite to revert to the
     * default frame sequence.<p>
     *
     * @param sequence an array of integers, where each integer represents
     * a frame index
     *       
     * @throws ArrayIndexOutOfBoundsException if seq is non-null and any member
     *         of the array has a value less than <code>0</code> or
     *         greater than or equal to the
     *         number of frames as reported by {@link #getRawFrameCount()}
     * @throws IllegalArgumentException if the array has less than
     * <code>1</code> element
     * @see #nextFrame
     * @see #prevFrame
     * @see #setFrame
     * @see #getFrame
     *
     */
    public void setFrameSequence(int sequence[]) {

        if (sequence == null) {
            // revert to the default sequence
            sequenceIndex = 0;
            customSequenceDefined = false;
            frameSequence = new int[numberFrames];
            // copy frames indices into frameSequence
            for (int i = 0; i < numberFrames; i++)
            {
                frameSequence[i] = i;
            }
            return;
        }

        if (sequence.length < 1) {
             throw new IllegalArgumentException();
        }

        for (int i = 0; i < sequence.length; i++)
        {
            if (sequence[i] < 0 || sequence[i] >= numberFrames) {
                throw new ArrayIndexOutOfBoundsException();
            }
        }
        customSequenceDefined = true;
        frameSequence = new int[sequence.length];
        System.arraycopy(sequence, 0, frameSequence, 0, sequence.length);
        sequenceIndex = 0;
    }
    
    /**
     * Changes the Image containing the Sprite's frames.  
     * <p>
     * Replaces the current raw frames of the Sprite with a new set of raw
     * frames.  See the constructor {@link #Sprite(Image, int, int)} for
     * information on how the frames are created from the image.  The 
     * values returned by {@link Layer#getWidth} and {@link Layer#getHeight}
     * will reflect the new frame width and frame height subject to the 
     * Sprite's current transform.
     * <p>
     * Changing the image for the Sprite could change the number of raw 
     * frames.  If the new frame set has as many or more raw frames than the
     * previous frame set, then:
     * <ul>
     * <li>The current frame will be unchanged
     * <li>If a custom frame sequence has been defined (using 
     *     {@link #setFrameSequence(int[])}), it will remain unchanged.  If no
     *     custom frame sequence is defined (i.e. the default frame
     *     sequence
     *     is in use), the default frame sequence will be updated to
     *     be the default frame sequence for the new frame set.  In other
     *     words, the new default frame sequence will include all of the
     *     frames from the new raw frame set, as if this new image had been
     *     used in the constructor.
     * </ul>
     * <p>
     * If the new frame set has fewer frames than the previous frame set, 
     * then:
     * <ul>
     * <li>The current frame will be reset to entry 0
     * <li>Any custom frame sequence will be discarded and the frame sequence
     *     will revert to the default frame sequence for the new frame
     *     set.
     * </ul>
     * <p>
     * The reference point location is unchanged as a result of calling this 
     * method, both in terms of its defined location within the Sprite and its
     * position in the painter's coordinate system.  However, if the frame
     * size is changed and the Sprite has been transformed, the position of 
     * the Sprite's upper-left corner may change such that the reference 
     * point remains stationary.
     * <p>
     * If the Sprite's frame size is changed by this method, the collision 
     * rectangle is reset to its default value (i.e. it is set to the new 
     * bounds of the untransformed Sprite).
     * <p> 
     * @param img the <code>Image</code> to use for
     * <code>Sprite</code>
     * @param frameWidth the width in pixels of the individual raw frames
     * @param frameHeight the height in pixels of the individual raw frames
     * @throws NullPointerException if <code>img</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>frameHeight</code> or 
     * <code>frameWidth</code> is less than <code>1</code>
     * @throws IllegalArgumentException if the image width is not an integer
     * multiple of the <code>frameWidth</code>
     * @throws IllegalArgumentException if the image height is not an integer 
     * multiple of the <code>frameHeight</code>
     */
    public void setImage(Image img, int frameWidth, int frameHeight) {

        // if image is null image.getWidth() will throw NullPointerException
        if ((frameWidth < 1 || frameHeight < 1) ||
            ((img.getWidth() % frameWidth) != 0) ||
            ((img.getHeight() % frameHeight) != 0)) {
             throw new IllegalArgumentException();
        }

        int noOfFrames = 
          (img.getWidth() / frameWidth)*(img.getHeight() / frameHeight);

        boolean maintainCurFrame = true;
        if (noOfFrames < numberFrames) {
            // use default frame , sequence index = 0
            maintainCurFrame = false; 
            customSequenceDefined = false;
        }

        if (! ((srcFrameWidth == frameWidth) &&
               (srcFrameHeight == frameHeight))) {

            // computing is the location
            // of the reference pixel in the painter's coordinate system.
            // and then use this to find x and y position of the Sprite
            int oldX = this.x + 
                getTransformedPtX(dRefX, dRefY, this.t_currentTransformation);

            int oldY = this.y +
                getTransformedPtY(dRefX, dRefY, this.t_currentTransformation);


            setWidthImpl(frameWidth);
            setHeightImpl(frameHeight);

            initializeFrames(img, frameWidth, frameHeight, maintainCurFrame);

            // initialize collision rectangle
            initCollisionRectBounds();

            // set the new x and y position of the Sprite
            this.x = oldX - 
                getTransformedPtX(dRefX, dRefY, this.t_currentTransformation);

            this.y = oldY -
                getTransformedPtY(dRefX, dRefY, this.t_currentTransformation);


            // Calculate transformed sprites collision rectangle
            // and transformed width and height

            computeTransformedBounds(this.t_currentTransformation);

        } else {
            // just reinitialize the animation frames.
            initializeFrames(img, frameWidth, frameHeight, maintainCurFrame);
        }

    }

    /**
     * Defines the Sprite's bounding rectangle that is used for collision 
     * detection purposes.  This rectangle is specified relative to the 
     * un-transformed Sprite's upper-left corner and defines the area that is
     * checked for collision detection.  For pixel-level detection, only those
     * pixels within the collision rectangle are checked. 
     *
     * By default, a Sprite's collision rectangle is located at 0,0 as has the
     * same dimensions as the Sprite.  The collision rectangle may be 
     * specified to be larger or smaller than the default rectangle; if made 
     * larger, the pixels outside the bounds of the Sprite are considered to be
     * transparent for pixel-level collision detection.
     *
     * @param inp_x the horizontal location of the collision 
     * rectangle relative to the untransformed Sprite's left edge
     * @param inp_y the vertical location of the collision rectangle relative to
     * the untransformed Sprite's top edge
     * @param width the width of the collision rectangle
     * @param height the height of the collision rectangle
     * @throws IllegalArgumentException if the specified
     * <code>width</code> or <code>height</code> is
     * less than <code>0</code>
     */
    public void defineCollisionRectangle(int inp_x, int inp_y, 
                                         int width, int height) {

        if (width < 0 || height < 0) {
             throw new IllegalArgumentException();
        }

        collisionRectX = inp_x;
        collisionRectY = inp_y;
        collisionRectWidth = width;
        collisionRectHeight = height;

        // call set transform with current transformation to 
        // update transformed sprites collision rectangle
        setTransformImpl(t_currentTransformation);
    }

    /**
     * Sets the transform for this Sprite.  Transforms can be 
     * applied to a Sprite to change its rendered appearance.  Transforms 
     * are applied to the original Sprite image; they are not cumulative, 
     * nor can they be combined.  By default, a Sprite's transform is 
     * {@link #TRANS_NONE}.
     * <P>
     * Since some transforms involve rotations of 90 or 270 degrees, their
     * use may result in the overall width and height of the Sprite 
     * being swapped.  As a result, the values returned by 
     * {@link Layer#getWidth} and {@link Layer#getHeight} may change.
     * <p>
     * The collision rectangle is also modified by the transform so that
     * it remains static relative to the pixel data of the Sprite.  
     * Similarly, the defined reference pixel is unchanged by this method,
     * but its visual location within the Sprite may change as a result.
     * <P>
     * This method repositions the Sprite so that the location of 
     * the reference pixel in the painter's coordinate system does not change
     * as a result of changing the transform.  Thus, the reference pixel 
     * effectively becomes the centerpoint for the transform.  Consequently,
     * the values returned by {@link #getRefPixelX} and {@link #getRefPixelY} 
     * will be the same both before and after the transform is applied, but 
     * the values returned by {@link #getX getX()} and {@link #getY getY()}
     * may change.  
     * <p>
     * @param transform the desired transform for this <code>Sprite</code>
     * @throws IllegalArgumentException if the requested
     * <code>transform</code> is invalid
     * @see #TRANS_NONE
     * @see #TRANS_ROT90
     * @see #TRANS_ROT180
     * @see #TRANS_ROT270
     * @see #TRANS_MIRROR
     * @see #TRANS_MIRROR_ROT90
     * @see #TRANS_MIRROR_ROT180
     * @see #TRANS_MIRROR_ROT270
     *
     */
    public void setTransform(int transform) {
        setTransformImpl(transform);
    }

    /**
     * Checks for a collision between this Sprite and the specified Sprite.
     * <P>
     * If pixel-level detection is used, a collision is detected only if
     * opaque pixels collide.  That is, an opaque pixel in the first
     * Sprite would have to collide with an opaque  pixel in the second
     * Sprite for a collision to be detected.  Only those pixels within
     * the Sprites' respective collision rectangles are checked.
     * <P>
     * If pixel-level detection is not used, this method simply
     * checks if the Sprites' collision rectangles intersect.
     * <P>
     * Any transforms applied to the Sprites are automatically accounted for.
     * <P>
     * Both Sprites must be visible in order for a collision to be
     * detected.
     * <P>
     * @param s the <code>Sprite</code> to test for collision with
     * @param pixelLevel <code>true</code> to test for collision on a
     * pixel-by-pixel basis, <code>false</code> to test using simple
     * bounds checking.
     * @return <code>true</code> if the two Sprites have collided, otherwise
     * <code>false</code>
     * @throws NullPointerException if Sprite <code>s</code> is 
     * <code>null</code>
     */
    public final boolean collidesWith(Sprite s, boolean pixelLevel) 
    {
        
        // check if either of the Sprite's are not visible
        if (!(s.visible && this.visible)) {
            return false;
        }

        // these are package private 
        // and can be accessed directly
        int otherLeft    = s.x + s.t_collisionRectX;
        int otherTop     = s.y + s.t_collisionRectY;
        int otherRight   = otherLeft + s.t_collisionRectWidth;
        int otherBottom  = otherTop  + s.t_collisionRectHeight;

        int left   = this.x + this.t_collisionRectX;
        int top    = this.y + this.t_collisionRectY;
        int right  = left + this.t_collisionRectWidth;
        int bottom = top  + this.t_collisionRectHeight;

        // check if the collision rectangles of the two sprites intersect
        if (intersectRect(otherLeft, otherTop, otherRight, otherBottom,
                          left, top, right, bottom)) {

            // collision rectangles intersect
            if (pixelLevel) {

                // we need to check pixel level collision detection.
                // use only the coordinates within the Sprite frame if 
                // the collision rectangle is larger than the Sprite 
                // frame 
                if (this.t_collisionRectX < 0) {
                    left = this.x;
                }
                if (this.t_collisionRectY < 0) {
                    top = this.y;
                }
                if ((this.t_collisionRectX + this.t_collisionRectWidth)
                    > this.width) {
                    right = this.x + this.width;
                }
                if ((this.t_collisionRectY + this.t_collisionRectHeight)
                    > this.height) {
                    bottom = this.y + this.height;
                }

                // similarly for the other Sprite
                if (s.t_collisionRectX < 0) {
                    otherLeft = s.x;
                }
                if (s.t_collisionRectY < 0) {
                    otherTop = s.y;
                }
                if ((s.t_collisionRectX + s.t_collisionRectWidth)
                    > s.width) {
                    otherRight = s.x + s.width;
                }
                if ((s.t_collisionRectY + s.t_collisionRectHeight)
                    > s.height) {
                    otherBottom = s.y + s.height;
                }

                // recheck if the updated collision area rectangles intersect
                if (!intersectRect(otherLeft, otherTop, otherRight, otherBottom,
                                  left, top, right, bottom)) {

                    // if they don't intersect, return false;
                    return false;
                }

                // the updated collision rectangles intersect,
                // go ahead with collision detection


                // find intersecting region, 
                // within the collision rectangles
                int intersectLeft = (left < otherLeft) ? otherLeft : left;
                int intersectTop  = (top < otherTop) ? otherTop : top;

                // used once, optimize.
                int intersectRight  = (right < otherRight) 
                                      ? right : otherRight;
                int intersectBottom = (bottom < otherBottom) 
                                      ? bottom : otherBottom;

                int intersectWidth  = Math.abs(intersectRight - intersectLeft);
                int intersectHeight = Math.abs(intersectBottom - intersectTop);

                // have the coordinates in painter space,
                // need coordinates of top left and width, height
                // in source image of Sprite.
                
                int thisImageXOffset = getImageTopLeftX(intersectLeft, 
                                                        intersectTop,
                                                        intersectRight,
                                                        intersectBottom);
                
                int thisImageYOffset = getImageTopLeftY(intersectLeft, 
                                                        intersectTop,
                                                        intersectRight,
                                                        intersectBottom);

                int otherImageXOffset = s.getImageTopLeftX(intersectLeft, 
                                                           intersectTop,
                                                           intersectRight,
                                                           intersectBottom);
                
                int otherImageYOffset = s.getImageTopLeftY(intersectLeft, 
                                                           intersectTop,
                                                           intersectRight,
                                                           intersectBottom);

                // check if opaque pixels intersect.

                return doPixelCollision(thisImageXOffset, thisImageYOffset,
                                        otherImageXOffset, otherImageYOffset,
                                        this.sourceImage,
                                        this.t_currentTransformation,
                                        s.sourceImage, 
                                        s.t_currentTransformation,
                                        intersectWidth, intersectHeight);

            } else {
                // collides!
                return true;
            }
        }
        return false;

    }

    /**
     * Checks for a collision between this Sprite and the specified
     * TiledLayer.  If pixel-level detection is used, a collision is
     * detected only if opaque pixels collide.  That is, an opaque pixel in
     * the Sprite would have to collide with an opaque pixel in TiledLayer
     * for a collision to be detected.  Only those pixels within the Sprite's
     * collision rectangle are checked.
     * <P>
     * If pixel-level detection is not used, this method simply checks if the
     * Sprite's collision rectangle intersects with a non-empty cell in the
     * TiledLayer.
     * <P>
     * Any transform applied to the Sprite is automatically accounted for.
     * <P>
     * The Sprite and the TiledLayer must both be visible in order for
     * a collision to be detected.
     * <P>
     * @param t the <code>TiledLayer</code> to test for collision with
     * @param pixelLevel <code>true</code> to test for collision on a
     * pixel-by-pixel basis, <code>false</code> to test using simple bounds
     * checking against non-empty cells.
     * @return <code>true</code> if this <code>Sprite</code> has
     * collided with the <code>TiledLayer</code>, otherwise
     * <code>false</code>
     * @throws NullPointerException if <code>t</code> is <code>null</code>
     */
    public final boolean collidesWith(TiledLayer t, boolean pixelLevel) {

        // check if either this Sprite or the TiledLayer is not visible
        if (!(t.visible && this.visible)) {
            return false;
        }

        // dimensions of tiledLayer, cell, and
        // this Sprite's collision rectangle

        // these are package private 
        // and can be accessed directly
        int tLx1 = t.x;
        int tLy1 = t.y;
        int tLx2 = tLx1 + t.width;
        int tLy2 = tLy1 + t.height;

        int tW = t.getCellWidth();
        int tH = t.getCellHeight();

        int sx1 = this.x + this.t_collisionRectX;
        int sy1 = this.y + this.t_collisionRectY;
        int sx2 = sx1 + this.t_collisionRectWidth;
        int sy2 = sy1 + this.t_collisionRectHeight;

        // number of cells
        int tNumCols = t.getColumns();
        int tNumRows = t.getRows();

        // temporary loop variables.
        int startCol; // = 0;
        int endCol;   // = 0;
        int startRow; // = 0;
        int endRow;   // = 0;

        if (!intersectRect(tLx1, tLy1, tLx2, tLy2, sx1, sy1, sx2, sy2)) {
            // if the collision rectangle of the sprite
            // does not intersect with the dimensions of the entire 
            // tiled layer
            return false;
        }

        // so there is an intersection

            // note sx1 < sx2, tLx1 < tLx2, sx2 > tLx1  from intersectRect()
            // use <= for comparison as this saves us some
            // computation - the result will be 0
            startCol = (sx1 <= tLx1) ? 0 : (sx1 - tLx1)/tW;
            startRow = (sy1 <= tLy1) ? 0 : (sy1 - tLy1)/tH;
            // since tLx1 < sx2 < tLx2, the computation will yield
            // a result between 0 and tNumCols - 1
            // subtract by 1 because sx2,sy2 represent
            // the enclosing bounds of the sprite, not the 
            // locations in the coordinate system.
            endCol = (sx2 < tLx2) ? ((sx2 - 1 - tLx1)/tW) : tNumCols - 1;
            endRow = (sy2 < tLy2) ? ((sy2 - 1 - tLy1)/tH) : tNumRows - 1;

        if (!pixelLevel) {
            // check for intersection with a non-empty cell,
            for (int row = startRow; row <= endRow; row++) {
                for (int col = startCol; col <= endCol; col++) {
                    if (t.getCell(col, row) != 0) {
                        return true;
                    }
                }
            }
            // worst case! we scanned through entire 
            // overlapping region and
            // all the cells are empty!
            return false;
        } else {
            // do pixel level

            // we need to check pixel level collision detection.
            // use only the coordinates within the Sprite frame if 
            // the collision rectangle is larger than the Sprite 
            // frame 
            if (this.t_collisionRectX < 0) {
                sx1 = this.x;
            }
            if (this.t_collisionRectY < 0) {
                sy1 = this.y;
            }
            if ((this.t_collisionRectX + this.t_collisionRectWidth)
                > this.width) {
                sx2 = this.x + this.width;
            }
            if ((this.t_collisionRectY + this.t_collisionRectHeight)
                > this.height) {
                sy2 = this.y + this.height;
            }
                
            if (!intersectRect(tLx1, tLy1, tLx2, tLy2, sx1, sy1, sx2, sy2)) {
                return (false);
            }
                
            // we have an intersection between the Sprite and 
            // one or more cells of the tiledlayer

            // note sx1 < sx2, tLx1 < tLx2, sx2 > tLx1  from intersectRect()
            // use <= for comparison as this saves us some
            // computation - the result will be 0
            startCol = (sx1 <= tLx1) ? 0 : (sx1 - tLx1)/tW;
            startRow = (sy1 <= tLy1) ? 0 : (sy1 - tLy1)/tH;
            // since tLx1 < sx2 < tLx2, the computation will yield
            // a result between 0 and tNumCols - 1
            // subtract by 1 because sx2,sy2 represent
            // the enclosing bounds of the sprite, not the 
            // locations in the coordinate system.
            endCol = (sx2 < tLx2) ? ((sx2 - 1 - tLx1)/tW) : tNumCols - 1;
            endRow = (sy2 < tLy2) ? ((sy2 - 1 - tLy1)/tH) : tNumRows - 1;

            // current cell coordinates
            int cellTop    = startRow * tH + tLy1;
            int cellBottom = cellTop  + tH;

            // the index of the current tile.
            int tileIndex; // = 0;

            for (int row = startRow; row <= endRow; 
                 row++, cellTop += tH, cellBottom += tH) {

                // current cell coordinates
                int cellLeft   = startCol * tW + tLx1;
                int cellRight  = cellLeft + tW;

                for (int col = startCol; col <= endCol; 
                     col++, cellLeft += tW, cellRight += tW) {

                    tileIndex = t.getCell(col, row);

                    if (tileIndex != 0) {
                        
                        // current cell/sprite intersection coordinates
                        // in painter coordinate system.
                        // find intersecting region, 
                        int intersectLeft = (sx1 < cellLeft) ? cellLeft : sx1;
                        int intersectTop  = (sy1 < cellTop) ? cellTop  : sy1;
                        
                        // used once, optimize.
                        int intersectRight  = (sx2 < cellRight) ? 
                                               sx2 : cellRight;
                        int intersectBottom = (sy2 < cellBottom) ? 
                                               sy2 : cellBottom;

                        if (intersectLeft > intersectRight) {
                            int temp = intersectRight;
                            intersectRight = intersectLeft;
                            intersectLeft = temp;
                        }

                        if (intersectTop > intersectBottom) {
                            int temp = intersectBottom;
                            intersectBottom = intersectTop;
                            intersectTop = temp;
                        }

                        int intersectWidth  = intersectRight  - intersectLeft;
                        int intersectHeight = intersectBottom - intersectTop;

                        int image1XOffset = getImageTopLeftX(intersectLeft, 
                                                             intersectTop,
                                                             intersectRight,
                                                             intersectBottom);

                        int image1YOffset = getImageTopLeftY(intersectLeft, 
                                                             intersectTop,
                                                             intersectRight,
                                                             intersectBottom);

                        int image2XOffset = t.tileSetX[tileIndex] +
                                            (intersectLeft - cellLeft);
                        int image2YOffset = t.tileSetY[tileIndex] +
                                            (intersectTop - cellTop);

                        if (doPixelCollision(image1XOffset,
                                             image1YOffset,
                                             image2XOffset,
                                             image2YOffset,
                                             this.sourceImage, 
                                             this.t_currentTransformation,
                                             t.sourceImage,
                                             TRANS_NONE,
                                             intersectWidth, intersectHeight)) {
                            // intersection found with this tile
                            return true;
                        }
                    }
                } // end of for col
            }// end of for row

            // worst case! we scanned through entire 
            // overlapping region and
            // no pixels collide!
            return false;
        }

    }

    /**
     * Checks for a collision between this Sprite and the specified Image
     * with its upper left corner at the specified location.  If pixel-level
     * detection is used, a collision is detected only if opaque pixels
     * collide.  That is, an opaque pixel in the Sprite would have to collide
     * with an opaque  pixel in Image for a collision to be detected.  Only
     * those pixels within the Sprite's collision rectangle are checked.
     * <P>
     * If pixel-level detection is not used, this method simply checks if the
     * Sprite's collision rectangle intersects with the Image's bounds.
     * <P>
     * Any transform applied to the Sprite is automatically accounted for.
     * <P>
     * The Sprite must be visible in order for a collision to be
     * detected.
     * <P>
     * @param image the <code>Image</code> to test for collision
     * @param inp_x the horizontal location of the <code>Image</code>'s
     * upper left corner
     * @param inp_y the vertical location of the <code>Image</code>'s
     * upper left corner
     * @param pixelLevel <code>true</code> to test for collision on a
     * pixel-by-pixel basis, <code>false</code> to test using simple
     * bounds checking
     * @return <code>true</code> if this <code>Sprite</code> has
     * collided with the <code>Image</code>, otherwise
     * <code>false</code>
     * @throws NullPointerException if <code>image</code> is
     * <code>null</code>
     */
    public final boolean collidesWith(Image image, int inp_x, 
                                      int inp_y, boolean pixelLevel) {

        // check if this Sprite is not visible
        if (!(visible)) {
            return false;
        }

        // if image is null 
        // image.getWidth() will throw NullPointerException
        int otherLeft    = inp_x;
        int otherTop     = inp_y;
        int otherRight   = inp_x + image.getWidth();
        int otherBottom  = inp_y + image.getHeight();

        int left   = x + t_collisionRectX;
        int top    = y + t_collisionRectY;
        int right  = left + t_collisionRectWidth;
        int bottom = top  + t_collisionRectHeight;

        // first check if the collision rectangles of the two sprites intersect
        if (intersectRect(otherLeft, otherTop, otherRight, otherBottom,
                          left, top, right, bottom)) {

            // collision rectangles intersect
            if (pixelLevel) {

                // find intersecting region, 

                // we need to check pixel level collision detection.
                // use only the coordinates within the Sprite frame if 
                // the collision rectangle is larger than the Sprite 
                // frame 
                if (this.t_collisionRectX < 0) {
                    left = this.x;
                }
                if (this.t_collisionRectY < 0) {
                    top = this.y;
                }
                if ((this.t_collisionRectX + this.t_collisionRectWidth)
                    > this.width) {
                    right = this.x + this.width;
                }
                if ((this.t_collisionRectY + this.t_collisionRectHeight)
                    > this.height) {
                    bottom = this.y + this.height;
                }

                // recheck if the updated collision area rectangles intersect
                if (!intersectRect(otherLeft, otherTop, 
                                   otherRight, otherBottom,
                                   left, top, right, bottom)) {

                    // if they don't intersect, return false;
                    return false;
                }

                // within the collision rectangles
                int intersectLeft = (left < otherLeft) ? otherLeft : left;
                int intersectTop  = (top < otherTop) ? otherTop : top;

                // used once, optimize.
                int intersectRight  = (right < otherRight) 
                                      ? right : otherRight;
                int intersectBottom = (bottom < otherBottom) 
                                      ? bottom : otherBottom;

                int intersectWidth  = Math.abs(intersectRight - intersectLeft);
                int intersectHeight = Math.abs(intersectBottom - intersectTop);

                // have the coordinates in painter space,
                // need coordinates of top left and width, height
                // in source image of Sprite.
                
                int thisImageXOffset = getImageTopLeftX(intersectLeft, 
                                                        intersectTop,
                                                        intersectRight,
                                                        intersectBottom);
                
                int thisImageYOffset = getImageTopLeftY(intersectLeft, 
                                                        intersectTop,
                                                        intersectRight,
                                                        intersectBottom);

                int otherImageXOffset = intersectLeft - inp_x;
                int otherImageYOffset = intersectTop  - inp_y;

                // check if opaque pixels intersect.
                return doPixelCollision(thisImageXOffset, thisImageYOffset,
                                        otherImageXOffset, otherImageYOffset,
                                        this.sourceImage,
                                        this.t_currentTransformation,
                                        image, 
                                        Sprite.TRANS_NONE,
                                        intersectWidth, intersectHeight);

            } else {
                // collides!
                return true;
            }
        }
        return false;

    }


    // -----

    //  ----- private -----

    /**
     * create the Image Array.
     *
     * @param image Image to use for Sprite
     * @param fWidth width, in pixels, of the individual raw frames
     * @param fHeight height, in pixels, of the individual raw frames
     * @param maintainCurFrame true if Current Frame is maintained
     */
    private void initializeFrames(Image image, int fWidth, 
                        int fHeight, boolean maintainCurFrame) {

        int imageW = image.getWidth();
        int imageH = image.getHeight();
            
        int numHorizontalFrames = imageW / fWidth;
        int numVerticalFrames   = imageH / fHeight;

        sourceImage = image;

        srcFrameWidth = fWidth;
          srcFrameHeight = fHeight;

        numberFrames = numHorizontalFrames*numVerticalFrames;

        frameCoordsX = new int[numberFrames];
        frameCoordsY = new int[numberFrames];

        if (!maintainCurFrame) {
            sequenceIndex = 0;
        }

        if (!customSequenceDefined) {
            frameSequence = new int[numberFrames];
        }

        int currentFrame = 0;

        for (int yy = 0; yy < imageH; yy += fHeight) {
            for (int xx = 0; xx < imageW; xx += fWidth) {

                frameCoordsX[currentFrame] = xx;
                frameCoordsY[currentFrame] = yy;
        
                if (!customSequenceDefined) {
                    frameSequence[currentFrame] = currentFrame;
                }
                currentFrame++;

            }
        }
    }

    /**
     * initialize the collision rectangle
     */
    private void initCollisionRectBounds() {

        // reset x and y of collision rectangle
        collisionRectX = 0;
        collisionRectY = 0;

        // intialize the collision rectangle bounds to that of the sprite
        collisionRectWidth = this.width;
        collisionRectHeight = this.height;

    }

    /**
     * Detect rectangle intersection
     * 
     * @param r1x1 left co-ordinate of first rectangle
     * @param r1y1 top co-ordinate of first rectangle
     * @param r1x2 right co-ordinate of first rectangle
     * @param r1y2 bottom co-ordinate of first rectangle
     * @param r2x1 left co-ordinate of second rectangle
     * @param r2y1 top co-ordinate of second rectangle
     * @param r2x2 right co-ordinate of second rectangle
     * @param r2y2 bottom co-ordinate of second rectangle
     * @return True if there is rectangle intersection
     */
    private boolean intersectRect(int r1x1, int r1y1, int r1x2, int r1y2, 
                                  int r2x1, int r2y1, int r2x2, int r2y2) {
        if (r2x1 >= r1x2 || r2y1 >= r1y2 || r2x2 <= r1x1 || r2y2 <= r1y1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Detect opaque pixel intersection between regions of two images
     * 
     * @param image1XOffset left coordinate in the first image
     * @param image1YOffset top coordinate in the first image
     * @param image2XOffset left coordinate in the second image
     * @param image2YOffset top coordinate in the second image
     * @param image1 first source image
     * @param transform1 The transform for the first image
     * @param image2 second source image
     * @param transform2 transform set on the second image
     * @param width width of overlapping region, when transformed
     * @param height height of overlapping region, when transformed
     * 
     * Clarification required on parameters:
     * XOffset and YOffset are the offsets from the top left
     * hand corner of the image.
     * width, height is the dimensions of the intersecting regions
     * in the two transformed images.
     * there fore appropriate conversions have to be made on these
     * dimensions when using the values, according to the transformation
     * that has been set.
     * 
     * @return True if there is a pixel level collision
     */
    private static boolean doPixelCollision(int image1XOffset,
                                            int image1YOffset,
                                            int image2XOffset,
                                            int image2YOffset,
                                            Image image1, int transform1, 
                                            Image image2, int transform2,
                                            int width, int height) {

        // starting point of comparison
        int startY1;
        // x and y increments
        int xIncr1, yIncr1;

        // .. for image 2
        int startY2;
        int xIncr2, yIncr2;

        int numPixels = height * width;

        int[] argbData1 = new int[numPixels];
        int[] argbData2 = new int[numPixels];

        if (0x0 != (transform1 & INVERTED_AXES)) {
            // inverted axes

            // scanlength = height

            if (0x0 != (transform1 & Y_FLIP)) {
                xIncr1 = -(height); // - scanlength

                startY1 = numPixels - height; // numPixels - scanlength
            } else {
                xIncr1 = height; // + scanlength
                
                startY1 = 0;
            }

            if (0x0 != (transform1 &  X_FLIP)) {
                yIncr1 = -1;

                startY1 += (height - 1);
            } else {
                yIncr1 = +1;
            }

            image1.getRGB(argbData1, 0, height, // scanlength = height
                          image1XOffset, image1YOffset, height, width);

        } else {

            // scanlength = width

            if (0x0 != (transform1 & Y_FLIP)) {

                startY1 = numPixels - width; // numPixels - scanlength

                yIncr1  = -(width); // - scanlength
            } else {
                startY1 = 0;

                yIncr1  = width; // + scanlength
            }

            if (0x0 != (transform1 &  X_FLIP)) {
                xIncr1  = -1;

                startY1 += (width - 1);
            } else {
                xIncr1  = +1;
            }

            image1.getRGB(argbData1, 0, width, // scanlength = width
                          image1XOffset, image1YOffset, width, height);

        }


        if (0x0 != (transform2 & INVERTED_AXES)) {
            // inverted axes

            if (0x0 != (transform2 & Y_FLIP)) {
                xIncr2 = -(height);

                startY2 = numPixels - height;
            } else {
                xIncr2 = height;

                startY2 = 0;
            }

            if (0x0 != (transform2 &  X_FLIP)) {
                yIncr2 = -1;
                
                startY2 += height - 1;
            } else {
                yIncr2 = +1;
            }

            image2.getRGB(argbData2, 0, height,
                          image2XOffset, image2YOffset, height, width);

        } else {

            if (0x0 != (transform2 & Y_FLIP)) {
                startY2 = numPixels - width;

                yIncr2  = -(width);
            } else {
                startY2 = 0;

                yIncr2  = +width;
            }

            if (0x0 != (transform2 &  X_FLIP)) {
                xIncr2  = -1;

                startY2 += (width - 1);
            } else {
                xIncr2  = +1;
            }

            image2.getRGB(argbData2, 0, width,
                          image2XOffset, image2YOffset, width, height);

        }


        int x1, x2;
        int xLocalBegin1, xLocalBegin2;

        // the loop counters
        int numIterRows;
        int numIterColumns;

        for (numIterRows = 0, xLocalBegin1 = startY1, xLocalBegin2 = startY2;
            numIterRows < height;
            xLocalBegin1 += yIncr1, xLocalBegin2 += yIncr2, numIterRows++) {

            for (numIterColumns = 0, x1 = xLocalBegin1, x2 = xLocalBegin2;
                 numIterColumns < width;
                 x1 += xIncr1, x2 += xIncr2, numIterColumns++) {

                if (((argbData1[x1] & ALPHA_BITMASK) == FULLY_OPAQUE_ALPHA) && 
                    ((argbData2[x2] & ALPHA_BITMASK) == FULLY_OPAQUE_ALPHA)) {

                    return true;
                }
                
            } // end for x        
            
        } // end for y
        
        // worst case!  couldn't find a single colliding pixel!
        return false;
    }

    /**
     * Given a rectangle that lies within the sprite 
     * in the painter's coordinates,
     * find the X coordinate of the top left corner 
     * in the source image of the sprite
     *
     * @param x1 the x coordinate of the top left of the rectangle
     * @param y1 the y coordinate of the top left of the rectangle
     * @param x2 the x coordinate of the bottom right of the rectangle
     * @param y2 the y coordinate of the bottom right of the rectangle
     * 
     * @return the X coordinate in the source image
     * 
     */
    private int getImageTopLeftX(int x1, int y1, int x2, int y2) {
        int retX = 0;

        // left = this.x
        // right = this.x + this.width
        // top = this.y
        // bottom = this.y + this.height

        switch (this.t_currentTransformation) {

        case TRANS_NONE:
        case TRANS_MIRROR_ROT180:
            retX = x1 - this.x;
            break;

        case TRANS_MIRROR:
        case TRANS_ROT180:
            retX = (this.x + this.width) - x2;
            break;

        case TRANS_ROT90:
        case TRANS_MIRROR_ROT270:
            retX = y1 - this.y;
            break;

        case TRANS_ROT270:
        case TRANS_MIRROR_ROT90:
            retX = (this.y + this.height) - y2;
            break;

          default:
            // for safety/completeness.
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "Sprite: t_currentTransformation=" + 
                           t_currentTransformation);
            return retX;
        }

        retX += frameCoordsX[frameSequence[sequenceIndex]];

        return retX;
    }

    /**
     * Given a rectangle that lies within the sprite 
     * in the painter's coordinates,
     * find the Y coordinate of the top left corner 
     * in the source image of the sprite
     *
     * @param x1 the x coordinate of the top left of the rectangle
     * @param y1 the y coordinate of the top left of the rectangle
     * @param x2 the x coordinate of the bottom right of the rectangle
     * @param y2 the y coordinate of the bottom right of the rectangle
     * 
     * @return the Y coordinate in the source image
     * 
     */
    private int getImageTopLeftY(int x1, int y1, int x2, int y2) {
        int retY = 0;

        // left = this.x
        // right = this.x + this.width
        // top = this.y
        // bottom = this.y + this.height

        switch (this.t_currentTransformation) {

        case TRANS_NONE:
        case TRANS_MIRROR:
            retY = y1 - this.y;
            break;

        case TRANS_ROT180:
        case TRANS_MIRROR_ROT180:
            retY = (this.y + this.height) - y2;
            break;

        case TRANS_ROT270:
        case TRANS_MIRROR_ROT270:
            retY = x1 - this.x;
            break;

        case TRANS_ROT90:
        case TRANS_MIRROR_ROT90:
            retY = (this.x + this.width) - x2;
            break;

        default:
            // for safety/completeness.
            Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                           "Sprite: t_currentTransformation=" + 
                           this.t_currentTransformation);
            return retY;
        }

        retY += frameCoordsY[frameSequence[sequenceIndex]];

        return retY;
    }

    /**
     * Sets the transform for this Sprite
     *
     * @param transform the desired transform for this Sprite
     */
    private void setTransformImpl(int transform) {

        // ---

        // setTransform sets up all transformation related data structures
        // except transforming the current frame's bitmap.
        
        // x, y, width, height, dRefX, dRefY, 
        // collisionRectX, collisionRectY, collisionRectWidth,
        // collisionRectHeight, t_currentTransformation,
        // t_bufferImage

        // The actual transformed frame is drawn at paint time.

        // ---

        // update top-left corner position
        this.x = this.x + 
            getTransformedPtX(dRefX, dRefY, this.t_currentTransformation) -
            getTransformedPtX(dRefX, dRefY, transform);

        this.y = this.y +
            getTransformedPtY(dRefX, dRefY, this.t_currentTransformation) -
            getTransformedPtY(dRefX, dRefY, transform);

        // Calculate transformed sprites collision rectangle
        // and transformed width and height
        computeTransformedBounds(transform);

        // set the current transform to be the one requested
        t_currentTransformation = transform;

    }

    /**
     * Calculate transformed sprites collision rectangle
     * and transformed width and height
     * @param transform the desired transform for this <code>Sprite</code>
     */
    private void computeTransformedBounds(int transform) {
        switch (transform) {

        case TRANS_NONE:

            t_collisionRectX = collisionRectX;
            t_collisionRectY = collisionRectY;
            t_collisionRectWidth = collisionRectWidth;
            t_collisionRectHeight = collisionRectHeight;
              this.width = srcFrameWidth;
              this.height = srcFrameHeight;

            break;

        case TRANS_MIRROR:

            // flip across vertical

            // NOTE: top left x and y coordinate must reflect the transformation
            // performed around the reference point

            // the X-offset of the reference point from the top left corner
            // changes.
            t_collisionRectX = srcFrameWidth - 
                               (collisionRectX + collisionRectWidth);

            t_collisionRectY = collisionRectY;
            t_collisionRectWidth = collisionRectWidth;
            t_collisionRectHeight = collisionRectHeight;

            // the Y-offset of the reference point from the top left corner
            // remains the same,
            // top left X-co-ordinate changes

              this.width = srcFrameWidth;
              this.height = srcFrameHeight;

            break;

        case TRANS_MIRROR_ROT180:

            // flip across horizontal

            // NOTE: top left x and y coordinate must reflect the transformation
            // performed around the reference point

            // the Y-offset of the reference point from the top left corner
            // changes
            t_collisionRectY = srcFrameHeight - 
                               (collisionRectY + collisionRectHeight);

            t_collisionRectX = collisionRectX;
            t_collisionRectWidth = collisionRectWidth;
            t_collisionRectHeight = collisionRectHeight;

            // width and height are as before
              this.width = srcFrameWidth;
              this.height = srcFrameHeight;
    
            // the X-offset of the reference point from the top left corner
            // remains the same.
            // top left Y-co-ordinate changes
            
            break;

        case TRANS_ROT90:

            // NOTE: top left x and y coordinate must reflect the transformation
            // performed around the reference point

            // the bottom-left corner of the rectangle becomes the 
            // top-left when rotated 90.

            // both X- and Y-offset to the top left corner may change

            // update the position information for the collision rectangle

            t_collisionRectX = srcFrameHeight - 
                               (collisionRectHeight + collisionRectY);
            t_collisionRectY = collisionRectX;

            t_collisionRectHeight = collisionRectWidth;
            t_collisionRectWidth = collisionRectHeight;

            // set width and height
              this.width = srcFrameHeight;
              this.height = srcFrameWidth;

            break;

        case TRANS_ROT180:

            // NOTE: top left x and y coordinate must reflect the transformation
            // performed around the reference point

            // width and height are as before

            // both X- and Y- offsets from the top left corner may change

            t_collisionRectX = srcFrameWidth - (collisionRectWidth + 
                                                collisionRectX);
            t_collisionRectY = srcFrameHeight - (collisionRectHeight + 
                                                 collisionRectY);

            t_collisionRectWidth = collisionRectWidth;
            t_collisionRectHeight = collisionRectHeight;

              // set width and height
              this.width = srcFrameWidth;
              this.height = srcFrameHeight;

            break;

        case TRANS_ROT270:

            // the top-right corner of the rectangle becomes the 
            // top-left when rotated 270.

            // both X- and Y-offset to the top left corner may change

            // update the position information for the collision rectangle

            t_collisionRectX = collisionRectY;
            t_collisionRectY = srcFrameWidth - (collisionRectWidth + 
                                                collisionRectX);

            t_collisionRectHeight = collisionRectWidth;
            t_collisionRectWidth = collisionRectHeight;

            // set width and height
              this.width = srcFrameHeight;
              this.height = srcFrameWidth;

            break;

        case TRANS_MIRROR_ROT90:

            // both X- and Y- offset from the top left corner may change

            // update the position information for the collision rectangle

            t_collisionRectX = srcFrameHeight - (collisionRectHeight + 
                                                 collisionRectY);
            t_collisionRectY = srcFrameWidth - (collisionRectWidth + 
                                                collisionRectX);

            t_collisionRectHeight = collisionRectWidth;
            t_collisionRectWidth = collisionRectHeight;

            // set width and height
              this.width = srcFrameHeight;
              this.height = srcFrameWidth;

            break;

        case TRANS_MIRROR_ROT270:

            // both X- and Y- offset from the top left corner may change

            // update the position information for the collision rectangle

            t_collisionRectY = collisionRectX;
            t_collisionRectX = collisionRectY;

            t_collisionRectHeight = collisionRectWidth;
            t_collisionRectWidth = collisionRectHeight;

            // set width and height
              this.width = srcFrameHeight;
              this.height = srcFrameWidth;

            break;

        default:
            // INVALID TRANSFORMATION!
            throw new IllegalArgumentException();

        }
    }

    /**
     * Given the x and y offsets off a pixel from the top left
     * corner, in an untransformed sprite, 
     * calculates the x coordinate of the pixel when the same sprite
     * is transformed, with the coordinates of the top-left pixel
     * of the transformed sprite as (0,0).
     *
     * @param inp_x Horizontal offset within the untransformed sprite
     * @param inp_y Vertical offset within the untransformed sprite
     * @param transform transform for the sprite
     * @return The x-offset, of the coordinates of the pixel,
     *         with the top-left corner as 0 when transformed.
     */
    int getTransformedPtX(int inp_x, int inp_y, int transform) {

        int t_x = 0;

          switch (transform) {

          case TRANS_NONE:
             t_x = inp_x;
              break;
          case TRANS_MIRROR:
             t_x = srcFrameWidth - inp_x - 1;
              break;
          case TRANS_MIRROR_ROT180:
             t_x = inp_x;
              break;
          case TRANS_ROT90:
             t_x = srcFrameHeight - inp_y - 1;
              break;
          case TRANS_ROT180:
             t_x = srcFrameWidth - inp_x - 1;
              break;
          case TRANS_ROT270:
             t_x = inp_y;
              break;
          case TRANS_MIRROR_ROT90:
             t_x = srcFrameHeight - inp_y - 1;
              break;
          case TRANS_MIRROR_ROT270:
             t_x = inp_y;
              break;
          default:
              // for safety/completeness.
              Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                             "Sprite: transform=" + transform);
              break;
          }

        return t_x;

    }
    
    /**
     * Given the x and y offsets off a pixel from the top left
     * corner, in an untransformed sprite, 
     * calculates the y coordinate of the pixel when the same sprite
     * is transformed, with the coordinates of the top-left pixel
     * of the transformed sprite as (0,0).
     *
     * @param inp_x Horizontal offset within the untransformed sprite
     * @param inp_y Vertical offset within the untransformed sprite
     * @param transform transform for the sprite
     * @return The y-offset, of the coordinates of the pixel,
     *         with the top-left corner as 0 when transformed.
     */
    int getTransformedPtY(int inp_x, int inp_y, int transform) {

        int t_y = 0;

          switch (transform) {
  
          case TRANS_NONE:
             t_y = inp_y;
              break;
          case TRANS_MIRROR:
             t_y = inp_y;
              break;
          case TRANS_MIRROR_ROT180:
             t_y = srcFrameHeight - inp_y - 1;
              break;
          case TRANS_ROT90:
             t_y = inp_x;
              break;
          case TRANS_ROT180:
             t_y = srcFrameHeight - inp_y - 1;
              break;
          case TRANS_ROT270:
             t_y = srcFrameWidth - inp_x - 1;
              break;
          case TRANS_MIRROR_ROT90:
             t_y = srcFrameWidth - inp_x - 1;
              break;
          case TRANS_MIRROR_ROT270:
             t_y = inp_x;
              break;
          default:
              // for safety/completeness.
              Logging.report(Logging.ERROR, LogChannels.LC_HIGHUI,
                             "Sprite: transform=" + transform);
              break;
          }

        return t_y;

    }
    
    // --- member variables

    /**
     * If this bit is set, it denotes that the transform causes the
     * axes to be interchanged
     */
    private static final int INVERTED_AXES = 0x4;

    /**
     * If this bit is set, it denotes that the transform causes the
     * x axis to be flipped.
     */
    private static final int X_FLIP = 0x2;

    /**
     * If this bit is set, it denotes that the transform causes the
     * y axis to be flipped.
     */
    private static final int Y_FLIP = 0x1;

    /**
     * Bit mask for channel value in ARGB pixel.
     */
    private static final int ALPHA_BITMASK = 0xff000000;

    /**
     * Alpha channel value for full opacity.
     */
    private static final int FULLY_OPAQUE_ALPHA = 0xff000000;

    /**
     * Source image
     */
    Image sourceImage;

    /**
     * The number of frames
     */
    int numberFrames; // = 0;
    
    /**
     * list of X coordinates of individual frames
     */
    int[] frameCoordsX;
    /**
     * list of Y coordinates of individual frames
     */
    int[] frameCoordsY;

    /**
     * Width of each frame in the source image
     */
    int srcFrameWidth;

    /**
     * Height of each frame in the source image
     */
    int srcFrameHeight;
    
    /**
     * The sequence in which to display the Sprite frames
     */
    int[] frameSequence;

    /**
     * The sequence index
     */
    private int sequenceIndex; // = 0

    /**
     * Set to true if custom sequence is used.
     */
    private boolean customSequenceDefined; // = false;


    // -- reference point
    /**
     * Horizontal offset of the reference point
     * from the top left of the sprite.
     */
    int dRefX; // =0

    /**
     * Vertical offset of the reference point
     * from the top left of the sprite.
     */
    int dRefY; // =0

    // --- collision rectangle
   
    /**
     * Horizontal offset of the top left of the collision
     * rectangle from the top left of the sprite.
     */
    int collisionRectX; // =0

    /**
     * Vertical offset of the top left of the collision
     * rectangle from the top left of the sprite.
     */
    int collisionRectY; // =0

    /**
     * Width of the bounding rectangle for collision detection.
     */
    int collisionRectWidth;

    /**
     * Height of the bounding rectangle for collision detection.
     */
    int collisionRectHeight;

    // --- transformation(s)
    // --- values that may change on setting transformations
    // start with t_

    /**
     * The current transformation in effect.
     */
    int t_currentTransformation;

    /**
     * Horizontal offset of the top left of the collision
     * rectangle from the top left of the sprite.
     */
    int t_collisionRectX;

    /**
     * Vertical offset of the top left of the collision
     * rectangle from the top left of the sprite.
     */
    int t_collisionRectY;

    /**
     * Width of the bounding rectangle for collision detection,
     * with the current transformation in effect.
     */
    int t_collisionRectWidth;

    /**
     * Height of the bounding rectangle for collision detection,
     * with the current transformation in effect.
     */
    int t_collisionRectHeight;

}

