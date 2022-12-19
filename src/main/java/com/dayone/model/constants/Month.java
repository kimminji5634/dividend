package com.dayone.model.constants;

// Jan, Feb 로 출력되는 걸 1, 2로 출력되도록 만들기 위해 enum 클래스 만든 것임

public enum Month {

    JAN("Jan", 1),
    FEB("Feb", 2),
    MAR("Mar", 3),
    APR("Apr", 4),
    MAY("May", 5),
    JUN("Jun", 6),
    JUL("Jul", 7),
    AUG("Aug", 8),
    SEP("Sep", 9),
    OCT("Oct", 10),
    NOV("Nov", 11),
    DEC("Dec", 12);

    private String s;
    private int number;

    Month(String s, int n) {
        this.s = s;
        this.number = n;
    }

    // s 문자열을 받을 때 그에 해당하는 숫자값 n을 찾아주는 메소드 생성해야함
    public static int strToNumber(String s) {
        // 파라미터와 동일한 값 만나면 그 숫자값을 반환시킴
        for (var m : Month.values()) {
            if (m.s.equals(s)) {
                return m.number;
            }
        }
        // for 문 다 도는데 값 못찾은 경우
        return -1;
    }
}
