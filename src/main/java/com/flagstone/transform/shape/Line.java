/*
 * Line.java
 * Transform
 *
 * Copyright (c) 2001-2009 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.flagstone.transform.shape;

import java.util.Map;


import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.Encoder;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.ShapeRecord;
import com.flagstone.transform.exception.IllegalArgumentRangeException;

/**
 * Line defines a straight line. The line is drawn from the current drawing
 * point to the end point specified in the Line object which is specified
 * relative to the current drawing point. Once the line is drawn, the end of the
 * line is now the current drawing point.
 *
 * <p>
 * Lines are drawn with rounded corners and line ends. Different join and line
 * end styles can be created by drawing line segments as a sequence of filled
 * shapes. With 1 twip equal to 1/20th of a pixel this technique can easily be
 * used to draw the narrowest of visible lines. In flash 8, SolidLine2 line
 * style was added that supports a range of different mitering options.
 * </p>
 *
 * @see LineStyle
 */
//TODO(class)
public final class Line implements ShapeRecord {

    private static final String FORMAT = "Line: (%d, %d);";

    private transient int xCoord;
    private transient int yCoord;

    private transient boolean vertical;
    private transient boolean general;
    private transient int size;

    /**
     * Creates and initialises a Line object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    // TODO(optimise)
    public Line(final SWFDecoder coder) throws CoderException {
        coder.adjustPointer(2); // shape and edge

        size = coder.readBits(4, false) + 2;

        if (coder.readBits(1, false) == 0) {
            if (coder.readBits(1, false) == 0) {
                xCoord = coder.readBits(size, true);
                yCoord = 0;
            } else {
                xCoord = 0;
                yCoord = coder.readBits(size, true);
            }
        } else {
            xCoord = coder.readBits(size, true);
            yCoord = coder.readBits(size, true);
        }
    }

    /**
     * Creates a Line with the specified relative coordinates.
     *
     * @param xCoord
     *            the x-coordinate of the end point, specified relative to the
     *            current drawing point. Must be in the range -65536..65535.
     * @param yCoord
     *            the y-coordinate of the end point, specified relative to the
     *            current drawing point. Must be in the range -65536..65535.
     */
    public Line(final int xCoord, final int yCoord) {
        setPoint(xCoord, yCoord);
    }

    /**
     * Creates and initialises a Line object using the values copied
     * from another Line object.
     *
     * @param object
     *            a Line object from which the values will be
     *            copied.
     */
    public Line(final Line object) {
        xCoord = object.xCoord;
        yCoord = object.yCoord;
    }

    /**
     * Returns the relative x-coordinate.
     */
    public int getX() {
        return xCoord;
    }

    /**
     * Returns the relative y-coordinate.
     */
    public int getY() {
        return yCoord;
    }

    /**
     * Sets the relative x and y coordinates.
     *
     * @param coordX
     *            the x-coordinate of the end point. Must be in the range
     *            -65536..65535.
     * @param coordY
     *            the y-coordinate of the end point. Must be in the range
     *            -65536..65535.
     */
    public void setPoint(final int coordX, final int coordY) {
        if ((coordX < -65536) || (coordX > 65535)) {
            throw new IllegalArgumentRangeException(-65535, 65535, coordX);
        }
        xCoord = coordX;

        if ((coordY < -65536) || (coordY > 65535)) {
            throw new IllegalArgumentRangeException(-65535, 65535, coordY);
        }
       yCoord = coordY;
    }

    /** TODO(method). */
    public Line copy() {
        return new Line(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, xCoord, yCoord);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        vertical = xCoord == 0;
        general = (xCoord != 0) && (yCoord != 0);
        size = Encoder.maxSize(xCoord, yCoord, 1);

        int numberOfBits = 7;

        if (general) {
            numberOfBits += size << 1;
        } else {
            numberOfBits += 1 + size;
        }

        final Map<Integer, Integer> vars = context.getVariables();
        vars.put(Context.SHAPE_SIZE, vars.get(Context.SHAPE_SIZE)
                + numberOfBits);

        return numberOfBits;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        coder.writeBits(3, 2);
        coder.writeBits(size - 2, 4);
        coder.writeBits(general ? 1 : 0, 1);

        if (general) {
            coder.writeBits(xCoord, size);
            coder.writeBits(yCoord, size);
        } else {
            coder.writeBits(vertical ? 1 : 0, 1);
            coder.writeBits(vertical ? yCoord : xCoord, size);
        }
    }
}