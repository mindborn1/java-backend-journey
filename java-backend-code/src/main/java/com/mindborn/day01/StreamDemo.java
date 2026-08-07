package com.mindborn.day01;

import java.util.*;
import java.util.stream.Collectors;

public class StreamDemo {
    public static void main(String[] args) {
        List<User> users =Arrays.asList(
                new User(1, "张三", 25, "北京", 15000),
                new User(2, "李四", 30, "上海", 20000),
                new User(3, "王五", 22, "北京", 8000),
                new User(4, "赵六", 35, "深圳", 30000),
                new User(5, "孙七", 28, "上海", 12000),
                new User(6, "周八", 26, "北京", 25000)
        );
        //1.过滤：年龄>25 且薪资>10000
        List<User> filtered = users.stream()
                .filter(u -> u.age() > 25 && u.salary() >10000 )
                .collect(Collectors.toList());
        System.out.println("高新且年龄>25：" + filtered);

        //2.按城市分组，统计每个城市的平均薪资
        Map<String,Double> avgSalaryByCity = users.stream()
                .collect(Collectors.groupingBy(
                        User::city,
                        Collectors.averagingDouble(User::salary)
                ));
        System.out.println("各城市平均薪资：" + avgSalaryByCity);

        //3.找出薪资最高的三个人
        List<User> top3 = users.stream()
                .sorted(Comparator.comparingDouble(User::salary).reversed())
                .limit(3)
                .collect(Collectors.toList());
        System.out.println("薪资 TOP3: " + top3);

        //4.所有用户名拼接成字符串
        String names = users.stream()
                .map(User::name)
                .collect(Collectors.joining(","));
        System.out.println("所有用户：" + names);

        //5.按年龄是否>30分区
        Map<Boolean,List<User>> partitioned = users.stream()
                .collect(Collectors.partitioningBy(u -> u.age() >30));
        System.out.println("分区（>30岁）" + partitioned);
    }
}
