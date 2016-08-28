package com.lex.sunshine;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.test.AndroidTestCase;

import com.lex.sunshine.db.WeatherContract;

/**
 * Created by Alex on 28/08/2016.
 */
public class TestProvider extends AndroidTestCase {
    // This test checks to make sure that the content provider is registered correctly.
    public void testProviderRegistry(){
        PackageManager pm = mContext.getPackageManager();

        // The component name is defined based on the package name from the context and the
        // WeatherProvider class.
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                WeatherProvider.class.getName());

        try{
            // Fetch the provider info using the component name from the PackageManager
            // This throw an exception if the Provider isn't registered.
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);

            // Make sure that the registered authority matches the authority from the Contract
            assertEquals("Error: WeatherProvider registered with authority: "
                    + providerInfo.authority + " instead of authority: "
                    + WeatherContract.CONTENT_AUTHORITY,
                    providerInfo.authority,
                    WeatherContract.CONTENT_AUTHORITY);

        }
        catch(PackageManager.NameNotFoundException e){
            // Probably the provider is not registered correctly.
            assertTrue("Error: WeatherProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }
}
