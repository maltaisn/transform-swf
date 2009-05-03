/*
 * RemoveObject.java
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

package com.flagstone.transform;

import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTag;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;

/**
 * RemoveObject removes an object from the Flash Player's Display List.
 *
 * <p>
 * An object placed on the display list is displayed in every frame of a movie
 * until it is explicitly removed. Objects must also be removed if its location
 * or appearance is changed using PlaceObject.
 * </p>
 *
 * <p>
 * Although only one object can be placed on any layer in the display list both
 * the object's unique identifier and the layer number must be specified. The
 * RemoveObject class is superseded in Flash 3 by the RemoveObject2 class which
 * lifts this requirement allowing an object to be referenced by the layer
 * number it occupies in the display list.
 * </p>
 *
 * @see Remove2
 * @see Place
 * @see Place2
 */
//TODO(class)
public final class Remove implements MovieTag {

    private static final String FORMAT = "Remove: { identifier=%d; layer=%d }";

    private int identifier;
    private int layer;

    /**
     * Creates and initialises a Remove object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    public Remove(final SWFDecoder coder) throws CoderException {

        if ((coder.readWord(2, false) & 0x3F) == 0x3F) {
            coder.readWord(4, false);
        }

        identifier = coder.readWord(2, false);
        layer = coder.readWord(2, false);
    }

    /**
     * Creates a RemoveObject object that will remove an object with the
     * specified identifier from the given layer in the display list.
     *
     * @param uid
     *            the unique identifier for the object currently on the display
     *            list. Must be in the range 1.65535.
     * @param layer
     *            the layer in the display list where the object is being
     *            displayed. Must be in the range 1.65535.
     */
    public Remove(final int uid, final int layer) {
        setIdentifier(uid);
        setLayer(layer);
    }

    /**
     * Creates and initialises a Remove object using the values copied
     * from another Remove object.
     *
     * @param object
     *            a Remove object from which the values will be
     *            copied.
     */
    public Remove(final Remove object) {
        identifier = object.identifier;
        layer = object.layer;
    }

    /**
     * Returns the identifier of the object to be removed from the display list.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Returns the layer in the display list where the object will be displayed.
     */
    public int getLayer() {
        return layer;
    }

    /**
     * Sets the identifier of the object to be removed.
     *
     * @param uid
     *            the unique identifier for the object currently on the display
     *            list. Must be in the range 1.65535.
     */
    public void setIdentifier(final int uid) {
        if ((uid < 1) || (uid > 65535)) {
            throw new IllegalArgumentException(Strings.IDENTIFIER_RANGE);
        }
        identifier = uid;
    }

    /**
     * Sets the layer in the display list where the object will be displayed.
     *
     * @param aLayer
     *            the layer in the display list where the object is being
     *            displayed. Must be in the range 1.65535.
     */
    public void setLayer(final int aLayer) {
        if ((aLayer < 1) || (aLayer > 65535)) {
            throw new IllegalArgumentException(Strings.LAYER_RANGE);
        }
        layer = aLayer;
    }

    /** TODO(method). */
    public Remove copy() {
        return new Remove(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, layer);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        return 6;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        coder.writeWord((MovieTypes.REMOVE << 6) | 4, 2);
        coder.writeWord(identifier, 2);
        coder.writeWord(layer, 2);
    }
}
