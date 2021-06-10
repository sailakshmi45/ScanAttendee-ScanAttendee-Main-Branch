package com.globalnest.scanattendee;

import com.globalnest.utils.Util;

public class Sai {
    public static void main(String []args){
        String sai = Util.NullChecker("").replaceAll("\\<.*?\\>", "");
        System.out.println(sai);
    }
}
