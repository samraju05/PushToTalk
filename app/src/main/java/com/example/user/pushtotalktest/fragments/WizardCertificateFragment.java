/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.user.pushtotalktest.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.user.pushtotalktest.certificate.QRPushToTalkCertificateGenerateTask;
import com.example.user.pushtotalktest.R;
import com.example.user.pushtotalktest.utils.Settings;
import com.example.user.pushtotalktest.interfaces.WizardNavigation;

import java.io.File;

/**
 * Created by andrew on 02/11/13.
 */
public class WizardCertificateFragment extends Fragment {

    private Button mGenerateButton;
    private WizardNavigation mNavigation;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mNavigation = (WizardNavigation) activity;
        } catch (ClassCastException e) {
            throw new RuntimeException(activity.getClass().getName() + " must implement WizardNavigation!");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wizard_certificate, container, false);
        mGenerateButton = (Button) view.findViewById(R.id.wizard_certificate_generate);
        mGenerateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateCertificate();
            }
        });
        return view;
    }

    private void generateCertificate() {
        final Settings settings = Settings.getInstance(getActivity());
        QRPushToTalkCertificateGenerateTask task = new QRPushToTalkCertificateGenerateTask(getActivity()) {
            @Override
            protected void onPostExecute(File result) {
                super.onPostExecute(result);
                settings.setCertificatePath(result.getAbsolutePath());
                mGenerateButton.setEnabled(false);
                mNavigation.next();
            }
        };
        task.execute();
    }
}
