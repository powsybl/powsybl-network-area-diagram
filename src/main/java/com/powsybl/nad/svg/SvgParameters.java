/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.svg;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class SvgParameters {

    private Padding diagramPadding = new Padding(2);
    private boolean insertNameDesc = false;
    private boolean svgWidthAndHeightAdded = false;
    private CssLocation cssLocation = CssLocation.INSERTED_IN_SVG;
    private SizeConstraint sizeConstraint = SizeConstraint.NONE;
    private int fixedWidth = -1;
    private int fixedHeight = -1;
    private double arrowShift = 0.3;
    private double arrowLabelShift = 0.19;
    private double converterStationWidth = 0.6;
    private double voltageLevelCircleRadius = 0.3;
    private double fictitiousVoltageLevelCircleRadius = 0.15;
    private double transformerCircleRadius = 0.2;
    private double nodeHollowWidth = 0.15;
    private double edgesForkLength = 0.8;
    private double edgesForkAperture = Math.toRadians(60);
    private double edgeStartShift = 0.02;
    private double unknownBusNodeExtraRadius = 0.1;
    private double loopDistance = 1.2;
    private double loopEdgesAperture = Math.toRadians(60);
    private double loopControlDistance = 0.4;
    private boolean textNodeBackground = true;
    private boolean edgeInfoAlongEdge = true;
    private double interAnnulusSpace = 0.05;
    private String svgPrefix = "";

    public enum CssLocation {
        INSERTED_IN_SVG, EXTERNAL_IMPORTED, EXTERNAL_NO_IMPORT
    }

    public enum SizeConstraint {
        NONE, FIXED_WIDTH, FIXED_HEIGHT
    }

    public SvgParameters() {
    }

    public SvgParameters(SvgParameters other) {
        this.diagramPadding = other.diagramPadding;
        this.insertNameDesc = other.insertNameDesc;
        this.svgWidthAndHeightAdded = other.svgWidthAndHeightAdded;
        this.cssLocation = other.cssLocation;
        this.sizeConstraint = other.sizeConstraint;
        this.fixedWidth = other.fixedWidth;
        this.fixedHeight = other.fixedHeight;
        this.arrowShift = other.arrowShift;
        this.arrowLabelShift = other.arrowLabelShift;
        this.converterStationWidth = other.converterStationWidth;
        this.voltageLevelCircleRadius = other.voltageLevelCircleRadius;
        this.fictitiousVoltageLevelCircleRadius = other.fictitiousVoltageLevelCircleRadius;
        this.transformerCircleRadius = other.transformerCircleRadius;
        this.nodeHollowWidth = other.nodeHollowWidth;
        this.edgesForkLength = other.edgesForkLength;
        this.edgesForkAperture = other.edgesForkAperture;
        this.edgeStartShift = other.edgeStartShift;
        this.unknownBusNodeExtraRadius = other.unknownBusNodeExtraRadius;
        this.loopDistance = other.loopDistance;
        this.loopEdgesAperture = other.loopEdgesAperture;
        this.loopControlDistance = other.loopControlDistance;
        this.textNodeBackground = other.textNodeBackground;
        this.edgeInfoAlongEdge = other.edgeInfoAlongEdge;
        this.svgPrefix = other.svgPrefix;
    }

    public Padding getDiagramPadding() {
        return diagramPadding;
    }

    public SvgParameters setDiagramPadding(Padding padding) {
        this.diagramPadding = Objects.requireNonNull(padding);
        return this;
    }

    public boolean isInsertNameDesc() {
        return insertNameDesc;
    }

    public SvgParameters setInsertNameDesc(boolean insertNameDesc) {
        this.insertNameDesc = insertNameDesc;
        return this;
    }

    public CssLocation getCssLocation() {
        return cssLocation;
    }

    public SvgParameters setCssLocation(CssLocation cssLocation) {
        this.cssLocation = Objects.requireNonNull(cssLocation);
        return this;
    }

    public int getFixedWidth() {
        return fixedWidth;
    }

    public SvgParameters setFixedWidth(int fixedWidth) {
        this.fixedWidth = fixedWidth;
        sizeConstraint = SizeConstraint.FIXED_WIDTH;
        return this;
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public SvgParameters setFixedHeight(int fixedHeight) {
        this.fixedHeight = fixedHeight;
        sizeConstraint = SizeConstraint.FIXED_HEIGHT;
        return this;
    }

    public SizeConstraint getSizeConstraint() {
        return sizeConstraint;
    }

    public SvgParameters setSizeConstraint(SizeConstraint sizeConstraint) {
        this.sizeConstraint = sizeConstraint;
        return this;
    }

    public boolean isSvgWidthAndHeightAdded() {
        return svgWidthAndHeightAdded;
    }

    public SvgParameters setSvgWidthAndHeightAdded(boolean svgWidthAndHeightAdded) {
        this.svgWidthAndHeightAdded = svgWidthAndHeightAdded;
        return this;
    }

    public double getArrowShift() {
        return arrowShift;
    }

    public SvgParameters setArrowShift(double arrowShift) {
        this.arrowShift = arrowShift;
        return this;
    }

    public double getArrowLabelShift() {
        return arrowLabelShift;
    }

    public SvgParameters setArrowLabelShift(double arrowLabelShift) {
        this.arrowLabelShift = arrowLabelShift;
        return this;
    }

    public double getConverterStationWidth() {
        return converterStationWidth;
    }

    public SvgParameters setConverterStationWidth(double converterStationWidth) {
        this.converterStationWidth = converterStationWidth;
        return this;
    }

    public double getVoltageLevelCircleRadius() {
        return voltageLevelCircleRadius;
    }

    public SvgParameters setVoltageLevelCircleRadius(double voltageLevelCircleRadius) {
        this.voltageLevelCircleRadius = voltageLevelCircleRadius;
        return this;
    }

    public double getTransformerCircleRadius() {
        return transformerCircleRadius;
    }

    public SvgParameters setTransformerCircleRadius(double transformerCircleRadius) {
        this.transformerCircleRadius = transformerCircleRadius;
        return this;
    }

    public double getNodeHollowWidth() {
        return nodeHollowWidth;
    }

    public SvgParameters setNodeHollowWidth(double nodeHollowWidth) {
        this.nodeHollowWidth = nodeHollowWidth;
        return this;
    }

    public double getEdgesForkAperture() {
        return edgesForkAperture;
    }

    public SvgParameters setEdgesForkAperture(double edgesForkApertureDegrees) {
        this.edgesForkAperture = Math.toRadians(edgesForkApertureDegrees);
        return this;
    }

    public double getLoopEdgesAperture() {
        return loopEdgesAperture;
    }

    public SvgParameters setLoopEdgesAperture(double loopEdgesApertureDegrees) {
        this.loopEdgesAperture = Math.toRadians(loopEdgesApertureDegrees);
        return this;
    }

    public double getEdgesForkLength() {
        return edgesForkLength;
    }

    public SvgParameters setEdgesForkLength(double edgesForkLength) {
        this.edgesForkLength = edgesForkLength;
        return this;
    }

    public double getEdgeStartShift() {
        return edgeStartShift;
    }

    public SvgParameters setEdgeStartShift(double edgeStartShift) {
        this.edgeStartShift = edgeStartShift;
        return this;
    }

    public double getUnknownBusNodeExtraRadius() {
        return unknownBusNodeExtraRadius;
    }

    public SvgParameters setUnknownBusNodeExtraRadius(double unknownBusNodeExtraRadius) {
        this.unknownBusNodeExtraRadius = unknownBusNodeExtraRadius;
        return this;
    }

    public double getLoopDistance() {
        return loopDistance;
    }

    public SvgParameters setLoopDistance(double loopDistance) {
        this.loopDistance = loopDistance;
        return this;
    }

    public double getFictitiousVoltageLevelCircleRadius() {
        return fictitiousVoltageLevelCircleRadius;
    }

    public SvgParameters setFictitiousVoltageLevelCircleRadius(double fictitiousVoltageLevelCircleRadius) {
        this.fictitiousVoltageLevelCircleRadius = fictitiousVoltageLevelCircleRadius;
        return this;
    }

    public double getLoopControlDistance() {
        return loopControlDistance;
    }

    public SvgParameters setLoopControlDistance(double loopControlDistance) {
        this.loopControlDistance = loopControlDistance;
        return this;
    }

    public boolean isTextNodeBackground() {
        return textNodeBackground;
    }

    public SvgParameters setTextNodeBackground(boolean textNodeBackground) {
        this.textNodeBackground = textNodeBackground;
        return this;
    }

    public boolean isEdgeInfoAlongEdge() {
        return edgeInfoAlongEdge;
    }

    public SvgParameters setEdgeInfoAlongEdge(boolean edgeInfoAlongEdge) {
        this.edgeInfoAlongEdge = edgeInfoAlongEdge;
        return this;
    }

    public double getInterAnnulusSpace() {
        return interAnnulusSpace;
    }

    public SvgParameters setInterAnnulusSpace(double interAnnulusSpace) {
        this.interAnnulusSpace = interAnnulusSpace;
        return this;
    }

    public String getSvgPrefix() {
        return svgPrefix;
    }

    public SvgParameters setSvgPrefix(String svgPrefix) {
        this.svgPrefix = svgPrefix;
        return this;
    }
}
