package com.mindborn.day08;

public class RecordTest {
    public static void main(String[] args) {
        Point p1 = new Point(3, 4);
        System.out.println("点：" + p1);
        System.out.println("x坐标：" +p1.x());
        System.out.println("距离原点：" + p1.distanceToOrigin());

        Point origin = Point.origin();
        System.out.println("原点：" + origin);

        Point p2 = new Point(3, 4);
        System.out.println("p1.equals(p2):" + p1.equals(p2));

        try{
            new UserRecord(1L,"", 25, "test@qq.com");
        }catch (IllegalArgumentException e) {
            System.out.println("校验失败：" + e.getMessage());
        }

        UserRecord user = new UserRecord(1L, "张三", 25, "zs@qq.com");
        System.out.println(user);
    }
}
