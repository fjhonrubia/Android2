package com.odobo.twlocator.di;

public interface DependencyInjectorCallback {

    <T> void inject(T instance);

}
