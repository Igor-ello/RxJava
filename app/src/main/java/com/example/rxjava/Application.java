package com.example.rxjava;

import com.vk.api.sdk.VK;
import com.vk.api.sdk.VKTokenExpiredHandler;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        VK.initialize(this);
        VK.addTokenExpiredHandler(tokenTracker);
    }

    VKTokenExpiredHandler tokenTracker = () -> {
        //Выполняется кода время токена истечёт
    };
}
