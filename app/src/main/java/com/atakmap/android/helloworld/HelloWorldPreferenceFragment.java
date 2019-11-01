
package com.atakmap.android.helloworld;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.atakmap.android.gui.PanEditTextPreference;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.preference.PluginPreferenceFragment;
import com.atakmap.android.helloworld.plugin.R;
import android.os.Bundle;
import android.view.View;

import com.atakmap.coremap.filesystem.FileSystemUtils;
import java.io.File;

import com.atakmap.android.gui.ImportFileBrowserDialog;

public class HelloWorldPreferenceFragment extends PluginPreferenceFragment {

    private static Context staticPluginContext;
    public static final String TAG = "HellWorldPreferenceFragment";

    /**
     * Only will be called after this has been instantiated with the 1-arg constructor.
     * Fragments must has a zero arg constructor.
     */
    public HelloWorldPreferenceFragment() {
        super(staticPluginContext, R.xml.preferences);
    }

    public HelloWorldPreferenceFragment(final Context pluginContext) {
        super(pluginContext, R.xml.preferences);
        staticPluginContext = pluginContext;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            ((PanEditTextPreference) findPreference("key_for_helloworld"))
                    .checkValidInteger();
        } catch (Exception e) {
        }
        findPreference("test_file_browser")
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference pref) {

                        ImportFileBrowserDialog.show("Test File Browser",
                                null,
                                new String[] {
                                        ".txt"
                        },
                                new ImportFileBrowserDialog.DialogDismissed() {
                                    public void onFileSelected(
                                            final File file) {
                                        if (!FileSystemUtils.isFile(file))
                                            return;
                                    }

                                    public void onDialogClosed() {
                                        //Do nothing
                                    }
                                }, HelloWorldPreferenceFragment.this
                                        .getActivity());
                        return true;
                    }
                });

    }

    @Override
    public String getSubTitle() {
        return getSubTitle("Tool Preferences", "Hello World Preferences");
    }

}