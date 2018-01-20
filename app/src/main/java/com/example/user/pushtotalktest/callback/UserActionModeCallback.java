package com.example.user.pushtotalktest.callback;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.user.pushtotalktest.utils.ModelUtils;
import com.example.user.pushtotalktest.R;
import com.example.user.pushtotalktest.utils.TintedMenuInflater;
import com.example.user.pushtotalktest.fragments.UserCommentFragment;
import com.example.user.pushtotalktest.interfaces.ChatTargetProvider;
import com.morlunk.jumble.IJumbleService;
import com.morlunk.jumble.model.Channel;
import com.morlunk.jumble.model.User;
import com.morlunk.jumble.net.Permissions;

import java.util.List;

public class UserActionModeCallback extends ChatTargetActionModeCallback {
    private Context mContext;
    private IJumbleService mService;
    private User mUser;
    private FragmentManager mFragmentManager;
    private LocalUserUpdateListener mListener;

    public UserActionModeCallback(Context context,
                                  IJumbleService service,
                                  User user,
                                  ChatTargetProvider chatTargetProvider,
                                  FragmentManager fragmentManager,
                                  LocalUserUpdateListener listener) {
        super(chatTargetProvider);
        mContext = context;
        mService = service;
        mUser = user;
        mFragmentManager = fragmentManager;
        mListener = listener;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        super.onCreateActionMode(actionMode, menu);
        TintedMenuInflater inflater = new TintedMenuInflater(mContext, actionMode.getMenuInflater());
        inflater.inflate(R.menu.context_user, menu);

        actionMode.setTitle(mUser.getName());
        actionMode.setSubtitle(R.string.current_chat_target);

        try {
            Channel channel = mService.getChannel(mUser.getChannelId());
            if (channel != null && channel.getPermissions() == 0)
                mService.requestPermissions(mUser.getChannelId());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        try {
            boolean self = mUser.getSession() == mService.getSession();
            int perms = mService.getPermissions();
            Channel channel = mService.getChannel(mUser.getChannelId());
            int channelPerms = channel.getId() != 0 ? channel.getPermissions() : perms;

            menu.findItem(R.id.context_kick).setVisible(
                    !self && (perms & (Permissions.Kick | Permissions.Ban | Permissions.Write)) > 0);
            menu.findItem(R.id.context_ban).setVisible(
                    !self && (perms & (Permissions.Ban | Permissions.Write)) > 0);
            menu.findItem(R.id.context_mute).setVisible(
                    ((channelPerms & (Permissions.Write | Permissions.MuteDeafen)) > 0 &&
                            (!self || mUser.isMuted() || mUser.isSuppressed())));
            menu.findItem(R.id.context_deafen).setVisible(
                    ((channelPerms & (Permissions.Write | Permissions.MuteDeafen)) > 0 &&
                            (!self || mUser.isDeafened())));
            menu.findItem(R.id.context_priority).setVisible(
                    ((channelPerms & (Permissions.Write | Permissions.MuteDeafen)) > 0));
            menu.findItem(R.id.context_move).setVisible(
                    !self && (perms & Permissions.Move) > 0);
            menu.findItem(R.id.context_change_comment).setVisible(self);
            menu.findItem(R.id.context_reset_comment).setVisible(
                    !self && mUser.getCommentHash() != null &&
                            !mUser.getCommentHash().isEmpty() &&
                            (perms & (Permissions.Move | Permissions.Write)) > 0);
            menu.findItem(R.id.context_view_comment).setVisible(
                    (mUser.getComment() != null && !mUser.getComment().isEmpty()) ||
                            (mUser.getCommentHash() != null && !mUser.getCommentHash().isEmpty()));
            menu.findItem(R.id.context_register).setVisible(mUser.getUserId() < 0 &&
                    (mUser.getHash() != null && !mUser.getHash().isEmpty()) &&
                    (perms & ((self ? Permissions.SelfRegister : Permissions.Register) | Permissions.Write)) > 0);
            menu.findItem(R.id.context_local_mute).setVisible(!self);
            menu.findItem(R.id.context_ignore_messages).setVisible(!self);

            menu.findItem(R.id.context_mute).setChecked(mUser.isMuted() || mUser.isSuppressed());
            menu.findItem(R.id.context_deafen).setChecked(mUser.isDeafened());
            menu.findItem(R.id.context_priority).setChecked(mUser.isPrioritySpeaker());
            menu.findItem(R.id.context_local_mute).setChecked(mUser.isLocalMuted());
            menu.findItem(R.id.context_ignore_messages).setChecked(mUser.isLocalIgnored());
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        try {
            boolean ban = false;
            switch (menuItem.getItemId()) {
                case R.id.context_ban:
                    ban = true;
                case R.id.context_kick:
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mContext);
                    alertBuilder.setTitle(R.string.user_menu_kick);
                    final EditText reasonField = new EditText(mContext);
                    reasonField.setHint(R.string.hint_reason);
                    alertBuilder.setView(reasonField);
                    final boolean finalBan = ban;
                    alertBuilder.setPositiveButton(R.string.user_menu_kick, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                mService.kickBanUser(mUser.getSession(), reasonField.getText().toString(), finalBan);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    alertBuilder.setNegativeButton(android.R.string.cancel, null);
                    alertBuilder.show();
                    break;
                case R.id.context_mute:
                    mService.setMuteDeafState(mUser.getSession(), !(mUser.isMuted() || mUser.isSuppressed()), mUser.isDeafened());
                    break;
                case R.id.context_deafen:
                    mService.setMuteDeafState(mUser.getSession(), mUser.isMuted(), !mUser.isDeafened());
                    break;
                case R.id.context_move:
                    showChannelMoveDialog();
                    break;
                case R.id.context_priority:
                    mService.setPrioritySpeaker(mUser.getSession(), !mUser.isPrioritySpeaker());
                    break;
                case R.id.context_local_mute:
                    mUser.setLocalMuted(!mUser.isLocalMuted());
                    mListener.onLocalUserStateUpdated(mUser);
                    break;
                case R.id.context_ignore_messages:
                    mUser.setLocalIgnored(!mUser.isLocalIgnored());
                    mListener.onLocalUserStateUpdated(mUser);
                    break;
                case R.id.context_change_comment:
                    showUserComment(true);
                    break;
                case R.id.context_view_comment:
                    showUserComment(false);
                    break;
                case R.id.context_reset_comment:
                    new AlertDialog.Builder(mContext)
                            .setMessage(mContext.getString(R.string.confirm_reset_comment, mUser.getName()))
                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        mService.setUserComment(mUser.getSession(), "");
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, null)
                            .show();
                    break;
                case R.id.context_register:
                    mService.registerUser(mUser.getSession());
                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
        actionMode.finish();
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        super.onDestroyActionMode(actionMode);
    }

    @Override
    public ChatTargetProvider.ChatTarget getChatTarget() {
        return new ChatTargetProvider.ChatTarget(mUser);
    }

    private void showUserComment(final boolean edit) {
        Bundle args = new Bundle();
        args.putInt("session", mUser.getSession());
        args.putString("comment", mUser.getComment());
        args.putBoolean("editing", edit);
        UserCommentFragment fragment = (UserCommentFragment) Fragment.instantiate(mContext, UserCommentFragment.class.getName(), args);
        fragment.show(mFragmentManager, UserCommentFragment.class.getName());
    }

    private void showChannelMoveDialog() throws RemoteException {
        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(R.string.user_menu_move);
        final List<Channel> channels = ModelUtils.getChannelList(mService.getRootChannel(), mService);
        final CharSequence[] channelNames = new CharSequence[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            channelNames[i] = channels.get(i).getName();
        }
        adb.setItems(channelNames, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Channel channel = channels.get(which);
                try {
                    mService.moveUserToChannel(mUser.getSession(), channel.getId());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
        adb.show();
    }

    public interface LocalUserUpdateListener {
        public void onLocalUserStateUpdated(User user);
    }
}
