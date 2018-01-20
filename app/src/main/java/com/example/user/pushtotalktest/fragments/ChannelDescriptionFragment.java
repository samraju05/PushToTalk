package com.example.user.pushtotalktest.fragments;

import android.os.RemoteException;

import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.model.Channel;
import com.morlunk.jumble.net.JumbleException;
import com.morlunk.jumble.util.JumbleObserver;


public class ChannelDescriptionFragment extends AbstractCommentFragment {

    @Override
    public void requestComment(final IJumbleService service) throws RemoteException {
        service.registerObserver(new JumbleObserver() {
            @Override
            public void onConnecting() throws RemoteException {

            }

            @Override
            public void onChannelStateUpdated(Channel channel) throws RemoteException {
                if (channel.getId() == getChannelId() &&
                        channel.getDescription() != null) {
                    loadComment(channel.getDescription());
                    service.unregisterObserver(this);
                }
            }

            @Override
            public void onDisconnected(JumbleException e) throws RemoteException {

            }
        });
        service.requestChannelDescription(getChannelId());
    }

    @Override
    public void editComment(IJumbleService service, String comment) throws RemoteException {
    }

    private int getChannelId() {
        return getArguments().getInt("channel");
    }
}
