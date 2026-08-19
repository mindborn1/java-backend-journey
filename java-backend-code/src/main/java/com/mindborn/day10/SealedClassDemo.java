package com.mindborn.day10;

/**
 * Sealed Classes 密封类 Demo
 * 场景：简知社区的用户权限体系
 */
public class SealedClassDemo {


    // ========== 1. 定义密封类 ==========

    /**
     * 用户类型 - 密封类
     * 只允许三种用户：普通用户、管理员、VIP用户
     */
    public sealed abstract class User permits NormalUser, Admin, VipUser {
        protected Long id;
        protected String username;

        public User(Long id, String username) {
            this.id = id;
            this.username = username;
        }

        // 每个子类必须实现权限描述
        public abstract String getPermission();

        public String getUsername() {
            return username;
        }
    }

    // ========== 2. 子类实现 ==========

    /**
     * 普通用户 - final，不能再被继承
     */
    public final class NormalUser extends User {
        public NormalUser(Long id, String username) {
            super(id, username);
        }

        @Override
        public String getPermission() {
            return "普通权限：可以浏览文章、发表评论";
        }
    }

    /**
     * 管理员 - final
     */
    public final class Admin extends User {
        public Admin(Long id, String username) {
            super(id, username);
        }
        @Override
        public String getPermission() {
            return "管理权限：可以审核文章、管理用户、查看统计";
        }
    }

    /**
     * VIP用户 - non-sealed，允许继续扩展（比如再分普通VIP/SVIP）
     */
    public non-sealed class VipUser extends User {
        protected int level;  // VIP等级

        public VipUser(Long id, String username, int level) {
            super(id, username);
            this.level = level;
        }

        @Override
        public String getPermission() {
            return "VIP权限(Lv." + level + ")：免广告、优先推荐、专属客服";
        }
    }

    // 可以继续继承 VipUser（因为 VipUser 是 non-sealed）
    public class SvipUser extends VipUser {
        public SvipUser(Long id, String username) {
            super(id, username, 99);
        }

        @Override
        public String getPermission() {
            return "SVIP权限：所有VIP权益 + 1对1导师 + 线下活动";
        }
    }

    // ========== 3. 模式匹配 switch ==========

    /**
     * 根据用户类型返回不同的欢迎语
     * 编译器会自动检查是否覆盖所有 permitted 子类
     */
    public String welcome(User user) {
        return switch (user) {
            case NormalUser u -> "欢迎，" + u.getUsername() + "！开始你的学习之旅吧~";
            case Admin a -> "管理员 " + a.getUsername() + "，辛苦了！";
            case VipUser v -> "尊贵的VIP " + v.getUsername() + "，欢迎回来！";
            // 不需要 default，因为 User 只 permits 这三个
        };
    }

    // ========== 4. main 测试 ==========

    public static void main(String[] args) {
        SealedClassDemo demo = new SealedClassDemo();

        User normal = demo.new NormalUser(1L, "小明");
        User admin = demo.new Admin(2L, "管理员A");
        User vip = demo.new VipUser(3L, "张三", 3);
        User svip = demo.new SvipUser(4L, "李四");

        System.out.println("=== 权限描述 ===");
        System.out.println(normal.getUsername() + ": " + normal.getPermission());
        System.out.println(admin.getUsername() + ": " + admin.getPermission());
        System.out.println(vip.getUsername() + ": " + vip.getPermission());
        System.out.println(svip.getUsername() + ": " + svip.getPermission());

        System.out.println("\n=== 欢迎语 ===");
        System.out.println(demo.welcome(normal));
        System.out.println(demo.welcome(admin));
        System.out.println(demo.welcome(vip));
        System.out.println(demo.welcome(svip));
    }
}
