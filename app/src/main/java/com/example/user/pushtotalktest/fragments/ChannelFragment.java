package com.example.user.pushtotalktest.fragments;

import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.user.pushtotalktest.interfaces.ChatTargetProvider;
import com.example.user.pushtotalktest.R;
import com.example.user.pushtotalktest.utils.Settings;
import com.morlunk.jumble.IJumbleObserver;
import com.morlunk.jumble.model.User;
import com.morlunk.jumble.net.JumbleException;
import com.morlunk.jumble.util.JumbleObserver;

import java.util.ArrayList;
import java.util.List;

public class ChannelFragment extends JumbleServiceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, ChatTargetProvider {

    private ViewPager mViewPager;
    private PagerTabStrip mTabStrip;
    private Button mTalkButton;
    private View mTalkView;

    private ChatTarget mChatTarget;
    private List<OnChatTargetSelectedListener> mChatTargetListeners = new ArrayList<OnChatTargetSelectedListener>();
    private boolean mTogglePTT;

    private JumbleObserver mObserver = new JumbleObserver() {
        @Override
        public void onConnecting() throws RemoteException {

        }

        @Override
        public void onUserTalkStateUpdated(User user) throws RemoteException {
            if (user != null && user.getSession() == getService().getSession()) {
                switch (user.getTalkState()) {
                    case TALKING:
                    case SHOUTING:
                    case WHISPERING:
                        mTalkButton.setPressed(true);
                        break;
                    case PASSIVE:
                        mTalkButton.setPressed(false);
                        break;
                }
            }
        }

        @Override
        public void onDisconnected(JumbleException e) throws RemoteException {

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_channel, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.channel_view_pager);
        mTabStrip = (PagerTabStrip) view.findViewById(R.id.channel_tab_strip);
        if (mTabStrip != null) {
            int[] attrs = new int[]{R.attr.colorPrimary, android.R.attr.textColorPrimaryInverse};
            TypedArray a = getActivity().obtainStyledAttributes(attrs);
            int titleStripBackground = a.getColor(0, -1);
            int titleStripColor = a.getColor(1, -1);
            a.recycle();

            mTabStrip.setTextColor(titleStripColor);
            mTabStrip.setTabIndicatorColor(titleStripColor);
            mTabStrip.setBackgroundColor(titleStripBackground);
            mTabStrip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        }

        mTalkView = view.findViewById(R.id.pushtotalk_view);
        mTalkButton = (Button) view.findViewById(R.id.pushtotalk);
        mTalkButton.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                try {
                    boolean oldState = getService().isTalking();
                    boolean newState;
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        newState = !mTogglePTT || !oldState;
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        newState = mTogglePTT && oldState;
                    } else {
                        return true;
                    }

                    if (newState != oldState) {
                        getService().setTalkingState(newState);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        configureInput();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);

        if (mViewPager != null) {
            ChannelFragmentPagerAdapter pagerAdapter = new ChannelFragmentPagerAdapter(getChildFragmentManager());
            mViewPager.setAdapter(pagerAdapter);
        } else {
            ChannelListFragment listFragment = new ChannelListFragment();
            Bundle listArgs = new Bundle();
            listArgs.putBoolean("pinned", isShowingPinnedChannels());
            listFragment.setArguments(listArgs);
            ChannelChatFragment chatFragment = new ChannelChatFragment();

            getChildFragmentManager().beginTransaction()
                    .replace(R.id.list_fragment, listFragment)
                    .replace(R.id.chat_fragment, chatFragment)
                    .commit();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Settings settings = Settings.getInstance(getActivity());
        switch (item.getItemId()) {
            case R.id.menu_input_voice:
                settings.setInputMethod(Settings.ARRAY_INPUT_METHOD_VOICE);
                return true;
            case R.id.menu_input_ptt:
                settings.setInputMethod(Settings.ARRAY_INPUT_METHOD_PTT);
                return true;
            case R.id.menu_input_continuous:
                settings.setInputMethod(Settings.ARRAY_INPUT_METHOD_CONTINUOUS);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public IJumbleObserver getServiceObserver() {
        return mObserver;
    }

    private boolean isShowingPinnedChannels() {
        return getArguments() != null &&
                getArguments().getBoolean("pinned");
    }

    private void configureInput() {
        Settings settings = Settings.getInstance(getActivity());
        boolean showPttButton = settings.isPushToTalkButtonShown() && settings.getInputMethod().equals(Settings.ARRAY_INPUT_METHOD_PTT);
        mTalkView.setVisibility(showPttButton ? View.VISIBLE : View.GONE);
        mTogglePTT = settings.isPushToTalkToggle();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (Settings.PREF_INPUT_METHOD.equals(key) ||
                Settings.PREF_PUSH_BUTTON_HIDE_KEY.equals(key) ||
                Settings.PREF_PTT_TOGGLE.equals(key))
            configureInput();
    }

    @Override
    public ChatTarget getChatTarget() {
        return mChatTarget;
    }

    @Override
    public void setChatTarget(ChatTarget target) {
        mChatTarget = target;
        for (OnChatTargetSelectedListener listener : mChatTargetListeners)
            listener.onChatTargetSelected(target);
    }

    @Override
    public void registerChatTargetListener(OnChatTargetSelectedListener listener) {
        mChatTargetListeners.add(listener);
    }

    @Override
    public void unregisterChatTargetListener(OnChatTargetSelectedListener listener) {
        mChatTargetListeners.remove(listener);
    }

    private class ChannelFragmentPagerAdapter extends FragmentPagerAdapter {

        public ChannelFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            Bundle args = new Bundle();
            switch (i) {
                case 0:
                    fragment = new ChannelListFragment();
                    args.putBoolean("pinned", isShowingPinnedChannels());
                    break;
                case 1:
                    fragment = new ChannelChatFragment();
                    break;
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.channel).toUpperCase();
                case 1:
                    return getString(R.string.chat).toUpperCase();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
