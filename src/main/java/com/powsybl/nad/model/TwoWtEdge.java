package com.powsybl.nad.model;

public class TwoWtEdge extends AbstractBranchEdge {

    private final double nominalV1;
    private final double nominalV2;

    public TwoWtEdge(String diagramId, String equipmentId, String nameOrId, boolean side1Connected, boolean side2Connected,
                     double nominalV1, double nominalV2) {
        super(diagramId, equipmentId, nameOrId, side1Connected, side2Connected);
        this.nominalV1 = nominalV1;
        this.nominalV2 = nominalV2;
    }

    public double getNominalV1() {
        return nominalV1;
    }

    public double getNominalV2() {
        return nominalV2;
    }

    public double getNominalV(Side side) {
        return side == Side.ONE ? nominalV1 : nominalV2;
    }
}
