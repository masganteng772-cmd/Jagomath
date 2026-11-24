package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.auth.firebaseAuthOrNull
import com.example.auth.resolveWebClientId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("EQ-Math", appName)
  }

  @Test
  fun `gracefully handles missing google services config`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    assertNull(resolveWebClientId(context))
    assertNull(firebaseAuthOrNull())
  }
}
