/*
 * ScreenVideoIT.java
 * Transform
 *
 * Copyright (c) 2009-2010 Flagstone Software Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  * Neither the name of Flagstone Software Ltd. nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package integration;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import org.junit.Test;

import com.flagstone.transform.Background;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieHeader;
import com.flagstone.transform.Place2;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.util.image.ImageBlocker;
import com.flagstone.transform.util.image.ImageDecoder;
import com.flagstone.transform.util.image.ImageFactory;
import com.flagstone.transform.video.Deblocking;
import com.flagstone.transform.video.DefineVideo;
import com.flagstone.transform.video.ImageBlock;
import com.flagstone.transform.video.ScreenPacket;
import com.flagstone.transform.video.VideoFormat;
import com.flagstone.transform.video.VideoFrame;

@SuppressWarnings({"PMD.ExcessiveMethodLength" })
public final class ScreenVideoIT {

    private static final int BLOCK_WIDTH = 64;
    private static final int BLOCK_HEIGHT = 64;
    private static final VideoFormat CODEC = VideoFormat.SCREEN;
    private static final Deblocking DEBLOCKING = Deblocking.OFF;
    private static final boolean SMOOTHING = false;

    @Test
    public void showPNG() throws IOException, DataFormatException {

        final File sourceDir = new File("src/test/resources/png-screenshots");
        final File destDir = new File(
                "build/integration-results/ScreenVideoIT");

        if (!destDir.exists() && !destDir.mkdirs()) {
            fail();
        }

        final FilenameFilter filter = new FilenameFilter() {
            @Override
			public boolean accept(final File directory, final String name) {
                return name.endsWith(".png");
            }
        };

        final String[] files = sourceDir.list(filter);

        File destFile = null;

        final int numberOfFrames = files.length;

        final ImageFactory factory = new ImageFactory();
        factory.read(new File(sourceDir, files[0]));
        ImageDecoder decoder = factory.getDecoder();

        final ImageBlocker blocker = new ImageBlocker();

        final int screenWidth = decoder.getWidth();
        final int screenHeight = decoder.getHeight();

        Movie movie = new Movie();
        int identifier = 1;

        final MovieHeader attrs = new MovieHeader();
        attrs.setFrameSize(new Bounds(0, 0, screenWidth * 20,
                        screenHeight * 20));
        attrs.setFrameRate(4.0f);

        movie.add(attrs);
        movie.add(new Background(WebPalette.ALICE_BLUE.color()));

        movie.add(new DefineVideo(identifier, numberOfFrames, screenWidth,
                screenHeight, DEBLOCKING, SMOOTHING, CODEC));

        final List<ImageBlock> prev = new ArrayList<ImageBlock>();
        final List<ImageBlock> next = new ArrayList<ImageBlock>();
        List<ImageBlock> delta;

        blocker.getImageAsBlocks(prev, BLOCK_WIDTH, BLOCK_HEIGHT,
                decoder.getWidth(), decoder.getHeight(), decoder.getImage());

        ScreenPacket packet = new ScreenPacket(true, screenWidth, screenHeight,
                BLOCK_WIDTH, BLOCK_HEIGHT, prev);

        movie.add(Place2.show(identifier, 1, 0, 0));
        movie.add(new VideoFrame(identifier, 1, packet.encode()));
        movie.add(ShowFrame.getInstance());

        Place2 place;

        for (int i = 1; i < numberOfFrames; i++) {
            final File srcFile = new File(sourceDir, files[i]);

            factory.read(srcFile);
            decoder = factory.getDecoder();

            blocker.getImageAsBlocks(next, BLOCK_WIDTH, BLOCK_HEIGHT,
                    decoder.getWidth(), decoder.getHeight(),
                    decoder.getImage());

            delta = new ArrayList<ImageBlock>(prev.size());

            for (int j = 0; j < prev.size(); j++) {
                if (prev.get(j).equals(next.get(j))) {
                    delta.add(new ImageBlock(0, 0, new byte[]{}));
                } else {
                    delta.add(next.get(j));
                }
            }

            packet = new ScreenPacket(false, screenWidth, screenHeight,
                    BLOCK_WIDTH, BLOCK_HEIGHT, delta);
            place = Place2.move(1, 0, 0);
            place.setRatio(i);

            movie.add(place);
            movie.add(new VideoFrame(identifier, i + 1, packet.encode()));
            movie.add(ShowFrame.getInstance());
        }

        destFile = new File(destDir, sourceDir.getName() + ".swf");
        movie.encodeToFile(destFile);
    }
}
