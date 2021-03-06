package org.tasks.receivers;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.provider.Astrid2TaskProvider;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.injection.ApplicationComponent;
import org.tasks.injection.ApplicationContext;
import org.tasks.injection.InjectingJobIntentService;
import org.tasks.preferences.DefaultFilterProvider;
import org.tasks.preferences.Preferences;

public class RefreshReceiver extends InjectingJobIntentService {

  @Inject @ApplicationContext Context context;
  @Inject DefaultFilterProvider defaultFilterProvider;
  @Inject TaskDao taskDao;
  @Inject Preferences preferences;

  @Override
  protected void doWork(@NonNull Intent intent) {
    if (preferences.getBoolean(R.string.p_badges_enabled, true)) {
      Filter badgeFilter = defaultFilterProvider.getBadgeFilter();
      ShortcutBadger.applyCount(context, taskDao.count(badgeFilter));
    }

    Astrid2TaskProvider.notifyDatabaseModification(context);
  }

  @Override
  protected void inject(ApplicationComponent component) {
    component.inject(this);
  }
}
