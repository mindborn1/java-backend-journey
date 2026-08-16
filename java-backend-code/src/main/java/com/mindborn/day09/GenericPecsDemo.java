package com.mindborn.day09;

import java.util.ArrayList;
import java.util.List;

/**
 * Day09 练习2：泛型通配符与 PECS 原则
 * PECS = Producer-Extends, Consumer-Super
 */
public class GenericPecsDemo {

    // ========== 类层次结构 ==========
    static class Animal{
        void eat() {
            System.out.println("Animal eating");
        }
    }

    // ========== Producer-Extends：安全读取 ====================

    /**
     * 使用 ? extends Animal：可以安全地读取 Animal 及其子类对象
     *
     * 适用于：遍历、获取元素（Producer 生产数据给你用）
     *
     * 不能 add，因为编译器不知道这个 List 底层到底是 List<Dog> 还是 List<Husky>
     * 如果允许 add(new Dog())，万一底层是 List<Husky> 就炸了
     */
    static void printAnimals(List<? extends Animal> animals) {
        // 可以安全读取为 Animal 类型（因为编译器保证：里面肯定是 Animal 或子类）
        for (Animal animal : animals) {
            animal.eat();  // ✅ 安全，Animal 肯定有 eat() 方法
        }

        // animals.add(new Animal()); // ❌ 编译错误！不能写入
        // animals.add(new Dog());    // ❌ 编译错误！
        // animals.add(null);         // ✅ 唯一例外：可以 add null
    }

    // ========== Consumer-Super：安全写入 ====================

    /**
     * 使用 ? super Dog：可以安全地写入 Dog 及其子类对象
     *
     * 适用于：添加元素（Consumer 消费/接收数据）
     *
     * 能 add Dog 和 Husky，因为编译器保证：这个 List 至少是 List<Dog> 或更上层
     * List<Animal> 可以装 Dog，List<Object> 也可以装 Dog
     *
     * 但读取时只能作为 Object，因为编译器不知道具体是 List<Animal> 还是 List<Object>
     */
    static void addDogs(List<? super Dog> dogs) {
        dogs.add(new Dog());      // ✅ 可以添加 Dog
        dogs.add(new Husky());    // ✅ 可以添加 Husky（Dog 的子类）
        // dogs.add(new Animal()); // ❌ 编译错误！不能添加父类

        // 读取只能作为 Object，因为不知道具体上限是什么
        Object obj = dogs.get(0);  // ✅ 只能当 Object 读
        // Animal a = dogs.get(0);  // ❌ 编译错误！可能是 List<Object>
    }

    static class Dog extends Animal{
        void bark() {
            System.out.println("Dog barking");
        }
    }

    static class Husky extends Dog {
        void pullSled() {
            System.out.println("Husky pulling sled");
        }
    }

    // ========== 先思考这个问题 ==========
    public static void main(String[] args) {
        List<Dog> dogs = new ArrayList<>();
        dogs.add(new Dog());
        dogs.add(new Husky());

        // 传入 List<Dog>，方法参数是 List<? extends Animal>，编译通过 ✅
        System.out.println("=== extends 测试 ===");
        printAnimals(dogs);

        // 也可以传 List<Husky>
        List<Husky> huskies = new ArrayList<>();
        huskies.add(new Husky());
        printAnimals(huskies);  // ✅ 也能传

        // ========== super 测试 ==========
        List<Animal> animals = new ArrayList<>();
        System.out.println("=== super 测试 ===");
        addDogs(animals);           // ✅ 传入 List<Animal>，参数是 List<? super Dog>
        System.out.println("animals size = " + animals.size()); // 2

        // 也可以传 List<Dog> 或 List<Object>
        List<Object> objects = new ArrayList<>();
        addDogs(objects);           // ✅ List<Object> 也是 ? super Dog
        System.out.println("objects size = " + objects.size());   // 2
    }
}
