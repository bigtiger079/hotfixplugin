package com.bigger.ziroom;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedHelpers;

public class CertInfo {
    public String CSP;
    public String CommonName;
    public String GetEncodedCert;
    public String Issuer;
    public String KeyContainer;
    public int KeyUsage;
    public String PublicKey;
    public String SerialNumber;
    public String Subject;
    public String ValidFrom;
    public String ValidTo;
    public String Verify;

    public static CertInfo convert(Object obj){
        CertInfo certInfo = new CertInfo();
        //XposedHelpers.getObjectField(obj, "")
        Field[] fields = CertInfo.class.getFields();
        for (Field field : fields) {
            if (field.getType().equals(int.class)) {
                XposedHelpers.setIntField(certInfo, field.getName(), XposedHelpers.getIntField(obj, field.getName()));
            } else {
                XposedHelpers.setObjectField(certInfo, field.getName(), XposedHelpers.getObjectField(obj, field.getName()));
            }

        }
        return certInfo;
    }

    @Override
    public String toString() {
        return "CertInfo:[CommonName=" + CommonName + ", Subject="+Subject+"]";
    }
}
