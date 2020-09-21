package com.apps.creativesource.censio;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules={AppModule.class, UtilModule.class})
public interface AppComponent {
    void inject(LoginActivity activity);
}