package com.odobo.twlocator.di;

import android.content.Context;

import com.path.android.jobqueue.di.DependencyInjector;

import java.lang.ref.SoftReference;

import dagger.Module;

@Module(
        includes = {
        },
        injects = {

        }
)
public class AppModule {

    private SoftReference<Context> contextSoftReference;
    private SoftReference<DependencyInjector> dependencyInjectorSoftReference;

    public AppModule(Context context, DependencyInjector dependencyInjector) {
        this.contextSoftReference = new SoftReference<Context>(context.getApplicationContext());
        this.dependencyInjectorSoftReference = new SoftReference<DependencyInjector>(dependencyInjector);
    }
/*
    @Provides
    public Context provideApplicationContext() {
        return contextSoftReference.get();
    }
*/
}
