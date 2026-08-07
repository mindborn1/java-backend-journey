package com.mindborn.day01;

record User(int id,String name,int age,String city,double salary) {
}

class RecordDemo {
    public static void main(String[] args) {
        User user = new User(1,"张三",25,"北京",15000);
        System.out.println(user);
        System.out.println("姓名：" + user.name());
        System.out.println("城市：" + user.city());
    }
}