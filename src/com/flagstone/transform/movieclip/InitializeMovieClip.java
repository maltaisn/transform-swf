/*
 * Initialize.java
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

package com.flagstone.transform.movieclip;

import java.util.ArrayList;
import java.util.List;

import com.flagstone.transform.Strings;
import com.flagstone.transform.action.ActionData;
import com.flagstone.transform.coder.Action;
import com.flagstone.transform.coder.CoderException;
import com.flagstone.transform.coder.Context;
import com.flagstone.transform.coder.MovieTag;
import com.flagstone.transform.coder.MovieTypes;
import com.flagstone.transform.coder.SWFDecoder;
import com.flagstone.transform.coder.SWFEncoder;
import com.flagstone.transform.coder.SWFFactory;

/**
 * Initialize is used to specify a sequence of actions that are executed to
 * initialise a movie clip before it is displayed.
 *
 * <p>
 * Initialize implements the #initclip pragma defined in the ActionScript
 * language.
 * </p>
 *
 * <p>
 * Unlike the DoAction class which specifies the actions that are executed when
 * a particular frame is displayed the actions contained in an Initialize object
 * are executed only once, regardless of where the object is included in a
 * movie. If a frame containing the Initialize object is played again the
 * actions are skipped. Also there can only be one Initialize object for each
 * movie clip defined in the movie.
 * </p>
 *
 * @see DoAction
 */
//TODO(class)
public final class InitializeMovieClip implements MovieTag {
    private static final String FORMAT = "Initialize: { identifier=%d; actions=%s }";

    private int identifier;
    private List<Action> actions;

    private transient int length;

    /**
     * Creates and initialises an InitializeMovieClip object using values encoded
     * in the Flash binary format.
     *
     * @param coder
     *            an SWFDecoder object that contains the encoded Flash data.
     *
     * @param context
     *            a Context object used to manage the decoders for different
     *            type of object and to pass information on how objects are
     *            decoded.
     *
     * @throws CoderException
     *             if an error occurs while decoding the data.
     */
    public InitializeMovieClip(final SWFDecoder coder, final Context context)
            throws CoderException {
        final int start = coder.getPointer();
        length = coder.readWord(2, false) & 0x3F;

        if (length == 0x3F) {
            length = coder.readWord(4, false);
        }
        final int end = coder.getPointer() + (length << 3);

        identifier = coder.readWord(2, false);
        actions = new ArrayList<Action>();

        final SWFFactory<Action> decoder = context.getRegistry()
                .getActionDecoder();

        if (decoder == null) {
            actions.add(new ActionData(coder.readBytes(new byte[length - 2])));
        } else {
            while (coder.getPointer() < end) {
                actions.add(decoder.getObject(coder, context));
            }
        }

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }

    /**
     * Creates a Initialize object that will initialise the movie clip with the
     * specified identifier with the actions in the array.
     *
     * @param uid
     *            the identifier of the movie clip to initialise. Must be in the
     *            range 1..65535.
     * @param anArray
     *            the array of action objects. Must not be null.
     */
    public InitializeMovieClip(final int uid, final List<Action> anArray) {
        setIdentifier(uid);
        setActions(anArray);
    }

    /**
     * Creates and initialises an InitializeMovieClip object using the values copied
     * from another InitializeMovieClip object.
     *
     * @param object
     *            an InitializeMovieClip object from which the values will be
     *            copied.
     */
    public InitializeMovieClip(final InitializeMovieClip object) {
        identifier = object.identifier;

        actions = new ArrayList<Action>(object.actions.size());

        for (final Action action : object.actions) {
            actions.add(action.copy());
        }
    }

    /**
     * Returns the identifier of the movie clip that will be initialised.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier of the movie clip that will be initialised.
     *
     * @param aNumber
     *            the identifier of the movie clip. The value must be in the
     *            range 1..65535.
     */
    public void setIdentifier(final int aNumber) {
        if ((aNumber < 1) || (aNumber > 65535)) {
            throw new IllegalArgumentException(Strings.IDENTIFIER_RANGE);
        }
        identifier = aNumber;
    }

    /**
     * Adds the action object to the array of actions.
     *
     * @param anAction
     *            an object belonging to a class derived from Action. Must not
     *            be null.
     */
    public InitializeMovieClip add(final Action anAction) {
        if (anAction == null) {
            throw new IllegalArgumentException(Strings.OBJECT_IS_NULL);
        }
        actions.add(anAction);
        return this;
    }

    /**
     * Get the array of actions that are used to initialise the movie clip.
     */
    public List<Action> getActions() {
        return actions;
    }

    /**
     * Set the array of actions of the movie clip that will be initialised.
     *
     * @param anArray
     *            the array of action objects. Must not be null.
     */
    public void setActions(final List<Action> anArray) {
        if (anArray == null) {
            throw new IllegalArgumentException(Strings.ARRAY_IS_NULL);
        }
        actions = anArray;
    }

    /**
     * Creates and returns a deep copy of this object.
     */
    public InitializeMovieClip copy() {
        return new InitializeMovieClip(this);
    }

    @Override
    public String toString() {
        return String.format(FORMAT, identifier, actions);
    }

    /** {@inheritDoc} */
    public int prepareToEncode(final SWFEncoder coder, final Context context) {
        length = 2;

        for (final Action action : actions) {
            length += action.prepareToEncode(coder, context);
        }

        return (length > 62 ? 6 : 2) + length;
    }

    /** {@inheritDoc} */
    public void encode(final SWFEncoder coder, final Context context)
            throws CoderException {
        final int start = coder.getPointer();

        if (length > 62) {
            coder.writeWord((MovieTypes.INITIALIZE << 6) | 0x3F, 2);
            coder.writeWord(length, 4);
        } else {
            coder.writeWord((MovieTypes.INITIALIZE << 6) | length, 2);
        }
        final int end = coder.getPointer() + (length << 3);

        coder.writeWord(identifier, 2);

        for (final Action action : actions) {
            action.encode(coder, context);
        }

        if (coder.getPointer() != end) {
            throw new CoderException(getClass().getName(), start >> 3, length,
                    (coder.getPointer() - end) >> 3);
        }
    }
}
