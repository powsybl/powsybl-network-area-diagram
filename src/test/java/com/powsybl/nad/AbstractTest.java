/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad;

import com.google.common.io.ByteStreams;
import com.powsybl.iidm.network.Network;
import com.powsybl.nad.build.iidm.IntIdProvider;
import com.powsybl.nad.build.iidm.NetworkGraphBuilder;
import com.powsybl.nad.layout.ForcedLayout;
import com.powsybl.nad.layout.LayoutParameters;
import com.powsybl.nad.model.Graph;
import com.powsybl.nad.svg.StyleProvider;
import com.powsybl.nad.svg.SvgParameters;
import com.powsybl.nad.svg.SvgWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractTest {

    protected boolean debugSvg = false;
    protected boolean overrideTestReferences = false;

    protected abstract LayoutParameters getLayoutParameters();

    protected abstract SvgParameters getSvgParameters();

    protected abstract StyleProvider getStyleProvider();

    protected String generateSvgString(Network network, String refFilename) {
        Graph graph = new NetworkGraphBuilder(network, new IntIdProvider()).buildGraph();
        new ForcedLayout().run(graph, getLayoutParameters());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new SvgWriter(getSvgParameters(), getStyleProvider()).writeSvg(graph, baos);
        if (debugSvg) {
            writeToHomeDir(refFilename, baos);
        }
        if (overrideTestReferences) {
            overrideTestReference(refFilename, baos);
        }
        return baos.toString();
    }

    private void writeToHomeDir(String refFilename, ByteArrayOutputStream baos) {
        try (OutputStream fos = new FileOutputStream(new File(System.getProperty("user.home"), refFilename))) {
            baos.writeTo(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void overrideTestReference(String filename, ByteArrayOutputStream baos) {
        File testReference = new File("src/test/resources", filename);
        if (!testReference.exists()) {
            return;
        }
        try (OutputStream os = new FileOutputStream(testReference)) {
            baos.writeTo(os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected String toString(String resourceName) {
        try {
            InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(resourceName));
            return normalizeLineSeparator(new String(ByteStreams.toByteArray(in), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String normalizeLineSeparator(String str) {
        return str.replace("\r\n", "\n")
                .replace("\r", "\n");
    }
}
