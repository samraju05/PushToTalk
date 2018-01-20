package com.example.user.pushtotalktest.fragments;

import android.os.RemoteException;

import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.model.User;
import com.morlunk.jumble.net.JumbleException;
import com.morlunk.jumble.util.JumbleObserver;


public class UserCommentFragment extends AbstractCommentFragment {

    @Override
    public void requestComment(final IJumbleService service) throws RemoteException {
        service.registerObserver(new JumbleObserver() {
            @Override
            public void onConnecting() throws RemoteException {

            }

            @Override
            public void onUserStateUpdated(User user) throws RemoteException {
                if (user.getSession() == getSession() &&
                        user.getComment() != null) {
                    loadComment(user.getComment());
                    service.unregisterObserver(this);
                }
            }

            @Override
            public void onDisconnected(JumbleException e) throws RemoteException {

            }
        });
        service.requestComment(getSession());
    }

    @Override
    public void editComment(IJumbleService service, String comment) throws RemoteException {
        service.setUserComment(getSession(), comment);
    }

    public int getSession() {
        return getArguments().getInt("session");
    }
}
