/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.nad.model;

import java.util.Objects;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class Point {

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point() {
        this(0, 0);
    }

    public static Point createMiddlePoint(Point point1, Point point2) {
        Objects.requireNonNull(point1);
        Objects.requireNonNull(point2);
        return new Point(0.5 * (point1.x + point2.x), 0.5 * (point1.y + point2.y));
    }

    public static Point createPointFromRhoTheta(double rho, double thetaDeg) {
        return new Point(rho * Math.cos(Math.toRadians(thetaDeg)),
                rho * Math.sin(Math.toRadians(thetaDeg)));
    }

    public double distanceSquare(Point other) {
        Objects.requireNonNull(other);
        double dx = other.x - x;
        double dy = other.y - y;
        return dx * dx + dy * dy;
    }

    public double distance(Point other) {
        Objects.requireNonNull(other);
        return Math.sqrt(distanceSquare(other));
    }

    public Point shift(double shiftX, double shiftY) {
        return new Point(x + shiftX, y + shiftY);
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public Point atDistance(double dist, Point direction) {
        double r = dist / distance(direction);
        return new Point(x + r * (direction.x - x),
                y + r * (direction.y - y));
    }
}
