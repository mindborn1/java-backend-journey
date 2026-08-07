package com.mindborn.day01;

public class SwitchDemo {
    public static void main(String[] args) {
        String day = "MONDAY";

        int numLetters = switch (day) {
            case "MONDAY" , "FRIDY" , "SUNDAY" -> 6;
            case "TUESDAY" ->7;
            case "WEDNESDAY" , "SATURDAY" ->8;
            case "THURSDAY" ->9;
            default -> {
                System.out.println("未知星期");
                yield 0;
            }
        };

        System.out.println(day + "有" + numLetters + " 个字母");

        int score = 85;
        String grade = switch (score / 10) {
            case 10 ,9 ->"A";
            case 8 ->"B";
            case 7 ->"C";
            case 6 ->"D";
            default -> "F";
        };
        System.out.println("分数" + score + " 的等级是" +grade);
    }
}
