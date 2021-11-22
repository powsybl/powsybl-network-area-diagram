package com.powsybl.nad.model;

import java.util.List;

public interface BranchEdge extends Edge {
    enum Side {
        ONE, TWO
    }

    List<Point> getLine(Side side);

    List<Point> getSide1();

    List<Point> getSide2();

    void setSide1(Point... points);

    void setSide2(Point... points);
}
