package nhdphuong.com.manga.features.home

import dagger.Subcomponent
import nhdphuong.com.manga.scope.ActivityScope

/*
 * Created by nhdphuong on 3/21/18.
 */
@ActivityScope
@Subcomponent(modules = [(HomeModule::class)])
interface HomeComponent {
    fun inject(homeActivity: HomeActivity)
}