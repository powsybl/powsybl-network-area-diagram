/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgParameters {

    private Padding diagramPadding = new Padding(2);
    private boolean insertName = false;
    private boolean svgWidthAndHeightAdded = false;

    public Padding getDiagramPadding() {
        return diagramPadding;
    }

    public void setDiagramPadding(Padding padding) {
        this.diagramPadding = padding;
    }

    public boolean isInsertName() {
        return insertName;
    }

    public void setInsertName(boolean insertName) {
        this.insertName = insertName;
    }

    public boolean isSvgWidthAndHeightAdded() {
        return svgWidthAndHeightAdded;
    }

    public void setSvgWidthAndHeightAdded(boolean svgWidthAndHeightAdded) {
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
    }

}
