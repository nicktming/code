package com.sourcecode.atomic_AtomicInteger;

public class AtomicReferenceTest {
    public static AtomicReference<User> atomicUserRef = new AtomicReference<User>();
    public static void main(String[] args) {
        User user = new User("conan", 15);
        atomicUserRef.set(user); // 非原子类操作
        User updateUser = new User("Shinichi", 17);
        atomicUserRef.compareAndSet(user, updateUser); // 原子类操作
        System.out.println(atomicUserRef.get().getName());
        System.out.println(atomicUserRef.get().getOld());
    }
    static class User {
        private String name;
        private int old;
        public User(String name, int old) {
            this.name = name;
            this.old = old;
        }
        public String getName() {
            return name;
        }
        public int getOld() {
            return old;
        }
    }
}
