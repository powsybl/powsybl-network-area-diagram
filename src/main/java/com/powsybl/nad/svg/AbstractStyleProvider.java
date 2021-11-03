/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import com.powsybl.nad.model.BranchEdge;
import com.powsybl.nad.model.Edge;
import com.powsybl.nad.model.Node;
import com.powsybl.nad.model.VoltageLevelNode;
import com.powsybl.sld.styles.BaseVoltageStyle;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public abstract class AbstractStyleProvider implements StyleProvider {

    private static final String CLASSES_PREFIX = "nad-";
    private static final String VOLTAGE_LEVEL_NODES_CLASS = CLASSES_PREFIX + "vl-nodes";
    private static final String DISCONNECTED_SIDE_EDGE_CLASS = CLASSES_PREFIX + "disconnected";

    private final BaseVoltageStyle baseVoltageStyle;

    public AbstractStyleProvider() {
        this(BaseVoltageStyle.fromPlatformConfig());
    }

    public AbstractStyleProvider(BaseVoltageStyle baseVoltageStyle) {
        this.baseVoltageStyle = Objects.requireNonNull(baseVoltageStyle);
    }

    @Override
    public String getStyleDefs() {
        StringBuilder styleSheetBuilder = new StringBuilder("\n");
        for (URL cssUrl : getCssUrls()) {
            try {
                styleSheetBuilder.append(new String(IOUtils.toByteArray(cssUrl), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException("Can't read css file " + cssUrl.getPath(), e);
            }
        }
        return styleSheetBuilder.toString()
                .replace("\r\n", "\n") // workaround for https://bugs.openjdk.java.net/browse/JDK-8133452
                .replace("\r", "\n");
    }

    protected List<URL> getCssUrls() {
        return getCssFilenames().stream()
                .map(n -> getClass().getResource("/" + n))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNodeStyleClasses(Node node) {
        List<String> styles = new ArrayList<>();
        if (node instanceof VoltageLevelNode) {
            double nominalV = ((VoltageLevelNode) node).getNominalV();
            baseVoltageStyle.getBaseVoltageName(nominalV, baseVoltageStyle.getDefaultProfile())
                    .ifPresent(styles::add);
        }
        return styles;
    }

    @Override
    public String getVoltageLevelNodeStyle() {
        return VOLTAGE_LEVEL_NODES_CLASS;
    }

    @Override
    public List<String> getSideEdgeStyleClasses(Edge edge, Edge.Side side) {
        Objects.requireNonNull(side);
        if (edge instanceof BranchEdge && !((BranchEdge) edge).isConnected(side)) {
            return Collections.singletonList(DISCONNECTED_SIDE_EDGE_CLASS);
        }
        return Collections.emptyList();
    }
}
