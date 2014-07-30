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

/**
 * A TiledLayer is a visual element composed of a grid of cells that
 * can be filled with a set of
 * tile images.  This class allows large virtual layers to be created
 * without the need for an
 * extremely large Image.  This technique is commonly used in 2D
 * gaming platforms to create
 * very large scrolling backgrounds,
 * <P>
 * <h3>Tiles</h3>
 * The tiles used to fill the TiledLayer's cells are provided in a
 * single Image object which
 * may be mutable or immutable.  The Image is broken up into a series
 * of equally-sized tiles;
 * the tile size is specified along with the Image.  As shown in the
 * figure below, the same
 * tile set can be stored in several different arrangements depending
 * on what is the most
 * convenient for the game developer.  
 * <br>
 * <center><img src="doc-files/tiles.gif" width=588 height=412
 *   ALT="Tiles"></center>
 * <br>
 * Each tile is assigned a unique index number.  The tile located in
 * the upper-left corner
 * of the Image is assigned an index of 1.  The remaining tiles are
 * then numbered consecutively
 * in row-major order (indices are assigned across the first row, then
 * the second row, and so on).
 * These tiles are regarded as <em>static tiles</em> because there is
 * a fixed link between
 * the tile and the image data associated with it.
 * <P> 
 * A static tile set is created when the TiledLayer is instantiated;
 * it can also be updated
 * at any time using the {@link #setStaticTileSet} method.
 * <P>
 * In addition to the static tile set, the developer can also define
 * several <em>animated tiles</em>.
 * An animated tile is a virtual tile that is dynamically associated
 * with a static tile; the appearance
 * of an animated tile will be that of the static tile that it is
 * currently associated with.
 * <P>
 * Animated tiles allow the developer to change the appearance of a
 * group of cells
 * very easily.  With the group of cells all filled with the animated
 * tile, the appearance
 * of the entire group can be changed by simply changing the static
 * tile associated with the
 * animated tile.  This technique is very useful for animating large
 * repeating areas without
 * having to explicitly change the contents of numerous cells.
 * <P>
 * Animated tiles are created using the {@link #createAnimatedTile}
 * method, which returns the
 * index to be used for the new animated tile.  The animated tile
 * indices are always negative
 * and consecutive, beginning with -1.  Once created, the static tile
 * associated with an
 * animated tile can be changed using the {@link #setAnimatedTile}
 * method.
 * <P>
 * <h3>Cells</h3>
 * The TiledLayer's grid is made up of equally sized cells; the number
 * of rows and
 * columns in the grid are specified in the constructor, and the
 * physical size of the cells
 * is defined by the size of the tiles.  
 * <P>
 * The contents of each cell is specified by means of a tile index; a
 * positive tile index refers
 * to a static tile, and a negative tile index refers to an animated
 * tile.  A tile index of 0
 * indicates that the cell is empty; an empty cell is fully
 * transparent and nothing is drawn
 * in that area by the TiledLayer.  By default, all cells contain tile
 * index 0.
 * <P>
 * The contents of cells may be changed using {@link #setCell} and
 * {@link #fillCells}.  Several
 * cells may contain the same tile; however, a single cell cannot
 * contain more than one tile.
 * The following example illustrates how a simple background can be
 * created using a TiledLayer.
 * <br>
 * <center><img src="doc-files/grid.gif" width=735 height=193
 * ALT="TiledLayer Grid"></center>
 * <br>
 * In this example, the area of water is filled with an animated tile
 * having an index of -1, which
 * is initially associated with static tile 5.  The entire area of
 * water may be animated by simply
 * changing the associated static tile using <code>setAnimatedTile(-1,
 * 7)</code>.
 * <br>
 * <center><img src="doc-files/grid2.gif" width=735 height=193
 * ALT="TiledLayer Grid 2"></center>
 * <br>
 * <P>
 * <h3>Rendering a TiledLayer</h3>
 * A TiledLayer can be rendered by manually calling its paint method;
 * it can also be rendered
 * automatically using a LayerManager object.
 * <P>
 * The paint method will attempt to render the entire TiledLayer
 * subject to the
 * clip region of the Graphics object; the upper left corner of the
 * TiledLayer is rendered at
 * its current (x,y) position relative to the Graphics object's
 * origin.  The rendered region
 * may be controlled by setting the clip region of the Graphics object
 * accordingly.
 * <P>
 */
public class TiledLayer extends Layer {

    /**
     * Creates a new TiledLayer.  <p>
     *
     * The TiledLayer's grid will be <code>rows</code> cells high and
     * <code>columns</code> cells wide.  All cells in the grid are initially
     * empty (i.e. they contain tile index 0).  The contents of the grid may
     * be modified through the use of {@link #setCell} and {@link #fillCells}.
     * <P>
     * The static tile set for the TiledLayer is created from the specified
     * Image with each tile having the dimensions of tileWidth x tileHeight.
     * The width of the source image must be an integer multiple of
     * the tile width, and the height of the source image must be an integer
     * multiple of the tile height; otherwise, an IllegalArgumentException
     * is thrown;<p>
     *
     * The entire static tile set can be changed using 
     * {@link  #setStaticTileSet(Image, int, int)}.
     * These methods should be used sparingly since they are both
     * memory and time consuming.
     * Where possible, animated tiles should be used instead to
     * animate tile appearance.<p>
     *
     * @param columns the width of the <code>TiledLayer</code>,
     * expressed as a number of cells
     * @param rows the height of the <code>TiledLayer</code>,
     * expressed as a number of cells
     * @param image the <code>Image</code> to use for creating
     *  the static tile set
     * @param tileWidth the width in pixels of a single tile
     * @param tileHeight the height in pixels of a single tile
     * @throws NullPointerException if <code>image</code> is <code>null</code>
     * @throws IllegalArgumentException if the number of <code>rows</code>
     *  or <code>columns</code> is less than <code>1</code>
     * @throws IllegalArgumentException if <code>tileHeight</code>
     *  or <code>tileWidth</code> is less than <code>1</code>
     * @throws IllegalArgumentException if the <code>image</code>
     *  width is not an integer multiple of the <code>tileWidth</code>
     * @throws IllegalArgumentException if the <code>image</code>
     * height is not an integer multiple of the <code>tileHeight</code>
     */
    public TiledLayer(int columns, int rows, Image image, int tileWidth,
		      int tileHeight) {
	// IllegalArgumentException will be thrown 
	// in the Layer super-class constructor
        super(columns < 1 || tileWidth < 1 ? -1 : columns * tileWidth, 
	         rows < 1 || tileHeight < 1 ? -1 : rows * tileHeight);

        // if img is null img.getWidth() will throw NullPointerException
        if (((image.getWidth() % tileWidth) != 0) || 
            ((image.getHeight() % tileHeight) != 0)) {
             throw new IllegalArgumentException();
	}
        this.columns = columns;
	this.rows = rows;

        cellMatrix = new int[rows][columns];

        int noOfFrames = 
            (image.getWidth() / tileWidth) * (image.getHeight() / tileHeight);
        // the zero th index is left empty for transparent tile
        // so it is passed in  createStaticSet as noOfFrames + 1
        // Also maintain static indices is true
	// all elements of cellMatrix[][] 
	// are set to zero by new, so maintainIndices = true
        createStaticSet(image,  noOfFrames + 1, tileWidth, tileHeight, true);
    }

    /**
     * Creates a new animated tile and returns the index that refers
     * to the new animated tile.  It is initially associated with
     * the specified tile index (either a static tile or 0).
     * <P>
     * The indices for animated tiles are always negative.  The first
     * animated tile shall have the index -1, the second, -2, etc.  
     *
     * @param staticTileIndex the index of the associated tile 
     * (must be <code>0</code> or a valid static tile index)
     * @return the index of newly created animated tile
     * @throws IndexOutOfBoundsException if the 
     * <code>staticTileIndex</code> is invalid
     */
    public int createAnimatedTile(int staticTileIndex) {
        // checks static tile 
        if (staticTileIndex < 0 || staticTileIndex >= numberOfTiles) { 
	    throw new IndexOutOfBoundsException();
	}

        if (anim_to_static == null) {
	    anim_to_static = new int[4];
	    numOfAnimTiles = 1;
        } else if (numOfAnimTiles == anim_to_static.length) {
	    // grow anim_to_static table if needed 
	    int new_anim_tbl[] = new int[anim_to_static.length * 2];
	    System.arraycopy(anim_to_static, 0, 
                         new_anim_tbl, 0, anim_to_static.length);
	    anim_to_static = new_anim_tbl;
	}
	anim_to_static[numOfAnimTiles] = staticTileIndex;
	numOfAnimTiles++;
        return (-(numOfAnimTiles - 1));
    }

    /**
     * Associates an animated tile with the specified static tile.  <p>
     *
     * @param animatedTileIndex the index of the animated tile
     * @param staticTileIndex the index of the associated tile
     * (must be <code>0</code> or a valid static tile index)
     * @throws IndexOutOfBoundsException if the 
     * <code>staticTileIndex</code> is invalid
     * @throws IndexOutOfBoundsException if the animated tile index
     * is invalid
     * @see #getAnimatedTile
     *
     */
    public void setAnimatedTile(int animatedTileIndex, int staticTileIndex) {
        // checks static tile
        if (staticTileIndex < 0 || staticTileIndex >= numberOfTiles) {  
	    throw new IndexOutOfBoundsException();
	}
        // do animated tile index check
	animatedTileIndex = - animatedTileIndex;
	if (anim_to_static == null || animatedTileIndex <= 0 
            || animatedTileIndex >= numOfAnimTiles) { 
	    throw new IndexOutOfBoundsException();
        }

        anim_to_static[animatedTileIndex] = staticTileIndex;

    }

    /**
     * Gets the tile referenced by an animated tile.  <p>
     *
     * Returns the tile index currently associated with the
     * animated tile.
     *
     * @param animatedTileIndex the index of the animated tile
     * @return the index of the tile reference by the animated tile
     * @throws IndexOutOfBoundsException if the animated tile index
     * is invalid
     * @see #setAnimatedTile
     */
    public int getAnimatedTile(int animatedTileIndex) {
        animatedTileIndex = - animatedTileIndex;
        if (anim_to_static == null || animatedTileIndex <= 0 
                   || animatedTileIndex >= numOfAnimTiles) { 
	    throw new IndexOutOfBoundsException();
        }
	
        return anim_to_static[animatedTileIndex];
    }

    /**
     * Sets the contents of a cell.  <P>
     *
     * The contents may be set to a static tile index, an animated
     * tile index, or it may be left empty (index 0)
     * @param col the column of cell to set
     * @param row the row of cell to set
     * @param tileIndex the index of tile to place in cell
     * @throws IndexOutOfBoundsException if there is no tile with index
     *         <code>tileIndex</code>
     * @throws IndexOutOfBoundsException if <code>row</code> or
     *         <code>col</code> is outside the bounds of the 
     *         <code>TiledLayer</code> grid
     * @see #getCell
     * @see #fillCells
     */
    public void setCell(int col, int row, int tileIndex) {

        if (col < 0 || col >= this.columns || row < 0 || row >= this.rows) {
            throw new IndexOutOfBoundsException();
        }

	if (tileIndex > 0) {
            // do checks for static tile 
            if (tileIndex >= numberOfTiles) { 
	        throw new IndexOutOfBoundsException();
	    }
	} else if (tileIndex < 0) {
            // do animated tile index check
	    if (anim_to_static == null ||
                (-tileIndex) >= numOfAnimTiles) { 
	        throw new IndexOutOfBoundsException();
            }
	}

        cellMatrix[row][col] = tileIndex;
 
    }

    /**
     * Gets the contents of a cell.  <p>
     *
     * Gets the index of the static or animated tile currently displayed in
     * a cell.  The returned index will be 0 if the cell is empty.
     *
     * @param col the column of cell to check
     * @param row the row of cell to check
     * @return the index of tile in cell
     * @throws IndexOutOfBoundsException if <code>row</code> or
     *         <code>col</code> is outside the bounds of the 
     *         <code>TiledLayer</code> grid
     * @see #setCell
     * @see #fillCells
     */
    public int getCell(int col, int row) {
        if (col < 0 || col >= this.columns || row < 0 || row >= this.rows) {
            throw new IndexOutOfBoundsException();
        }
        return cellMatrix[row][col];
    }

    /**
     * Fills a region cells with the specific tile.  The cells may be filled
     * with a static tile index, an animated tile index, or they may be left 
     * empty (index <code>0</code>). 
     *
     * @param col the column of top-left cell in the region
     * @param row the row of top-left cell in the region
     * @param numCols the number of columns in the region
     * @param numRows the number of rows in the region
     * @param tileIndex the Index of the tile to place in all cells in the 
     * specified region
     * @throws IndexOutOfBoundsException if the rectangular region
     *         defined by the parameters extends beyond the bounds of the
     *         <code>TiledLayer</code> grid
     * @throws IllegalArgumentException if <code>numCols</code> is less
     * than zero
     * @throws IllegalArgumentException if <code>numRows</code> is less
     * than zero
     * @throws IndexOutOfBoundsException if there is no tile with
     *         index <code>tileIndex</code>
     * @see #setCell
     * @see #getCell
     */
    public void fillCells(int col, int row, int numCols, int numRows,
                          int tileIndex) {


	if (numCols < 0 || numRows < 0) {
            throw new IllegalArgumentException();
	}

        if (col < 0 || col >= this.columns || row < 0 || row >= this.rows ||
	    col + numCols > this.columns || row + numRows > this.rows) {
            throw new IndexOutOfBoundsException();
        }

	if (tileIndex > 0) {
            // do checks for static tile 
            if (tileIndex >= numberOfTiles) { 
	        throw new IndexOutOfBoundsException();
	    }
	} else if (tileIndex < 0) {
            // do animated tile index check
	    if (anim_to_static == null || 
                (-tileIndex) >= numOfAnimTiles) { 
	            throw new IndexOutOfBoundsException();
            }
	}

        for (int rowCount = row; rowCount < row + numRows; rowCount++) {
            for (int columnCount = col; 
                     columnCount < col + numCols; columnCount++) {
                cellMatrix[rowCount][columnCount] = tileIndex;
            }
        }
    }


    /**
     * Gets the width of a single cell, in pixels.
     * @return the width in pixels of a single cell in the 
     * <code>TiledLayer</code> grid
     */
    public final int getCellWidth() {
        return cellWidth;
    }

    /**
     * Gets the height of a single cell, in pixels.
     * @return the height in pixels of a single cell in the 
     * <code>TiledLayer</code> grid
     */
    public final int getCellHeight() {
        return cellHeight;
    }

    /**
     * Gets the number of columns in the TiledLayer grid. 
     * The overall width of the TiledLayer, in pixels, 
     * may be obtained by calling {@link #getWidth}.
     * @return the width in columns of the 
     * <code>TiledLayer</code> grid
     */
    public final int getColumns() {
        return columns;
    }

    /**
     * Gets the number of rows in the TiledLayer grid.  The overall
     * height of the TiledLayer, in pixels, may be obtained by
     * calling {@link #getHeight}.
     * @return the height in rows of the 
     * <code>TiledLayer</code> grid
     */
    public final int getRows() {
        return rows;
    }

    /**
     * Change the static tile set.  <p>
     *
     * Replaces the current static tile set with a new static tile set.
     * See the constructor {@link #TiledLayer(int, int, Image, int, int)}
     * for information on how the tiles are created from the
     * image.<p>
     *
     * If the new static tile set has as many or more tiles than the
     * previous static tile set,
     * the the animated tiles and cell contents will be preserve.  If
     * not, the contents of
     * the grid will be cleared (all cells will contain index 0) and
     * all animated tiles
     * will be deleted.
     * <P>
     * @param image the <code>Image</code> to use for creating the
     * static tile set
     * @param tileWidth the width in pixels of a single tile
     * @param tileHeight the height in pixels of a single tile
     * @throws NullPointerException if <code>image</code> is <code>null</code>
     * @throws IllegalArgumentException if <code>tileHeight</code>
     *  or <code>tileWidth</code> is less than <code>1</code>
     * @throws IllegalArgumentException if the <code>image</code>
     *  width is not an integer  multiple of the <code>tileWidth</code>
     * @throws IllegalArgumentException if the <code>image</code>
     *  height is not an integer  multiple of the <code>tileHeight</code>
     */
    public void setStaticTileSet(Image image, int tileWidth, int tileHeight) {
        // if img is null img.getWidth() will throw NullPointerException
        if (tileWidth < 1 || tileHeight < 1 ||
	    ((image.getWidth() % tileWidth) != 0) || 
            ((image.getHeight() % tileHeight) != 0)) {
             throw new IllegalArgumentException();
	}
        setWidthImpl(columns * tileWidth);
	setHeightImpl(rows * tileHeight);

        int noOfFrames = 
          (image.getWidth() / tileWidth) * (image.getHeight() / tileHeight);

        // the zero th index is left empty for transparent tile
        // so it is passed in  createStaticSet as noOfFrames + 1

	if (noOfFrames >= (numberOfTiles - 1)) {
	    // maintain static indices
	    createStaticSet(image, noOfFrames + 1, tileWidth, tileHeight, true);
	} else {
            createStaticSet(image, noOfFrames + 1, tileWidth, 
                                tileHeight, false);
	}	
    }

    /**
     * Draws the TiledLayer.  
     *
     * The entire TiledLayer is rendered subject to the clip region of
     * the Graphics object.
     * The TiledLayer's upper left corner is rendered at the
     * TiledLayer's current
     * position relative to the origin of the Graphics object.   The current
     * position of the TiledLayer's upper-left corner can be retrieved by 
     * calling {@link #getX()} and {@link #getY()}.
     * The appropriate use of a clip region and/or translation allows
     * an arbitrary region
     * of the TiledLayer to be rendered.
     * <p>
     * If the TiledLayer's Image is mutable, the TiledLayer is rendered 
     * using the current contents of the Image.
     * @param g the graphics object to draw the <code>TiledLayer</code>
     * @throws NullPointerException if <code>g</code> is <code>null</code>
     */
    public final void paint(Graphics g) {

        if (g == null) {
            throw new NullPointerException();
        }

        if (visible) {
	    int startColumn = 0;
	    int endColumn = this.columns;
	    int startRow = 0;
	    int endRow = this.rows;

	    // calculate the number of columns left of the clip
	    int number = (g.getClipX() - this.x) / cellWidth;
	    if (number > 0) {
		startColumn = number;
	    }

	    // calculate the number of columns right of the clip
	    int endX = this.x + (this.columns * cellWidth);
	    int endClipX = g.getClipX() + g.getClipWidth();
	    number = (endX - endClipX) / cellWidth;
	    if (number > 0) {
		endColumn -= number;
	    }

	    // calculate the number of rows above the clip
	    number = (g.getClipY() - this.y) / cellHeight;
	    if (number > 0) {
		startRow = number;
	    }

	    // calculate the number of rows below the clip
	    int endY = this.y + (this.rows * cellHeight);
	    int endClipY = g.getClipY() + g.getClipHeight();
	    number = (endY - endClipY) / cellHeight;
	    if (number > 0) {
		endRow -= number;
	    }

	    // paint all visible cells
	    int tileIndex = 0;

	    // y-coordinate
	    int ty        = this.y + (startRow * cellHeight);
            for (int row = startRow; 
		 row < endRow; row++, ty += cellHeight) {

	        // reset the x-coordinate at the beginning of every row
                // x-coordinate to draw tile into
	        int tx = this.x + (startColumn * cellWidth);
                for (int column = startColumn; column < endColumn; 
		    column++, tx += cellWidth) {
		
                    tileIndex = cellMatrix[row][column];
	            // check the indices 
		    // if animated get the corresponding 
		    // static index from anim_to_static table
		    if (tileIndex == 0) { // transparent tile
			continue;
                    } else if (tileIndex < 0) {
                        tileIndex = getAnimatedTile(tileIndex);
		    }

		    g.drawRegion(sourceImage, 
				 tileSetX[tileIndex], 
				 tileSetY[tileIndex], 
				 cellWidth, cellHeight,
				 Sprite.TRANS_NONE,
				 tx, ty,
				 Graphics.TOP | Graphics.LEFT);

                }
            }
	}
    }

    // private implementation

    /**
     * create the Image Array.
     *
     * @param image Image to use for creating the static tile set
     * @param noOfFrames total number of frames
     * @param tileWidth The width, in pixels, of a single tile
     * @param tileHeight The height, in pixels, of a single tile
     * @param maintainIndices 
     */

    private void createStaticSet(Image image, int noOfFrames, int tileWidth, 
                      int tileHeight, boolean maintainIndices) {

        cellWidth = tileWidth;
        cellHeight = tileHeight;

	int imageW = image.getWidth();
	int imageH = image.getHeight();

	sourceImage = image;

	numberOfTiles = noOfFrames;
	tileSetX = new int[numberOfTiles];
	tileSetY = new int[numberOfTiles];
	
	if (!maintainIndices) {
            // populate cell matrix, all the indices are 0 to begin with
            for (rows = 0; rows < cellMatrix.length; rows++) {
                int totalCols = cellMatrix[rows].length;
                for (columns = 0; columns < totalCols; columns++) {
                    cellMatrix[rows][columns] = 0;
                }
            }
	    // delete animated tiles
	    anim_to_static = null;
	} 

        int currentTile = 1;

        for (int locY = 0; locY < imageH; locY += tileHeight) {
            for (int locX = 0; locX < imageW; locX += tileWidth) {

		tileSetX[currentTile] = locX;
		tileSetY[currentTile] = locY;

                currentTile++;
            }
        }
    }

    /** 
     * the overall height of the TiledLayer grid
     */
    private int cellHeight; // = 0;
    /** 
     * the overall cell width of the TiledLayer grid
     */
    private int cellWidth; // = 0;

    /** 
     * The num of rows of the TiledLayer grid.
     */
    private int rows; // = 0;

    /** 
     * the num of columns in the TiledLayer grid
     */
    private int columns; // = 0;

    /** 
     * int array for storing row and column of cell
     *
     * it contains the tile Index for both static and animated tiles
     */
    private int[][] cellMatrix; // = null;

    /**
     * Source image for tiles
     */
    // package access as it is used by Pixel level Collision
    // detection with a Sprite
    Image sourceImage; // = null;
    
    /**
     * no. of tiles
     */
    private int numberOfTiles; // = 0;

    /**
     * X co-ordinate definitions for individual frames into the source image
     */
    // package access as it is used by Pixel level Collision
    // detection with a Sprite
    int[] tileSetX;

    /**
     * Y co-ordinate definitions for individual frames into the source image
     */ 
    // package access as it is used by Pixel level Collision
    // detection with a Sprite
    int[] tileSetY;
    
    /** 
     * Table to map from animated Index to static Index
     * 0th location is unused.
     * anim --> static Index
     * -1 --> 21
     * -2 --> 34
     * -3 --> 45
     * for now keep 0 the location of the table empty instead of computing
     * -index make index +ve and access this Table.
     *  
     */
    private int[] anim_to_static; // = null;

    /** 
     * total number of animated tiles. This variable is also used as 
     * index in the above table to add new entries to the anim_to_static table.
     * initialized to 1 when table is created.
     */
    private int numOfAnimTiles; // = 0

}
