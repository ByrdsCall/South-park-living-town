package android.util;
// Desktop-mirror shim (internal QA tooling only).
public interface AttributeSet {
    int getAttributeCount();
    String getAttributeName(int index);
    String getAttributeValue(int index);
    String getAttributeValue(String namespace, String name);
    boolean getAttributeBooleanValue(String namespace, String name, boolean def);
    float getAttributeFloatValue(String namespace, String name, float def);
    int getAttributeIntValue(String namespace, String name, int def);
}
