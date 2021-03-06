package org.tasks.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.core.SortHelper;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.FragmentComponent;
import org.tasks.injection.InjectingDialogFragment;
import org.tasks.preferences.Preferences;
import timber.log.Timber;

public class SortDialog extends InjectingDialogFragment {

  private static final String EXTRA_MANUAL_ENABLED = "extra_manual_enabled";
  private static final String EXTRA_ASTRID_ENABLED = "extra_astrid_enabled";
  private static final String EXTRA_SELECTED_INDEX = "extra_selected_index";
  @Inject Preferences preferences;
  @Inject DialogBuilder dialogBuilder;
  private boolean manualEnabled;
  private boolean astridEnabled;
  private int selectedIndex;
  private AlertDialog alertDialog;
  private SortDialogCallback callback;

  public static SortDialog newSortDialog(Filter filter) {
    SortDialog sortDialog = new SortDialog();
    Bundle args = new Bundle();
    args.putBoolean(EXTRA_MANUAL_ENABLED, filter.supportsManualSort());
    args.putBoolean(EXTRA_ASTRID_ENABLED, filter.supportsAstridSorting());
    sortDialog.setArguments(args);
    return sortDialog;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    onCreate(savedInstanceState);

    Bundle arguments = getArguments();
    manualEnabled = arguments.getBoolean(EXTRA_MANUAL_ENABLED);
    astridEnabled = arguments.getBoolean(EXTRA_ASTRID_ENABLED) && preferences.getBoolean(R.string.p_astrid_sort_enabled, false);

    if (savedInstanceState != null) {
      selectedIndex = savedInstanceState.getInt(EXTRA_SELECTED_INDEX);
    } else {
      selectedIndex = getIndex(preferences.getSortMode());
    }

    List<String> items = new ArrayList<>();

    if (manualEnabled) {
      items.add(getString(R.string.SSD_sort_my_order));
    } else if (astridEnabled) {
      items.add(getString(R.string.astrid_sort_order));
    }

    items.add(getString(R.string.SSD_sort_auto));
    items.add(getString(R.string.SSD_sort_due));
    items.add(getString(R.string.SSD_sort_importance));
    items.add(getString(R.string.SSD_sort_alpha));
    items.add(getString(R.string.SSD_sort_modified));
    items.add(getString(R.string.sort_created));

    if (manualEnabled) {
      if (preferences.isManualSort()) {
        selectedIndex = 0;
      }
    } else if (astridEnabled) {
      if (preferences.isAstridSort()) {
        selectedIndex = 0;
      }
    } else {
      selectedIndex -= 1;
    }

    alertDialog =
        dialogBuilder
            .newDialog()
            .setSingleChoiceItems(
                items,
                selectedIndex,
                (dialog, which) -> {
                  selectedIndex = which;
                  enableReverse();
                })
            .setPositiveButton(R.string.TLA_menu_sort, (dialog, which) -> setSelection(false))
            .setNeutralButton(R.string.reverse, (dialog, which) -> setSelection(true))
            .setNegativeButton(android.R.string.cancel, null)
            .show();

    enableReverse();

    return alertDialog;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    callback = (SortDialogCallback) activity;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    outState.putInt(EXTRA_SELECTED_INDEX, selectedIndex);
  }

  private void enableReverse() {
    if (manualEnabled) {
      Button reverse = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
      reverse.setEnabled(selectedIndex != 0);
    }
  }

  private void setSelection(boolean reverse) {
    preferences.setBoolean(R.string.p_reverse_sort, reverse);

    boolean wasManual = preferences.isManualSort();
    boolean wasAstrid = preferences.isAstridSort();
    boolean isManual = manualEnabled && selectedIndex == 0;
    boolean isAstrid = astridEnabled && selectedIndex == 0;
    preferences.setBoolean(R.string.p_manual_sort, isManual);
    preferences.setBoolean(R.string.p_astrid_sort, isAstrid);

    if (!isManual && !isAstrid) {
      preferences.setSortMode(getSortMode(manualEnabled || astridEnabled ? selectedIndex : selectedIndex + 1));
    }

    callback.sortChanged(wasManual != isManual || wasAstrid != isAstrid);
  }

  private int getIndex(int sortMode) {
    switch (sortMode) {
      case SortHelper.SORT_AUTO:
        return 1;
      case SortHelper.SORT_DUE:
        return 2;
      case SortHelper.SORT_IMPORTANCE:
        return 3;
      case SortHelper.SORT_ALPHA:
        return 4;
      case SortHelper.SORT_MODIFIED:
        return 5;
      case SortHelper.SORT_CREATED:
        return 6;
    }

    Timber.e("Invalid sort mode: %s", sortMode);
    return 1;
  }

  private int getSortMode(int index) {
    switch (index) {
      case 1:
        return SortHelper.SORT_AUTO;
      case 2:
        return SortHelper.SORT_DUE;
      case 3:
        return SortHelper.SORT_IMPORTANCE;
      case 4:
        return SortHelper.SORT_ALPHA;
      case 5:
        return SortHelper.SORT_MODIFIED;
      case 6:
        return SortHelper.SORT_CREATED;
    }

    Timber.e("Invalid sort mode: %s", index);
    return SortHelper.SORT_ALPHA;
  }

  @Override
  protected void inject(FragmentComponent component) {
    component.inject(this);
  }

  public interface SortDialogCallback {
    void sortChanged(boolean reload);
  }
}
