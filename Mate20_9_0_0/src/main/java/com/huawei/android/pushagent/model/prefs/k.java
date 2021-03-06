package com.huawei.android.pushagent.model.prefs;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.android.pushagent.utils.a.c;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map.Entry;

public class k extends n {
    private static k gg = null;

    private k(Context context) {
        super(context, "PushRouteInfo");
        vp();
    }

    public static synchronized k rh(Context context) {
        synchronized (k.class) {
            k kVar;
            if (gg != null) {
                kVar = gg;
                return kVar;
            }
            gg = new k(context);
            kVar = gg;
            return kVar;
        }
    }

    public long uh() {
        return getLong("pushSrvValidTime", Long.MAX_VALUE);
    }

    public boolean uk(long j) {
        return setValue("pushSrvValidTime", Long.valueOf(j));
    }

    public int getResult() {
        return getInt("result", -1);
    }

    public boolean uj(int i) {
        return setValue("result", Integer.valueOf(i));
    }

    public String getBelongId() {
        return getString("belongId", "-1");
    }

    public int sw() {
        return getInt("isChkToken", 1);
    }

    public int tr() {
        return getInt("isChkNcToken", 1);
    }

    public String getAnalyticUrl() {
        return getString("analyticUrl", null);
    }

    public String rz() {
        return getString("passTrustPkgs", "");
    }

    public String sa() {
        return getString("noticeTrustPkgs", "");
    }

    public String sd() {
        return getString("dawnWhiteList", "");
    }

    public long tq() {
        return getLong("upAnalyticUrlInterval", 345600000);
    }

    public String getConnId() {
        return c.j(getString("connId", ""));
    }

    public int rk() {
        return getInt("pushConnectLog", 0);
    }

    public int rl() {
        return getInt("heartbeatFailLog", 0);
    }

    public int rm() {
        return getInt("tokenReqLog", 0);
    }

    public int ri() {
        return getInt("onlineStatusLog", 0);
    }

    public int rj() {
        return getInt("msgLog", 0);
    }

    public int rn() {
        return getInt("channelCloseLog", 0);
    }

    public long tx() {
        return getLong("minReportItval", 1800000);
    }

    public long tu() {
        return getLong("fatalMinReportItval", 1200000);
    }

    public int ub() {
        return getInt("reportMaxCount", 200);
    }

    public int uc() {
        return getInt("reportUpperCount", 100);
    }

    public int ua() {
        return getInt("reportMaxByteCount", 2500);
    }

    public String getServerIP() {
        return getString("serverIp", "");
    }

    public int getServerPort() {
        return getInt("serverPort", -1);
    }

    public long ug() {
        return getLong("trsValid_min", 7200);
    }

    public long uf() {
        return getLong("trsValid_max", 2592000);
    }

    public int td() {
        return getInt("bastetInterval", 3);
    }

    public long getWifiMinHeartbeat() {
        return getLong("wifiMinHeartbeat", 1800);
    }

    public long getWifiMaxHeartbeat() {
        return getLong("wifiMaxHeartbeat", 1800);
    }

    public long get3GMinHeartbeat() {
        return getLong("g3MinHeartbeat", 900);
    }

    public long get3GMaxHeartbeat() {
        return getLong("g3MaxHeartbeat", 1800);
    }

    public long sf() {
        return getLong("serverRec1_min", 3);
    }

    public long sg() {
        return getLong("serverRec2_min", 10);
    }

    public long sh() {
        return getLong("serverRec3_min", 30);
    }

    public long si() {
        return getLong("serverRec4_min", 300);
    }

    public long sj() {
        return getLong("serverRec5_min", 300);
    }

    public long sk() {
        return getLong("serverRec6_min", 600);
    }

    public long sl() {
        return getLong("serverRec7_min", 900);
    }

    public long sm() {
        return getLong("serverRec8_min", 1800);
    }

    public long ro() {
        return getLong("noNetHeartbeat", 7200);
    }

    public long tt() {
        return getLong("connTrsItval", 300);
    }

    public long ts() {
        return getLong("connTrsErrItval", 1800);
    }

    public long sn() {
        return getLong("SrvMaxFail_times", 6);
    }

    public long tw() {
        return getLong("maxQTRS_times", 6);
    }

    public long rw() {
        return getLong("socketConnTimeOut", 30);
    }

    public long rx() {
        return getLong("socketConnectReadOut", 10);
    }

    public long sx() {
        return getLong("pushLeastRun_time", 30);
    }

    public long sy() {
        return getLong("push1StartInt", 3);
    }

    public long sz() {
        return getLong("push2StartInt", 30);
    }

    public long ta() {
        return getLong("push3StartInt", 600);
    }

    public long tb() {
        return getLong("push4StartInt", 1800);
    }

    public long th() {
        return getLong("firstQueryTRSDayTimes", 6);
    }

    public long ti() {
        return getLong("firstQueryTRSHourTimes", 2);
    }

    public long tj() {
        return getLong("maxQueryTRSDayTimes", 1);
    }

    public HashMap<Long, Long> tk() {
        return tv("flowcInterval", "flowcVlomes");
    }

    public long tl() {
        return getLong("wifiFirstQueryTRSDayTimes", 18);
    }

    public long tm() {
        return getLong("wifiFirstQueryTRSHourTimes", 6);
    }

    public long tn() {
        return getLong("wifiMaxQueryTRSDayTimes", 3);
    }

    public long ue() {
        return getLong("stopServiceItval", 5);
    }

    public long ru() {
        return getLong("heartBeatRspTimeOut", 10) * 1000;
    }

    public HashMap<Long, Long> to() {
        return tv("wifiFlowcInterval", "wifiFlowcVlomes");
    }

    public long sp() {
        return getLong("ConnRange", 600) * 1000;
    }

    public long sq() {
        return getLong("ConnRangeIdle", 1800) * 1000;
    }

    public int so() {
        return getInt("MaxConnTimes", 4);
    }

    public boolean sc() {
        return getInt("allowPry", 0) == 1;
    }

    private HashMap<Long, Long> tv(String str, String str2) {
        String str3 = "\\d{1,3}";
        HashMap<Long, Long> hashMap = new HashMap();
        for (String str4 : vq().keySet()) {
            if (str4.matches(str + str3)) {
                hashMap.put(Long.valueOf(getLong(str4, 1)), Long.valueOf(getLong(str4.replace(str, str2), 2147483647L)));
            }
        }
        return hashMap;
    }

    public HashMap<String, String> rv() {
        HashMap<String, String> hashMap = new HashMap();
        String str = "apn_";
        for (Entry entry : vq().entrySet()) {
            String str2 = (String) entry.getKey();
            if (str2.startsWith(str)) {
                hashMap.put(str2, (String) entry.getValue());
            }
        }
        return hashMap;
    }

    public int te() {
        return getInt("grpNum", 0);
    }

    public String tf() {
        String str = "CE6935516BA17DB6174D77DAB902ED0F75D8C9B071FD46981BB1D05AA95F14277122B362304D6B3B865D1C00F5D8C6FF8BC2D432B8CDB11CF95B2450B7ADA9E20957068AD84E1BD4666E30BB103C5BCE485643755E7921AE0430A87C71DEB42F764779D4118F9A4183ABB2CBA6C31913AE6141DE168C51A270BADC91518DCE317F3309B50CCFB4B1949DC41520CBB3354C0CA3FC6943FE75DADA3B2A89397A3D68D6DC6AEBA0B6178AC0089FFEF6D2CF6DD36327C5AAB4ECE3A59B7D6B4E250D05746A19E8F052A90AB4A7F41958013E66EB207798DB766342701D0E8F6D5141B910887F7D43EE58A63AC9AF4D7B4A2B27B67C42DBD5142501DB629C3208E760B20BE1775C387F823733E9D5407F291B10C1846F77B7452EEF25B4720A103B90DD19B1B12CD7D0D0A1F7EEAAD0210E2C21494299D1E1E8FC83C088886E03BB1CDFD8D3B0AF28023D0F9E1AB8ACF0D4B5900EC2B5E3BCAE23020B581271136A56FB404CAAECE005D78DBB71ADE08ED965F9304F4F2CB13C6B3242CB04D28A05ED5D75669BEDF0F788AA3D8C1B3FFFEF3D2C0A2700E6E266E33D6ABFD6B7377D65FB60CB1C7288CE12CD584C357E84C446";
        str = getString("publicKey", "CE6935516BA17DB6174D77DAB902ED0F75D8C9B071FD46981BB1D05AA95F14277122B362304D6B3B865D1C00F5D8C6FF8BC2D432B8CDB11CF95B2450B7ADA9E20957068AD84E1BD4666E30BB103C5BCE485643755E7921AE0430A87C71DEB42F764779D4118F9A4183ABB2CBA6C31913AE6141DE168C51A270BADC91518DCE317F3309B50CCFB4B1949DC41520CBB3354C0CA3FC6943FE75DADA3B2A89397A3D68D6DC6AEBA0B6178AC0089FFEF6D2CF6DD36327C5AAB4ECE3A59B7D6B4E250D05746A19E8F052A90AB4A7F41958013E66EB207798DB766342701D0E8F6D5141B910887F7D43EE58A63AC9AF4D7B4A2B27B67C42DBD5142501DB629C3208E760B20BE1775C387F823733E9D5407F291B10C1846F77B7452EEF25B4720A103B90DD19B1B12CD7D0D0A1F7EEAAD0210E2C21494299D1E1E8FC83C088886E03BB1CDFD8D3B0AF28023D0F9E1AB8ACF0D4B5900EC2B5E3BCAE23020B581271136A56FB404CAAECE005D78DBB71ADE08ED965F9304F4F2CB13C6B3242CB04D28A05ED5D75669BEDF0F788AA3D8C1B3FFFEF3D2C0A2700E6E266E33D6ABFD6B7377D65FB60CB1C7288CE12CD584C357E84C446");
        CharSequence j = c.j(str);
        if (!TextUtils.isEmpty(j)) {
            return j;
        }
        com.huawei.android.pushagent.utils.f.c.ep("PushLog3413", "public key is empty, use origin.");
        uk(0);
        return str;
    }

    public boolean isValid() {
        if ("".equals(getServerIP()) || -1 == getServerPort() || getResult() != 0) {
            return false;
        }
        return true;
    }

    public boolean isNotAllowedPush() {
        int result = getResult();
        if (25 == result || 26 == result || 27 == result) {
            return true;
        }
        return false;
    }

    public long ty() {
        return getLong("fir3gHb", 150) * 1000;
    }

    public long tz() {
        return getLong("firWifiHb", 150) * 1000;
    }

    public long ss() {
        return getLong("ReConnInterval", 300) * 1000;
    }

    public long st() {
        return getLong("ReConnIntervalIdle", 600) * 1000;
    }

    public long sr() {
        return getLong("KeepConnTime", 300) * 1000;
    }

    public static boolean tp(String str) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(str);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public String su() {
        String string = getString("idlePeriodBegin", "00:00");
        if (string.length() == "HH:mm".length() && (tp(string) ^ 1) == 0) {
            return string;
        }
        return "00:00";
    }

    public String sv() {
        String string = getString("idlePeriodEnd", "06:00");
        if (string.length() == "HH:mm".length() && (tp(string) ^ 1) == 0) {
            return string;
        }
        return "06:00";
    }

    public long rt() {
        return getLong("wifiHbValid", 604800) * 1000;
    }

    public long rs() {
        return getLong("dataHbValid", 604800) * 1000;
    }

    public int rr() {
        return getInt("bestHBCheckTime", 2);
    }

    public boolean tc() {
        if (getInt("allowBastet", 1) == 1) {
            return true;
        }
        return false;
    }

    public boolean ry() {
        if (getInt("needCheckAgreement", 1) == 1) {
            return true;
        }
        return false;
    }

    public boolean tg() {
        if (getInt("needSolinger", 1) == 1) {
            return true;
        }
        return false;
    }

    public long se() {
        return getLong("msgResponseTimeOut", 3600) * 1000;
    }

    public long ud() {
        return getLong("resetBastetTimeOut", 30) * 1000;
    }

    public long sb() {
        return getLong("responseMsgTimeout", 60) * 1000;
    }

    public long getNextConnectTrsInterval() {
        return getLong("nextConnectInterval", 86400) * 1000;
    }

    public boolean ui(long j) {
        return setValue("nextConnectInterval", Long.valueOf(j));
    }

    public long rq() {
        return getLong("minHeartbeatStep", 30) * 1000;
    }

    public long rp() {
        return getLong("maxHeartbeatStep", 60) * 1000;
    }
}
