package com.github.lzyzsd.jsbridge;

import android.content.Context;
import android.webkit.WebView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test class for persistent callback functionality
 * Tests the fix for issue #280 - persistent callbacks should be reusable
 */
@RunWith(AndroidJUnit4.class)
public class PersistentCallbackTest {

    private BridgeWebView bridgeWebView;
    private Context context;

    @Mock
    private OnBridgeCallback mockCallback;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        context = ApplicationProvider.getApplicationContext();
        bridgeWebView = new BridgeWebView(context);
        bridgeWebView.setGson(new Gson());
    }

    @Test
    public void testPersistentCallbackReuse() {
        // This test verifies that persistent callbacks can be reused multiple times
        // without being deleted after first use
        
        final int[] callCount = {0};
        
        // Create a callback that should be persistent
        OnBridgeCallback persistentCallback = new OnBridgeCallback() {
            @Override
            public void onCallBack(String data) {
                callCount[0]++;
            }
        };

        // Use the persistent callback method
        bridgeWebView.callHandlerPersistent("testHandler", "testData", persistentCallback);
        
        // Get the callback ID that was generated
        String callbackId = null;
        for (String id : bridgeWebView.getCallbacks().keySet()) {
            if (bridgeWebView.getPersistentCallbacks().containsKey(id)) {
                callbackId = id;
                break;
            }
        }
        
        assertNotNull("Persistent callback should be stored", callbackId);

        // Simulate multiple responses from JavaScript
        // This should work without the callback being deleted
        bridgeWebView.sendResponse("response1", callbackId);
        bridgeWebView.sendResponse("response2", callbackId);
        bridgeWebView.sendResponse("response3", callbackId);

        // Verify the callback was called multiple times
        assertEquals("Persistent callback should be called 3 times", 3, callCount[0]);
        
        // Verify the callback is still available after multiple uses
        assertTrue("Persistent callback should still exist after multiple uses", 
                   bridgeWebView.getCallbacks().containsKey(callbackId));
        assertTrue("Persistent callback should be marked as persistent", 
                   bridgeWebView.getPersistentCallbacks().containsKey(callbackId));
    }

    @Test
    public void testNormalCallbackBehavior() {
        // This test verifies that normal (non-persistent) callbacks still work as before
        // and are deleted after first use
        
        final int[] callCount = {0};
        
        OnBridgeCallback normalCallback = new OnBridgeCallback() {
            @Override
            public void onCallBack(String data) {
                callCount[0]++;
            }
        };
        
        // Call handler with normal callback - should be deleted after first use
        bridgeWebView.callHandler("testHandler", "testData", normalCallback);
        
        // The callback should be stored initially
        assertFalse("Normal callbacks should be stored initially", 
                    bridgeWebView.getCallbacks().isEmpty());
        
        // Get the callback ID that was generated
        String callbackId = null;
        for (String id : bridgeWebView.getCallbacks().keySet()) {
            callbackId = id;
            break;
        }
        
        assertNotNull("Normal callback should be stored", callbackId);
        
        // Verify it's not marked as persistent
        assertFalse("Normal callback should not be marked as persistent", 
                    bridgeWebView.getPersistentCallbacks().containsKey(callbackId));
        
        // Simulate response from JavaScript
        bridgeWebView.sendResponse("response", callbackId);
        
        // Verify the callback was called
        assertEquals("Normal callback should be called once", 1, callCount[0]);
        
        // Try to call it again - should not work since it should be deleted
        bridgeWebView.sendResponse("response2", callbackId);
        
        // Verify the callback was not called again
        assertEquals("Normal callback should not be called again after deletion", 1, callCount[0]);
    }

    @Test
    public void testCallbackIdGeneration() {
        // Test that callback IDs are generated correctly
        OnBridgeCallback callback1 = mock(OnBridgeCallback.class);
        OnBridgeCallback callback2 = mock(OnBridgeCallback.class);
        
        bridgeWebView.callHandler("handler1", "data1", callback1);
        int size1 = bridgeWebView.getCallbacks().size();
        
        bridgeWebView.callHandler("handler2", "data2", callback2);
        int size2 = bridgeWebView.getCallbacks().size();
        
        assertEquals("Each callback should be stored with unique ID", size1 + 1, size2);
    }
}