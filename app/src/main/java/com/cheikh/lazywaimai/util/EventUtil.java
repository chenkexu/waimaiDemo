package com.cheikh.lazywaimai.util;

import com.squareup.otto.Bus;

public class EventUtil {

    private static Bus bus = new Bus();

    public static void register(Object context){

        bus.register(context);
    }

    public static void unregister(Object context){
        bus.unregister(context);
    }

    //发送用户账号改变的事件
    public static void sendEvent(Object object){
        bus.post(object);
    }
}