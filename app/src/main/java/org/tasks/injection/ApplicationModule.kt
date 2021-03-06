package org.tasks.injection

import android.content.Context
import com.todoroo.astrid.dao.Database
import com.todoroo.astrid.dao.TaskDao
import dagger.Module
import dagger.Provides
import org.tasks.analytics.Firebase
import org.tasks.billing.BillingClient
import org.tasks.billing.BillingClientImpl
import org.tasks.billing.Inventory
import org.tasks.data.*
import org.tasks.jobs.WorkManager
import org.tasks.locale.Locale
import org.tasks.location.Geocoder
import org.tasks.location.MapboxGeocoder
import org.tasks.notifications.NotificationDao
import javax.inject.Singleton

@Module
class ApplicationModule(@get:Provides @get:ApplicationContext val context: Context) {

    @get:Provides
    val locale: Locale
        get() = Locale.getInstance(context)

    @Provides
    @Singleton
    fun getJavaLocale(locale: Locale): java.util.Locale = locale.locale

    @Provides
    @Singleton
    fun getNotificationDao(db: Database): NotificationDao = db.notificationDao()

    @Provides
    @Singleton
    fun getTagDataDao(db: Database): TagDataDao = db.tagDataDao

    @Provides
    @Singleton
    fun getUserActivityDao(db: Database): UserActivityDao = db.userActivityDao

    @Provides
    @Singleton
    fun getTaskAttachmentDao(db: Database): TaskAttachmentDao = db.taskAttachmentDao

    @Provides
    @Singleton
    fun getTaskListMetadataDao(db: Database): TaskListMetadataDao = db.taskListMetadataDao

    @Provides
    @Singleton
    fun getGoogleTaskDao(db: Database): GoogleTaskDao = db.googleTaskDao

    @Provides
    @Singleton
    fun getAlarmDao(db: Database): AlarmDao = db.alarmDao

    @Provides
    @Singleton
    fun getGeofenceDao(db: Database): LocationDao = db.locationDao

    @Provides
    @Singleton
    fun getTagDao(db: Database): TagDao = db.tagDao

    @Provides
    @Singleton
    fun getFilterDao(db: Database): FilterDao = db.filterDao

    @Provides
    @Singleton
    fun getGoogleTaskListDao(db: Database): GoogleTaskListDao = db.googleTaskListDao

    @Provides
    @Singleton
    fun getCaldavDao(db: Database): CaldavDao = db.caldavDao

    @Provides
    @Singleton
    fun getTaskDao(db: Database, workManager: WorkManager): TaskDao {
        val taskDao = db.taskDao
        taskDao.initialize(workManager)
        return taskDao
    }

    @Provides
    @Singleton
    fun getDeletionDao(db: Database): DeletionDao = db.deletionDao

    @Provides
    fun getBillingClient(inventory: Inventory, firebase: Firebase): BillingClient
            = BillingClientImpl(context, inventory, firebase)

    @get:Provides
    val geocoder: Geocoder
        get() = MapboxGeocoder(context)
}