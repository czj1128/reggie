package com.czj.reggie.controller;

import org.junit.jupiter.api.Test;

public class DishControllerTest {
    @Test
    public void removeByidS(){
        String ids="1540759317638430721,1413384757047271425,1413385247889891330,1413342036832100354,1397862477831122945,1397862198033297410,1397861683434139649,1397860963880316929,1397860792492666881,1397860578738352129";
        String[] sids = ids.split(",");
        Long[] iids=new Long[sids.length];
        for (int i=0;i<sids.length;i++){
            iids[i]=Long.valueOf(sids[i]);
        }
        System.out.println("----");
        for (Long iid : iids) {
            System.out.println(iid);
        }
    }
}
