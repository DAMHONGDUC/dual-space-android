package com.dd.dual.space.features.virtualization.data

import android.content.pm.ComponentInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProxyComponentExposureTest {
    @Test
    fun engineProxiesAreNotReachableFromOtherApps() {
        val info: PackageInfo = packageInfo()
        val proxies: List<ComponentInfo> = (info.activities.orEmpty().toList<ComponentInfo>() + info.services.orEmpty() + info.receivers.orEmpty())
            .filter { component -> component.name.startsWith(proxyPackagePrefix) }

        assertTrue("merged manifest lost the engine proxies", proxies.size > 200)
        val exposed: List<String> = proxies
            .filter { component -> component.exported && !isSystemBoundJobService(component) }
            .map(ComponentInfo::name)
        assertEquals(emptyList<String>(), exposed)
    }

    @Test
    fun exportedJobProxiesRequireTheSystemBindPermission() {
        val jobServices = packageInfo().services.orEmpty()
            .filter { service -> service.name.startsWith("$proxyPackagePrefix.ProxyJobService") }

        assertTrue(jobServices.isNotEmpty())
        jobServices.forEach { service -> assertEquals(bindJobServicePermission, service.permission) }
    }

    private fun packageInfo(): PackageInfo {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        return context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS or
                PackageManager.GET_DISABLED_COMPONENTS,
        )
    }

    private fun isSystemBoundJobService(component: ComponentInfo): Boolean =
        component is android.content.pm.ServiceInfo && component.permission == bindJobServicePermission

    private companion object {
        const val proxyPackagePrefix = "com.dd.the.universe.proxy"
        const val bindJobServicePermission = "android.permission.BIND_JOB_SERVICE"
    }
}
