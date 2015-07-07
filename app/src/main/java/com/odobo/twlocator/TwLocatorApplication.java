package com.odobo.twlocator;

import android.app.Application;

import com.odobo.twlocator.di.AppModule;
import com.path.android.jobqueue.BaseJob;
import com.path.android.jobqueue.di.DependencyInjector;

import dagger.ObjectGraph;

public class TwLocatorApplication extends Application implements DependencyInjector {

        ObjectGraph objectGraph;

        @Override
        public void onCreate() {
            super.onCreate();

            objectGraph = ObjectGraph.create(new AppModule(this, this));
        }

        public ObjectGraph getObjectGraph() {
            return objectGraph;
        }

        @Override
        public void inject(BaseJob job) {
            objectGraph.inject(job);
        }

}

