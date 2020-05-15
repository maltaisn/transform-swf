/*
 * WAVDecoderEventIT.java
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
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.DataFormatException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.flagstone.transform.Background;
import com.flagstone.transform.DoAction;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieHeader;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.action.BasicAction;
import com.flagstone.transform.datatype.Bounds;
import com.flagstone.transform.datatype.WebPalette;
import com.flagstone.transform.sound.DefineSound;
import com.flagstone.transform.sound.SoundInfo;
import com.flagstone.transform.sound.StartSound;
import com.flagstone.transform.util.sound.SoundFactory;

@RunWith(Parameterized.class)
public final class WAVDecoderEventIT {

	private static final float FRAME_RATE = 12.0f;

    @Parameters
    public static Collection<Object[]> files() {

        final File srcDir = new File("src/test/resources/wav-reference");
        final File destDir = new File(
                "build/integration-results/WAVDecoderEventIT");

        if (!destDir.exists() && !destDir.mkdirs()) {
            fail();
        }

        final FilenameFilter filter = new FilenameFilter() {
            @Override
			public boolean accept(final File directory, final String name) {
                return name.endsWith(".wav");
            }
        };

        final String[] files = srcDir.list(filter);
        final Object[][] collection = new Object[files.length][2];

        for (int i = 0; i < files.length; i++) {
            collection[i][0] = new File(srcDir, files[i]);
            collection[i][1] = new File(destDir,
                    files[i].substring(0, files[i].lastIndexOf('.')) + ".swf");
        }
        return Arrays.asList(collection);
    }

    private final transient File sourceFile;
    private final transient File destFile;

    public WAVDecoderEventIT(final File src, final File dst) {
        sourceFile = src;
        destFile = dst;
    }

    @Test
    public void playSound() throws IOException, DataFormatException {

        try {
            final Movie movie = new Movie();
            int uid = 1;

            final SoundFactory factory = new SoundFactory();
            factory.read(sourceFile);
            final DefineSound sound = factory.defineSound(uid);

            final MovieHeader attrs = new MovieHeader();
            attrs.setFrameSize(new Bounds(0, 0, 8000, 4000));
            attrs.setFrameRate(FRAME_RATE);

            movie.add(attrs);
            movie.add(new Background(WebPalette.LIGHT_BLUE.color()));

            final float duration = ((float) sound.getSampleCount()
                    / (float) sound.getRate());
            final int numberOfFrames = (int) (duration * FRAME_RATE);

            movie.add(sound);
            movie.add(new StartSound(new SoundInfo(sound.getIdentifier(),
                    SoundInfo.Mode.START, 0, null)));

            for (int j = 0; j < numberOfFrames; j++) {
                movie.add(ShowFrame.getInstance());
            }

            final DoAction action = new DoAction();
            action.add(BasicAction.STOP);

            movie.add(action);
            movie.add(ShowFrame.getInstance());

            movie.encodeToFile(destFile);
        } catch (Exception e) {
            if (System.getProperty("test.trace") != null) {
                e.printStackTrace(); //NOPMD
            }
            fail(sourceFile.getPath());
        }
    }
}
