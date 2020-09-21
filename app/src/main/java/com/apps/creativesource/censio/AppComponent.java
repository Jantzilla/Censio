package com.apps.creativesource.censio;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, UtilModule.class})
public interface AppComponent {
    void inject(LoginActivity activity);
    void inject(AddActivity activity);
    void inject(SettingActivity activity);
    void inject(ChoiceDetailFragment fragment);
    void inject(CommentDetailFragment fragment);
    void inject(SettingsFragment fragment);
    void inject(NotificationService service);
}