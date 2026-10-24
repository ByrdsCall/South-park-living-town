package org.xmlpull.v1;
// Desktop-mirror shim (internal QA tooling only).
public interface XmlPullParser {
    int START_DOCUMENT = 0, END_DOCUMENT = 1, START_TAG = 2, END_TAG = 3, TEXT = 4;
    String getName();
    String getAttributeValue(String namespace, String name);
    int getAttributeCount();
    String getAttributeName(int index);
    String getAttributeValue(int index);
    int next() throws Exception;
    int getEventType();
    String getText();
}
