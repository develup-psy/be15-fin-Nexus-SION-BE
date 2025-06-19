package com.nexus.sion.feature.member.util;

public class Validator {
    public static boolean isPasswordValid(String password) {
        if (password == null)
            return false;
        // 최소 8자, 영문자, 숫자, 특수문자 포함
        String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,}$";
        return password.matches(regex);
    }

    public static boolean isPhonenumberValid(String phoneNumber) {
        if (phoneNumber == null)
            return false;
        // 숫자 11자리, 01로 시작하는 휴대폰 번호만 허용
        return phoneNumber.matches("^01[016789]\\d{7,8}$");
    }

    public static boolean isEmailValid(String email) {
        if (email == null)
            return false;
        // RFC 5322 기준 간단 버전
        String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
}
