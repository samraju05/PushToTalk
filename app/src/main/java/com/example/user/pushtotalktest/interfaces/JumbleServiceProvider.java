package com.example.user.pushtotalktest.interfaces;


import com.example.user.pushtotalktest.servies.QRPushToTalkService;
import com.example.user.pushtotalktest.fragments.JumbleServiceFragment;

public interface JumbleServiceProvider {
    public QRPushToTalkService.QRPushToTalkBinder getService();

    public void addServiceFragment(JumbleServiceFragment fragment);

    public void removeServiceFragment(JumbleServiceFragment fragment);
}
