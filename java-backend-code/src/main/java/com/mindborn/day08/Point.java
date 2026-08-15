package com.mindborn.day08;

/**
 * Record 练习：二维坐标点
 */

public record Point(int x, int y ) {
    public  double distanceToOrigin() {
        return Math.sqrt( x * x + y * y );
    }
    public static Point origin() {
        return new Point(0, 0);
    }
}
