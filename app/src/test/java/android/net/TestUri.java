package android.net;

import java.util.Collections;
import java.util.List;

/**
 * Minimal concrete Uri for JVM unit tests only.
 *
 * android.net.Uri is abstract with a package-private constructor. Because this
 * class is in the same package (android.net), javac allows the implicit super()
 * call.  Instances are created via {@link #create()}, which uses sun.misc.Unsafe
 * to allocate the object without invoking any constructor — bypassing the
 * RuntimeException("Stub!") that Uri.<init>() throws after Object.<init>().
 * TestUri itself is concrete, so Unsafe.allocateInstance succeeds (it only
 * fails for abstract classes).
 *
 * FakeEvidenceGateway.importFromUri never calls any method on the uri parameter,
 * so a zero-field instance is safe here.
 *
 * NOT shipped in production: this file lives in src/test/java.
 */
public final class TestUri extends Uri {

    /** Use this instead of new TestUri() to avoid the stub-throw from super(). */
    @SuppressWarnings("unchecked")
    public static TestUri create() {
        try {
            // Access sun.misc.Unsafe via reflection to avoid javac module-system errors.
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);
            // allocateInstance works on concrete classes (TestUri is concrete, not abstract).
            java.lang.reflect.Method alloc = unsafeClass.getMethod("allocateInstance", Class.class);
            return (TestUri) alloc.invoke(unsafe, TestUri.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not allocate TestUri for unit test", e);
        }
    }

    // Private: use create() instead.
    private TestUri() { }

    // --- Abstract Uri methods — all stubs, none called in tests ---

    @Override public String toString()                     { return "test:"; }
    @Override public String getScheme()                    { return "test"; }
    @Override public String getSchemeSpecificPart()        { return ""; }
    @Override public String getEncodedSchemeSpecificPart() { return ""; }
    @Override public String getAuthority()                 { return ""; }
    @Override public String getEncodedAuthority()          { return ""; }
    @Override public String getEncodedUserInfo()           { return null; }
    @Override public String getUserInfo()                  { return null; }
    @Override public String getHost()                      { return ""; }
    @Override public int    getPort()                      { return -1; }
    @Override public String getPath()                      { return ""; }
    @Override public String getEncodedPath()               { return ""; }
    @Override public String getQuery()                     { return null; }
    @Override public String getEncodedQuery()              { return null; }
    @Override public String getFragment()                  { return null; }
    @Override public String getEncodedFragment()           { return null; }
    @Override public List<String> getPathSegments()        { return Collections.emptyList(); }
    @Override public String getLastPathSegment()           { return null; }
    @Override public boolean isHierarchical()              { return false; }
    @Override public boolean isRelative()                  { return false; }
    @Override public android.net.Uri.Builder buildUpon()   { return new android.net.Uri.Builder(); }
    @Override public void writeToParcel(android.os.Parcel dest, int flags) { }
    @Override public int describeContents()                { return 0; }
}

